package uk.ac.ceh.dynamo;

import freemarker.template.Template;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
/**
 *
 * @author Christopher Johnson
 */
public class MapServerViewTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    
    @Test
    public void checkThatTemplateIsExecuted() throws Exception {
        //Given
        URL url = getURLWhichReturns("Any old gibberish", new ByteArrayOutputStream());
        
        Template template = mock(Template.class);
                
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        ServletOutputStream mapViewOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(mapViewOutputStream);
        
        Map<String, ?> templateModel = new HashMap<>();
        
        //When
        MapServerView view = new MapServerView(url, template, testFolder.getRoot());
        view.render(templateModel, request, response);
        
        //Then        
        verify(template).process(eq(templateModel), any(FileWriter.class)); //expect the template to be processed to a file writer
    }
    
    @Test
    public void checkThatTemplateIsCreatedInTheCorrectPlace() throws Exception {
        //Given
        ByteArrayOutputStream mapServerOutput = new ByteArrayOutputStream();
        URL url = getURLWhichReturns("Any old gibberish", mapServerOutput);
        
        Template template = mock(Template.class);
                
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        ServletOutputStream mapViewOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(mapViewOutputStream);
        Map<String, ?> templateModel = new HashMap<>();
        
        //When
        MapServerView view = new MapServerView(url, template, testFolder.getRoot());
        view.render(templateModel, request, response);
        
        //Then
        Map<String, String> mapServerRequestQuery = parseQuery(mapServerOutput.toString());
        
        assertSame("Expected the template temp folder to have been cleaned up", 0, testFolder.getRoot().list().length);
        assertTrue("Expected a map parameter to be passed to mapserver", mapServerRequestQuery.containsKey("map"));
        assertEquals("Expected a map to have been a file in test folder", new File(mapServerRequestQuery.get("map")).getParentFile(), testFolder.getRoot());
    }
    
    @Test
    public void checkThatContentFromMapServerIsForwaredToOutput() throws Exception {
        //Given
        String mapServerContent = "Response From Map Server";
        URL url = getURLWhichReturns(mapServerContent, new ByteArrayOutputStream());
        
        Template template = mock(Template.class);
                
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        ServletOutputStreamSaver mapViewOutputStream = new ServletOutputStreamSaver();
        when(response.getOutputStream()).thenReturn(mapViewOutputStream);
        
        //When
        MapServerView view = new MapServerView(url, template, testFolder.getRoot());
        view.render(null, request, response);
        
        //Then
        assertEquals("Expected the output from mapserver to be sent to the http response", mapServerContent, mapViewOutputStream.toString());
    }
    
    private URL getURLWhichReturns(String content, OutputStream output) throws IOException {
        HttpURLConnection connection = mock(HttpURLConnection.class);
        
        when(connection.getOutputStream()).thenReturn(output);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
        
        return mockUrl(connection);
    }
    
    //helper class to store what a servlet output has been told to write
    private static class ServletOutputStreamSaver extends ServletOutputStream{

        private ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        @Override
        public void write(int b) throws IOException {
            output.write(b);
        }
        
        @Override
        public String toString() {
            return output.toString();
        } 
    }
    
    private URL mockUrl(final URLConnection connection) throws MalformedURLException {
        return new URL("http://foo.bar", "foo.bar", 80, "", new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(final URL arg0) throws IOException {
                return connection;
            }
        });
    }
    
    //I can't find anything that can parse the query sent to map server
    //Using this code (slightly modified) from stackoverflow 
    // @ http://stackoverflow.com/questions/13592236/parse-the-uri-string-into-name-value-collection-in-java
    private Map<String, String> parseQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }
}
