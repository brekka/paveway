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
     * Retrieve the file name.
     * @return
     */
    String getFileName();
    
    String getMimeType();
    
    /**
     * The thing to write the bytes to
     * @return
     */
    PartAllocator allocatePart(OutputStream os);
}
