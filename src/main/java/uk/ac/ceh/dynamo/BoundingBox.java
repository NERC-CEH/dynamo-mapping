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
}
