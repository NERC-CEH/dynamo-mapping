package uk.ac.ceh.dynamo.bread;

import java.io.File;
import java.util.Comparator;

/**
 * A simple file comparator which will order files into the order which they
 * were last modified. With early modification being put first.
 * @author Christopher Johnson
 */
public class FileLastModifiedComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        long difference = o1.lastModified() - o2.lastModified();
        if     (difference < 0) return -1;
        else if(difference > 0) return 1;
        else                    return 0;
    }

}
