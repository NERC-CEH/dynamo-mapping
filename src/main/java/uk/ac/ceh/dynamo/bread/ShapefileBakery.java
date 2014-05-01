package uk.ac.ceh.dynamo.bread;

import java.io.File;

/**
 * Once constructed calls to getData will return the location of a shapefile on 
 * disk for a supplied sql statement. Subsequent sql statements will yield the 
 * same shape file instantly as long as the staleTime has not surpassed for that
 * sql statement (represented as a bread slice) and as long as the climate does
 * make that slice of bread mouldy.
 * @author Christopher Johnson
 */
public class ShapefileBakery extends Bakery<String, File> {
    public ShapefileBakery(File workSurface, Climate climate, ShapefileGenerator generator, long staleTime, long rottenTime) {
        super(workSurface, climate, generator, generator, staleTime, rottenTime);
    }
}
