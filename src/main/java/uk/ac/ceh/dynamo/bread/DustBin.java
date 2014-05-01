package uk.ac.ceh.dynamo.bread;

/**
 *
 * @author cjohn
 */
public interface DustBin<T, W> {
    void delete(BreadSlice<T, W> slice);
}
