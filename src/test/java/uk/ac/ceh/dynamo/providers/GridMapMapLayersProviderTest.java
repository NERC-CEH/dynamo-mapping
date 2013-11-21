package uk.ac.ceh.dynamo.providers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import uk.ac.ceh.dynamo.GridMap;
import uk.ac.ceh.dynamo.GridMap.GridLayer;
import uk.ac.ceh.dynamo.GridMap.Layer;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapMapLayersProviderTest {
    private GridMapMapLayersProvider provider;
    
    @Before
    public void createGridMapMapLayersProvider() {
        provider = new GridMapMapLayersProvider();
    }
    
    @Test
    public void provideForLayersWhenBackgroundAndOverlayAreSupplied() throws NoSuchMethodException {
        //Given
        GridMap gridMap = mock(GridMap.class);
        GridLayer gridLayer = mock(GridLayer.class);
        when(gridLayer.layer()).thenReturn("10kLayer");
        when(gridMap.defaultBackgrounds()).thenReturn(new String[0]);
        when(gridMap.layers()).thenReturn(new GridLayer[]{gridLayer});
        
        Layer background = mock(Layer.class);
        when(background.name()).thenReturn("gbi");
        when(background.layers()).thenReturn(new String[]{"gbiLayer"});
        when(gridMap.backgrounds()).thenReturn(new Layer[]{background});
        
        Layer overlayers = mock(Layer.class);
        when(overlayers.name()).thenReturn("overlay");
        when(overlayers.layers()).thenReturn(new String[]{"overlayLayer"});
        when(gridMap.overlays()).thenReturn(new Layer[]{overlayers});
        
        List<String> backgrounds = Arrays.asList("gbi");
        List<String> overlays = Arrays.asList("overlay");
                
        //When
        Map<String,String[]> query = provider.processRequestParameters(gridMap, backgrounds, overlays, gridLayer);
        
        //Then
        assertArrayEquals("Expected a LAYERS", new String[]{"gbiLayer,10kLayer,overlayLayer"}, query.get("LAYERS"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void requestBackgroundLayerWhichIsntDefined() throws NoSuchMethodException {
        //Given
        GridMap gridMap = mock(GridMap.class);
        GridLayer gridLayer = mock(GridLayer.class);
        when(gridLayer.layer()).thenReturn("10kLayer");
        when(gridMap.layers()).thenReturn(new GridLayer[]{gridLayer});
        when(gridMap.backgrounds()).thenReturn(new Layer[]{});
        when(gridMap.defaultBackgrounds()).thenReturn(new String[0]);
        when(gridMap.overlays()).thenReturn(new Layer[]{});
        
        List<String> backgrounds = Arrays.asList("gbi");
                
        //When
        Map<String,String[]> query = provider.processRequestParameters(gridMap, backgrounds, null, gridLayer);
        
        //Then
        fail("expected an illegalargument exception");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void requestOverlayLayerWhichIsntDefined() throws NoSuchMethodException {
        //Given
        GridMap gridMap = mock(GridMap.class);
        GridLayer gridLayer = mock(GridLayer.class);
        when(gridLayer.layer()).thenReturn("10kLayer");
        when(gridMap.layers()).thenReturn(new GridLayer[]{gridLayer});
        when(gridMap.backgrounds()).thenReturn(new Layer[]{});
        when(gridMap.defaultBackgrounds()).thenReturn(new String[0]);
        when(gridMap.overlays()).thenReturn(new Layer[]{});
                
        List<String> overlays = Arrays.asList("overlay");
                
        //When
        Map<String,String[]> query = provider.processRequestParameters(gridMap, null, overlays, gridLayer);
        
        //Then
        fail("expected an illegalargument exception");
    }
}
