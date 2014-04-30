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
    private final Clock clock;
    private final File scratchPad;
    private final long staleTime, rottenTime;
    
    private int breadSliceId;
    
    public Baker(Climate climate, File scratchPad, ShapefileGenerator generator, long staleTime, long rottenTime) {
        this(climate, scratchPad, new LinkedListBreadBin(), generator, new SystemClock(), staleTime, rottenTime);
    }
    
    public Baker(Climate climate, File scratchPad, BreadBin breadBin, ShapefileGenerator generator, Clock clock, long staleTime, long rottenTime) {
        this.scratchPad = scratchPad;
        this.breadBin = breadBin;
        this.generator = generator;
        this.breadSliceId = 0;
        this.cache = new HashMap<>();
        this.bakingCache = new HashMap<>();
        this.clock = clock;
        this.staleTime = staleTime;
        this.rottenTime = rottenTime;
        this.climate = climate;
        
        this.breadOvens = Executors.newCachedThreadPool();
        this.fileRemoverExecutor = Executors.newSingleThreadExecutor();
        
        File[] shapefiles = scratchPad.listFiles(new ShapefileFilter());
        Arrays.sort(shapefiles, new FileLastModifiedComparator());
        for(File shapefile: shapefiles) {
            String shapefileName = shapefile.getName();
            String[] nameparts = shapefileName.substring(0, shapefileName.length()-4).split("_");
            this.breadSliceId = Integer.parseInt(nameparts[0]);
            
            BreadSlice slice = new BreadSlice(this, breadSliceId, nameparts[1]);
            slice.setFileLocation(shapefile, shapefile.lastModified());
            putInBreadBin(slice);
        }
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
                if(isStale(slice) && !bakingCache.containsKey(hash)) {
                    //The given slice is stale, but not rotten.
                    BreadSlice staleReplacement = new BreadSlice(this, breadSliceId++, hash);
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
                slice = new BreadSlice(this, breadSliceId++, hash);
                putInBreadBin(slice);
                bake = true; // Bake the new slice outside of the sync block.
            }
            
            slice.registerUsage(); //Register that a thread is using this bread slice
        }
        
        if(bake) {
            bake(slice, sqlQuery);
        }
        return slice.getFileLocation().getAbsolutePath();
    }
    
    private void putInBreadBin(BreadSlice slice) {
        cache.put(slice.getHash(), slice);
        breadBin.addBreadSlice(slice);
    }
    
    private void bake(BreadSlice slice, String sql) throws BreadException {
        try {
            File desiredFile = new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".shp");
            File shapefile = generator.getShapefile(desiredFile, sql);
            slice.setFileLocation(shapefile, clock.getTimeInMillis());
        }
        catch(BreadException ex) {
            slice.setException(ex);
            throw ex;
        }
    }
    
    public boolean isStale(BreadSlice slice) {
        if(slice.isBaked()) {
            return (slice.getTimeBaked() + staleTime) < clock.getTimeInMillis();
        }
        return false;
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
                    BreadSlice oldBreadslice = cache.put(slice.getHash(), slice); //add to the real cache
                    breadBin.addBreadSlice(slice);
                    if (oldBreadslice != null) {
                        oldBreadslice.markAsRotten(); //bread is no use, delete at earliest convienience
                    }
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
        long earliestBakeTime = clock.getTimeInMillis() - (long)(rottenTime * climate.getCurrentClimate());
        for(BreadSlice slice: breadBin.removeRottenBreadSlices(earliestBakeTime)) {
            slice.markAsRotten();
            cache.remove(slice.getHash());
        }
    }
    
    public void submitForDeletion(final BreadSlice slice) {
        fileRemoverExecutor.submit(new Runnable() {
            @Override
            public void run() {
                new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".shp").delete();
                new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".shx").delete();
                new File(scratchPad, slice.getId() + "_" + slice.getHash() + ".dbf").delete();
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
