package uk.ac.ceh.dynamo.providers;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapMapProviderTest {
    private GridMapMapProvider provider;
    
    @Before
    public void createGridMapMapProvider() {
        provider = new GridMapMapProvider();
    }

    @Test
    public void checkThatWeCanGenerateGridMapRequestsWithDefaultFormat() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(new HashMap<String,String[]>());
        
        //When
        Map<String, String[]> query = provider.processRequestParameters(request);
        
        //Then
        assertArrayEquals("Expected a SERVICE", new String[]{"WMS"}, query.get("SERVICE"));
        assertArrayEquals("Expected a VERSION", new String[]{"1.1.1"}, query.get("VERSION"));
        assertArrayEquals("Expected a REQUEST", new String[]{"GetMap"}, query.get("REQUEST"));
        assertArrayEquals("Expected a STYLES", new String[]{""}, query.get("STYLES"));
        assertArrayEquals("Expected a TRANSPARENT", new String[]{"true"}, query.get("TRANSPARENT"));
        assertArrayEquals("Expected a FORMAT", new String[]{"image/png"}, query.get("FORMAT"));
    }
    
    @Test
    public void checkThatWeCanGenerateGridMapRequestsWithParticularFormat() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        
        Map<String, String[]> map = new HashMap<>();
        map.put("format", new String[]{"gif"});
        when(request.getParameterMap()).thenReturn(map);
        
        //When
        Map<String, String[]> query = provider.processRequestParameters(request);
        
        //Then
        assertArrayEquals("Expected a SERVICE", new String[]{"WMS"}, query.get("SERVICE"));
        assertArrayEquals("Expected a VERSION", new String[]{"1.1.1"}, query.get("VERSION"));
        assertArrayEquals("Expected a REQUEST", new String[]{"GetMap"}, query.get("REQUEST"));
        assertArrayEquals("Expected a STYLES", new String[]{""}, query.get("STYLES"));
        assertArrayEquals("Expected a TRANSPARENT", new String[]{"true"}, query.get("TRANSPARENT"));
        assertArrayEquals("Expected a FORMAT", new String[]{"image/gif"}, query.get("FORMAT"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void checkThatWeCantGenerateGridMapRequestsWithUnsupportedFormat() {
        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String[]> map = new HashMap<>();
        map.put("format", new String[]{"psd"});
        when(request.getParameterMap()).thenReturn(map);
        
        //When
        Map<String, String[]> query = provider.processRequestParameters(request);
        
        //Then
        fail("didn't expect to be able to generate request");
    }
}
