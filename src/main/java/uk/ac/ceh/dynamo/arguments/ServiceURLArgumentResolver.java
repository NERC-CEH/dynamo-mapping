package uk.ac.ceh.dynamo.arguments;

import uk.ac.ceh.dynamo.arguments.annotations.ServiceURL;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * A spring mvc argument resolver which resolves Strings which are annotated 
 * with @ServiceURL.
 * 
 * It does this by reading a query parameters sent during an HttpServletRequest
 * and reporting the subset of those query parameters which are responsible for
 * generating the Spring MVC model.
 * 
 * For example a spring mvc requestmapping which handles WMS requests may be called
 * with lots of parameters, which are not necessarily specific for generating a 
 * spring mvc model (SERVICE=GetMap&VERSION=1.3.0)
 * 
 * These parameters can be stripped from the result of this argument resolver
 * so only the relevant ones are kept. To determine which parameters are relevant
 * QueryParameterResolvers are required
 * 
 * @see ServiceURL
 * @see QueryParameterResolver
 * @see RequestParamResolver
 * @author Christopher Johnson
 */
public class ServiceURLArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String URL_ENCODING = "UTF-8";
    
    private final List<QueryParameterResolver> queryParameterResolvers;
    
    /**
     * Default Constructor which registers an empty list of custom 
     * QueryParameterResolvers
     */
    public ServiceURLArgumentResolver() {
        this(new ArrayList<QueryParameterResolver>());
    }
    
    /**
     * Registers a list of custom queryParameterResolvers and then adds the 
     * default RequestParamResolver after these
     * @param queryParameterResolvers Resolvers to process, the order of these
     *  dictates the order in which they will be executed to find query params
     */
    public ServiceURLArgumentResolver(List<QueryParameterResolver> queryParameterResolvers) {       
        this.queryParameterResolvers = queryParameterResolvers;
        this.queryParameterResolvers.add(new RequestParamResolver());
    }
    
    @Override
    public boolean supportsParameter(MethodParameter mp) {
        return mp.hasParameterAnnotation(ServiceURL.class) && 
               mp.getParameterType().equals(String.class);
    }

    @Override
    public String resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavc, NativeWebRequest webRequest, WebDataBinderFactory wdbf) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        StringBuilder toReturn = new StringBuilder(request.getRequestURL()).append("?");
        Set<String> mapServiceApplicableQueryParams = getMapServiceApplicableQueryParams(methodParameter.getMethod());
        Map<String, String[]> parameters = request.getParameterMap();
        for(Map.Entry<String, String[]> paramEntry : parameters.entrySet()) {
            String paramKey = paramEntry.getKey();
            if(mapServiceApplicableQueryParams.contains(paramKey)) {
                for(String paramValue : paramEntry.getValue()) {
                    toReturn
                        .append(URLEncoder.encode(paramKey, URL_ENCODING))
                        .append("=")
                        .append(URLEncoder.encode(paramValue, URL_ENCODING))
                        .append("&");
                }
            }
        }
        return toReturn.toString();
    }
    
    private Set<String> getMapServiceApplicableQueryParams(Method method) {
        Set<String> queryParams = new HashSet<>();
        
        for(MethodParameter methodParameter: getMethodParameters(method)) {
            for(QueryParameterResolver resolver : queryParameterResolvers) {
                if(resolver.supportsParameter(methodParameter)) {
                    queryParams.addAll(resolver.getUtilisedQueryParameters(methodParameter));
                }
            }
        }
        return queryParams;
    }
    
    private List<MethodParameter> getMethodParameters(Method method) {
        List<MethodParameter> toReturn = new ArrayList<>();
        for(int i=0; i<method.getParameterTypes().length; i++) {
            toReturn.add(new MethodParameter(method, i));
        }
        return toReturn;
    }
}