package uk.ac.ceh.components.dynamo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author Christopher Johnson
 */
public class GridSquareFactory {
    
    public static GridSquare bng(String ref) {
        // if (ref.matches(""))
        // TODO 2006-02-05 : check format
        char char1 = ref.charAt(0);
        char char2 = ref.charAt(1);
        // Thanks to Nick Holloway for pointing out the radix bug here
        int east = Integer.parseInt(ref.substring(2, 5)) * 100;
        int north = Integer.parseInt(ref.substring(5, 8)) * 100;
        if (char1 == 'H') {
          north += 1000000;
        } else if (char1 == 'N') {
          north += 500000;
        } else if (char1 == 'O') {
          north += 500000;
          east += 500000;
        } else if (char1 == 'T') {
          east += 500000;
        }
        int char2ord = char2;
        if (char2ord > 73)
          char2ord--; // Adjust for no I
        double nx = ((char2ord - 65) % 5) * 100000;
        double ny = (4 - Math.floor((char2ord - 65) / 5)) * 100000;


        return new GridSquare((long)(east + nx), (long)(north + ny), 100000);
    }

    @Data
    @EqualsAndHashCode
    public static class GridSquare {
        private final long easting, northing;
        private final int resolution;
    }
}
