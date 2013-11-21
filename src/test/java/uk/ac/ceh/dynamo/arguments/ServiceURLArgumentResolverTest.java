package uk.ac.ceh.dynamo.arguments;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.springframework.core.MethodParameter;
import static org.junit.Assert.*;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;
import uk.ac.ceh.dynamo.arguments.annotations.ServiceURL;

/**
 *
 * @author Christopher Johnson
 */
public class ServiceURLArgumentResolverTest {
    private ServiceURLArgumentResolver resolver;
    
    @Before
    public void createServiceURLArgumentResolver() {
        resolver = new ServiceURLArgumentResolver();
    }
    
    @Test
    public void checkSupportsServiceURLAnnotation() {
        //Given
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.hasParameterAnnotation(ServiceURL.class)).thenReturn(true);
        when(methodParam.getParameterType()).thenReturn((Class)String.class);
        
        //When
        boolean supportsParameter = resolver.supportsParameter(methodParam);
        
        //Then
        assertTrue("Expected the parameter to be supported", supportsParameter);
    }
    
    @Test
    public void checkNonServiceURLIsNotSupported() {
        //Given
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.getParameterType()).thenReturn((Class)Object.class);
        
        //When
        boolean supportsParameter = resolver.supportsParameter(methodParam);
        
        //Then
        assertFalse("Expected the parameter to be supported", supportsParameter);
    }
    
    @Test
    public void resolveArgumentWithRequestParameter() throws Exception {
        //Given
        Object handle = new Object() {
            public void method(@RequestParam("incoming") String incoming) {}
        };
        
        MethodParameter mock = mock(MethodParameter.class);
        when(mock.getMethod()).thenReturn(ReflectionUtils.findMethod(handle.getClass(), "method", (Class<?>[]) null));
        
        Map<String, String[]> requestParameters = new HashMap<>();
        requestParameters.put("incoming", new String[]{"requestValue1", "requestValue2"});
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://web.request.com"));
        when(request.getParameterMap()).thenReturn(requestParameters);
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(request);
        
        //When
        String url = resolver.resolveArgument(mock, null, webRequest, null);
        
        //Then
        assertEquals("Expected a url to be returned", "http://web.request.com?incoming=requestValue1&incoming=requestValue2&", url);
    }
}
