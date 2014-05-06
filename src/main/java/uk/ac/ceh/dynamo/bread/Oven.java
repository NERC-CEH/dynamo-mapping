package uk.ac.ceh.dynamo.bread;

import java.util.List;

/**
 * The interface for an Oven, this defines the actual work which a given bakery does.
 * The main method here being cook, given a bread slice and some ingredients we can
 * bake a slice of bread.
 * @author Christopher Johnson
 */
public interface Oven<T, I, W> {
    /**
     * Obtain a list of BreadSlices which are already present on the work surface
     * This method will be called by a bakery when it is first constructed to get
     * it into the correct state.
     * 
     * Each breadslice generated by this method should have the same workSurface and
     * dustbin as passed in the this method.
     * 
     * @param clock The bakery's clock
     * @param workSurface the work surface to scan
     * @param bin the bakery's dustbin
     * @param staleTime the time it takes bread slices to go stale
     * @return A list of existing BreadSlices from the work surface, never null
     */
    List<BreadSlice<T, W>> reload(Clock clock, W workSurface, DustBin<W> bin, long staleTime);
    
    /**
     * Given a bread slice and some ingredients, cook a slice of bread. This 
     * method will do the actual processing that a bakery is set up to do. Here 
     * you will want to perform some long processing and store the results somewhere, 
     * either in memory or on disk. To this end, each bread slice is provided with 
     * the bakery's work surface.
     * 
     * @param slice The slice which we want to cook in this oven. 
     * @param ingredients The ingredients (maybe an sql statement) to bake
     * @return An instance of T which was built from ingredients
     * @throws BreadException if it was not possible to cook the ingredients
     */
    T cook(BreadSlice<T, W> slice, I ingredients) throws BreadException;
}
