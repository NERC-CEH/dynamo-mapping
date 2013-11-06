package uk.ac.ceh.components.dynamo.arguments;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.ac.ceh.components.dynamo.DynamoMap;

/**
 *
 * @author Chris Johnson
 */
public class DynamoMapArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(DynamoMap.class);
    }

    @Override
    public DynamoMap resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return parameter.getMethodAnnotation(DynamoMap.class);
    }
    
}
