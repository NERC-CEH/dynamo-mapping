package uk.ac.ceh.dynamo.bread;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Hex;

/**
 * The following is an instance of a bakery. Bakery's bake bread slices and put 
 * them into a bread bin.
 * 
 * Fundamentally a bakery is a double buffered cache for objects of type T from
 * a given string statement, it is thread safe and intended to be used in a dynamo 
 * mapping web application.
 * 
 * The baker will manage the removal of mouldy shapefiles.
 * @author Christopher Johnson
 */
public class Bakery<T, I, W> {
    private final Object lock = new Object();
    
    private final W workSurface;
    private final Oven<T, I, W> oven;
    private final DustBin<W> dustbin;
    private final ExecutorService breadOvens;
    private final BreadBin<T, W> breadBin;
    private final Map<String, BreadSlice<T, W>> cache;
    private final Map<String, BreadSlice<T, W>> bakingCache;
    private final ClimateMeter<T, I, W> climate;
    private final Clock clock;
    private final long staleTime, bestBeforeTime;
    
    private int breadSliceId;
    
    /**
     * Constructs a Baker with a default LinkedListBreadBin, ShapefileRemover
     * and a system clock
     */
    public Bakery(W workSurface, ClimateMeter<T,I,W> climate, DustBin<W> dustbin, Oven<T, I, W> oven, long staleTime, long rottenTime) {
        this(workSurface, climate, new BreadBin<T, W>(), dustbin, oven, new SystemClock(), staleTime, rottenTime);
    }
    
    /**
     * Construct a Bakery in a given scrachpad. If the scratch pad already contains
     * shapefiles, these will be processed in to bread slices and then be hosted
     * as long as they are not stale or mouldy.
     */
    public Bakery(W workSurface, ClimateMeter<T,I,W> climate, BreadBin<T, W> breadBin, DustBin<W> dustbin, Oven<T, I, W> oven, Clock clock, long staleTime, long bestBeforeTime) {
        this(workSurface, climate, breadBin, dustbin, oven, clock, staleTime, bestBeforeTime, new HashMap<String, BreadSlice<T,W>>(), new HashMap<String, BreadSlice<T,W>>(), Executors.newCachedThreadPool());
    }
    
    /**
     * Construct a Bakery in a given scrachpad. If the scratch pad already contains
     * shapefiles, these will be processed in to bread slices and then be hosted
     * as long as they are not stale or mouldy.
     * 
     * @param workSurface a surface which can be used by bakers as a temp location
     * @param climate the climate which dictates if a bread slice is mouldy
     * @param breadBin the breadBin implementation will keeps track of breadslices 
     *  which are in action
     * @param dustbin an implementation of a bin, dead slices will be thrown away
     *  if they are mouldy and not in use or replace by fresh slices
     * @param oven the oven logic which dictates how ingredients get baked
     * @param clock a clock representing the current time
     * @param staleTime the first time in which a bread slice will become stale
     *  after it has been baked
     * @param bestBeforeTime the time which when multipled by the current climate
     *  dictates if a bread slice is mouldy
     * @param cache An implementation of a Map which will be used for storing breadslices
     *  against there hash key
     * @param bakingCache An implementation of a Map which will be used for storing 
     *  the breadslices which are currently in a bread oven
     * @param breadOvens an executer which Baker instances will be submitted to
     */
    protected Bakery(W workSurface, ClimateMeter<T,I,W> climate, BreadBin<T, W> breadBin, DustBin<W> dustbin, Oven<T, I, W> oven, Clock clock, long staleTime, long bestBeforeTime, Map<String, BreadSlice<T,W>> cache, Map<String, BreadSlice<T,W>> bakingCache, ExecutorService breadOvens) {
        this.workSurface = workSurface;
        this.oven = oven;
        this.breadBin = breadBin;
        this.dustbin = dustbin;
        this.breadSliceId = 0;
        this.cache = cache;
        this.bakingCache = bakingCache;
        this.clock = clock;
        this.staleTime = staleTime;
        this.bestBeforeTime = bestBeforeTime;
        this.climate = climate;
        this.breadOvens = breadOvens;
        
         //The oven may contain existing built caches, we can bring this 
        //baker back into action based upon the data there.
        List<BreadSlice<T, W>> existingSlices = new ArrayList<>(oven.reload(clock, workSurface, dustbin, staleTime));
        Collections.sort(existingSlices); //Sort into order
        for(BreadSlice slice: existingSlices) {
            cache.put(slice.getMixName(), slice); //Put the slice into the cache
            breadBin.add(slice);               //and the bread bin
            breadSliceId = slice.getId() + 1;
        }
        cleanOutBreadBin(); //The baker may have been stoped for some time, clean out preemptively
    }
        
