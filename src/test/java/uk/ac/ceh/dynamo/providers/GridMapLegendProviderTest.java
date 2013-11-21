package uk.ac.ceh.dynamo.providers;

import java.util.Map;
import org.junit.Test;
import uk.ac.ceh.dynamo.GridMap;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.Before;
/**
 *
 * @author Christopher Johnson
 */
public class GridMapLegendProviderTest {
    private GridMapLegendProvider provider;
    
    @Before
    public void createGridMapLegendProvider() {
        provider = new GridMapLegendProvider();
    }

    @Test
    public void checkThatWeCanGenerateGridMapLegendRequests() {
        //Given
        GridMap.GridLayer gridLayer = mock(GridMap.GridLayer.class);
        when(gridLayer.layer()).thenReturn("testLayer");
        
        //When
        Map<String, String[]> query = provider.processRequestParameters(gridLayer);
        
        //Then
        assertArrayEquals("Expected a SERVICE", new String[]{"WMS"}, query.get("SERVICE"));
        assertArrayEquals("Expected a VERSION", new String[]{"1.1.1"}, query.get("VERSION"));
        assertArrayEquals("Expected a REQUEST", new String[]{"GetLegendGraphic"}, query.get("REQUEST"));
        assertArrayEquals("Expected a TRANSPARENT", new String[]{"true"}, query.get("TRANSPARENT"));
        assertArrayEquals("Expected a FORMAT", new String[]{"image/png"}, query.get("FORMAT"));
        assertArrayEquals("Expected a LAYER", new String[]{"testLayer"}, query.get("LAYER"));
    }

}
