package uk.ac.ceh.dynamo.bread;

import java.util.List;

/**
 * A Bread Bin is a utility for a baker to help keep track of which slices of 
 * bread a baker has baked. As is the nature with these things, at a given time
 * bread will go mouldy. Bread bins have a method to remove old mouldy pieces
 * of bread. We can also remove arbitrary bread slices if they are no longer needed.
 * @author Christopher Johnson
 */
public interface BreadBin {
    /**
     * Removes and returns a list of mouldy breadslices, those baked before 
     * latest bake time
     * @param latestBakedTime
     * @return a list of breadslices which were baked before the latestBakeTime.
     */
    List<BreadSlice> removeMouldy(long latestBakeTime);
    
    /**
     * Adds the given bread slice to the bread bin. Bread slices will be added
     * in baked order.
     * @param slice 
     */
    void add(BreadSlice slice);
    
    /**
     * A slice of bread may need to be pulled out from the middle of the bread bin
     * @param slice
     * @return if the slice of bread was removed
     */
    boolean remove(BreadSlice slice);
}
