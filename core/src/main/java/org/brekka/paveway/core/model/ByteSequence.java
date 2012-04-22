/**
 * 
 */
package org.brekka.paveway.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author Andrew Taylor
 */
public interface ByteSequence {
    /**
     * Unique ID assigned to this sequence
     * @return
     */
    UUID getId();
    
    /**
     * Used when allocating the part
     * @return
     * @throws IOException
     */
    OutputStream getOutputStream();
    
    /**
     * Used to retrieve the part content (encrypted).
     * @return
     * @throws IOException
     */
    InputStream getInputStream();
    
    /**
     * Indicate that this byte sequence should be stored more permanently.
     */
    void persist();
    
    /**
     * Discard this byte sequence, it is no longer required.
     */
    void discard();
}
