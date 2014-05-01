package uk.ac.ceh.dynamo.bread;

import java.util.ArrayDeque;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Christopher Johnson
 */
public class BreadBinTest {
    private BreadBin breadBin;
    private ArrayDeque<BreadSlice> backingList;
    
    @Before
    public void createArrayBackedBreadBin() {
        this.backingList = new ArrayDeque<>();
        this.breadBin = new BreadBin(backingList);
    }
    
    @Test
    public void checkThatBreadSliceGetsAddedToList() {
        //Given       
        BreadSlice slice = mock(BreadSlice.class);
        when(slice.isBaked()).thenReturn(true);
        
        //When
        breadBin.add(slice);
        
        //Then
        assertTrue("Expected one element in list", backingList.size() == 1);
    }
    
    @Test
    public void checkThatBreadSliceCanBeRemoved() {
        //Given       
        BreadSlice slice = mock(BreadSlice.class);
        when(slice.isBaked()).thenReturn(true);
        breadBin.add(slice);
        
        //When
        breadBin.remove(slice);
        
        //Then
        assertTrue("Expected no element in list", backingList.isEmpty());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void checkThatCantBeAddedOutOfOrder() {
        //Given
        BreadSlice oldSlice = mock(BreadSlice.class);
        when(oldSlice.getTimeBaked()).thenReturn(1000L);
        when(oldSlice.isBaked()).thenReturn(true);
        
        BreadSlice newSlice = mock(BreadSlice.class);
        when(newSlice.getTimeBaked()).thenReturn(2000L);
        when(newSlice.isBaked()).thenReturn(true);
        
        //When
        breadBin.add(newSlice);
        breadBin.add(oldSlice);
        
        //Then
        fail("Expected to fail with illegal argument exception");
    }
    
    @Test
    public void checkThatCanBeAddedInOrder() {
        //Given
        BreadSlice oldSlice = mock(BreadSlice.class);
        when(oldSlice.getTimeBaked()).thenReturn(1000L);
        when(oldSlice.isBaked()).thenReturn(true);
        
        BreadSlice newSlice = mock(BreadSlice.class);
        when(newSlice.getTimeBaked()).thenReturn(2000L);
        when(newSlice.isBaked()).thenReturn(true);
        
        //When
        breadBin.add(oldSlice);
        breadBin.add(newSlice);
        
        //Then
        assertSame("Expected two elements in queue", 2, backingList.size());
    }
    
    @Test
    public void checkThatMouldyBreadSlicesCanBeRemoved() {
        //Given
        BreadSlice oldSlice = mock(BreadSlice.class);
        when(oldSlice.getTimeBaked()).thenReturn(1000L);
        when(oldSlice.isBaked()).thenReturn(true);
        
        BreadSlice newSlice = mock(BreadSlice.class);
        when(newSlice.getTimeBaked()).thenReturn(2000L);
        when(newSlice.isBaked()).thenReturn(true);
        
        breadBin.add(oldSlice);
        breadBin.add(newSlice);
        
        //When        
        List mouldy = breadBin.removeMouldy(1500L);
        
        //Then
        assertSame("Expected only one removed element", 1, mouldy.size());
        assertSame("Expected old slice to be removed", mouldy.get(0), oldSlice);
        
        
        assertSame("Expected one element left in queue", 1, backingList.size());
        assertSame("Expected old slice to be removed", backingList.getFirst(), newSlice);
    }
    
    @Test
    public void checkThatWeCanRemoveMoudlyWhenBinIsEmpty() {
        //Given
        //Nothing
        
        //When
        List mouldy = breadBin.removeMouldy(1500L);
        
        //Then
        assertTrue("Expected empty mouldy list", mouldy.isEmpty());
    }
    
    @Test
    public void checkThatAllBreadSlicesAreRemovedWhenAllAreMouldy() {
        //Given
        BreadSlice oldSlice = mock(BreadSlice.class);
        when(oldSlice.getTimeBaked()).thenReturn(1000L);
        when(oldSlice.isBaked()).thenReturn(true);
        
        BreadSlice newSlice = mock(BreadSlice.class);
        when(newSlice.getTimeBaked()).thenReturn(2000L);
        when(newSlice.isBaked()).thenReturn(true);
        
        breadBin.add(oldSlice);
        breadBin.add(newSlice);
        
        //When
        List mouldy = breadBin.removeMouldy(3000L);
        
        //Then
        assertSame("Expected two elements in mouldy list", 2, mouldy.size());
        assertTrue("Expected nothing in deque", backingList.isEmpty());
    }
}
