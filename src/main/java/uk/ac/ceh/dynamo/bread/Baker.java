package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Hex;

/**
 *
 * @author Christopher Johnson
 */
public class Baker {
    private final Object lock = new Object();
    
    private final ShapefileGenerator generator;
    private final ExecutorService breadOvens, fileRemoverExecutor;
    private final BreadBin breadBin;
    private final Map<String, BreadSlice> cache;
    private final Map<String, BreadSlice> bakingCache;
    private final Climate climate;
    
    private final File scratchPad;
    private int breadSliceId;
    private final long staleTime, rottenTime;
    
    public Baker(Climate climate, File scratchPad, BreadBin breadBin, ShapefileGenerator generator, long staleTime, long rottenTime) {
        this.scratchPad = scratchPad;
        this.breadBin = breadBin;
        this.generator = generator;
        this.breadSliceId = 0;
        this.cache = new HashMap<>();
        this.bakingCache = new HashMap<>();
        this.staleTime = staleTime;
        this.rottenTime = rottenTime;
        this.climate = climate;
        
        this.breadOvens = Executors.newCachedThreadPool();
        this.fileRemoverExecutor = Executors.newSingleThreadExecutor();
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
            slice = cache.get(hash);
            if(!cache.containsKey(hash) && !bakingCache.containsKey(hash)) {
                slice = new BreadSlice(this, breadSliceId++, hash);
                cache.put(hash, slice);
                breadBin.addBreadSlice(slice);
                bake = true; // Bake the new slice outside of the sync block.
            }
            else if( cache.containsKey(hash) && isStale(slice) && !bakingCache.containsKey(hash)) {
                //The given slice is stale, but not rotten.
                BreadSlice staleReplacement = new BreadSlice(this, breadSliceId++, hash);
                bakingCache.put(hash, slice);
                
                breadOvens.submit(new BreadOven(staleReplacement, sqlQuery));
            }
            else {
                // The given slice is not in the main cache, but it is being 
                // populated in a BreadOven. Lets wait upon that.
                slice = bakingCache.get(hash);
            }
            
            slice.registerUsage(); //Register that a thread is using this bread slice
        }
        
        if(bake) {
            bake(slice, sqlQuery);
        }
        return slice.getFileLocation().getAbsolutePath();
    }
    
    private void bake(BreadSlice slice, String sql) throws BreadException {
        try {
            File desiredFile = new File(scratchPad, slice.getId() + " " + slice.getHash() + ".shp");
            File shapefile = generator.getShapefile(desiredFile, sql);
            slice.setFileLocation(shapefile, Calendar.getInstance().getTimeInMillis());
        }
        catch(BreadException ex) {
            slice.setException(ex);
            throw ex;
        }
    }
    
    public boolean isStale(BreadSlice slice) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        return slice.isBaked() && (slice.getTimeBaked() + staleTime) > currentTime;
    }
    
    @AllArgsConstructor
    private class BreadOven implements Runnable {
        private BreadSlice slice;
        private String sql;
        
        @Override
        public void run() {
            try {
                bake(slice, sql);

                synchronized(lock) {
                    bakingCache.remove(slice.getHash()); //remove from the baking list
                    cache.put(slice.getHash(), slice);   //add to the real cache
                    breadBin.addBreadSlice(slice);
                }
            }
            catch(BreadException ex) {
                synchronized(lock) {
                    bakingCache.remove(slice.getHash()); //remove from the baking list
                }
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
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long earliestBakeTime = currentTime - (long)(rottenTime * climate.getCurrentClimate());
        for(BreadSlice slice: breadBin.removeRottenBreadSlices(earliestBakeTime)) {
            slice.markAsRotten();
            cache.remove(slice.getHash());
        }
    }
    
    public void submitForDeletion(final BreadSlice slice) {
        fileRemoverExecutor.submit(new Runnable() {
            @Override
            public void run() {
                new File(scratchPad, slice.getId() + " " + slice.getHash() + ".shp").delete();
                new File(scratchPad, slice.getId() + " " + slice.getHash() + ".shx").delete();
                new File(scratchPad, slice.getId() + " " + slice.getHash() + ".dbf").delete();
            }
        });
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
