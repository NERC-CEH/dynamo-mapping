package uk.ac.ceh.dynamo.bread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Mockito.*;

/**
 *
 * @author Christopher Johnson
 */
public class BreadSliceTest {
    private ExecutorService executor;
    
    @Before
    public void createExecutorService() {
        executor = Executors.newCachedThreadPool();
    }
    
    @After
    public void cleanExecutorService() {
        executor.shutdownNow();
    }

    @Test(timeout=1000L)
    public void checkThatBreadSliceIsPreBaked() throws BreadException {
        //Given
        String backingContent = "prebaked data";
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> preBaked = new BreadSlice<>(backingContent, 2000, 0, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        String content = preBaked.getBaked();
        
        //Then
        assertSame("Expected to get the supplied string", content, backingContent);
    }
    
    @Test(timeout=1000L)
    public void checkThatBreadSliceCanBeSet() throws BreadException {
        //Given
        String content = "Going to resolve later";
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> notBakedYet = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        notBakedYet.setBaked(content); //Set the baked content
                
        //Then
        assertSame("Expeceted the set content to be supplied", notBakedYet.getBaked(), content);
    }
    
    @Test(timeout=1000L)
    public void checkThatBreadIsGivenBakedTimeWhenSet() throws BreadException {
        //Given
        String content = "Going to resolve later";
        Clock clock = mock(Clock.class);
        when(clock.getTimeInMillis()).thenReturn(5000L);
        
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> notBakedYet = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        notBakedYet.setBaked(content); //Set the baked content
                
        //Then
        assertEquals("Expected the mocked time to be set", notBakedYet.getTimeBaked(), 5000L);
    }
    
    @Test(timeout=1000L, expected=BreadException.class)
    public void checkThatBreadSliceThrowsExceptionIfFailedToBake() throws BreadException {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> notBakedYet = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        notBakedYet.setException(new BreadException("Oh no, I failed to bake"));
        notBakedYet.getBaked();
        
        //Then
        fail("Expected to fail with bread exception");
    }
    
    @Test
    public void checkThatNotBakedWhenNoContentSet() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> notBakedYet = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        boolean isBaked = notBakedYet.isBaked();
        
        //Then
        assertFalse("Expected the content to not be baked", isBaked);
    }
    
    @Test
    public void checkThatBreadIsBakedWhenConstructed() {
        //Given
        String backingContent = "prebaked data";
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> preBaked = new BreadSlice<>(backingContent, 2000, 0, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        boolean isBaked = preBaked.isBaked();
        
        //Then
        assertTrue("Excepted the bread to be baked", isBaked);
    }
    
    @Test
    public void checkThatBreadIsBakedWhenResolved() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> notBakedYet = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        notBakedYet.setBaked("Some content");
        
        //When
        boolean isBaked = notBakedYet.isBaked();
        
        //Then
        assertTrue("Excepted the bread to be baked", isBaked);
    }
    
    @Test
    public void checkNotStaleIfNotBaked() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> notBakedYet = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        boolean isStale = notBakedYet.isStale();
        
