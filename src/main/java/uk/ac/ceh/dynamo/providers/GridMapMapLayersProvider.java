package uk.ac.ceh.dynamo.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ceh.dynamo.GridMap;
import uk.ac.ceh.dynamo.GridMap.Layer;
import uk.ac.ceh.dynamo.GridMapMethod;
import uk.ac.ceh.dynamo.Provides;

/**
 *
 * @author Chris Johnson
 */
public class GridMapMapLayersProvider {
    @Provides(GridMapMethod.MAP)
    public Map<String, String[]> processRequestParameters(
            GridMap gridMapProperties,
            @RequestParam(value="background", required=false) List<String> requestedBackgroundLayers,
            @RequestParam(value="overlay", required=false) List<String> requestedOverlayLayers,
            GridMap.GridLayer layer) {
        Map<String, String[]> toReturn = new HashMap<>();
        
        List<String> layersToRequest = new ArrayList<>();
        
        layersToRequest.addAll(getLayersToRequest(requestedBackgroundLayers, gridMapProperties.backgrounds(), gridMapProperties.defaultBackgrounds()));
        layersToRequest.add(layer.layer()); //add the resolution layer
        layersToRequest.addAll(getLayersToRequest(requestedOverlayLayers, gridMapProperties.overlays(), gridMapProperties.defaultOverlays()));
        
        toReturn.put("LAYERS",  new String[]{ StringUtils.collectionToCommaDelimitedString(layersToRequest) });
        return toReturn;
    }
    
    private List<String> getLayersToRequest(List<String> requestedBackgroundLayers, Layer[] validLayers, String[] defaultLayers) {
        List<Layer> supportedBackgroundLayers = Arrays.asList(validLayers); //get the valid background layers as a list
        List<String> backgroundLayers = (requestedBackgroundLayers != null)     //get the requested background layers
                ? requestedBackgroundLayers 
                : Arrays.asList(defaultLayers); 
        
        List<Layer> toReturn = new ArrayList<>();
        //check the requested list is valid
        for(String currRequestedLayer : backgroundLayers) {
            toReturn.add(getBackground(currRequestedLayer, supportedBackgroundLayers));
        }   
        return getWMSLayersFromLayerList(toReturn);
    }
    
    private static Layer getBackground(String name, List<Layer> toFindIn) {
        for(Layer currBackground : toFindIn) {
            if(currBackground.name().equals(name)) {
                return currBackground;
            }
        }
        throw new IllegalArgumentException("The background layer " + name + 
                        " was requested but is not valid.");
    }
    
    private static List<String> getWMSLayersFromLayerList(List<Layer> layerList) {
        List<String> toReturn = new ArrayList<>();
        for(Layer currBackground : layerList) {
            toReturn.addAll(Arrays.asList(currBackground.layers()));
        }
        return toReturn;
    } 
}
