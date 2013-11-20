package uk.ac.ceh.dynamo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.ceh.dynamo.GridMapRequestFactory.GridMapRequest;

/**
 * A grid map reader which will process a buffered images with regards to a 
 * grid map request and allow reading of the colours for grid squares present in
 * that image.
 * @author Christopher Johnson
 */
public class GridMapImage {
    private Map<GridSquare, Color> squaresColours;
    private Map<Color, List<GridSquare>> colouredSquares;
    
    /**
     * Constructor for processing the given image against the given gridmaprequest
     * @param image the image to read colours from
     * @param request the grid map request used for creating the image
     */
    public GridMapImage(BufferedImage image, GridMapRequest request) {
        this.squaresColours = new HashMap<>();
        this.colouredSquares = new HashMap<>();
        
        int squareSize = request.getAmountOfPixelsForGrid();
        
        for(int y=0; y<request.getAmountOfSquaresY(); y++) {
            for(int x=0; x<request.getAmountOfSquaresX(); x++) {
                //image coordinates start at top left, where as bbox's start at the
                //bottom left. Need to flip the Y to get the correct pixel
                int imageY = request.getAmountOfSquaresY() - y - 1;
                int rgb = image.getRGB(x*squareSize, imageY*squareSize);
                addColouredSquare(getGridSquareAtPixel(x, y, request), new Color(rgb));
            }
        }
    }
    
    /**
     * Gets the colour of the grid square from the BufferedImage which generated
     * this GridMapReader
     * @param square The grid square to read the colour of
     * @return The colour of the grid square if that grid square is present in
     *  the buffered image. If it is not present this method will return null
     */
    public Color getColourAt(GridSquare square) {        
        return squaresColours.get(square);
    }
    
    /**
     * Gets all the gridsquares in the buffered image which are coloured the same
     * as the given colour
     * @param colour the colour to find grid squares of
     * @return A list of gridsquares which are coloured the same as the given colour,
     *  if there are no gridsquares with the given colour, an empty list will be
     *  returned
     */
    public List<GridSquare> getGridSquaresByColour(Color colour) {
        return Collections.unmodifiableList(getMutableGridSquaresByColour(colour));
    }
    
    private void addColouredSquare(GridSquare square, Color colour) {
        squaresColours.put(square, colour);
        getMutableGridSquaresByColour(colour).add(square);
    }
    
    private List<GridSquare> getMutableGridSquaresByColour(Color colour) {
        if(!colouredSquares.containsKey(colour)) {
            colouredSquares.put(colour, new ArrayList<GridSquare>());
        }
        return colouredSquares.get(colour);
    }
    
    private static GridSquare getGridSquareAtPixel(int x, int y, GridMapRequest request) {
        int[] griddedBBox = request.getGriddedBBox();
        return new GridSquare(
            griddedBBox[0] + x * request.getResolution(),
            griddedBBox[1] + y * request.getResolution(),
            request.getResolution());
    }
}
