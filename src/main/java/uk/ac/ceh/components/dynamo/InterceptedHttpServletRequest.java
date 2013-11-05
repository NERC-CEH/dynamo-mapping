package uk.ac.ceh.components.dynamo;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 * @author Christopher Johnson
 */
public class InterceptedHttpServletRequest extends HttpServletRequestWrapper {
    private final Map<String, String[]> parameters;
    
    public InterceptedHttpServletRequest(HttpServletRequest request) {
        super(request);
        parameters = new HashMap<>(request.getParameterMap());
    }
    
    public InterceptedHttpServletRequest(HttpServletRequest request, Map<String, String[]> toPut) {
        this(request);
        parameters.putAll(toPut);
    }
    
    @Override public String getParameter(final String name) {
        String[] strings = getParameterMap().get(name);
        if (strings != null) {
            return strings[0];
        }
        return super.getParameter(name);
    }

    @Override public Map<String, String[]> getParameterMap() {
        //Return an unmodifiable collection because we need to uphold the interface contract.
        return Collections.unmodifiableMap(parameters);
    }

    @Override public Enumeration<String> getParameterNames() {
        return Collections.enumeration(getParameterMap().keySet());
    }

    @Override public String[] getParameterValues(final String name) {
        return getParameterMap().get(name);
    }
    
    /**
     * Set the specific parameter to the list of values
     * @param key The parameter name to set
     * @param value the values to use for this parameter
     * @return The old version of the value if it exists
     */
    public String[] setParameterValues(String key, String[] value) {
        return parameters.put(key, value);
    }
    
    /**
     * Puts all the parameters in the passed on to this HttpServletRequest
     * @param toPut The map to use to fill this request
     */
    public void putAllParameters(Map<String, String[]> toPut) {
        parameters.putAll(toPut);
    }
    
    /**
     * Removes a parameter from this HttpRequest and returns it
     * @param key The parameter to remove
     * @return The value which was removed
     */
    public String[] removeParameter(String key) {
        return parameters.remove(key);
    }
}