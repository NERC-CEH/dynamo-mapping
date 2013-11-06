package uk.ac.ceh.components.dynamo.arguments;

import java.util.Set;
import org.springframework.core.MethodParameter;

/**
 *
 * @author Christopher Johnson
 */
public interface QueryParameterResolver {
    boolean supports(MethodParameter methodParameter);
    
    Set<String> getUtilisedQueryParameters(MethodParameter methodParameter);
}
