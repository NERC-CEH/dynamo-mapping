package uk.ac.ceh.dynamo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ceh.components.dynamo.BoundingBox;
import uk.ac.ceh.components.dynamo.GridMapReader;
import uk.ac.ceh.components.dynamo.GridMapRequestFactory;
import static org.junit.Assert.*;
import static uk.ac.ceh.components.dynamo.GridSquareFactory.*;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapReaderTest {
    private GridMapRequestFactory gridMapRequestFactory;
    
    @Before
    public void initialize() {
        gridMapRequestFactory = new GridMapRequestFactory(null);
    }
    
    @Test
    public void checkTestImageHasXHighlighted() throws IOException {
        //Given
        BufferedImage gridMap = ImageIO.read(GridMapReaderTest.class.getResource("10km @ ImageSize2.png"));
        BoundingBox bbox = new BoundingBox("EPSG:27700", 
                                            BigDecimal.valueOf(-250000), 
                                            BigDecimal.valueOf(-50000),
                                            BigDecimal.valueOf(750000),
                                            BigDecimal.valueOf(1310000));
        GridMapRequestFactory.GridMapRequest gridMapRequest = gridMapRequestFactory.getGridMapRequest(bbox, 10000, 2);
                
        //When
        GridMapReader reader = new GridMapReader(gridMap, gridMapRequest);
        List<GridSquare> gridSquaresByColour = reader.getGridSquaresByColour(Color.YELLOW);
        
        //Then
        assertSame("Expected tl45 to be selected", reader.getColourAt(bng("TL000900")), 0xFF000000);
    }
}
