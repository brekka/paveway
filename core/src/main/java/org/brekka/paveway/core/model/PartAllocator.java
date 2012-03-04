/**
 * 
 */
package org.brekka.paveway.core.model;

import java.io.File;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;

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
    
    /**
     * The part length (of the unencrypted). This is a count of the actual bytes received
     * @return
     */
    long getLength();
    
    /**
     * Complete the allocation
     */
    void complete(FileItem fileItem, long offset);
}
