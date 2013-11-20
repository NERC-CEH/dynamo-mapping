package uk.ac.ceh.dynamo.providers;

import java.util.HashMap;
import java.util.Map;
import uk.ac.ceh.dynamo.GridMap;
import uk.ac.ceh.dynamo.GridMapMethod;
import uk.ac.ceh.dynamo.Provides;

/**
 *
 * @author Chris Johnson
 */
public class GridMapLegendProvider {
    
    @Provides(GridMapMethod.LEGEND) 
    public Map<String, String[]> processRequestParameters(GridMap.GridLayer layer) {
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
