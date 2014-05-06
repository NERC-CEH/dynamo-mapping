package uk.ac.ceh.dynamo.bread;

import lombok.AllArgsConstructor;

/**
 * The following climate will vary dependant on the amount of bread slices which
 * are currently exist in the supplied bakery
 * given directory.
 * @author Christopher Johnson
 */
@AllArgsConstructor
public class BreadSliceCountClimateMeter implements ClimateMeter {
    private final int maxBreadSlices;

    /**
     * Calculates a value between 1 and 0 which is linearly mapped to the amount
     * of bread slices currently in existance for the given bakery
     * @param bakery the bakery to calculate the climate of
     * @return 0 when no slices are present, ramping up to 1 when the bakeries
     *  slice count moves to maxBreadSlices;
     */
    @Override
    public double getCurrentClimate(Bakery bakery) {
        return Math.max(1 - ((double)bakery.getBreadSliceCount() / (double)maxBreadSlices), 0);
    }
}