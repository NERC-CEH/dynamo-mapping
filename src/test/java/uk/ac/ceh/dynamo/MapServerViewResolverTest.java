package uk.ac.ceh.dynamo;

import java.net.URI;
import java.util.Locale;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
/**
 *
 * @author Christopher Johnson
 */
public class MapServerViewResolverTest {
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    @Mock CloseableHttpClient httpClient;
    
    @Before
    public void createHttpClient() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void resolveViewForTemplateWhichExists() throws Exception {
        //Given
        MapServerViewResolver resolver = new MapServerViewResolver(httpClient, testFolder.getRoot(), new URI("http://localhost"));
        testFolder.newFile("test.map");
        
        //When
        MapServerView resolvedView = resolver.resolveViewName("test.map", Locale.ENGLISH);
        
        //Then
        assertNotNull("Expected a view to be created", resolvedView);
    }
    
    @Test
    public void attemptToResolveViewForTemplateWhichDoesntExist() throws Exception {
        //Given
        MapServerViewResolver resolver = new MapServerViewResolver(httpClient, testFolder.getRoot(), new URI("http://localhost"));
        
        //When
        MapServerView resolvedView = resolver.resolveViewName("idon'texist.map", Locale.ENGLISH);
        
        //Then
        assertNull("Expected to not find a view", resolvedView);
    }
}
