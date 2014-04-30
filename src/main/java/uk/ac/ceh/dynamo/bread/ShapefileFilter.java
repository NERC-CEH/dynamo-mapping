package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A simple filename filter for locating shapefiles. Those files which have the 
 * extension .shp
 * @author Christopher Johnson
 */
public class ShapefileFilter implements FilenameFilter {

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
