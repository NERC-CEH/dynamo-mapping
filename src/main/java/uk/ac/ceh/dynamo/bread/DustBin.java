package uk.ac.ceh.dynamo.bread;

/**
 * An interface which defines a dust bin for a bakery. A dust bin has the method
 * to remove a BreadSlice when it is no longer needed.
 * 
 * The dust bin is only typed on the workspace, not the actual output of the BreadSlice.
 * This is because the details which are required for removing a bread slice should
 * be defined by the id and hash of the bread slice. The actual generated content
 * should be irrelevant
 * @author cjohn
 */
public interface DustBin<W> {
    /**
     * Given some bread slice, submit for deletion. The implementation of this 
     * method may do nothing delete the slice straight away, or submit for deletion
     * at a later time.
     * @param slice the slice to clear up
     */
    void delete(BreadSlice<?, W> slice);
}
