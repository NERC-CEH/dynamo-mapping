package uk.ac.ceh.dynamo;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.web.servlet.View;
import uk.ac.ceh.dynamo.bread.BreadSlice;

/**
 * The following View is responsible for rendering a map file template and storing
 * it on disk and then posting the location of that map file to a mapserver instance
 * along with any other request parameters which may have been supplied in the 
 * HttpServletRequest which initialised the creation of this view.
 * 
 * @author Christopher Johnson
 */
public class MapServerView implements View {    
    private static final String URL_PARAMETER_ENCODING = "UTF-8";
    private final URI mapServerURI;
    private final Template mapFileTemplate;
    private final File templateDirectory;
    private final CloseableHttpClient httpClient;

    /**
     * Creates a MapServerView for the given mapFileTemplate to be called against
     * a given mapServer
     * @param httpClient the apache http client to use for connecting to mapserver
     * @param mapServerURI The url for the mapserver which this view should be rendered against
     * @param mapFileTemplate The map template to process to create a map file to pass to mapserver
     * @param templateDirectory The folder which the template was loaded from 
     *  and to use for creating the temporary map file to pass to mapserver
     */
    public MapServerView(CloseableHttpClient httpClient, URI mapServerURI, Template mapFileTemplate, File templateDirectory) {
        this.httpClient = httpClient;
        this.mapServerURI = mapServerURI;
        this.mapFileTemplate = mapFileTemplate;
        this.templateDirectory = templateDirectory;
    }
    
    /**
     * Let MapServer report it's content type.
     * @return always null
     */
    @Override public String getContentType() {
        return null;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse servletResponse) throws Exception {
        File mapFile = getMapFile(model);
        try {
            HttpPost httppost = new HttpPost(mapServerURI);
            httppost.setEntity(getQueryFromMap(getMapServerRequest(mapFile, request)));

            try (CloseableHttpResponse response = httpClient.execute(httppost)) {
                HttpEntity entity = response.getEntity();
                servletResponse.setContentType(entity.getContentType().getValue());
                copyAndClose(entity, servletResponse);
            }
        }
        finally {
            mapFile.delete();
            BreadSlice.finishedEating(); //We can get rid of any breadslices which were used now
        }
    }
    
    private File getMapFile(Map<String, ?> model) throws IOException, TemplateException {
        // File output
        File file = File.createTempFile("generated", ".map", templateDirectory);
        try (Writer out = new FileWriter (file)) {
            mapFileTemplate.process(model, out);
            out.flush();
        }
        return file;
    }
    
    private static Map<String, String[]> getMapServerRequest(File mapFile, HttpServletRequest request) {
        Map<String, String[]> modifiedQuery = new HashMap<>(request.getParameterMap());
        modifiedQuery.put("map", new String[] {mapFile.getAbsolutePath()});
        return modifiedQuery;
    }
    
    private static UrlEncodedFormEntity getQueryFromMap(Map<String, String[]> query) throws UnsupportedEncodingException {
        List<NameValuePair> nvps = new ArrayList<>();
        
        for(Map.Entry<String, String[]> entry : query.entrySet()) {
            for(String currValue : entry.getValue()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), currValue));
            }
        }
        return new UrlEncodedFormEntity(nvps, URL_PARAMETER_ENCODING);
    }
    
    private static void copyAndClose(HttpEntity in, HttpServletResponse response) throws IOException {
        try (ServletOutputStream out = response.getOutputStream()) {
            in.writeTo(out);
        }
    }
    
    @Data
    @AllArgsConstructor
    private class Response {
        private final String contentType;
        private InputStream inputStream;
    }
}
