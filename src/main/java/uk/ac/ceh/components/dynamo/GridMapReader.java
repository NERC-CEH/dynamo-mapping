package uk.ac.ceh.components.dynamo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ceh.components.dynamo.GridMapRequestFactory.GridMapRequest;
import uk.ac.ceh.components.dynamo.GridSquareFactory.GridSquare;

/**
 *
 * @author Christopher Johnson
 */
public class GridMapReader {
    private Map<GridSquare, Color> squaresColours;
    
    public GridMapReader(BufferedImage image, GridMapRequest request) {
        this.squaresColours = new HashMap<>();
        
        int squareSize = request.getAmountOfPixelsForGrid();
        
        for(int y=0; y<request.getAmountOfSquaresY(); y++) {
            for(int x=0; x<request.getAmountOfSquaresX(); x++) {
                //image coordinates start at top left, where as bbox's start at the
                //bottom left. Need to flip the Y to get the correct pixel
                int imageY = request.getAmountOfSquaresY() - y - 1;
                int rgb = image.getRGB(x*squareSize, imageY*squareSize);
                squaresColours.put(getGridSquareAtPixel(x, y, request), new Color(rgb));
            }
        }
    }
    
    public Color getColourAt(GridSquare square) {        
        return squaresColours.get(square);
    }
    
    public List<GridSquare> getGridSquaresByColour(Color colour) {
        List<GridSquare> squares = new ArrayList<>();
        for(Entry<GridSquare, Color> entry : squaresColours.entrySet()) {
            if(entry.getValue().equals(colour)) {
                squares.add(entry.getKey());
            }
        }
        return squares;
    }
    
    public static GridSquare getGridSquareAtPixel(int x, int y, GridMapRequest request) {
        int[] griddedBBox = request.getGriddedBBox();
        return new GridSquare(
            griddedBBox[0] + x * request.getResolution(),
            griddedBBox[1] + y * request.getResolution(),
            request.getResolution());
    }
}
