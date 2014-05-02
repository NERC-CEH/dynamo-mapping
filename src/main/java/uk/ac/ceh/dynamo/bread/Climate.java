package uk.ac.ceh.dynamo.bread;

/**
 * The following interface defines the climate in which a baker is baking it will
 * dictate, along with the best before time, if a slice of bread has gone mouldy
 * or not.
 * 
 * The idea is that you would implement climate to limit the size of disk used for
 * generating files. For example, you may monitor the size of a directory. If it
 * goes over a certain threshold we can start ramping down the climate. This will
 * mean that bread slices will expire quicker and overall disk usage wont jump too
 * high.
 * @author Christopher Johnson
 */
public interface Climate<T,W> {
    /**
     * Returns a value between 0 and 1. Where 0 represents the worst type of 
     * climate, so bad that once a bread slice has been baked it will instantly 
     * become mouldy. And 1, the climate in which the best before time is met.
     * @param bakery the bakery to determine the climate of
     * @return The current climate in which a given baker is operating.
     */
    double getCurrentClimate(Bakery<T, W> bakery);
}
