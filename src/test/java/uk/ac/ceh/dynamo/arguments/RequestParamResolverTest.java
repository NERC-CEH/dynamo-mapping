package uk.ac.ceh.dynamo.arguments;

import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Christopher Johnson
 */
public class RequestParamResolverTest {
    private RequestParamResolver resolver;
    
    @Before
    public void createGridMapArgumentResolver() {
        resolver = new RequestParamResolver();
    }
    
    @Test
    public void checkSupportsRequestParamAnnotation() {
        //Given
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.hasParameterAnnotation(RequestParam.class)).thenReturn(true);
        
        //When
        boolean supportsParameter = resolver.supportsParameter(methodParam);
        
        //Then
        assertTrue("Expected the parameter to be supported", supportsParameter);
    }
    
    @Test
    public void checkNonRequestParamIsNotSupported() {
        //Given
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.getParameterType()).thenReturn((Class)Object.class);
        
        //When
        boolean supportsParameter = resolver.supportsParameter(methodParam);
        
        //Then
        assertFalse("Expected the parameter to be supported", supportsParameter);
    }
    
    @Test
    public void canResolveArgumentFromParameter() throws Exception {
        //Given
        RequestParam requestParam = mock(RequestParam.class);
        when(requestParam.value()).thenReturn("queryParam");
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.getParameterAnnotation(RequestParam.class)).thenReturn(requestParam);
        
        //When
        Set<String> queryParams = resolver.getUtilisedQueryParameters(methodParam);
        
        //Then
        assertSame("Expected only one parameter to be present", 1, queryParams.size());
        assertEquals("Expected the single value to be queryParam", "queryParam", queryParams.iterator().next());
    }
}
