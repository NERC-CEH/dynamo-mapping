package uk.ac.ceh.dynamo.bread;

import java.util.List;

/**
 *
 * @author Christopher Johnson
 */
public interface Oven<T, W> {
    List<BreadSlice<T, W>> reload(Clock clock, W workSurface, DustBin<T,W> bin, long staleTime);
    T cook(W workSurface, BreadSlice<T, W> slice, String ingredients) throws BreadException;
}
