package uk.ac.ceh.dynamo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Representation of a bounding box which has coordinates at minX,minY,maxX,maxY
 * in the given projection system (EPSG:CODE)
 * @author Chris Johnson
 */
@Data
@AllArgsConstructor
public class BoundingBox {
    private String epsgCode;
    private BigDecimal minX, minY, maxX, maxY;
    
        
    /**
     * The following method will buffer a given bounding box in all directions by
     * a factor of bufferFactor.
     * @param bufferFactor The factor to buffer in all directions. 0.05 will will 
     *  buffer 5% for a given dimension in all directions
     * @return A buffered bounding box
     */
    public BoundingBox getBufferedBoundingBox(double bufferFactor) {
        double xDistance = getMaxX().subtract(getMinX()).abs().doubleValue();
        double yDistance = getMaxY().subtract(getMinY()).abs().doubleValue();
        BigDecimal xBuffer = new BigDecimal(xDistance*bufferFactor);
        BigDecimal yBuffer = new BigDecimal(yDistance*bufferFactor);
        return new BoundingBox(
                getEpsgCode(), 
                getMinX().subtract(xBuffer), 
                getMinY().subtract(yBuffer), 
                getMaxX().add(xBuffer), 
                getMaxY().add(yBuffer));
    }
}
