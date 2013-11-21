package uk.ac.ceh.dynamo;

import java.net.URL;
import java.util.Locale;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
/**
 *
 * @author Christopher Johnson
 */
public class MapServerViewResolverTest {
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Test
    public void resolveViewForTemplateWhichExists() throws Exception {
        //Given
        MapServerViewResolver resolver = new MapServerViewResolver(testFolder.getRoot(), new URL("http://localhost"));
        testFolder.newFile("test.map");
        
        //When
        MapServerView resolvedView = resolver.resolveViewName("test.map", Locale.ENGLISH);
        
        //Then
        assertNotNull("Expected a view to be created", resolvedView);
    }
    
    @Test
    public void attemptToResolveViewForTemplateWhichDoesntExist() throws Exception {
        //Given
        MapServerViewResolver resolver = new MapServerViewResolver(testFolder.getRoot(), new URL("http://localhost"));
        
        //When
        MapServerView resolvedView = resolver.resolveViewName("idon'texist.map", Locale.ENGLISH);
        
        //Then
        assertNull("Expected to not find a view", resolvedView);
    }
}
