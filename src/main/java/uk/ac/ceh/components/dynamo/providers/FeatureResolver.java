package uk.ac.ceh.components.dynamo.providers;

import uk.ac.ceh.components.dynamo.BoundingBox;

/**
 *
 * @author Christopher Johnson
 */
public interface FeatureResolver {
    public BoundingBox getFeature(String featureId) throws IllegalArgumentException;
}
