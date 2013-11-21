package uk.ac.ceh.dynamo;

import org.junit.Test;
import uk.ac.ceh.dynamo.GridMap.GridLayer;
import static org.junit.Assert.*;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapControllerTest {

    @Test
    public void getADefinedResolutionFromTheGridMap() throws NoSuchMethodException {
        //Given
        Object mapService = new Object() {
            @GridMap(
                layers={
                    @GridLayer(name="TenK", resolution=10000, layer="TenKLayer")
                },
                defaultLayer="TenK")
            public void method() {}
        };
        
        GridMap gridMap = mapService.getClass() 
                                    .getMethod("method")
                                    .getAnnotation(GridMap.class);
        
        //When
        GridMap.GridLayer resolution = GridMapController.getResolution(gridMap, "TenK");
        
        //Then
        assertEquals("Expected TenK resolution", "TenK", resolution.name());
        assertEquals("Expected 10000 resolution", 10000, resolution.resolution());
        assertEquals("Expected TenKLayer resolution", "TenKLayer", resolution.layer());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void tryAndGetDefaultLayerWhichIsNotDefined() throws NoSuchMethodException {
        //Given
        Object mapService = new Object() {
            @GridMap(
                layers={},
                defaultLayer="TenK")
            public void method() {}
        };
        
        GridMap gridMap = mapService.getClass() 
                                    .getMethod("method")
                                    .getAnnotation(GridMap.class);
        
        //When
        GridMap.GridLayer resolution = GridMapController.getResolution(gridMap, null);
        
        //Then
        fail("Expected an illegalArgumentException");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void tryAndGetLayerWhichIsNotDefined() throws NoSuchMethodException {
        //Given
        Object mapService = new Object() {
            @GridMap(
                layers={
                    @GridLayer(name="TenK", resolution=10000, layer="TenKLayer"),
                    @GridLayer(name="TwoK", resolution=2000, layer="TwoKLayer")
                },
                defaultLayer="TenK")
            public void method() {}
        };
        
        GridMap gridMap = mapService.getClass() 
                                    .getMethod("method")
                                    .getAnnotation(GridMap.class);
        
        //When
        GridMap.GridLayer resolution = GridMapController.getResolution(gridMap, "EightK");
        
        //Then
        fail("Expected an illegalArgumentException");
    }
}
