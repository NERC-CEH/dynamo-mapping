package uk.ac.ceh.dynamo.bread;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A Bread Bin is a utility for a baker to help keep track of which slices of 
 * bread a baker has baked. As is the nature with these things, at a given time
 * bread will go mouldy. Bread bins have a method to remove old mouldy pieces
 * of bread. We can also remove arbitrary bread slices if they are no longer needed.
 * @see Bakery
 * @author Christopher Johnson
 */
public class BreadBin<T, W> {
    private List<BreadSlice<T,W>> breadSlices;
    
    /**
     * A linked list based implementation of a bread bin. This implementation makes
     * use of LinkedLists O(1) insertion time and O(1) removal time for pulling 
     * mouldy slices of bread from the top of the linked list
     * 
     * This is the default implementation of a bread bin and is used by the Baker
     */
    public BreadBin() {
        breadSlices = new LinkedList<>();
    }
    
    /**
     * Constructs a bread bin with a given backing list of bread slices
     * @param breadSlices 
     */
    public BreadBin(List<BreadSlice<T,W>> breadSlices) {
        this.breadSlices = breadSlices;
    }

    /**
     * Remove and obtain the list of BreadSlices which are considered mouldy and
     * not fit for eating.
     * @param latestBakeTime The time which if baked before, the bread slice would
     *  be considered mouldy
     * @return A list of mouldy bread slices taken out of this bread bin
     */
    public List<BreadSlice<T, W>> removeMouldy(long latestBakeTime) {
        List<BreadSlice<T, W>> rottenBreadSlices = new LinkedList<>();
        
        Iterator<BreadSlice<T,W>> iterator = breadSlices.iterator();
        while(iterator.hasNext()) {
            BreadSlice slice = iterator.next();
            
            if (slice.isBaked() && slice.getTimeBaked() < latestBakeTime) {
                rottenBreadSlices.add(slice);
                iterator.remove();
            }
            else {
                break;
            }
        }
        return rottenBreadSlices;
    }

    /**
     * Adds the given bread slice to the bread bin. Bread slices will be added
     * in baked order.
     * @param slice 
     */
    public void add(BreadSlice<T, W> slice) {
        breadSlices.add(slice);
    }
    
    /**
     * A slice of bread may need to be pulled out from the middle of the bread bin
     * @param slice
     * @return if the slice of bread was removed
     */
    public boolean remove(BreadSlice<T, W> slice) {
        return breadSlices.remove(slice);
    }
}
