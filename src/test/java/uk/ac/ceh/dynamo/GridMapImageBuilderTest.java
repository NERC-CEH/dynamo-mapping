package uk.ac.ceh.dynamo;

import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.*;
import uk.ac.ceh.dynamo.GridMap.Extent;
import uk.ac.ceh.dynamo.GridMap.GridLayer;
import static org.junit.Assert.*;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapImageBuilderTest {
    private GridMapImageBuilder builder;
    private WebResource resource;
    
    @Before
    public void createImageBuilder() {
        resource = mock(WebResource.class);
        GridLayer tenkm = mock(GridLayer.class);
        when(tenkm.name()).thenReturn("10km");
        when(tenkm.resolution()).thenReturn(10000);
        resource = mock(WebResource.class);
        
        GridLayer twokm = mock(GridLayer.class);
        when(twokm.name()).thenReturn("2km");
        when(twokm.resolution()).thenReturn(2000);
        
        when(resource.queryParam(anyString(), anyString())).thenReturn(resource);
        when(resource.get(InputStream.class)).thenReturn(getClass().getResourceAsStream("10km @ ImageSize2.png"));
        
        GridMap gridMap = mock(GridMap.class);
        when(gridMap.layers()).thenReturn(new GridLayer[]{tenkm, twokm});
        when(gridMap.defaultExtent()).thenReturn("The whole world");
                
        Extent extent = mock(Extent.class);
        when(extent.name()).thenReturn("The whole world");
        when(extent.extent()).thenReturn(new int[]{1,2,3,4});
        when(extent.epsgCode()).thenReturn("EPSG:7357");
        when(gridMap.extents()).thenReturn(new Extent[]{extent});
        
        builder = new GridMapImageBuilder(resource, gridMap).resolution("10km");
    }

    @Test
    public void checkToSeeThatFeatureIsSetBeforeCalling() throws IOException {
        //Given
        String feature = "setFeature";
        
        //When
        builder.feature(feature).build();
        
        //Then
        verify(resource).queryParam("feature", feature);
    }
    
    @Test
    public void checkToSeeThatFeatureIsntSetOnResourceBeforeCalling() throws IOException {
        //Given
        //Nothing
        
        //When
        builder.build();
        
        //Then
        verify(resource, atMost(1)).queryParam(eq("feature"), anyString());
    }
    
    @Test
    public void checkToSeeThatNationalExtentIsSetBeforeCalling() throws IOException {
        //Given
        String extent = "The whole world";
        
        //When
        builder.nationalExtent(extent).build();
        
        //Then
        verify(resource).queryParam("nationalextent", extent);
    }
    
    @Test
    public void checkToSeeThatNationalExtentIsntSetOnResourceBeforeCalling() throws IOException {
        //Given
        //Nothing
        
        //When
        builder.build();
        
        //Then
        verify(resource, atMost(1)).queryParam(eq("nationalextent"), anyString());
    }
    
    @Test
    public void expectRequiredParamsToBeSetWhenBuilding() throws IOException {
        //Given
        //nothing
        
        //When
        builder.build();
        
        //Then
        verify(resource, atMost(1)).queryParam("resolution", "10km");
        verify(resource, atMost(1)).queryParam("imagesize", "10"); //default image size
    }
    
    @Test
    public void expectResolutionToBeModifyable() throws IOException {
        //Given
        String customResolution = "2km";
        
        //When
        builder.resolution(customResolution).build();
        
        //Then
        verify(resource, atMost(1)).queryParam("resolution", customResolution);
    }
    
    @Test
    public void expectImageSizeToBeModifyable() throws IOException {
        //Given
        int imagesize = 2;
        
        //When
        builder.imageSize(imagesize).build();
        
        //Then
        verify(resource, atMost(1)).queryParam("imagesize", "2");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void expectOnlyDefinedResolutionsToBeSelectable() throws IOException {
        //Given
        String customResolution = "undefined resolution";
        
        //When
        builder.resolution(customResolution).build();
        
        //Then
        fail("Expected to be unable to use custom resolution");
    }
}
