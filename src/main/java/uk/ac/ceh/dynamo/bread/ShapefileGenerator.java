package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * A simple class for managing the ogr2ogr utility and submitting sql statements
 * to obtain shapefiles.
 * 
 * This implementation enforces only a certain amount of simultaneous class to
 * #getShapefile(File, String). This simply limits the maximum amount of 
 * processes which java will fork to create.
 * 
 * @author Christopher Johnson
 */
public class ShapefileGenerator {
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
        this.ogr2ogr = ogr2ogr;
        this.connectionString = connectionString;
        this.semaphore = new Semaphore(simultaneousProcesses, true);
    }
    
    /**
     * Performs a call to the ogr2ogr command. This method will wait if the maximum
     * simultaneous calls are being performed.
     * @param output The desired output file
     * @param sql the sql statement to use for generating the shape file
     * @return the outputed shape file (the .shp part)
     * @throws BreadException 
     */
    public File getShapefile(File output, String sql) throws BreadException {
        try {
            semaphore.acquire();
            try {
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
                return output;
            }
            finally {
                semaphore.release();
            }
        }
        catch(IOException | InterruptedException ex) {
            throw new BreadException("Failed to generate shapefile", ex);
        }
    }
}
