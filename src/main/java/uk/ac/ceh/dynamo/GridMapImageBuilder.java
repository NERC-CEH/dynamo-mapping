package uk.ac.ceh.dynamo;

import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * The following class is a simple builder for creating grid map images powered by
 * Jersey Client Web Resources.
 * 
 * The idea here is to be able to load grid map images from a grid map server
 * which has been fully deployed next to a MapServer. By being fully deployed, the
 * system will be able to communicate with mapserver and actually generate grid 
 * map images.
 * 
 * If the @GridMap annotation is known for the grid map end point 
 * (e.g. http://my.gis.com/Something/map), then we can easily construct a 
 * GridMapImage to read the details of the requested image.
 * 
 * If you are intending to use this GridMapImageBuilder for testing grid map end
 * points, then you can obtain the @GridMap annotation trivially using reflection.
 *  
 * @author Christopher Johnson
 */
@Data
@Accessors(fluent = true)
public class GridMapImageBuilder {
    private final WebResource gridMapServer;
    private final GridMap gridMap;
    private int imageSize = 10;
    private String resolution, feature, nationalExtent;
    private FeatureResolver featureResolver;

    public GridMapImage build() throws IOException {
        GridMap.GridLayer gridResolution = GridMapController.getResolution(gridMap, resolution);

        WebResource resource = gridMapServer
                                .queryParam("resolution", gridResolution.name())
                                .queryParam("imagesize", Integer.toString(imageSize));

        if(feature != null) {
            resource.queryParam("feature", feature);
        }

        if(nationalExtent != null) {
            resource.queryParam("nationalextent", nationalExtent);
        }

        GridMapRequestFactory factory = new GridMapRequestFactory(featureResolver);
        BoundingBox featureToFocusOn = factory.getFeatureToFocusOn(feature, nationalExtent, gridMap);

        return new GridMapImage(
                ImageIO.read(resource.get(InputStream.class)), 
                factory.getGridMapRequest(
                    featureToFocusOn, 
                    gridResolution.resolution(), 
                    imageSize)
        );
    }
}