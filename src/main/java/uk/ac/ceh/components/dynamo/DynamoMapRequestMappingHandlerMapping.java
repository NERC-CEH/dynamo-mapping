package uk.ac.ceh.components.dynamo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 *
 * @author Christopher Johnson
 */
public class DynamoMapRequestMappingHandlerMapping extends RequestMappingHandlerMapping {
    
    private @Autowired RequestMappingHandlerAdapter adapter;
    private @Autowired GridMapRequestFactory gridMapHelper;
    private @Autowired ServletContext context;
    
    private final Set<Method> methods;
    
    public DynamoMapRequestMappingHandlerMapping() {
        this.methods = HandlerMethodSelector.selectMethods(DynamoMapController.class, new ReflectionUtils.MethodFilter() {
            @Override
            public boolean matches(Method method) {
                return getMappingForMethod(method, DynamoMapController.class) != null;
            }
        });
    }
    
    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        DynamoMapController controller = new DynamoMapController(method.getAnnotation(DynamoMap.class), 
                                                                    getProviderMethods(), 
                                                                    gridMapHelper, 
                                                                    context);
        
        for(Method m : methods) {
            RequestMappingInfo newMapping = mapping.combine(getMappingForMethod(m, DynamoMapController.class));
            super.registerHandlerMethod(controller, m, newMapping);
        }
    }
    
    private Map<DynamoMapMethod, List<InvocableHandlerMethod>> getProviderMethods() {
        Map<DynamoMapMethod, List<InvocableHandlerMethod>> toReturn = new EnumMap<>(DynamoMapMethod.class);
        for(DynamoMapMethod method: DynamoMapMethod.values()) {
            toReturn.put(method, getInvocableHandlerMethod(method));
        }
        return toReturn;
    }
    
    private List<InvocableHandlerMethod> getInvocableHandlerMethod(DynamoMapMethod providesFor){
        List<InvocableHandlerMethod> toReturn = new ArrayList<>();
        //Get the collection of providers
        Collection<Object> providers = getApplicationContext().getBeansWithAnnotation(Provider.class).values();
        for(Object provider: providers) {
            for(Method providesMethod: provider.getClass().getMethods()) {
                if(isProviderFor(providesMethod, providesFor)) {
                    InvocableHandlerMethod methodInvoker = new InvocableHandlerMethod(provider, providesMethod);
                    methodInvoker.setHandlerMethodArgumentResolvers(adapter.getArgumentResolvers());
                    toReturn.add(methodInvoker);
                }
            }
        }
        return toReturn;
    }
    
    /**
     * Check the method to see if it is annotated as being a provider for the 
     * specified DynamoMapMethod
     */
    private boolean isProviderFor(Method method, DynamoMapMethod providesFor) {
        Provides providesAnnot = method.getAnnotation(Provides.class);
        if(providesAnnot != null) {
            return Arrays.asList(providesAnnot.value()).contains(providesFor);
        }
        return false;
    }
}
