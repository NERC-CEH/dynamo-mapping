package uk.ac.ceh.dynamo.bread;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
/**
 *
 * @author Christopher Johnson
 */
public class BreadSliceCountClimateMeterTest {
    @Mock Bakery bakery;
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void checkThatClimateReturns0WhenAtFullUsage() {
        //Given
        BreadSliceCountClimateMeter climate = new BreadSliceCountClimateMeter(5000);
        
        //When
        when(bakery.getBreadSliceCount()).thenReturn(5000);
        double climateVal = climate.getCurrentClimate(bakery);
        
        //Then
        assertEquals("Expected the poor climate", 0, climateVal, 0);
    }
    
    @Test
    public void checkThatClimateReturns0WhenBeyondFullUsage() {
        //Given
        BreadSliceCountClimateMeter climate = new BreadSliceCountClimateMeter(5000);
        
        //When
        when(bakery.getBreadSliceCount()).thenReturn(10000);
        double climateVal = climate.getCurrentClimate(bakery);
        
        //Then
        assertEquals("Expected the poor climate", 0, climateVal, 0);
    }
    
    @Test
    public void checkThatClimateReturns1WhenAtZeroUsage() {
        //Given
        BreadSliceCountClimateMeter climate = new BreadSliceCountClimateMeter(5000);
        
        //When
        when(bakery.getBreadSliceCount()).thenReturn(0);
        double climateVal = climate.getCurrentClimate(bakery);
        
        //Then
        assertEquals("Expected the poor climate", 1, climateVal, 0);
    }
}
