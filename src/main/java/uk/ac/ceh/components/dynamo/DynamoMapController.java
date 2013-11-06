package uk.ac.ceh.components.dynamo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.ac.ceh.components.dynamo.providers.GridMapRequestFactory;

/**
 *
 * @author Chris Johnson
 */
public class DynamoMapController {
    private final DynamoMap annotation;
    private final Map<DynamoMapMethod, List<InvocableHandlerMethod>> providers;
    private final ServletContext context;
    private final GridMapRequestFactory gridMapHelper;

    /**
     * Constructor for the dynamo mapping controller. 
     * @param annotation The annotation which is present on the request mapped method
     *  which this controller will use as its mapping endpoint
     * @param providers A map of method types to the providers for that type
     * @param gridMapHelper The grid map helper to use for grid mapping
     * @param context The context this controller is running in
     * @see DynamoMapRequestMappingHandlerMapping
     */
    public DynamoMapController(DynamoMap annotation, 
                                Map<DynamoMapMethod, List<InvocableHandlerMethod>> providers, 
                                GridMapRequestFactory gridMapHelper, 
                                ServletContext context) {
        this.annotation = annotation;
        this.providers = providers;
        this.gridMapHelper = gridMapHelper;
        this.context = context;
    }
    
    @RequestMapping({"map", "legend"})
    public void map(NativeWebRequest request, ModelAndViewContainer mavContainer, HttpServletResponse response) throws NoSuchMethodException, Exception {
        String uri = request.getNativeRequest(HttpServletRequest.class).getRequestURI();
        
        //Obtain the end part of the requested path until the last '/'. Lookup this 
        //in the DynamoMapMethod enum
        DynamoMapMethod dynamoMethod = DynamoMapMethod.valueOf(uri.substring(uri.lastIndexOf('/') + 1).toUpperCase());
        
        InterceptedHttpServletRequest newRequest = provideForRequest(dynamoMethod, request, mavContainer);
        //Strip the map method part of the url and the servlet context, this is the address
        //to proxy with the wrapped HttpServletRequest
        String host = uri.substring(context.getContextPath().length(), uri.lastIndexOf('/'));
        RequestDispatcher requestDispatcher = context.getRequestDispatcher(host);
        requestDispatcher.forward(newRequest, response);
    }

    /*
     * The following resource will handle resolution requests and produce a
     * map whose keys are the zoom levels for the grid map and values are arrays of
     * the valid resolutions for the respective zoom level
     */
    @RequestMapping("resolutions")
    @ResponseBody
    public Map<String, List<String>> resolutions(HttpServletRequest request,
            @RequestParam(value = "feature", required = false) String featureId,
            @RequestParam(value = "nationalextent", required = false) String nationalExtent) {
        Map<String, List<String>> toReturn = new HashMap<>();
        BoundingBox featureToFocusOn = gridMapHelper.getFeatureToFocusOn(featureId, nationalExtent, annotation);
        
        for(int i=1; i<=GridMapRequestFactory.ZOOM_LEVELS; i++) {
            toReturn.put(Integer.toString(i), getAvailableResolutionListForImagesSize(
                annotation.layers(), featureToFocusOn, i
            ));
        }
        return toReturn;
    }
    
    private InterceptedHttpServletRequest provideForRequest(DynamoMapMethod type, NativeWebRequest request, ModelAndViewContainer mavContainer) throws Exception {
        return new InterceptedHttpServletRequest(
                request.getNativeRequest(HttpServletRequest.class), 
                provideFor(type, request, mavContainer));
    } 
    
    private Map<String, String[]> provideFor(DynamoMapMethod type, NativeWebRequest request, ModelAndViewContainer mavContainer) throws Exception {
        Map<String, String[]> toReturn = new HashMap<>();
        DynamoMap.GridLayer resolution = getResolution(request.getParameter("resolution"));
        for(InvocableHandlerMethod handler: providers.get(type)) {
            Map<String,String[]> providersResponse = (Map<String, String[]>)handler.invokeForRequest(request, mavContainer, annotation, resolution);
            toReturn.putAll(providersResponse);
        }
        return toReturn;
    }
    
    private List<String> getAvailableResolutionListForImagesSize(DynamoMap.GridLayer[] layers, BoundingBox featureToFocusOn, int imageSize){
        List<String> toReturn = new ArrayList<>();
        for(DynamoMap.GridLayer currLayer : layers) {
            if(gridMapHelper.getGridMapRequest(featureToFocusOn, currLayer.resolution(), imageSize).isValidRequest()) {
                toReturn.add(currLayer.name());
            }
        }
        return toReturn;
    }
        
    private DynamoMap.GridLayer getResolution(String resolution) {
        //Work out which layer to use. Either one requested or this Grid maps default
        String resolutionToUse = (resolution != null) ? resolution : annotation.defaultLayer();
        //Find the layer which corresponds to this resolution
        for(DynamoMap.GridLayer currLayer : annotation.layers()) {
            if(resolutionToUse.equals(currLayer.name())) {
                return currLayer;
            }
        }
        throw new IllegalArgumentException("This map service does not support the resolution " + resolutionToUse);
    }
}
