package uk.ac.ceh.dynamo.providers;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import uk.ac.ceh.dynamo.DynamoMapMethod;
import uk.ac.ceh.dynamo.Provider;
import uk.ac.ceh.dynamo.Provides;

/**
 *
 * @author Chris Johnson
 */
@Component
@Provider
public class DynamoMapMapProvider {
    private static final Map<String, String> FORMATS;
    
    static {
        FORMATS = new HashMap<>();
        FORMATS.put("png", "image/png");
        FORMATS.put("gif", "image/gif");
        FORMATS.put("jpeg", "image/jpeg");
    }
    
    @Provides(DynamoMapMethod.MAP)
    public Map<String, String[]> processRequestParameters(HttpServletRequest request) {
        Map<String, String[]> toReturn = new HashMap<>();
        toReturn.put("SERVICE", new String[]{"WMS"});
        toReturn.put("VERSION", new String[]{"1.1.1"});
        toReturn.put("REQUEST", new String[]{"GetMap"});
        toReturn.put("STYLES", new String[]{""});
        toReturn.put("TRANSPARENT", new String[]{"true"});
        toReturn.put("FORMAT", new String[]{ checkAndGetValue(FORMATS, getValue(request.getParameterMap(), "format", "png")) });
        return toReturn;
    }
    
    private static String checkAndGetValue(Map<String, String> map, String toGet) {
        if(!map.containsKey(toGet)) {
            throw new IllegalArgumentException("I don't understand " + toGet + 
                    " valid values are " + map.keySet());
        }
        return map.get(toGet);
    }
    
    private static String getValue(Map<String, String[]> query, String toGet, String defaultVal) {
        return (query.containsKey(toGet)) ? query.get(toGet)[0] : defaultVal;
    }
}
