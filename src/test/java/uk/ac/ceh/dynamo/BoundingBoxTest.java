package uk.ac.ceh.dynamo;

import java.math.BigDecimal;
import org.junit.Test;
import static org.junit.Assert.*;
/**
 *
 * @author Christopher Johnson
 */
public class BoundingBoxTest {

    @Test
    public void bufferABoundingBoxByPositiveFactor() {
        //Given
        BoundingBox bbox = new BoundingBox("EPSG:27700",
                                            BigDecimal.valueOf(-10),
                                            BigDecimal.valueOf(-20),
                                            BigDecimal.valueOf(10),
                                            BigDecimal.valueOf(20));
        //When
        BoundingBox bufferedBBox = bbox.getBufferedBoundingBox(0.1);
        
        //Then
        assertEquals("Expected the same epsg code", "EPSG:27700", bufferedBBox.getEpsgCode());
        assertEquals("Expected Buffered minx", BigDecimal.valueOf(-12), bufferedBBox.getMinX());
        assertEquals("Expected Buffered miny", BigDecimal.valueOf(-24), bufferedBBox.getMinY());
        assertEquals("Expected Buffered maxx", BigDecimal.valueOf(12), bufferedBBox.getMaxX());
        assertEquals("Expected Buffered maxy", BigDecimal.valueOf(24), bufferedBBox.getMaxY());
    }
}
