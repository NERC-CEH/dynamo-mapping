package uk.ac.ceh.dynamo.bread;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The following class represents a slice of bread. A slice of bread is something
 * which can be:
 *  - Freshly baked - just been generated
 *  - Gone stale    - can be eaten and enjoyed, but might not be as fresh as desired
 *  - Gone Mouldy   - Not fit for consumption. In this set up, we will just be waiting
 *                    for everyone to finish dining. Then we will throw it away.
 * 
 * Going stale is just a matter of time, amazingly whether or not a slice of bread
 * goes stale is just dependent on if a fixed amount of time has passed.
 * 
 * However a slice of bread can go mouldy in a variable amount of time based upon
 * a given climate. It is the role of the baker to find mouldy slices of bread
 * @see Baker#cleanOutBreadBin() 
 * @author Christopher Johnson
 */
public class BreadSlice<T, W> implements Comparable<BreadSlice<T, W>> {
    private static final ThreadLocal<LinkedList<BreadSlice>> SLICES_USED_BY_THREAD = new ThreadLocal<LinkedList<BreadSlice>>() {
        @Override
        protected LinkedList<BreadSlice> initialValue() {
            return new LinkedList<>(); //initial empty list
        }
    };
    
    private final int id;
    private final String queryHash;
    private final AtomicInteger useCounter;
    private long bakedTime;
    private final CountDownLatch latch;
    private final DustBin<T,W> dustBin;
    private final W workSurface;
    private final long staleTime;
    private final Clock clock;
    private final Object lock = new Object();
    
    private T baked;
    private BreadException exception;
    private boolean isRotten;
    
    /**
     * The Bread Slice constructor for creating a bread slice which is not yet baked
     * 
     * Use the #setBakedFile() for setting the baked file afterwards
     * @param id This breadslices unique id
     * @param queryHash The sha1 hash of the query which this bread slice will represent
     * @param staleTime The time it takes (after baking) for this bread slice to become stale
     * @param clock a clock for measuring time
     * @param remover the shapefile remover for deleteing the full set of shapefile parts
     */
    public BreadSlice(int id, String queryHash, long staleTime, Clock clock, W workSurface, DustBin<T, W> dustBin) {
        this.id = id;
        this.queryHash = queryHash;
        this.dustBin = dustBin;
        this.staleTime = staleTime;
        this.clock = clock;
        this.workSurface = workSurface;
        this.isRotten = false;
        this.useCounter = new AtomicInteger(0);
        this.latch = new CountDownLatch(1);
    }
    
    /**
     * Constructor for building a bread slice off of a pre baked file
     * @param preBakedFile The pre baked file, the id and hash key will be obtained from its name
     * @param staleTime The time it takes for this file to go stale
     * @param clock A clock to use for obtaining time
     * @param remover The remover to use for removing this BreadSlice when it is not needed
     */
    public BreadSlice(T preBaked, long bakedTime, int id, String queryHash, long staleTime, Clock clock, W workSurface, DustBin<T, W> dustBin) {
        this.id = id;
        this.queryHash = queryHash;
        this.dustBin = dustBin;
        this.staleTime = staleTime;
        this.clock = clock;
        this.workSurface = workSurface;
        this.isRotten = false;
        this.useCounter = new AtomicInteger(0);
        this.latch = new CountDownLatch(0); //already generated shapefile, no need to wait
        
        //Set location and usage time
        this.baked = preBaked;
        this.bakedTime = bakedTime;
    }
    
    /**
     * @return the sha1 hash of the query which this bread slice represents
     */
    public String getHash() {
        return queryHash;
    }
    
    /**
     * @return the time in milliseconds when this bread slice was baked
     */
    public long getTimeBaked() {
        return bakedTime;
    }
    
    /**
     * @return if this breadslice is already baked
     */
    public boolean isBaked() {
        return baked != null;
    }
    
    /**
     * @return the unique id (within a given baker) for this bread slice
     */
    public int getId() {
        return id;
    }
    
    /**
     * Get the worksurface this breadslice was baked on
     * @return 
     */
    public W getWorkSurface() {
        return workSurface;
    }
    
    /**
     * @return if this bread slice is stale or not
     */
    public boolean isStale() {
        if(isBaked()) {
            return (getTimeBaked() + staleTime) < clock.getTimeInMillis();
        }
        return false;
    }
    
    /**
     * Obtain this location of the baked file which represents this bread slice.
     * This method may return immediately if the bread slice has already been
     * populated. Or it will wait until it has been resolved.
     * 
     * Resolution can be done using either:
     *  #setBakedFile()
     *  #setException()
     * 
     * Or if this bread slice is populated with a pregenerated baked file
     * 
     * @return The file set using the setBaked file method
     * @throws BreadException if setException has been called
     */
    public T getBaked() throws BreadException {
        try {
            latch.await(); //block until the latch is down to zero
            if(exception != null) {
                throw exception;
            }
            else {
                return baked;
            }
        }
        catch(InterruptedException ie) {
            throw new BreadException("Interrupted whilst waiting", ie);
        }
    }
    
    /**
     * Threads should call this method to note that they are eating this 
     * bread slice. We don't want to we don't want to throw bread slices away
     * whilst threads are eating them.
     * 
     * The baker will call this method on a threads behalf.
     */
    public void startEating() {
        useCounter.getAndIncrement();
        SLICES_USED_BY_THREAD.get().add(this); //register this breadslice to the thread
    }
    
    /**
     * A thread should call this method to state that it has finished eating the
     * bread slices is was chomping on. 
     * 
     * This is called by dynamo maps on behalf of the thread
     * @see uk.ac.ceh.dynamo.MapServerView
     */
    public static void finishedEating() {
        for(BreadSlice slice : SLICES_USED_BY_THREAD.get()) {
            slice.useCounter.decrementAndGet();
            slice.submitForDeletionIfReady();
        }
        SLICES_USED_BY_THREAD.set(new LinkedList<BreadSlice>());
    }
    
    /**
     * Flag that this bread is now mouldy. We don't want to send out mouldy bits
     * of bread to anyone who request. If a thread has started eating a bread slice
     * and it becomes mouldy we will wait until that thread is done before throwing
     * the slice of bread away.
     */
    public void markAsMouldy() {
        isRotten = true;
        submitForDeletionIfReady();
    }
    
    
    private void submitForDeletionIfReady() {
        synchronized(lock) {
            if(isRotten && useCounter.get() == 0) {
                dustBin.delete(this);
            }
        }
    }

    /**
     * An attempt to bake this slice of bread resulted in an exception, we can 
     * store this exception here so that any thread which is waiting on the
     * resolution of this breadslice will also be able to report this exception.
     * @param ex The exception which was thrown when trying to bake this bread
     */
    public void setException(BreadException ex) {
        this.exception = ex; //Store the exception
        markAsMouldy();      //and mark the bread slice as rotten.
        latch.countDown();   //Then we can allow calls to getFileLocation()
    }
    
    /**
     * Sets the baked file location, this is the reference to the file which is
     * fully populated and is ready to be sent to map server for rendering
     * @param output the pre baked content
     */
    public void setBaked(T output) {
        this.baked = output;
        this.bakedTime = clock.getTimeInMillis();
        latch.countDown();
    }

    @Override
    public int compareTo(BreadSlice<T, W> o) {
        long difference = bakedTime - o.bakedTime;
        if     (difference < 0) return -1;
        else if(difference > 0) return 1;
        else                    return 0;
    }
}
