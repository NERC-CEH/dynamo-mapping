package uk.ac.ceh.dynamo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * A spring mvc view resolver which checks to see if a template exists before
 * returning a Map Sever View ready for processing
 * @see MapServerView
 * @author Christopher Johnson
 */
public class MapServerViewResolver implements ViewResolver {
    private final URL mapServerURL;
    private final Configuration config;
    private final File templateDirectory;
    
    public MapServerViewResolver(File templateDirectory, URL mapServerURL) throws IOException {
        this.config = new Configuration();
        this.mapServerURL = mapServerURL;
        this.templateDirectory = templateDirectory;
        config.setDirectoryForTemplateLoading(templateDirectory);
    }
    
    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        File template = new File(templateDirectory, viewName);
        if(template.isFile() && template.exists()) { 
            Template mapFileTemplate = config.getTemplate(viewName);
            return new MapServerView(mapServerURL, mapFileTemplate, template.getParentFile());
        }
        else {
            return null;
        }
    }
}
