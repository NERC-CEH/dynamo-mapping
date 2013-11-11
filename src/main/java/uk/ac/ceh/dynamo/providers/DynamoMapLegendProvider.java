package uk.ac.ceh.dynamo.providers;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.ac.ceh.dynamo.DynamoMap;
import uk.ac.ceh.dynamo.DynamoMapMethod;
import uk.ac.ceh.dynamo.Provider;
import uk.ac.ceh.dynamo.Provides;

/**
 *
 * @author Chris Johnson
 */
@Component
@Provider
public class DynamoMapLegendProvider {
    
    @Provides(DynamoMapMethod.LEGEND) 
    public Map<String, String[]> processRequestParameters(DynamoMap.GridLayer layer) {
        Map<String, String[]> toReturn = new HashMap<>();
        toReturn.put("SERVICE", new String[]{"WMS"});
        toReturn.put("VERSION", new String[]{"1.1.1"});
        toReturn.put("REQUEST", new String[]{"GetLegendGraphic"});
        toReturn.put("TRANSPARENT", new String[]{"true"});
        toReturn.put("FORMAT", new String[]{"image/png"});
        toReturn.put("LAYER", new String[]{ layer.layer() });
        return toReturn;
    }
}
