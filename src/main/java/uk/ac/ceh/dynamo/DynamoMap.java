package uk.ac.ceh.dynamo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method annotation which registers a map service method as being dynamo map
 * capable
 * @author Christopher Johnson
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamoMap {
    static class Resolution {
        public static final int TEN_KM = 10000, TWO_KM = 2000, ONE_KM=1000, ONE_HUNDRED_METERS = 100;
    }
    
    @interface GridLayer {
        String name();
        String layer();
        int resolution();
    }
    
    @interface Layer {
        String name();
        String[] layers();
    }
    
    @interface Extent {
        String name();
        String epsgCode();
        int[] extent();
    }

    GridLayer[] layers();
    String defaultLayer();
    Layer[] backgrounds() default {
        @Layer(name="os", layers="OS-Scale-Dependent" ),
        @Layer(name="gb", layers="GB-Coast" ),
        @Layer(name="i", layers="Ireland-Coast" ),
        @Layer(name="gbi", layers={"GB-Coast", "Ireland-Coast"} ),
        @Layer(name="gb100kgrid", layers="GB-Coast-with-Hundred-km-Grid" ),
        @Layer(name="i100kgrid", layers="Ireland-coast-with-Hundred-km-Grid" ),
        @Layer(name="gbi100kgrid", layers={"GB-Coast-with-Hundred-km-Grid", "Ireland-coast-with-Hundred-km-Grid"} ),
        @Layer(name="vicecounty", layers="Vice-counties" ),
        @Layer(name="gb100kextent", layers="GB-Hundred-km-Grid" ),
        @Layer(name="gb10kextent", layers="GB-Ten-km-Grid" ),
        @Layer(name="i100kextent", layers="Ireland-Hundred-km-Grid" ),
        @Layer(name="i10kextent", layers="Ireland-Ten-km-Grid" ),
        @Layer(name="gbi10kextent", layers={"GB-Ten-km-Grid-Ireland-cutout","Ireland-Ten-km-Grid-GB-cutout"} ),
        @Layer(name="gbi100kextent", layers={"GB-Hundred-km-Grid-Ireland-cutout","Ireland-Hundred-km-Grid-GB-cutout"} )
    };
    Layer[] overlays() default {};
    String[] defaultBackgrounds() default {"gbi100kgrid"};
    String[] defaultOverlays() default {};
    Extent[] extents() default {
        @Extent(name="gb",      epsgCode="EPSG:27700", extent={0, 0, 700000, 1310000}),
        @Extent(name="ireland", epsgCode="EPSG:29903", extent={-10000, -10000, 410000, 510000}),
        @Extent(name="gbi",     epsgCode="EPSG:27700", extent={-250000, -50000, 750000, 1310000})
    };
    
    String defaultExtent() default "gbi";    
}
