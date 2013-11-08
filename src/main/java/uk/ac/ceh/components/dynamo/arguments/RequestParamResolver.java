package uk.ac.ceh.components.dynamo.arguments;

import java.util.Collections;
import java.util.Set;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Christopher Johnson
 */
public class RequestParamResolver implements QueryParameterResolver {

    @Override
    public boolean supports(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(RequestParam.class);
    }

    @Override
    public Set<String> getUtilisedQueryParameters(MethodParameter methodParameter) {
        return Collections.singleton(methodParameter
                .getParameterAnnotation(RequestParam.class).value());
    }

}