package uk.ac.ceh.dynamo.bread;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Christopher Johnson
 */
public class SystemClockTest {
    private SystemClock clock;
    
    @Before
    public void createSystemClock() {
        clock = new SystemClock();
    }
    
    @Test
    public void checkThatSystemTimeIsEarlierThanClock() throws InterruptedException {
        //Given
        long systemTime = System.currentTimeMillis();
        
        //When
        Thread.sleep(5);
        long clockTime = clock.getTimeInMillis();
        
        //Then
        assertTrue("Expected the system time to be behind the clock time", systemTime < clockTime);
    }
    
    @Test
    public void checkThatSystemTimeIsLaterThanClock() throws InterruptedException {
        //Given
        long clockTime = clock.getTimeInMillis();
        
        //When
        Thread.sleep(5);        
        long systemTime = System.currentTimeMillis();
        
        //Then
        assertTrue("Expected the system time to be ahead the clock time", systemTime > clockTime);
    }
}
