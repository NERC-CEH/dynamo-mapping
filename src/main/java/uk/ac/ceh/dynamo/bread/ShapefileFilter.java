package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author Christopher Johnson
 */
public class ShapefileFilter implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".shp");
    }

}
