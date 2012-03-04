/**
 * 
 */
package org.brekka.paveway.core.model;

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
    
    void setLength(long length);
    
    /**
     * Complete the allocation
     */
    void complete();
}