        //Then
        assertFalse("Expected bread to not be stale as it is not yet baked", isStale);
    }
    
    @Test
    public void checkNotStaleIfInStaleTime() {
        //Given
        String backingContent = "prebaked data";
        Clock clock = mock(Clock.class);        
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> preBaked = new BreadSlice<>(backingContent, 2000, 0, "SLICE-HASH", 500, clock, null, bin);
        
        when(clock.getTimeInMillis()).thenReturn(2400L); //At this time the bread shouldn't be stale
        
        //When
        boolean isStale = preBaked.isStale();
        
        //Then
        assertFalse("Expected bread to not be stale as it is not yet baked", isStale);
    }
    
    @Test
    public void checkStaleIfOutsideStaleTime() {
        //Given
        String backingContent = "prebaked data";
        Clock clock = mock(Clock.class);        
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> preBaked = new BreadSlice<>(backingContent, 2000, 0, "SLICE-HASH", 500, clock, null, bin);
        
        when(clock.getTimeInMillis()).thenReturn(2700L); //At this time the bread should be stale
        
        //When
        boolean isStale = preBaked.isStale();
        
        //Then
        assertTrue("Expected bread to be stale as it is not yet baked", isStale);
    }
    
    @Test
    public void checkThatBreadIsPutInBinIfMouldyAndNotInUse() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        //No use has been registerd on slice
        BreadSlice<String, Void> slice = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        slice.markAsMouldy();
        
        //Then
        verify(bin, times(1)).delete(slice);
    }
    
    @Test
    public void checkThatBreadIsNotPutInBinWhileInUse() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        //No use has been registerd on slice
        BreadSlice<String, Void> slice = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        slice.startEating(); //register a single use
        slice.markAsMouldy();
        
        //Then
        verify(bin, never()).delete(slice);
    }
    
    @Test
    public void checkThatBreadSlicesAreThrownAwayWhenFinishedEating() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> slice1 = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        BreadSlice<String, Void> slice2 = new BreadSlice<>(2, "SLICE-HASH", 500, clock, null, bin);
        BreadSlice<String, Void> slice3 = new BreadSlice<>(3, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        slice1.startEating();
        slice2.startEating();
        slice3.startEating();
        
        slice1.markAsMouldy();
        slice2.markAsMouldy();
        slice3.markAsMouldy();
        
        BreadSlice.finishedEating(); //Call static finished eating method
        
        //Then
        verify(bin, times(3)).delete(any(BreadSlice.class));
    }
    
    @Test
    public void checkThatBreadSlicesAreNotThrownAwayWhenFinishedEatingIfNotMouldy() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> slice1 = new BreadSlice<>(1, "SLICE-HASH", 500, clock, null, bin);
        BreadSlice<String, Void> slice2 = new BreadSlice<>(2, "SLICE-HASH", 500, clock, null, bin);
        BreadSlice<String, Void> slice3 = new BreadSlice<>(3, "SLICE-HASH", 500, clock, null, bin);
        
        //When
        slice1.startEating();
        slice2.startEating();
        slice3.startEating();
        
        BreadSlice.finishedEating(); //Call static finished eating method
        
        //Then
        verify(bin, never()).delete(any(BreadSlice.class));
    }
    
    @Test
    public void checkNaturalOrderingOfSlicesWhenAddedBackwards() {
        //Given
        List<BreadSlice> slices = new ArrayList<>();
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> slice1 = new BreadSlice<>("content1", 200L, 1, "SLICE-HASH1", 500, clock, null, bin);
        BreadSlice<String, Void> slice2 = new BreadSlice<>("content2", 300L, 2, "SLICE-HASH2", 500, clock, null, bin);
        
        slices.add(slice2);
        slices.add(slice1);
        
        //When
        Collections.sort(slices);
        
        //Then
        assertEquals("Expected the lists to be in the same order", slices, Arrays.asList(slice1, slice2));
    }
    
    @Test
    public void checkNaturalOrderingOfSlices() {
        //Given
        List<BreadSlice> slices = new ArrayList<>();
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> slice1 = new BreadSlice<>("content1", 200L, 1, "SLICE-HASH1", 500, clock, null, bin);
        BreadSlice<String, Void> slice2 = new BreadSlice<>("content2", 300L, 2, "SLICE-HASH2", 500, clock, null, bin);
        
        slices.add(slice1);
        slices.add(slice2);
        
        //When
        Collections.sort(slices);
        
        //Then
        assertEquals("Expected the lists to be in the same order", slices, Arrays.asList(slice1, slice2));
    }
    
    @Test
    public void checkGetHash() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> slice = new BreadSlice<>("content1", 200L, 1, "SLICE-HASH1", 500, clock, null, bin);
        
        //When
        String hash = slice.getMixName();
        
        //Then
        assertEquals("Expect that hash to be hash1", hash, "SLICE-HASH1");
    }
    
    @Test
    public void checkGetId() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> slice = new BreadSlice<>("content1", 200L, 5, "SLICE-HASH1", 500, clock, null, bin);
        
        //When
        int id = slice.getId();
        
        //Then
        assertEquals("Expect that id to be 5", id, 5);
    }
    
    @Test
    public void checkGetTimeBaked() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin<Void> bin = mock(DustBin.class);
        BreadSlice<String, Void> slice = new BreadSlice<>("content1", 200L, 5, "SLICE-HASH1", 500, clock, null, bin);
        
        //When
        long timeBaked = slice.getTimeBaked();
        
        //Then
        assertEquals("Expect that timeBaked to be 200", timeBaked, 200L);
    }
    
    @Test
    public void checkWeCanGetWorkSurface() {
        //Given
        Clock clock = mock(Clock.class);
        Object workSurface = new Object();
        DustBin<Object> bin = mock(DustBin.class);
        BreadSlice<String, Object> slice = new BreadSlice<>("content1", 200L, 5, "SLICE-HASH1", 500, clock, workSurface, bin);
        
        //When
        Object workSurfaceFromBreadSlice = slice.getWorkSurface();
        
        //Then
        assertSame("Expected to be able to get the work surface", workSurface, workSurfaceFromBreadSlice);
    }
    
    @Test(expected=TimeoutException.class)
    public void checkBreadSliceTimesoutIfOutputNotSet() throws InterruptedException, ExecutionException, TimeoutException {
        //Given
        Clock clock = mock(Clock.class);
        Object workSurface = new Object();
        DustBin<Object> bin = mock(DustBin.class);
        final BreadSlice<String, Object> slice = new BreadSlice<>(5, "SLICE-HASH1", 500, clock, workSurface, bin);
        
        //When
        Future submit = executor.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                return slice.getBaked();
            }
        });
        
        submit.get(10, TimeUnit.MILLISECONDS); //Ten milliseconds is plenty of time for the thread to work in
        
        //Then
        fail("Expected the thread to timeout");
    }
    
    @Test
    public void checkBreadSliceBlocksUntilOutputSet() throws InterruptedException, ExecutionException, TimeoutException {
        //Given
        Clock clock = mock(Clock.class);
        Object workSurface = new Object();
        DustBin<Object> bin = mock(DustBin.class);
        final BreadSlice<String, Object> slice = new BreadSlice<>(5, "SLICE-HASH1", 500, clock, workSurface, bin);
        
        //When
        Future<String> waitingThread = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return slice.getBaked();
            }
        });
        
        executor.submit(new Runnable() {
            @Override
            public void run() {
                slice.setBaked("Baked Content");
            }
        });
        
        String bakedContent = waitingThread.get(10, TimeUnit.MILLISECONDS); //Ten milliseconds is plenty of time for the thread to work in
        
        //Then
        assertEquals("Expected the baked content to have been set", bakedContent, "Baked Content");
    }
}
