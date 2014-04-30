package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A shape file remover. This will delete shape files when they are no longer 
 * needed. A baker will be responsible for deleting these shapefiles. 
 * 
 * This particular implementation will queue up requests to delete shapefiles on
 * to a single threaded executor service. This will reduce the amount of disk
 * activity occurring simultaneously
 * @author Christopher Johnson
 */
public class ShapefileRemover {
    private final ExecutorService remover;
    private final File scratchPad;
    
    /**
     * Creates a shapefile remover which operates in a given scratchpad
     * @param scratchPad to delete shapefiles from
     */
    public ShapefileRemover(File scratchPad) {
        this.remover = Executors.newSingleThreadExecutor();
        this.scratchPad = scratchPad;
    }
    
    /**
     * Will queue up the request to delete the slice of bread onto the executor
     * service
     * @param slice to delete
     */
    public void submitForDeletion(final BreadSlice slice) {
        remover.submit(new Runnable() {
            @Override
            public void run() {
                new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".shp").delete();
                new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".shx").delete();
                new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".dbf").delete();
            }
        });
    }
}
