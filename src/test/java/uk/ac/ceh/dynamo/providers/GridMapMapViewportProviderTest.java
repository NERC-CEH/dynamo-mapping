package uk.ac.ceh.dynamo.providers;

import java.math.BigDecimal;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import uk.ac.ceh.dynamo.BoundingBox;
import uk.ac.ceh.dynamo.GridMap;
import uk.ac.ceh.dynamo.GridMapRequestFactory;
import uk.ac.ceh.dynamo.GridMapRequestFactory.GridMapRequest;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapMapViewportProviderTest {
    private GridMapMapViewportProvider provider;
    
    @Before
    public void createGridMapMapViewportProvider() {
        provider = new GridMapMapViewportProvider();
    }
    
    @Test
    public void getViewportForAFullRequest() {
        //Given
        String featureID = "featureId", nationalExtent = "nationalExtent";
        
        GridMapRequestFactory factory = mock(GridMapRequestFactory.class);
        BoundingBox bbox = new BoundingBox("EPSG:7357", BigDecimal.valueOf(1),
                                                BigDecimal.valueOf(2),
                                                BigDecimal.valueOf(3),
                                                BigDecimal.valueOf(4));
        
        GridMapRequest request = mock(GridMapRequest.class);
        when(request.getHeight()).thenReturn(200);
        when(request.getWidth()).thenReturn(100);
        when(request.getBBox()).thenReturn("GeneratedBBox");
        when(request.isValidRequest()).thenReturn(true);
        
        GridMap gridMap = mock(GridMap.class);
        when(factory.getFeatureToFocusOn(featureID, nationalExtent, gridMap)).thenReturn(bbox);
        when(factory.getGridMapRequest(bbox, 10000, 10)).thenReturn(request);
        
        GridMap.GridLayer gridLayer = mock(GridMap.GridLayer.class);
        when(gridLayer.resolution()).thenReturn(10000);
        
        //When
        Map<String,String[]> query = provider.processRequestParameters(factory, gridMap, gridLayer, "10", featureID, nationalExtent);
        
        //Then
        assertArrayEquals("Expected the srs base layer", new String[]{"EPSG:7357"}, query.get("SRS"));
        assertArrayEquals("Expected the HEIGHT to be 200", new String[]{"200"}, query.get("HEIGHT"));
        assertArrayEquals("Expected the WIDTH to be 100", new String[]{"100"}, query.get("WIDTH"));
        assertArrayEquals("Expected the BBOX to be GeneratedBBox", new String[]{"GeneratedBBox"}, query.get("BBOX"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void makeAnInvalidRequest() {
        //Given
        GridMapRequestFactory factory = mock(GridMapRequestFactory.class);
        BoundingBox bbox = new BoundingBox("EPSG:7357", BigDecimal.valueOf(1),
                                                BigDecimal.valueOf(2),
                                                BigDecimal.valueOf(3),
                                                BigDecimal.valueOf(4));
        
        GridMapRequest request = mock(GridMapRequest.class);
        when(request.isValidRequest()).thenReturn(false);
        
        GridMap gridMap = mock(GridMap.class);
        when(factory.getFeatureToFocusOn(null, null, gridMap)).thenReturn(bbox);
        when(factory.getGridMapRequest(bbox, 10000, 10)).thenReturn(request);
        
        GridMap.GridLayer gridLayer = mock(GridMap.GridLayer.class);
        when(gridLayer.resolution()).thenReturn(10000);
        
        //When
        Map<String,String[]> query = provider.processRequestParameters(factory, gridMap, gridLayer, "10", null, null);
        
        //Then
        fail("Expeceted to fail with an illegal argument exception");
    }
}
