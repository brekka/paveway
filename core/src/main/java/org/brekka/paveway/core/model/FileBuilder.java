/**
 * 
 */
package org.brekka.paveway.core.model;

import java.io.OutputStream;


/**
 * @author Andrew Taylor
 *
 */
public interface FileBuilder {

    
    
    
    /**
     * The thing to write the bytes to
     * @return
     */
    PartAllocator allocatePart(OutputStream os);

    void setLength(long length);
    
    /**
     * @return
     */
    boolean isComplete();

}
