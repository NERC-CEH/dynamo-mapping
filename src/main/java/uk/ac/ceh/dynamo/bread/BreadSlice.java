package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;

/**
 *
 * @author Christopher Johnson
 */
public class BreadSlice {
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
        latch.countDown();
        this.location = location;
        this.bakedTime = bakedTime;
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
        new Thread(new UsageTracker(Thread.currentThread())).start();
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
        useCounter.decrementAndGet();
        markAsRotten();
    }
    
    @AllArgsConstructor
    private class UsageTracker implements Runnable {
        private Thread thread;
        
        @Override
        public void run() {
            try {
                thread.join();
            }
            catch (InterruptedException ex) {
            }
            useCounter.decrementAndGet();
            submitForDeletionIfReady();
        }
    }
}
