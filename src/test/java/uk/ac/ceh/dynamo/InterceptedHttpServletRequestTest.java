package uk.ac.ceh.dynamo;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Christopher Johnson
 */
public class InterceptedHttpServletRequestTest {

    @Test
    public void createInterceptedRequestWithCopyOfHttpRequestParameterValues() {
        //Given
        HttpServletRequest mock = mock(HttpServletRequest.class);
        
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("key", new String[]{"value"});
        
        when(mock.getParameterMap()).thenReturn(parameters);
                
        InterceptedHttpServletRequest interceptedRequest = new InterceptedHttpServletRequest(mock);
        
        //When
        Map<String, String[]> interceptedMap = interceptedRequest.getParameterMap();
        
        //Then
        assertEquals("Expected a copy of the parameter map", parameters, interceptedMap);
        assertEquals("Expected single value for key [key]", "value", interceptedRequest.getParameter("key"));
        assertArrayEquals("Expected single value for key [key]", new String[]{"value"}, interceptedRequest.getParameterValues("key"));
    }
    
    @Test
    public void replaceAParameter() {
        //Given
        HttpServletRequest mock = mock(HttpServletRequest.class);
        
        Map<String, String[]> originalParams = new HashMap<>();
        originalParams.put("key", new String[]{"value"});
        
        when(mock.getParameterMap()).thenReturn(originalParams);
                
        InterceptedHttpServletRequest interceptedRequest = new InterceptedHttpServletRequest(mock);
        
        //When
        interceptedRequest.setParameterValues("key", new String[]{"replacedValue"});
        
        //Then
        assertEquals("Expected single value for key to have been replaced", "replacedValue", interceptedRequest.getParameter("key"));
    }
    
    @Test
    public void addANewParameter() {
        //Given
        HttpServletRequest mock = mock(HttpServletRequest.class);
        
        Map<String, String[]> originalParams = new HashMap<>();
        originalParams.put("key", new String[]{"value"});
        
        when(mock.getParameterMap()).thenReturn(originalParams);
                
        InterceptedHttpServletRequest interceptedRequest = new InterceptedHttpServletRequest(mock);
        
        //When
        interceptedRequest.setParameterValues("newKey", new String[]{"newValue"});
        
        //Then
        assertEquals("Expected original value to be intact", "value", interceptedRequest.getParameter("key"));
        assertEquals("Expected new value to have been added", "newValue", interceptedRequest.getParameter("newKey"));
    }
    
    @Test
    public void removeAnExistingParameter() {
        //Given
        HttpServletRequest mock = mock(HttpServletRequest.class);
        
        Map<String, String[]> originalParams = new HashMap<>();
        originalParams.put("key", new String[]{"value"});
        
        when(mock.getParameterMap()).thenReturn(originalParams);
                
        InterceptedHttpServletRequest interceptedRequest = new InterceptedHttpServletRequest(mock);
        
        //When
        interceptedRequest.removeParameter("key");
        
        //Then
        assertTrue("Expected no parameters to be left", interceptedRequest.getParameterMap().isEmpty());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void checkThatTheParameterMapIsImutable() {
        //Given
        HttpServletRequest mock = mock(HttpServletRequest.class);
        
        Map<String, String[]> originalParams = new HashMap<>();
        originalParams.put("key", new String[]{"value"});
        
        when(mock.getParameterMap()).thenReturn(originalParams);
        
        InterceptedHttpServletRequest interceptedRequest = new InterceptedHttpServletRequest(mock);
        //When
        interceptedRequest.getParameterMap().put("messing", new String[]{"around","here"});
        
        //Then
        fail("Didn't Expect to be able to update the intercepted parameter map");
    }
    
}
