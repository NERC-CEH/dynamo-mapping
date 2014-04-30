package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Hex;

/**
 * The following is an instance of a baker. A baker bake bread slices and puts 
 * them into a bread bin.
 * 
 * Fundamentally a baker is a double buffered cache for shapefiles from sql 
 * statements, it is thread safe and intended to be used in a dynamo mapping 
 * web application.
 * 
 * Once constructed calls to getData will return the location of a shapefile on 
 * disk for a supplied sql statement. Subsequent sql statements will yield the 
 * same shape file instantly as long as the staleTime has not surpassed for that
 * sql statement (represented as a bread slice) and as long as the climate does
 * make that slice of bread mouldy.
 * 
 * The baker will manage the removal of mouldy shapefiles.
 * @author Christopher Johnson
 */
public class Baker {
    private final Object lock = new Object();
    
    private final ShapefileGenerator generator;
    private final ExecutorService breadOvens;
    private final BreadBin breadBin;
    private final Map<String, BreadSlice> cache;
    private final Map<String, BreadSlice> bakingCache;
    private final Climate climate;
    private final Clock clock;
    private final File scratchPad;
    private final long staleTime, bestBeforeTime;
    private final ShapefileRemover remover;
    
    private int breadSliceId;
    
    /**
     * Constructs a Baker with a default LinkedListBreadBin, ShapefileRemover
     * and a system clock
     */
    public Baker(Climate climate, File scratchPad, ShapefileGenerator generator, long staleTime, long rottenTime) {
        this(climate, scratchPad, new LinkedListBreadBin(), generator, new ShapefileRemover(scratchPad), new SystemClock(), staleTime, rottenTime);
    }
    
    /**
     * Construct a Baker in a given scrachpad. If the scratch pad already contains
     * shapefiles, these will be processed in to bread slices and then be hosted
     * as long as they are not stale or mouldy.
     * 
     * @param climate the climate which dictates if a bread slice is mouldy
     * @param scratchPad the location to store shapefiles to
     * @param breadBin the breadBin implementation will keeps track of breadslices 
     *  which are in action
     * @param generator the shape file generator which creates shape files
     * @param remover the shape file deleter which removes shapefiles under the
     *  bakers instruction
     * @param clock a clock representing the current time
     * @param staleTime the first time in which a bread slice will become stale
     *  after it has been baked
     * @param bestBeforeTime the time which when multipled by the current climate
     *  dictates if a bread slice is mouldy
     */
    public Baker(Climate climate, File scratchPad, BreadBin breadBin, ShapefileGenerator generator, ShapefileRemover remover, Clock clock, long staleTime, long bestBeforeTime) {
        this.remover = remover;
        this.scratchPad = scratchPad;
        this.breadBin = breadBin;
        this.generator = generator;
        this.breadSliceId = 0;
        this.cache = new HashMap<>();
        this.bakingCache = new HashMap<>();
        this.clock = clock;
        this.staleTime = staleTime;
        this.bestBeforeTime = bestBeforeTime;
        this.climate = climate;
        
        this.breadOvens = Executors.newCachedThreadPool();
        
        //The scratch pad may contain existing cache files, we can bring this 
        //baker back into action based upon the data there.
        File[] shapefiles = scratchPad.listFiles(new ShapefileFilter());
        Arrays.sort(shapefiles, new FileLastModifiedComparator());
        for(File shapefile: shapefiles) {
            BreadSlice slice = new BreadSlice(shapefile, staleTime, clock, remover);
            putInBreadBin(slice);
            breadSliceId = slice.getId() + 1;
        }
        cleanOutBreadBin(); //The baker may have been stoped for some time, clean out preemptively
    }
        
    /**
     * Provide an sql query to the breadbin, if a bread slice exists in this bin
     * which has not gone rotten then we can return straight away. Otherwise we 
     * will block until a one has been created.
     * @param sqlQuery to query against the shapefile generator
     * @return The location to the shapefile stored on disk
     */
    public String getData(String sqlQuery) throws BreadException {
        String hash = getSha1(sqlQuery); //get the hash of the query
        BreadSlice slice;
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
                    BreadSlice staleReplacement = new BreadSlice(breadSliceId++, hash, staleTime, clock, remover);
                    bakingCache.put(hash, staleReplacement);
                    breadOvens.submit(new BreadOven(staleReplacement, sqlQuery));
                }
            }
            else if(bakingCache.containsKey(hash)) {
                //The given slice is not in the main cache, but it is being
                //populated in a BreadOven. Lets wait upon that.
                slice = bakingCache.get(hash);
            }
            else { //Neither the main cache or the baking cache contain a matching slice of bread
                slice = new BreadSlice(breadSliceId++, hash, staleTime, clock, remover);
                putInBreadBin(slice);
                bake = true; // Bake the new slice outside of the sync block.
            }
            
            slice.startEating(); //Register that a thread is using this bread slice
        }
        
        if(bake) {
            new BreadOven(slice, sqlQuery).bake(); //Bake synchronously
        }
        return slice.getBakedFile().getAbsolutePath();
    }
    
    /**
     * Pops a bread slice into the bread bin, this baker will also keep track of 
     * it, handing it out to threads for eating as long as has not gone mouldy
     * @param slice 
     */
    public final void putInBreadBin(BreadSlice slice) {
        synchronized (lock) {
            BreadSlice oldBreadslice = cache.put(slice.getHash(), slice); //add to the real cache
            breadBin.add(slice);
            if (oldBreadslice != null) {
                breadBin.remove(oldBreadslice);
                oldBreadslice.markAsMouldy(); //bread is no use, delete at earliest convienience
            }
        }
    }
    
    @AllArgsConstructor
    private class BreadOven implements Runnable {
        private final BreadSlice slice;
        private final String sql;
        
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
                    bakingCache.remove(slice.getHash()); //remove from the baking list
                    if(slice.isBaked()) { //if this slice managed to bake, put in the bread bin
                        putInBreadBin(slice);
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
                File desiredFile = new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".shp");
                File shapefile = generator.getShapefile(desiredFile, sql);
                slice.setBakedFile(shapefile);
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
        long earliestBakeTime = clock.getTimeInMillis() - (long)(bestBeforeTime * climate.getCurrentClimate());
        for(BreadSlice slice: breadBin.removeMouldy(earliestBakeTime)) {
            slice.markAsMouldy();
            cache.remove(slice.getHash());
        }
    }
    
    private String getSha1(String query) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            return Hex.encodeHexString(messageDigest.digest(query.getBytes("utf8")));
        }
        catch(NoSuchAlgorithmException nsae) {
            throw new RuntimeException("Sha-1 is not present as a message digest");
        }
        catch(UnsupportedEncodingException uee) {
            throw new RuntimeException("UTF8 is not available as encodding format");
        }
    }
}
