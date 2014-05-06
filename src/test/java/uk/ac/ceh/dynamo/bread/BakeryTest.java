package uk.ac.ceh.dynamo.bread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.ac.ceh.dynamo.bread.Bakery.Baker;

/**
 *
 * @author Christopher Johnson
 */
public class BakeryTest {
    final static long STALE_TIME = 100;
    final static long MOULDY_TIME = 1000;
    
    Object workSurface;
    @Mock Clock clock;
    @Mock DustBin bin;
    @Mock Oven oven;
    @Mock ClimateMeter climate;
    @Mock BreadBin breadBin;
    @Spy Map cache, bakingCache;
    @Spy ExecutorService breadOvens;
    
    @Before
    public void mockBakeryDependencies() {
        workSurface = new Object();
        cache = new HashMap();
        bakingCache = new HashMap();
        breadOvens = Executors.newCachedThreadPool();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkCanGetDustBin() {
        //Given
        Bakery bakery = createBakery();
        
        //When
        DustBin obtainedBin = bakery.getDustbin();
        
        //Then
        assertSame("Expected to get the bin", bin, obtainedBin);
    }
    
    @Test
    public void checkCanGetClock() {
        //Given
        Bakery bakery = createBakery();
        
        //When
        Clock obtainedClock = bakery.getClock();
        
        //Then
        assertSame("Expected to get the clock", clock, obtainedClock);
    }
    
    @Test
    public void checkThatCurrentClimateIsPoweredByClimateMeter() {
        //Given
        Bakery bakery = createBakery();
        
        //When
        when(climate.getCurrentClimate(bakery)).thenReturn(0.3);
        
        //Then
        assertEquals("Expected 0.3 as the current bakery climate", 0.3, bakery.getCurrentClimate(), 0);
    }
    
    @Test
    public void checkCanAddMapSizes() {
        //Given
        doReturn(30).when(cache).size();
        doReturn(77).when(bakingCache).size();
        Bakery bakery = createBakery();
        
        //When
        int sliceCount = bakery.getBreadSliceCount();
        
        //Then
        assertEquals("Expected 107 slice count", 107, sliceCount);
    }
    
    @Test
    public void checkCanGetWorkSurface() {
        //Given
        Bakery bakery = createBakery();

        //When
        Object obtainedWorkSurface = bakery.getWorkSurface();
        
        //Then
        assertSame("Expected to get the worksurface", workSurface, obtainedWorkSurface);
    }
    
    @Test
    public void checkThatBreadBinIsFilledWithExistingBreadSlices() {
        //Given
        BreadSlice slice = mock(BreadSlice.class);
        
        //When
        when(oven.reload(clock, workSurface, bin, STALE_TIME)).thenReturn(Arrays.asList(slice));
        createBakery();

        //Then
        verify(breadBin, times(1)).add(slice);
    }
    
    @Test
    public void checkThatAddingABreadSliceIncrementsID() throws BreadException {
        //Given
        Bakery bakery = createBakery();
        int initialBakeryValue = bakery.getNextId();
        
        //When
        bakery.getData("SomeFresh Ingredients");
        
        //Then
        assertEquals("Expect that the next initalValue will be supplied", bakery.getNextId(), initialBakeryValue + 1);        
    }
    
    @Test
    public void checkThatReloadingSetsLatestID() {
        //Given
        BreadSlice slice = mock(BreadSlice.class);
        
        //When
        when(slice.getId()).thenReturn(504);
        when(oven.reload(clock, workSurface, bin, STALE_TIME)).thenReturn(Arrays.asList(slice));
        Bakery bakery = createBakery();

        //Then
        assertEquals("Expected the bread id to be one higher", bakery.getNextId(), 505);
    }
    
    @Test
    public void checkFreshIngredientsResultInNewBake() throws BreadException {
        //Given
        Bakery bakery = createBakery();
        String ingredients = "My new ingredients";
                
        //When
        bakery.getData(ingredients);
        
        //Then
        verify(oven, times(1)).cook(any(BreadSlice.class), eq(ingredients));
    }
    
    @Test
    public void checkReBakedIngredientsOnlyBakesOnceWhenNotStale() throws BreadException {
        //Given
        Bakery bakery = createBakery();
        String ingredients = "My new ingredients";
        bakery.getData(ingredients);
        
        //When
        bakery.getData(ingredients);
        
        //Then
        verify(oven, times(1)).cook(any(BreadSlice.class), eq(ingredients));
        verify(breadOvens, never()).submit(any(Baker.class));
    }
    
    @Test
    public void checkDifferentBakeResultsInTwoBakes() throws BreadException {
        //Given
        Bakery bakery = createBakery();
        String firstIngredients = "My new ingredients";
        String secondIngredients = "Anothor set of ingredients";
        
        //When
        bakery.getData(firstIngredients);
        bakery.getData(secondIngredients);
        
        //Then
        verify(oven, times(1)).cook(any(BreadSlice.class), eq(firstIngredients));
        verify(oven, times(1)).cook(any(BreadSlice.class), eq(secondIngredients));
    }
    
    @Test
    public void checkStaleDataResultsInReBake() throws BreadException, InterruptedException {
        //Given
        String ingredients = "My Ingredients";
        
        when(clock.getTimeInMillis()).thenReturn(0L);
        when(oven.cook(any(BreadSlice.class), eq(ingredients)))
                .thenReturn("firstBake")
                .thenReturn("secondBake");
        
        Bakery bakery = createBakery();
        
        //When
        Object firstRequest = bakery.getData(ingredients);
        when(clock.getTimeInMillis()).thenReturn(STALE_TIME + 1);
        Object secondRequest = bakery.getData(ingredients);
        
        //Then
        assertSame("Expected the stale bake to yield the same as the fresh bake", firstRequest, secondRequest);
        verify(breadOvens, times(1)).submit(any(Baker.class));
        verify(bakingCache, times(1)).put(any(), any());
    }
    
    @Test
    public void checkStaleDataRequestIncreasesId() throws BreadException {
        //Given
        String ingredients = "My Ingredients";
        
        when(clock.getTimeInMillis()).thenReturn(0L);
        when(oven.cook(any(BreadSlice.class), eq(ingredients)))
                .thenReturn("firstBake")
                .thenReturn("secondBake");
        
        Bakery bakery = createBakery();
        int initialBakeryValue = bakery.getNextId();
        
        //When
        bakery.getData(ingredients);
        when(clock.getTimeInMillis()).thenReturn(STALE_TIME + 1); //first request is now stale
        bakery.getData(ingredients);
        
        //Then
        assertEquals("Expected the initial baked id to have gone up by two", initialBakeryValue + 2, bakery.getNextId());
    }
    
    @Test
    public void checkThatMoudlyDataIsRemoved() throws BreadException {
        //Given                
        Bakery bakery = createBakery();
        
        BreadSlice oldSlice = mock(BreadSlice.class);
        when(oldSlice.getMixName()).thenReturn("old key");
        
        //When
        when(clock.getTimeInMillis()).thenReturn(7000L);
        when(breadBin.removeMouldy(7000L)).thenReturn(Arrays.asList(oldSlice));
        bakery.getData("Some data");
        
        //Then
        verify(cache, times(1)).remove("old key");
        verify(oldSlice, times(1)).markAsMouldy();
    }
    
    @Test
    public void checkThatMoudlyDataIsRemovedOnStartup() {
        //Given
        BreadSlice oldSlice = mock(BreadSlice.class);
        when(clock.getTimeInMillis()).thenReturn(7000L);
        when(oldSlice.getMixName()).thenReturn("old key");
        when(breadBin.removeMouldy(7000L)).thenReturn(Arrays.asList(oldSlice));
        
        //When
        createBakery();
        
        //Then
        verify(cache, times(1)).remove("old key");
        verify(oldSlice, times(1)).markAsMouldy();
    }
    
    @Test
    public void checkThatExistingDataInTheOvenIsAdded() {
        //Given
        BreadSlice existingSlice = mock(BreadSlice.class);
        when(oven.reload(clock, workSurface, bin, STALE_TIME)).thenReturn(Arrays.asList(existingSlice));
        
        //When
        createBakery();
        
        //Then
        verify(cache, times(1)).put(existingSlice.getMixName(), existingSlice);
        verify(breadBin, times(1)).add(existingSlice);
    }
    
    @Test
    public void checkThatASuccessfulBackgroundBakeReplacesStaleData() throws BreadException, InterruptedException {
        //Given
        String ingredients = "My Ingredients";
        when(clock.getTimeInMillis()).thenReturn(0L);
        when(oven.cook(any(BreadSlice.class), eq(ingredients)))
                .thenReturn("firstBake")
                .thenReturn("secondBake");
        
        Bakery bakery = createBakery();
        
        //When
        bakery.getData(ingredients);
        when(clock.getTimeInMillis()).thenReturn(STALE_TIME + 1);
        bakery.getData(ingredients);

        breadOvens.shutdown();
        breadOvens.awaitTermination(1, TimeUnit.SECONDS); //plenty of time for background baking to finish
        
        //Then
        verify(breadBin, times(2)).add(any(BreadSlice.class)); //Two slices have been addded
        verify(breadBin, times(1)).remove(any(BreadSlice.class)); //Old slice removed
        verify(cache, times(2)).put(any(String.class), any(BreadSlice.class)); //cache has been updated
        verify(bakingCache, times(1)).remove(any(String.class)); //Background baking cleaned up
    }
    
    @Test
    public void checkThatAFailedBackgroundBakeDoesNotReplaceStaleData() throws BreadException, InterruptedException {
        //Given
        String ingredients = "My Ingredients";
        when(clock.getTimeInMillis()).thenReturn(0L);
        when(oven.cook(any(BreadSlice.class), eq(ingredients)))
                .thenReturn("firstBake")
                .thenThrow(new BreadException("Forcing bake failure"));
        
        Bakery bakery = createBakery();
        
        //When
        bakery.getData(ingredients);
        when(clock.getTimeInMillis()).thenReturn(STALE_TIME + 1);
        bakery.getData(ingredients);

        breadOvens.shutdown();
        breadOvens.awaitTermination(1, TimeUnit.SECONDS); //plenty of time for background baking to finish
        
        //Then
        verify(breadBin, times(1)).add(any(BreadSlice.class));                  //Only the first slice was added to the bread bin
        verify(breadBin, never()).remove(any(BreadSlice.class));                //Nothing was taken out
        verify(cache, times(1)).put(any(String.class), any(BreadSlice.class));  //Only one element was put into the cache
        verify(bakingCache, times(1)).remove(any(String.class));                //The Baking cache was cleaned up
    }
    
    @Test
    public void checkTheBakingCacheIsUsedIfNoElementCurrentlyExists() throws BreadException {
        //Given
        String ingredients = "My Ingredients";
        String ingredientsSha1 = "67c06865eafa56e744142175c8a19104a79e0ff0";
        Bakery bakery = createBakery();
        BreadSlice slice = new BreadSlice("PreBakedValue", 0, 0, ingredientsSha1, STALE_TIME, clock, workSurface, bin);
        bakingCache.put(ingredientsSha1, slice);
        
        //When
        Object bakeryData = bakery.getData(ingredients);
        
        //Then
        assertEquals("Expected to get the pre baked value out the bakery", "PreBakedValue", bakeryData);
    }
    
    @Test
    public void checkThatBakeryReturnsSha1AsMixName() {
        //Given
        String ingredients = "My Ingredients";
        Bakery bakery = createBakery();
        
        //When
        String mixName = bakery.getMixName(ingredients);
        
        //Then
        assertEquals("Expected the sha1 hash for the mixName", "67c06865eafa56e744142175c8a19104a79e0ff0", mixName);
    }
    
    @Test
    public void checkThatBakeryCanHandle1000SimultaneousRequests() throws InterruptedException, ExecutionException, BreadException {
        //Given
        ExecutorService executor = Executors.newCachedThreadPool();
        String ingredients = "data";
        String bakedData = "my baked data";
        Bakery bakery = createBakery();
        when(oven.cook(any(BreadSlice.class), eq(ingredients))).thenReturn(bakedData);
        
        //When
        List<Future> executed = new ArrayList<>();
        for(int i=0; i<1000; i++) {
            executed.add(executor.submit(new BreadSliceRequestThread(bakery, ingredients)));
        }
        
        //Then
        for(Future request: executed) {
            assertEquals("Expected the correct output", bakedData, request.get());
        }
        
        verify(oven, times(1)).cook(any(BreadSlice.class), eq(ingredients));
    }
    
    @Test
    public void checkThatCookedDataDoesNotGetReturnedFromAnotherQuery() throws InterruptedException, ExecutionException, BreadException {
        //Given
        ExecutorService executor = Executors.newCachedThreadPool();
        String ingredients1 = "data";
        String ingredients2 = "other data";
        String bakedData1 = "my baked data";
        String bakedData2 = "other datas baked data";
        
        Bakery bakery = createBakery();
        when(oven.cook(any(BreadSlice.class), eq(ingredients1))).thenReturn(bakedData1);
        when(oven.cook(any(BreadSlice.class), eq(ingredients2))).thenReturn(bakedData2);
        
        //When
        List<Future> executed1 = new ArrayList<>();
        List<Future> executed2 = new ArrayList<>();
        for(int i=0; i<1000; i++) {
            executed1.add(executor.submit(new BreadSliceRequestThread(bakery, ingredients1)));
            executed2.add(executor.submit(new BreadSliceRequestThread(bakery, ingredients2)));
        }
        
        //Then
        for(Future request: executed1) {
            assertEquals("Expected the correct output", bakedData1, request.get());
        }
        
        for(Future request: executed2) {
            assertEquals("Expected the correct output", bakedData2, request.get());    
        }
        
        verify(oven, times(1)).cook(any(BreadSlice.class), eq(ingredients1));
        verify(oven, times(1)).cook(any(BreadSlice.class), eq(ingredients2));
    }
    
    @AllArgsConstructor
    public static class BreadSliceRequestThread<T, I> implements Callable {
        private Bakery<T, I, ?> bakery;
        private I data;
        
        @Override
        public T call() throws BreadException {
            try {
                return bakery.getData(data);
            }
            finally {
                BreadSlice.finishedEating();
            }
        }
    }
    
    private Bakery createBakery() {
        return new Bakery(workSurface, climate, breadBin, bin, oven, clock, STALE_TIME, MOULDY_TIME, cache, bakingCache, breadOvens);
    }
}
