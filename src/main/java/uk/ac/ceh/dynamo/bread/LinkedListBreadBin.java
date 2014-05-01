package uk.ac.ceh.dynamo.bread;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A linked list based implementation of a bread bin. This implementation makes
 * use of LinkedLists O(1) insertion time and O(1) removal time for pulling 
 * mouldy slices of bread from the top of the linked list
 * 
 * This is the default implementation of a bread bin and is used by the Baker
 * @see Baker
 * @author Christopher Johnson
 */
public class LinkedListBreadBin<T, W> implements BreadBin<T, W> {
    private List<BreadSlice> breadSlices;
    
    public LinkedListBreadBin() {
        breadSlices = new LinkedList<>();
    }

    /**
     * Remove and obtain the list of BreadSlices which are considered mouldy and
     * not fit for eating.
     * @param latestBakeTime The time which if baked before, the bread slice would
     *  be considered mouldy
     * @return A list of mouldy bread slices taken out of this bread bin
     */
    @Override
    public List<BreadSlice<T, W>> removeMouldy(long latestBakeTime) {
        List<BreadSlice<T, W>> rottenBreadSlices = new LinkedList<>();
        
        Iterator<BreadSlice> iterator = breadSlices.iterator();
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
     * Add a slice of bread to the end of the linked list (FIFO)
     * @param slice The slice of bread to add to the list. As this is using a 
     *  linked list, it will have O(1) timing.
     */
    @Override
    public void add(BreadSlice<T, W> slice) {
        breadSlices.add(slice);
    }
    
    /**
     * Remove a slice of bread from the linked list. This could occur anywhere
     * in the linked list and therefore may have O(n) timing.
     * @param slice the slice to remove
     * @return if the slice was removed
     */
    @Override
    public boolean remove(BreadSlice<T, W> slice) {
        return breadSlices.remove(slice);
    }
}
