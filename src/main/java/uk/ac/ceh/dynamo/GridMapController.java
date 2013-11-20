package uk.ac.ceh.dynamo;

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

/**
 *
 * @author Chris Johnson
 */
public class GridMapController {
    private final GridMap annotation;
    private final Map<GridMapMethod, List<InvocableHandlerMethod>> providers;
    private final ServletContext context;
    private final GridMapRequestFactory gridMapHelper;

    /**
     * Constructor for the grid mapping controller. 
     * @param annotation The annotation which is present on the request mapped method
     *  which this controller will use as its mapping endpoint
     * @param providers A map of method types to the providers for that type
     * @param gridMapHelper The grid map helper to use for grid mapping
     * @param context The context this controller is running in
     * @see GridMapRequestMappingHandlerMapping
     */
    public GridMapController(GridMap annotation, 
                                Map<GridMapMethod, List<InvocableHandlerMethod>> providers, 
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
        //in the GridMapMethod enum
        GridMapMethod gridMapMethod = GridMapMethod.valueOf(uri.substring(uri.lastIndexOf('/') + 1).toUpperCase());
        
        InterceptedHttpServletRequest newRequest = provideForRequest(gridMapMethod, request, mavContainer);
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
    
    private InterceptedHttpServletRequest provideForRequest(GridMapMethod type, NativeWebRequest request, ModelAndViewContainer mavContainer) throws Exception {
        return new InterceptedHttpServletRequest(
                request.getNativeRequest(HttpServletRequest.class), 
                provideFor(type, request, mavContainer));
    } 
    
    private Map<String, String[]> provideFor(GridMapMethod type, NativeWebRequest request, ModelAndViewContainer mavContainer) throws Exception {
        Map<String, String[]> toReturn = new HashMap<>();
        GridMap.GridLayer resolution = getResolution(annotation, request.getParameter("resolution"));
        for(InvocableHandlerMethod handler: providers.get(type)) {
            Map<String,String[]> providersResponse = (Map<String, String[]>)handler.invokeForRequest(request, mavContainer, annotation, resolution, gridMapHelper);
            toReturn.putAll(providersResponse);
        }
        return toReturn;
    }
    
    private List<String> getAvailableResolutionListForImagesSize(GridMap.GridLayer[] layers, BoundingBox featureToFocusOn, int imageSize){
        List<String> toReturn = new ArrayList<>();
        for(GridMap.GridLayer currLayer : layers) {
            if(gridMapHelper.getGridMapRequest(featureToFocusOn, currLayer.resolution(), imageSize).isValidRequest()) {
                toReturn.add(currLayer.name());
            }
        }
        return toReturn;
    }
    
    /**
     * Obtain the selected GridLayer from an instance of GridMap and a requested 
     * resolution
     * @param annotation The configuration for the gridmap service
     * @param resolution The requested resolution which should be defined in the
     *  layers section of the grid map annotation. If null, return the default 
     *  GridMap.GridLayer as defined in the annotation
     * @return The requested GridLayer
     */
    public static GridMap.GridLayer getResolution(GridMap annotation, String resolution) {
        //Work out which layer to use. Either one requested or this Grid maps default
        String resolutionToUse = (resolution != null) ? resolution : annotation.defaultLayer();
        //Find the layer which corresponds to this resolution
        for(GridMap.GridLayer currLayer : annotation.layers()) {
            if(resolutionToUse.equals(currLayer.name())) {
                return currLayer;
            }
        }
        throw new IllegalArgumentException("This map service does not support the resolution " + resolutionToUse);
    }
}
