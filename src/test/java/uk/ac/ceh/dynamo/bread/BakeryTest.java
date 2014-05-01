package uk.ac.ceh.dynamo.bread;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Christopher Johnson
 */
public class BakeryTest {

    @Test
    public void checkCanGetDustBin() {
        //Given
        DustBin bin = mock(DustBin.class);
        Oven oven = mock(Oven.class);
        Climate climate = mock(Climate.class);
        Bakery bakery = new Bakery(null, climate, bin, oven, 100, 1000);
        
        //When
        DustBin obtainedBin = bakery.getDustbin();
        
        //Then
        assertSame("Expected to get the bin", bin, obtainedBin);
    }
    
    @Test
    public void checkCanGetClock() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin bin = mock(DustBin.class);
        Oven oven = mock(Oven.class);
        Climate climate = mock(Climate.class);
        BreadBin breadBin = mock(BreadBin.class);
        Bakery bakery = new Bakery(null, climate, breadBin, bin, oven, clock, 100, 1000);
        
        //When
        Clock obtainedClock = bakery.getClock();
        
        //Then
        assertSame("Expected to get the clock", clock, obtainedClock);
        
    }
    
    @Test
    public void checkCanGetWorkSurface() {
        //Given
        Object workSurface = new Object();
        Clock clock = mock(Clock.class);
        DustBin bin = mock(DustBin.class);
        Oven oven = mock(Oven.class);
        Climate climate = mock(Climate.class);
        BreadBin breadBin = mock(BreadBin.class);
        Bakery bakery = new Bakery(workSurface, climate, breadBin, bin, oven, clock, 100, 1000);

        //When
        Object obtainedWorkSurface = bakery.getWorkSurface();
        
        //Then
        assertSame("Expected to get the worksurface", workSurface, obtainedWorkSurface);
    }
    
    @Test
    public void checkThatBreadBinIsFilledWithExistingBreadSlices() {
        //Given
        Clock clock = mock(Clock.class);
        DustBin bin = mock(DustBin.class);
        Oven oven = mock(Oven.class);
        Climate climate = mock(Climate.class);
        BreadBin breadBin = mock(BreadBin.class);
        long staleTime = 100;
        BreadSlice slice = mock(BreadSlice.class);
        
        //When
        when(oven.reload(clock, null, bin, staleTime)).thenReturn(Arrays.asList(slice));
        Bakery bakery = new Bakery(null, climate, breadBin, bin, oven, clock, staleTime, 1000);

        //Then
        verify(breadBin, times(1)).add(slice);
    }
}
