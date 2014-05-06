package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * A simple class for managing the ogr2ogr utility and submitting sql statements
 * to obtain shapefiles.
 * 
 * This implementation enforces only a certain amount of simultaneous class to
 * #cook(File, String). This simply limits the maximum amount of processes which
 * java will fork to create.
 * 
 * @author Christopher Johnson
 */
public class ShapefileGenerator implements DustBin<File>, Oven<String, String, File> {
    private final ExecutorService remover;
    private final Semaphore semaphore;
    private final String ogr2ogr, connectionString;
    
    /**
     * Creates a shapefile generator based upon an installation of ogr2ogr
     * @param ogr2ogr the location to the ogr2ogr utlity
     * @param connectionString the connection string to supply in calls
     * @param simultaneousProcesses the maximum amount of processes to perform
     *  simultaneously
     */
    public ShapefileGenerator(String ogr2ogr, String connectionString, int simultaneousProcesses) {
        this(ogr2ogr, connectionString, new Semaphore(simultaneousProcesses, true), Executors.newSingleThreadExecutor());
    }
    
    /**
     * Dependency injection constructor
     */
    protected ShapefileGenerator(String ogr2ogr, String connectionString, Semaphore semaphore, ExecutorService remover) {
        this.ogr2ogr = ogr2ogr;
        this.connectionString = connectionString;
        this.semaphore = semaphore;
        this.remover = remover;
    }

    /**
     * A slice of bread to delete from the work surface
     * @param slice the slice to remove from disk
     */
    @Override
    public void delete(final BreadSlice<?, File> slice) {
        remover.submit(new Runnable() {
            @Override
            public void run() {
                new File(slice.getWorkSurface(), slice.getId() + "_" + slice.getMixName() + ".shp").delete();
                new File(slice.getWorkSurface(), slice.getId() + "_" + slice.getMixName() + ".shx").delete();
                new File(slice.getWorkSurface(), slice.getId() + "_" + slice.getMixName() + ".dbf").delete();
            }
        });
    }

    /**
     * Scan through the work surface directory to find any existing shapefiles
     * which can be reloaded as bread slices.
     * @param clock The clock that each bread slice should use
     * @param workSurface the work surface each slice will live on and to read
     * @param bin the dust bin to give to each bread slice for deletion later
     * @param staleTime the time it takes for these slices of bread to go stale
     * @return A list of bread slices from the work surface
     */
    @Override
    public List<BreadSlice<String, File>> reload(Clock clock, File workSurface, DustBin<File> bin, long staleTime) {
        List<BreadSlice<String, File>> slices = new ArrayList<>();
        for(File shapefile: workSurface.listFiles(new ShapefileFilter())) {
            String shapefileName = shapefile.getName();
            String[] nameparts = shapefileName.substring(0, shapefileName.length()-4).split("_");
             
            slices.add(new BreadSlice<>(    shapefile.getAbsolutePath(), 
                                            shapefile.lastModified(), 
                                            Integer.parseInt(nameparts[0]), 
                                            nameparts[1],
                                            staleTime,
                                            clock,
                                            workSurface,
                                            bin));
        }
        return slices;
    }
    
    /**
     * Performs a call to the ogr2ogr command. This method will wait if the maximum
     * simultaneous calls are being performed.
     * @param slice the slice to populate
     * @param sql the sql statement to use for generating the shape file
     * @return the outputed shape file (the .shp part)
     * @throws BreadException 
     */
    @Override
    public String cook(BreadSlice<String, File> slice, String sql) throws BreadException {
        File output = new File(slice.getWorkSurface(), slice.getId() + "_" + slice.getMixName() + ".shp");
        try {
            semaphore.acquire();
            try {
                process(output, sql);
                return output.getAbsolutePath();
            }
            finally {
                semaphore.release();
            }
        }
        catch(IOException | InterruptedException ex) {
            throw new BreadException("Failed to generate shapefile", ex);
        }
    }
    
    protected void process(File output, String sql) throws IOException, InterruptedException, BreadException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                ogr2ogr,
                "-f",
                "ESRI Shapefile",
                output.getAbsolutePath(),
                connectionString,
                "-sql",
                sql
            );

        //Start the process and wait for it to end
        processBuilder.inheritIO(); //Output any errors to the default log files
        Process process = processBuilder.start();
        if (process.waitFor() != 0) {
            throw new BreadException("The ogr2ogr command failed to execute");
        }
    }
    
    /**
    * A simple filename filter for locating shapefiles. Those files which have the 
    * extension .shp
    * @author Christopher Johnson
    */
   public static class ShapefileFilter implements FilenameFilter {

       /**
        * Check if the given file is a shapefile
        * @param dir The directory which the shapefile was found
        * @param name The name of the file to check
        * @return If the files extension is .shp
        */
       @Override
       public boolean accept(File dir, String name) {
           return name.toLowerCase().endsWith(".shp");
       }
   }
}
