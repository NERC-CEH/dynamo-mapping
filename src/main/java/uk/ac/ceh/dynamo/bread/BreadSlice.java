package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Christopher Johnson
 */
public class BreadSlice {
    private static final ThreadLocal<LinkedList<BreadSlice>> slicesUsedInThread = new ThreadLocal<LinkedList<BreadSlice>>() {
        @Override
        protected LinkedList<BreadSlice> initialValue() {
            return new LinkedList<>();
        }
    };
    
    private final Object lock = new Object();
    
    private BreadException exception;
    private final int id;
    private String queryHash;
    private AtomicInteger useCounter;
    private long bakedTime;
    private CountDownLatch latch;
    private File location;
    private boolean isRotten;
    private final Baker baker;
    
    public BreadSlice(Baker baker, int id, String queryHash) {
        this.baker = baker;
        this.id = id;
        this.queryHash = queryHash;
        this.useCounter = new AtomicInteger(0);
        this.latch = new CountDownLatch(1);
        this.isRotten = false;
    }
    
    public String getHash() {
        return queryHash;
    }
    
    public long getTimeBaked() {
        return bakedTime;
    }
    
    public boolean isBaked() {
        return location != null;
    }
    
    public int getId() {
        return id;
    }
    
    public void setFileLocation(File location, long bakedTime) {
        this.location = location;
        this.bakedTime = bakedTime;
        latch.countDown();
    }
    
    public File getFileLocation() throws BreadException {
        try {
            latch.await(); //block until the latch is down to zero
            if(exception != null) {
                throw exception;
            }
            else {
                return location;
            }
        }
        catch(InterruptedException ie) {
            throw new BreadException("Interrupted whilst waiting", ie);
        }
    }
    
    public void registerUsage() {
        useCounter.getAndIncrement();
        slicesUsedInThread.get().add(this); //register this breadslice to the thread
    }
    
    public static void finishedWithBreadSlices() {
        for(BreadSlice slice : slicesUsedInThread.get()) {
            slice.useCounter.decrementAndGet();
            slice.submitForDeletionIfReady();
        }
        slicesUsedInThread.set(new LinkedList<BreadSlice>());
    }
    
    public void markAsRotten() {
        isRotten = true;
        submitForDeletionIfReady();
    }
    
    private void submitForDeletionIfReady() {
        synchronized(lock) {
            if(isRotten && useCounter.get() == 0) {
                baker.submitForDeletion(this);
            }
        }
    }

    public void setException(BreadException ex) {
        this.exception = ex;
        markAsRotten();
        latch.countDown();
    }
}
