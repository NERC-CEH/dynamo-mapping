package uk.ac.ceh.components.dynamo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author Chris Johnson
 */
@Data
@AllArgsConstructor
public class BoundingBox {
    private String epsgCode;
    private BigDecimal minX, minY, maxX, maxY;
}
