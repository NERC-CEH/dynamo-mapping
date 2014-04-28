package uk.ac.ceh.dynamo.bread;

import java.util.List;

/**
 *
 * @author Christopher Johnson
 */
public interface BreadBin {
    /**
     * Returns a list of breadslices which were baked before the latestBakeTime.
     * @param latestBakedTime
     * @return 
     */
    List<BreadSlice> removeRottenBreadSlices(long latestBakeTime);
    
    /**
     * Adds the given bread slice to the bread bin. Bread slices will be added
     * in baked order.
     * @param slice 
     */
    void addBreadSlice(BreadSlice slice);
}
