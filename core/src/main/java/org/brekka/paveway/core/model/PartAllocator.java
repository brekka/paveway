/**
 * 
 */
package org.brekka.paveway.core.model;

import java.io.IOException;
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
    OutputStream getOutputStream() throws IOException;
    
    /**
     * The part length (of the unencrypted). This is a count of the actual bytes received
     * @return
     */
    long getLength();
    
    
    /**
     * Complete the allocation
     */
    void complete(long offset);
}
