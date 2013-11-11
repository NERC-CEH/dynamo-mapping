package uk.ac.ceh.dynamo;

/**
 * A feature resolver which given a featureid returns a BoundingBox which entirely
 * covers the feature represented by that feature id
 * @author Christopher Johnson
 */
public interface FeatureResolver {
    /**
     * Generates a bounding box which entirely surrounds the specified feature
     * @param featureId The id of the feature to resolve
     * @return A BoundingBox which surrounds the feature
     * @throws IllegalArgumentException if there is no feature which is 
     *  represented by featureid
     */
    BoundingBox getFeature(String featureId) throws IllegalArgumentException;
}
