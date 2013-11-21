package uk.ac.ceh.dynamo.arguments;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.springframework.core.MethodParameter;
import uk.ac.ceh.dynamo.GridMap;
import static org.junit.Assert.*;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapArgumentResolverTest {
    private GridMapArgumentResolver resolver;
    
    @Before
    public void createGridMapArgumentResolver() {
        resolver = new GridMapArgumentResolver();
    }
    
    @Test
    public void checkSupportsGridMapAnnotation() {
        //Given
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.getParameterType()).thenReturn((Class)GridMap.class);
        
        //When
        boolean supportsParameter = resolver.supportsParameter(methodParam);
        
        //Then
        assertTrue("Expected the parameter to be supported", supportsParameter);
    }
    
    @Test
    public void checkNonGridMapIsNotSupported() {
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
        GridMap gridMap = mock(GridMap.class);
        MethodParameter methodParam = mock(MethodParameter.class);
        when(methodParam.getMethodAnnotation(GridMap.class)).thenReturn(gridMap);
        
        //When
        GridMap resolveArgument = resolver.resolveArgument(methodParam, null, null, null);
        
        //Then
        assertEquals("Expected the get gridmap back", gridMap, resolveArgument);
    }
}
