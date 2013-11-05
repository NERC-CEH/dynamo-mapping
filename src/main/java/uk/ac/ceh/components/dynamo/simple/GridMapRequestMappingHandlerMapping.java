package uk.ac.ceh.components.dynamo.simple;

import java.lang.String;
import java.lang.reflect.Method;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethodSelector;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    /*@PostConstruct
    public void registerGridMaps() throws NoSuchMethodException {
        
        
        for(Entry<RequestMappingInfo, HandlerMethod> entry: handlerMapping.getHandlerMethods().entrySet()){
            GridMap methodAnnotation = entry.getValue().getMethodAnnotation(GridMap.class);
            if(methodAnnotation != null) {
                //RequestMappingInfo subRequestMappingInfo = getSubRequestMappingInfo(entry.getKey(), new PatternsRequestCondition("map"));
                
                RequestMappingInfo patterns = entry.getKey();
                
                /*for(String urlPath: patterns) {
                    GridMapController controller = new GridMapController(urlPath);
                    //RequestMappingInfo.class
                    registerHandler(urlPath + "/map", new HandlerMethod(controller, "handleRequest", HttpServletRequest.class, HttpServletResponse.class));
                }/
            }
        }
    }*/
    
    /*protected void initHandlerMethods() {
        detectHandlerMethods(new GridMapController());
    }*/
    
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        GridMapController controller = new GridMapController();
        
        Set<Method> methods = HandlerMethodSelector.selectMethods(GridMapController.class, new ReflectionUtils.MethodFilter() {
            public boolean matches(Method method) {
                return getMappingForMethod(method, GridMapController.class) != null;
            }
        });
        
        for(Method m : methods) {
            RequestMappingInfo newMapping = mapping.combine(getMappingForMethod(m, GridMapController.class));
            super.registerHandlerMethod(controller, m, newMapping);
        }
    }
    
    public static class GridMapController {
        
        @RequestMapping("resolutions")
        public void resolutions(NativeWebRequest request, ModelAndViewContainer mavContainer) throws NoSuchMethodException, Exception {
            Test test = new Test();
            InvocableHandlerMethod invoker = new InvocableHandlerMethod(test, "dummy", String.class);
            invoker.invokeForRequest(request, mavContainer);
            
        }
        
        @RequestMapping("handle")
        @ResponseBody()
        public String handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            //RequestDispatcher requestDispatcher = request.getRequestDispatcher(mapping);
            
            //requestDispatcher.forward(map(request), response);
            //return null;
            return "This is a test";
        }

        /*private static HttpServletRequest map(HttpServletRequest request) {
            InterceptedHttpServletRequest newReq = new InterceptedHttpServletRequest(request);
            newReq.setParameterValues("hello", new String[] {"injected"});
            return newReq;
        }*/
        
    }
    
    private static class Test {
        public void dummy(@RequestParam("bob") String request) {
            System.out.println("CHRIS" + request);
        }
    }
    
    private static RequestMappingInfo getSubRequestMappingInfo(RequestMappingInfo info, PatternsRequestCondition patterns) {
        return info.combine(new RequestMappingInfo(patterns, null,null,null,null,null,null));
    }
}
