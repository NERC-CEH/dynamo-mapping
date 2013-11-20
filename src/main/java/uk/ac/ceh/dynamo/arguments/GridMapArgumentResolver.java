package uk.ac.ceh.dynamo.arguments;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.ac.ceh.dynamo.GridMap;

/**
 * A spring mvc method argument resolver which injects the GridMap annotation
 * into a spring mvc request mapping handler.
 * @author Chris Johnson
 */
public class GridMapArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(GridMap.class);
    }

    @Override
    public GridMap resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return parameter.getMethodAnnotation(GridMap.class);
    }
    
}
