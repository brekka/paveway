/**
 * 
 */
package org.brekka.paveway.core.model;

import java.io.File;
import java.io.OutputStream;

/**
 * @author Andrew Taylor
 *
 */
public interface PartAllocator {
    /**
     * The thing to write the bytes to
     * @return
     */
    OutputStream allocate(OutputStream os);
    
    void setOffset(long offset);
    
    /**
     * The part length (of the unencrypted). This is a count of the actual bytes received
     * @return
     */
    long getLength();
    
    void setBackingFile(File backingFile);
    
    /**
     * Complete the allocation
     */
    void complete();
}
