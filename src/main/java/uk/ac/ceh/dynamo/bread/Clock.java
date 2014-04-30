package uk.ac.ceh.dynamo.bread;

/**
 * A simple interface which allows us to fake the system clock. This is of particular
 * use in unit tests of the Bread framework
 * @author Christopher Johnson
 */
public interface Clock {
    /**
     * A simple method to obtain the current time in milliseconds. This is used be the baker to
     * determine if BreadSlices have gone stale or rotten
     * @return 
     */
    long getTimeInMillis();
}
