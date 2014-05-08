package uk.ac.ceh.dynamo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.web.servlet.ViewResolver;

/**
 * A spring mvc view resolver which checks to see if a template exists before
 * returning a Map Sever View ready for processing
 * @see MapServerView
 * @author Christopher Johnson
 */
public class MapServerViewResolver implements ViewResolver {
    private final CloseableHttpClient httpClient;
    private final URI mapServerURI;
    private final Configuration config;
    private final File templateDirectory;
    
    public MapServerViewResolver(CloseableHttpClient httpClient, File templateDirectory, URI mapServerURI) throws IOException {
        this.config = new Configuration();
        this.httpClient = httpClient;
        this.mapServerURI = mapServerURI;
        this.templateDirectory = templateDirectory;
        config.setDirectoryForTemplateLoading(templateDirectory);
    }
    
    @Override
    public MapServerView resolveViewName(String viewName, Locale locale) throws Exception {
        File template = new File(templateDirectory, viewName);
        if(template.isFile() && template.exists()) { 
            Template mapFileTemplate = config.getTemplate(viewName);
            return new MapServerView(httpClient, mapServerURI, mapFileTemplate, template.getParentFile());
        }
        else {
            return null;
        }
    }
}
