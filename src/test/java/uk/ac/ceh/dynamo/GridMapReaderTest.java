package uk.ac.ceh.dynamo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapReaderTest {
    private GridMapImage reader;
    
    @Before
    public void initialize() throws IOException {
        GridMapRequestFactory gridMapRequestFactory = new GridMapRequestFactory(null);
        BufferedImage gridMap = ImageIO.read(GridMapReaderTest.class.getResource("10km @ ImageSize2.png"));
        BoundingBox bbox = new BoundingBox("EPSG:27700", 
                                            BigDecimal.valueOf(-250000), 
                                            BigDecimal.valueOf(-50000),
                                            BigDecimal.valueOf(750000),
                                            BigDecimal.valueOf(1310000));
        GridMapRequestFactory.GridMapRequest gridMapRequest = gridMapRequestFactory.getGridMapRequest(bbox, 10000, 2);
                
        reader = new GridMapImage(gridMap, gridMapRequest);
    }
    
    @Test
    public void checkTestImageHasTL09Highlighted() {
        //Given
        GridSquare tl09 = new GridSquare(500000, 290000, 10000);
        
        //When
        Color colourOfSquare = reader.getColourAt(tl09);
        
        //Then
        assertEquals("Expected tl45 to be selected", colourOfSquare, Color.YELLOW);
    }
    
    @Test
    public void checkTestImageHasSO23Highlighted() {
        //Given
        GridSquare so23 = new GridSquare(320000, 230000, 10000);
        
        //When
        Color colourOfSquare = reader.getColourAt(so23);
        
        //Then
        assertFalse("Didn't expect the colour of so23 to be yellow", colourOfSquare.equals(Color.YELLOW));
    }
    
    @Test
    public void checkThatThereA2kmSquareIsntPresent() {
        //Given
        GridSquare twoKmSquare = new GridSquare(320000, 230000, 2000);
        
        //When
        Color colourOfSquare = reader.getColourAt(twoKmSquare);
        
        //Then
        assertNull("Didn't expect to find a square with 2km resolution", colourOfSquare);
    }
    
    @Test
    public void checkTestImageHasOneSquareColouredYellow() {
        //Given
        Color colour = Color.YELLOW;
        
        //When
        List<GridSquare> yellowSquares = reader.getGridSquaresByColour(colour);
        
        //Then
        assertEquals("Expected one square to be coloured", yellowSquares.size(), 1);
    }
    
    @Test
    public void checkTestImageDoesntHaveAnyBlueSquares() {
        //Given
        Color colour = Color.BLUE;
        
        //When
        List<GridSquare> blueSquares = reader.getGridSquaresByColour(colour);
        
        //Then
        assertEquals("Expected there to be no blue squares", blueSquares.size(), 0);
    }
}
