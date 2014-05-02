package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.IOException;
import java.lang.String;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 *
 * @author Christopher Johnson
 */
public class ShapefileGeneratorTest {
    private static String OGR2OGR = "";
    private static String CONNECTION_STRING = "";
    
    Semaphore semaphore;
    ExecutorService remover;
    @Spy ShapefileGenerator generator;
    public @Rule TemporaryFolder folder = new TemporaryFolder();
    
    @Before
    public void spyOnGenerator() throws IOException, InterruptedException, BreadException {
        remover = mock(ExecutorService.class);
        semaphore = mock(Semaphore.class);
        generator = new ShapefileGenerator(OGR2OGR, CONNECTION_STRING, semaphore, remover);
        MockitoAnnotations.initMocks(this);
        
        doNothing().when(generator).process(any(File.class), any(String.class));
    }

    @Test
    public void checkThatCanCookData() throws BreadException, InterruptedException, IOException {
        //Given
        BreadSlice<String, File> slice = mock(BreadSlice.class);
        File workSurface = folder.newFolder("folder");
        when(slice.getWorkSurface()).thenReturn(workSurface);
        when(slice.getId()).thenReturn(0);
        when(slice.getHash()).thenReturn("HASH");
        String sql = "my sql statement";
        
        //When
        generator.cook(slice, sql);
        
        //Then
        verify(semaphore, times(1)).acquire(); //Semaphore went down
        verify(semaphore, times(1)).release();//Semaphore went up
        verify(generator, times(1)).process(eq(new File(workSurface, "0_HASH")), eq(sql));
    }
    
    @Test
    public void checkThatCanDeleteBreadSlice() throws IOException, InterruptedException {
        //Given
        File shpFile = folder.newFile("0_HASH.shp");
        File shxFile = folder.newFile("0_HASH.shx");
        File dbfFile = folder.newFile("0_HASH.dbf");
        
        BreadSlice<String, File> slice = mock(BreadSlice.class);
        when(slice.getWorkSurface()).thenReturn(folder.getRoot());
        when(slice.getId()).thenReturn(0);
        when(slice.getHash()).thenReturn("HASH");
        
        remover = spy(Executors.newCachedThreadPool());
        generator = new ShapefileGenerator(OGR2OGR, CONNECTION_STRING, semaphore, remover);
        
        //When
        generator.delete(slice);
        
        remover.shutdown();
        remover.awaitTermination(1, TimeUnit.SECONDS);
        
        //Then
        assertFalse("Expected shape file to be deleted", shpFile.exists());
        assertFalse("Expected shape file to be deleted", shxFile.exists());
        assertFalse("Expected shape file to be deleted", dbfFile.exists());
    } 
    
    @Test
    public void checkCanReloadFromExistingDirectory() {
        
    }
}
