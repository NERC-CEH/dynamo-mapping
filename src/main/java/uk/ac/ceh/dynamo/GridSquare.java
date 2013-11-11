package uk.ac.ceh.dynamo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The following class represents a grid square. The easting and northing values
 * are the coordinates of the bottom left corner of the given grid square.
 * 
 * The resolution is the value which this grid square is snapped to
 * @author Christopher Johnson
 */
@Data
@EqualsAndHashCode
public class GridSquare {
    private final long easting, northing;
    private final int resolution;
}