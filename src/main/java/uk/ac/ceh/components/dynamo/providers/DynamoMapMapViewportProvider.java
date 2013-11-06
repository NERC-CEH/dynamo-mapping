package uk.ac.ceh.components.dynamo.providers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ceh.components.dynamo.BoundingBox;
import uk.ac.ceh.components.dynamo.DynamoMap;
import uk.ac.ceh.components.dynamo.DynamoMapMethod;
import uk.ac.ceh.components.dynamo.Provider;
import uk.ac.ceh.components.dynamo.Provides;
import uk.ac.ceh.components.dynamo.providers.GridMapRequestFactory.GridMapRequest;

/**
 *
 * @author Chris Johnson
 */
@Component
@Provider
public class DynamoMapMapViewportProvider {
    @Autowired GridMapRequestFactory helper;
    
    @Provides(DynamoMapMethod.MAP)
    public Map<String, String[]> processRequestParameters(
                    DynamoMap gridMapProperties, 
                    DynamoMap.GridLayer layer,
                    @RequestParam(value="imagesize", required=false, defaultValue="10") String imagesizeStr,
                    @RequestParam(value="feature", required=false) String featureId,
                    @RequestParam(value="nationalextent", required=false) String nationExtent) {
        Map<String, String[]> toReturn = new HashMap<>();
        BoundingBox featureToFocusOn = helper.getFeatureToFocusOn(featureId, nationExtent, gridMapProperties);
        GridMapRequest request = helper.getGridMapRequest(featureToFocusOn, layer.resolution(), Integer.parseInt(imagesizeStr));
        
        if(!request.isValidRequest()) {
            throw new IllegalArgumentException("It is not possible to create an image for the given parameters.");
        }
        
        toReturn.put("SRS",     new String[]{ featureToFocusOn.getEpsgCode() });
        toReturn.put("HEIGHT",  new String[]{ Integer.toString(request.getHeight()) });
        toReturn.put("WIDTH",   new String[]{ Integer.toString(request.getWidth()) });
        toReturn.put("BBOX", new String[] {   request.getBBox()});
        return toReturn;
    }
}