    /**
     * Provide ingredients to the breadbin and get an instance of T, if a bread 
     * slice exists in this bin which has not gone rotten then we can return 
     * straight away. Otherwise we will block until a one has been created.
     * @param ingredients to query against the oven
     * @return an instance of T generated by the oven
     */
    public T getData(I ingredients) throws BreadException {
        String hash = getMixName(ingredients); //get the hash of the query
        BreadSlice<T, W> slice;
        boolean bake = false; //assume that we don't need to bake
        
        synchronized (lock) {
            // Before we create any more breadslices, lets make sure that the bread
            // bin is as clean as possible
            cleanOutBreadBin();

            //If the given queryHash is not in our cache then create a new breadslice
            //unless a new slice is currently being baked. Then we can just wait on that            
            if(cache.containsKey(hash)) {
                slice = cache.get(hash);
                if(slice.isStale() && !bakingCache.containsKey(hash)) {
                    //The given slice is stale, but not rotten.
                    BreadSlice<T, W> staleReplacement = new BreadSlice<>(breadSliceId++, hash, staleTime, clock, workSurface, dustbin);
                    bakingCache.put(hash, staleReplacement);
                    breadOvens.submit(new Baker(staleReplacement, ingredients));
                }
            }
            else if(bakingCache.containsKey(hash)) {
                //The given slice is not in the main cache, but it is being
                //populated in a BreadOven. Lets wait upon that.
                slice = bakingCache.get(hash);
            }
            else { //Neither the main cache or the baking cache contain a matching slice of bread
                slice = new BreadSlice<>(breadSliceId++, hash, staleTime, clock, workSurface, dustbin);
                cache.put(slice.getMixName(), slice); //Put the slice into the cache
                bake = true; // Bake the new slice outside of the sync block.
            }
            
            slice.startEating(); //Register that a thread is using this bread slice
        }
        
        if(bake) {
            new Baker(slice, ingredients).bake(); //Bake synchronously
        }
        return slice.getBaked();
    }
    
    /**
     * Obtain the next id which will be assigned to a bread slice
     * @return the next id for a bread slice
     */
    public int getNextId() {
        return breadSliceId;
    }
    
    /**
     * Calculates the current amount of bread slices which are to be either 
     * baking or baked and have not been submitted for deletion.
     * @return the cache and baking cache sizes combined
     */
    public int getBreadSliceCount() {
        synchronized(lock) {
            return cache.size() + bakingCache.size();
        }
    }
    
    /**
     * Gets the current climate for this bakery given the specified
     * climate meter
     */
    public double getCurrentClimate() {
        return climate.getCurrentClimate(this);
    }
    
    /**
     * Returns the dustbin which is used by this bakery. This is used to throw 
     * away old slices of bread.
     * @return This bakery's dust bin
     */
    public DustBin<W> getDustbin() {
        return dustbin;
    }
    
    /**
     * Obtain the clock used by this bakery
     * @return the bakery's clock
     */
    public Clock getClock() {
        return clock;
    }
    
    /**
     * The work surface, this is a place which may be used by this bakery for making
     * bread slices.
     */
    public W getWorkSurface() {
        return workSurface;
    }
    
    @AllArgsConstructor
    protected class Baker implements Runnable {
        private final BreadSlice<T, W> slice;
        private final I ingredients;
        
        /**
         * A wrapper around the baking method for use in the background. Bread 
         * slices which have been submitted to the bakingCache (the double buffer)
         * can be put in a bread oven which is cooked in the background
         */
        @Override
        public void run() {
            try {
                bake();
            }
            catch(BreadException ex) {
                //Do nothing here. If an exception was thrown, the slice wont be 
                //baked. We can check this in the finally block
            }
            finally {
                synchronized(lock) {
                    bakingCache.remove(slice.getMixName()); //remove from the baking list
                    if(slice.isBaked()) { //if this slice managed to bake, put in to action
                        BreadSlice<T, W> oldBreadslice = cache.put(slice.getMixName(), slice); //add to the real cache
                        if (oldBreadslice != null) { //check if a slice was already in action. If it was, we can remove it
                            breadBin.remove(oldBreadslice);
                            oldBreadslice.markAsMouldy(); //bread is no use, delete at earliest convienience
                        }
                    }
                }
            }
        }
        
        /**
         * Use the shapefile generator to obtain a shapefile for the required 
         * breadslice. If this method succeeds without exception then slice.isBaked()
         * will return true.
         * @throws BreadException If the slice failed to bake
         */
        public void bake() throws BreadException {
            try {
                T cooked = oven.cook(slice, ingredients);
                synchronized (lock) {
                    slice.setBaked(cooked);
                    breadBin.add(slice); //once the slice has been baked, add to the bin
                }
            }
            catch(BreadException ex) {
                slice.setException(ex);
                throw ex;
            }
        }
    }
            
    /**
     * Use the bread bin to find the slices of bread which are rotten. We want to
     * remove these from our internal cache as we will not want to serve these up
     * again. Other threads may be using these slices so we can't always delete them
     * straight away. However we can flag these as rotten, which means we can
     * delete them when the usage hits zero
     */
    private void cleanOutBreadBin() {
        long earliestBakeTime = clock.getTimeInMillis() - (long)(bestBeforeTime * getCurrentClimate());
        for(BreadSlice slice: breadBin.removeMouldy(earliestBakeTime)) {
            slice.markAsMouldy();
            cache.remove(slice.getMixName());
        }
    }
    
    /**
     * This is the default method for obtaining a mix for some given ingredients.
     * It will call the objects toString method and then obtain a sha1 hash
     * @param ingredients The generically typed ingredients
     * @return The sha1 of the ingredients toString method
     */
    protected String getMixName(I ingredients) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            return Hex.encodeHexString(messageDigest.digest(ingredients.toString().getBytes("utf8")));
        }
        catch(NoSuchAlgorithmException nsae) {
            throw new RuntimeException("Sha-1 is not present as a message digest");
        }
        catch(UnsupportedEncodingException uee) {
            throw new RuntimeException("UTF8 is not available as encodding format");
        }
    }
}
