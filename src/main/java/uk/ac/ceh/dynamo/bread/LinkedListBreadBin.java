package uk.ac.ceh.dynamo.bread;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Christopher Johnson
 */
public class LinkedListBreadBin implements BreadBin {
    private List<BreadSlice> breadSlices;
    
    public LinkedListBreadBin() {
        breadSlices = new LinkedList<>();
    }

    @Override
    public List<BreadSlice> removeRottenBreadSlices(long latestBakeTime) {
        List<BreadSlice> rottenBreadSlices = new LinkedList<>();
        
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

    @Override
    public void addBreadSlice(BreadSlice slice) {
        breadSlices.add(slice);
    }
}
