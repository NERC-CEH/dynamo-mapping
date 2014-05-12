package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
    private static String SHPTREE = "";
    private static String CONNECTION_STRING = "";
    
    Semaphore semaphore;
    ExecutorService remover;
    @Spy ShapefileGenerator generator;
    public @Rule TemporaryFolder folder = new TemporaryFolder();
    
    @Before
    public void spyOnGenerator() throws IOException, InterruptedException, BreadException {
        remover = mock(ExecutorService.class);
        semaphore = mock(Semaphore.class);
        generator = new ShapefileGenerator(OGR2OGR, SHPTREE, CONNECTION_STRING, semaphore, remover);
        MockitoAnnotations.initMocks(this);
        
        doNothing().when(generator).generateShapefile(any(File.class), any(String.class));
        doNothing().when(generator).indexShapefile(any(File.class));
    }

    @Test
    public void checkThatCanCookData() throws BreadException, InterruptedException, IOException {
        //Given
        BreadSlice<String, File> slice = mock(BreadSlice.class);
        File workSurface = folder.newFolder("folder");
        when(slice.getWorkSurface()).thenReturn(workSurface);
        when(slice.getId()).thenReturn(0);
        when(slice.getMixName()).thenReturn("HASH");
        String sql = "my sql statement";
        
        //When
        generator.cook(slice, sql);
        
        //Then
        verify(semaphore, times(1)).acquire(); //Semaphore went down
        verify(semaphore, times(1)).release();//Semaphore went up
        verify(generator, times(1)).generateShapefile(eq(new File(workSurface, "0_HASH.shp")), eq(sql));
        verify(generator, times(1)).indexShapefile(eq(new File(workSurface, "0_HASH.shp")));
    }
    
    @Test
    public void checkThatCanDeleteBreadSlice() throws IOException, InterruptedException {
        //Given
        File shpFile = folder.newFile("0_HASH.shp");
        File shxFile = folder.newFile("0_HASH.shx");
        File dbfFile = folder.newFile("0_HASH.dbf");
        File qixFile = folder.newFile("0_HASH.qix");
        
        BreadSlice<String, File> slice = mock(BreadSlice.class);
        when(slice.getWorkSurface()).thenReturn(folder.getRoot());
        when(slice.getId()).thenReturn(0);
        when(slice.getMixName()).thenReturn("HASH");
        
        remover = spy(Executors.newCachedThreadPool());
        generator = new ShapefileGenerator(OGR2OGR, SHPTREE, CONNECTION_STRING, semaphore, remover);
        
        //When
        generator.delete(slice);
        
        remover.shutdown();
        remover.awaitTermination(1, TimeUnit.SECONDS);
        
        //Then
        assertFalse("Expected shape file to be deleted", shpFile.exists());
        assertFalse("Expected shape file to be deleted", shxFile.exists());
        assertFalse("Expected shape file to be deleted", dbfFile.exists());
        assertFalse("Expected shape file to be deleted", qixFile.exists());
    } 
    
    @Test
    public void checkCanReloadFromExistingDirectory() throws IOException {
        //Given
        Clock clock = mock(Clock.class);
        long staleTime = 2000;
        folder.newFile("0_HASH-WHATEVER.shp");
        folder.newFile("2_HASH-WHATEVER.shp");
        
        //When
        List<BreadSlice<String, File>> slices = generator.reload(clock, folder.getRoot(), generator, staleTime);
        
        //Then
        assertEquals("Expected two slices in the bread bin", 2, slices.size());
    }
    
    @Test
    public void checkThatOtherFilesDontGetLoaded() throws IOException {
        Clock clock = mock(Clock.class);
        long staleTime = 2000;
        folder.newFile("AnyoldFile.db");
        folder.newFile("2_HASH-WHATEVER.shp");
        
        //When
        List<BreadSlice<String, File>> slices = generator.reload(clock, folder.getRoot(), generator, staleTime);
        
        //Then
        assertEquals("Expected only one slices in the bread bin", 1, slices.size());
    }
    
    @Test(timeout=1000L)
    public void checkThatShapefileIsLoadedCorrectly() throws IOException, BreadException {
        //Given
        Clock clock = mock(Clock.class);
        long staleTime = 2000;
        File shapeFile = folder.newFile("2_HASH-WHATEVER.shp");
        
        //When
        BreadSlice<String, File> slice = generator.reload(clock, folder.getRoot(), generator, staleTime).get(0);
        
        //Then
        assertEquals("Expected full path", slice.getBaked(), shapeFile.getAbsolutePath());
        assertEquals("Expected slice to have the id 2", 2, slice.getId());
        assertEquals("Expected slice to have the correct hash", "HASH-WHATEVER", slice.getMixName());
        assertEquals("Expected to get the correct workSurface", folder.getRoot(), slice.getWorkSurface());
    }
}
