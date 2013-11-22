package uk.ac.ceh.dynamo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.support.DefaultDataBinderFactory;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.ac.ceh.dynamo.providers.GridMapLegendProvider;
import uk.ac.ceh.dynamo.providers.GridMapMapLayersProvider;
import uk.ac.ceh.dynamo.providers.GridMapMapProvider;
import uk.ac.ceh.dynamo.providers.GridMapMapViewportProvider;

/**
 * The request mapping handler which will register the GridMapControllers request
 * handlers at all of the end points of Controller Methods annotated with @GridMap
 * @author Christopher Johnson
 */
public class GridMapRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    
    private @Autowired RequestMappingHandlerAdapter adapter;
    private @Autowired(required=false) FeatureResolver resolver;
    private @Autowired ServletContext context;
    
    private final GridMapRequestFactory gridMapHelper;
    private final Set<Method> methods;
    private final List<Object> providers;
    
    /**
     * Default constructor which registers the default providers
     * @see GridMapLegendProvider
     * @see GridMapMapLayersProvider
     * @see GridMapMapProvider
     * @see GridMapMapViewportProvider
     */
    public GridMapRequestMappingHandlerMapping() {
        this(new GridMapLegendProvider(),
                new GridMapMapLayersProvider(),
                new GridMapMapProvider(),
                new GridMapMapViewportProvider());
    }
    
    /**
     * Constructor which allows you to register your own set of GridMap Providers
     * which have methods annotated with @Provides
     * @param providers Variable length list of providers
     */
    public GridMapRequestMappingHandlerMapping(Object... providers) {
        this(Arrays.asList(providers));
    }
    
    /**
     * Constructor which allows you to register a list of providers 
     * @param providers A list of providers which have methods annotated with @Provides
     */
    public GridMapRequestMappingHandlerMapping(List<Object> providers) {
        this.providers = new ArrayList<>(providers);
        this.gridMapHelper = new GridMapRequestFactory(resolver);
        //scan for request mapping methods in the GridMapController
        this.methods = new HashSet<>();
        for(Method currMethod: GridMapController.class.getMethods()) {
            if(getMappingForMethod(currMethod, GridMapController.class) != null) {
                this.methods.add(currMethod);
            }
        }
    }
    
    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        GridMapController controller = new GridMapController(method.getAnnotation(GridMap.class), 
                                                                    getProviderMethods(), 
                                                                    gridMapHelper, 
                                                                    context);
        
        for(Method m : methods) {
            RequestMappingInfo newMapping = mapping.combine(getMappingForMethod(m, GridMapController.class));
            super.registerHandlerMethod(controller, m, newMapping);
        }
    }
    
    private Map<GridMapMethod, List<InvocableHandlerMethod>> getProviderMethods() {
        Map<GridMapMethod, List<InvocableHandlerMethod>> toReturn = new EnumMap<>(GridMapMethod.class);
        for(GridMapMethod method: GridMapMethod.values()) {
            toReturn.put(method, getInvocableHandlerMethod(method));
        }
        return toReturn;
    }
    
    private List<InvocableHandlerMethod> getInvocableHandlerMethod(GridMapMethod providesFor){
        List<InvocableHandlerMethod> toReturn = new ArrayList<>();
        //Get the collection of providers
        for(Object provider: providers) {
            for(Method providesMethod: provider.getClass().getMethods()) {
                if(isProviderFor(providesMethod, providesFor)) {
                    InvocableHandlerMethod methodInvoker = new InvocableHandlerMethod(provider, providesMethod);
                    methodInvoker.setHandlerMethodArgumentResolvers(adapter.getArgumentResolvers());
                    methodInvoker.setDataBinderFactory(new DefaultDataBinderFactory(adapter.getWebBindingInitializer()));
                    toReturn.add(methodInvoker);
                }
            }
        }
        return toReturn;
    }
    
    /**
     * Check the method to see if it is annotated as being a provider for the 
     * specified GridMapMethod
     */
    private boolean isProviderFor(Method method, GridMapMethod providesFor) {
        Provides providesAnnot = method.getAnnotation(Provides.class);
        if(providesAnnot != null) {
            return Arrays.asList(providesAnnot.value()).contains(providesFor);
        }
        return false;
    }
}
