package uk.ac.ceh.dynamo.bread;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A simple lombok wrapper around a climate. It will create with a default
 * climate of 1 but can be manipulated later
 * @author Christopher Johnson
 */
@Data
@AllArgsConstructor
public class UpdatableClimate implements Climate {
    private double currentClimate = 1d;

    @Override
    public double getCurrentClimate(Bakery bakery) {
        return getCurrentClimate();
    }
}
