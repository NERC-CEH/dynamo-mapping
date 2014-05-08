package uk.ac.ceh.dynamo;

import freemarker.template.Template;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.mockito.ArgumentCaptor;
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
        CloseableHttpClient httpClient = getURLWhichReturns("Any old gibberish", "image/png");
        
        Template template = mock(Template.class);
                
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        ServletOutputStream mapViewOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(mapViewOutputStream);
        
        Map<String, ?> templateModel = new HashMap<>();
        
        //When
        MapServerView view = new MapServerView(httpClient, null, template, testFolder.getRoot());
        view.render(templateModel, request, response);
        
        //Then        
        verify(template).process(eq(templateModel), any(FileWriter.class)); //expect the template to be processed to a file writer
    }
    
    @Test
    public void checkThatTemplateIsCreatedInTheCorrectPlace() throws Exception {
        //Given
        CloseableHttpClient httpClient = getURLWhichReturns("Any old gibberish", "image/png");
        
        Template template = mock(Template.class);
                
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        ServletOutputStream mapViewOutputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(mapViewOutputStream);
        Map<String, ?> templateModel = new HashMap<>();
        
        //When
        MapServerView view = new MapServerView(httpClient, null, template, testFolder.getRoot());
        view.render(templateModel, request, response);
        
        //Then
        ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
        verify(httpClient).execute(argument.capture());
        ServletOutputStreamSaver outputStream = new ServletOutputStreamSaver();
        argument.getValue().getEntity().writeTo(outputStream);
        
        Map<String, String> mapServerRequestQuery = parseQuery(outputStream.toString());
        assertSame("Expected the template temp folder to have been cleaned up", 0, testFolder.getRoot().list().length);
        assertTrue("Expected a map parameter to be passed to mapserver", mapServerRequestQuery.containsKey("map"));
        assertEquals("Expected a map to have been a file in test folder", new File(mapServerRequestQuery.get("map")).getParentFile(), testFolder.getRoot());
    }
    
    @Test
    public void checkThatContentFromMapServerIsForwaredToOutput() throws Exception {
        //Given
        String mapServerContent = "Response From Map Server";
        CloseableHttpClient httpClient = getURLWhichReturns(mapServerContent, "image/png");
        
        Template template = mock(Template.class);
                
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        ServletOutputStreamSaver mapViewOutputStream = new ServletOutputStreamSaver();
        when(response.getOutputStream()).thenReturn(mapViewOutputStream);
        
        //When
        MapServerView view = new MapServerView(httpClient, null, template, testFolder.getRoot());
        view.render(null, request, response);
        
        //Then
        assertEquals("Expected the output from mapserver to be sent to the http response", mapServerContent, mapViewOutputStream.toString());
    }
    
    private CloseableHttpClient getURLWhichReturns(String content, String type) throws IOException {
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        HttpEntity entity = new StringEntity(content, ContentType.create(type));
        
        when(httpClient.execute(any(HttpPost.class))).thenReturn(response);
        when(response.getEntity()).thenReturn(entity);
        
        return httpClient;
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
