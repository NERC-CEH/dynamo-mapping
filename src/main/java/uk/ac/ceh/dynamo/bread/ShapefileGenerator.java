package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Christopher Johnson
 */
public class ShapefileGenerator {
    private final Semaphore semaphore;
    private final String ogr2ogr, connectionString;
    
    public ShapefileGenerator(String ogr2ogr, String connectionString, int simultaneousProcesses) {
        this.ogr2ogr = ogr2ogr;
        this.connectionString = connectionString;
        this.semaphore = new Semaphore(simultaneousProcesses, true);
    }
    
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
