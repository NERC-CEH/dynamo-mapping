package uk.ac.ceh.dynamo.arguments;

import java.util.Set;
import org.springframework.core.MethodParameter;

/**
 * Interface which will state which query parameters of a request are used in 
 * generating the given MethodParameter. 
 * 
 * The ServiceURLArgumentResolver is an example of where this is used
 * @see ServiceURLArgumentResolver
 * @author Christopher Johnson
 */
public interface QueryParameterResolver {
    /**
     * Determines if this instance can resolve the query parameters used for the
     * given MethodParameter
     * @param methodParameter The method parameter to be resolved
     * @return true if this MethodParameter can be resolved, else false
     */
    boolean supportsParameter(MethodParameter methodParameter);
    
    /**
     * Obtain a set of query parameters which may have been used to generate an
     * instance of the methodparameter.
     * 
     * The #supports(MethodParameter) method will already have been called and 
     * returned a value of true before this method is called
     * @param methodParameter The method parameter to obtain the query parameters
     *  of
     * @return A set of query parameters which may be used to generate an instance
     *  of the method parameter
     */
    Set<String> getUtilisedQueryParameters(MethodParameter methodParameter);
}
