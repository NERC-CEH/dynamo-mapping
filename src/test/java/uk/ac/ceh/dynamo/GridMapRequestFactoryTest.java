package uk.ac.ceh.dynamo;

import java.math.BigDecimal;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import uk.ac.ceh.dynamo.GridMapRequestFactory.GridMapRequest;
/**
 *
 * @author Christopher Johnson
 */
public class GridMapRequestFactoryTest {

    @Test
    public void attemptToFocusOnAFeatureWithOutAFeatureResolver() throws NoSuchMethodException {
        //Given
        GridMap gridMap = getGridMapAnnotation(new Object() {
            @GridMap(
                layers={
                    @GridMap.GridLayer(name="TenK", resolution=10000, layer="TenKLayer")
                },
                extents= {
                    @GridMap.Extent(name="default",     epsgCode="EPSG:27700", extent={-250000, -50000, 750000, 1310000})
                },
                defaultExtent="default", 
                defaultLayer="TenK")
            public void method() {}
        });        
        
        GridMapRequestFactory factory = new GridMapRequestFactory();
        
        //When
        BoundingBox featureToFocusOn = factory.getFeatureToFocusOn("myfeatureID", null, gridMap);
        
        //Then
        BoundingBox defaultExtent = new BoundingBox("EPSG:27700", 
                                                        BigDecimal.valueOf(-250000), 
                                                        BigDecimal.valueOf(-50000), 
                                                        BigDecimal.valueOf(750000), 
                                                        BigDecimal.valueOf(1310000));
        assertEquals("Expected to be zoomed in on the default extent", defaultExtent, featureToFocusOn);
    }
    
    @Test
    public void requestAKnownExtent() throws NoSuchMethodException {
        //Given        
        GridMap gridMap = getGridMapAnnotation(new Object() {
            @GridMap(
                layers={
                    @GridMap.GridLayer(name="TenK", resolution=10000, layer="TenKLayer")
                },
                extents= {
                    @GridMap.Extent(name="default",     epsgCode="EPSG:27700", extent={-250000, -50000, 750000, 1310000})
                },
                defaultExtent="somethingElse", 
                defaultLayer="TenK")
            public void method() {}
        });
        
        GridMapRequestFactory factory = new GridMapRequestFactory();
        
        //When
        BoundingBox featureToFocusOn = factory.getFeatureToFocusOn(null, "default", gridMap);
        
        //Then
        
        BoundingBox defaultExtent = new BoundingBox("EPSG:27700", 
                                                        BigDecimal.valueOf(-250000), 
                                                        BigDecimal.valueOf(-50000), 
                                                        BigDecimal.valueOf(750000), 
                                                        BigDecimal.valueOf(1310000));
        assertEquals("Expected to be zoomed in on the default extent", defaultExtent, featureToFocusOn);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void requestAnUnknownExtent() throws NoSuchMethodException {
        //Given
        GridMap gridMap = getGridMapAnnotation(new Object() {
            @GridMap(
                layers={
                    @GridMap.GridLayer(name="TenK", resolution=10000, layer="TenKLayer")
                },
                extents= {
                    @GridMap.Extent(name="default",     epsgCode="EPSG:27700", extent={-250000, -50000, 750000, 1310000})
                },
                defaultExtent="default", 
                defaultLayer="TenK")
            public void method() {}
        });
        
        GridMapRequestFactory factory = new GridMapRequestFactory();
        
        //When
        BoundingBox featureToFocusOn = factory.getFeatureToFocusOn(null, "random extent", gridMap);
        
        //Then
        fail("Expected to not be able to zoom to an unknown extent");
    }
    
    public void resolveABoundingBoxWithAFeature() throws NoSuchMethodException {
        //Given
        BoundingBox feature = new BoundingBox("EPSG:27700", 
                                                        BigDecimal.valueOf(-2500), 
                                                        BigDecimal.valueOf(-500), 
                                                        BigDecimal.valueOf(7500), 
                                                        BigDecimal.valueOf(13100));
        
        FeatureResolver resolver = mock(FeatureResolver.class);
        when(resolver.getFeature("requestfeature")).thenReturn(feature);
        
        GridMapRequestFactory factory = new GridMapRequestFactory(resolver);
        
        GridMap gridMap = getGridMapAnnotation(new Object() {
            @GridMap(
                layers={
                    @GridMap.GridLayer(name="TenK", resolution=10000, layer="TenKLayer")
                },
                extents= {
                    @GridMap.Extent(name="default",     epsgCode="EPSG:27700", extent={-250000, -50000, 750000, 1310000})
                },
                defaultExtent="default", 
                defaultLayer="TenK")
            public void method() {}
        });
        
        //When
        BoundingBox featureToFocusOn = factory.getFeatureToFocusOn("requestfeature", null, gridMap);
        
        //Then
        verify(resolver).getFeature("requestfeature");
        assertSame("Expected the same extent to be sent from the feature resolver", feature, featureToFocusOn);
    }
    
    @Test
    public void makeAValidGridMapRequest() {
        //Given
        BoundingBox feature = new BoundingBox("EPSG:27700", 
                                                        BigDecimal.valueOf(-2500), 
                                                        BigDecimal.valueOf(-500), 
                                                        BigDecimal.valueOf(7500), 
                                                        BigDecimal.valueOf(13100));
        GridMapRequestFactory factory = new GridMapRequestFactory();
        
        //When
        GridMapRequest gridMapRequest = factory.getGridMapRequest(feature, 10000, 10);
        
        //Then
        assertEquals("Expected a the bbox to be gridded to 10000 grid", "-10000,-10000,10000,20000", gridMapRequest.getBBox());
        assertTrue("Expected the request to be valid", gridMapRequest.isValidRequest());
    }
    
    @Test
    public void attemptToMakeAnInvalidGridMapRequest() {
        //Given
        BoundingBox bigExtent = new BoundingBox("EPSG:27700", 
                                                        BigDecimal.valueOf(-250000), 
                                                        BigDecimal.valueOf(-50000), 
                                                        BigDecimal.valueOf(750000), 
                                                        BigDecimal.valueOf(1310000));
        
        GridMapRequestFactory factory = new GridMapRequestFactory();
        
        //When
        GridMapRequest gridMapRequest = factory.getGridMapRequest(bigExtent, 100, 10);
        
        //Then
        assertFalse("Expected the request to be invalid, can't be enough pixels", gridMapRequest.isValidRequest());        
    }
    
    @Test
    public void checkHeightAndWidthDimensionsOfGridMapRequest() {
        //Given
        //Given
        BoundingBox feature = new BoundingBox("EPSG:27700", 
                                                        BigDecimal.valueOf(-2500), 
                                                        BigDecimal.valueOf(-500), 
                                                        BigDecimal.valueOf(7500), 
                                                        BigDecimal.valueOf(13100));
        GridMapRequestFactory factory = new GridMapRequestFactory();
        
        //When
        GridMapRequest gridMapRequest = factory.getGridMapRequest(feature, 10000, 10);
        
        //Then
        assertEquals("Expected height to be 1365 pixels", gridMapRequest.getHeight(), 1365);
        assertEquals("Expected width to be 910 pixels", gridMapRequest.getWidth(), 910);
    }
    
    private GridMap getGridMapAnnotation(Object object) throws NoSuchMethodException {
        return object.getClass() 
                        .getMethod("method")
                        .getAnnotation(GridMap.class);
    }
}
