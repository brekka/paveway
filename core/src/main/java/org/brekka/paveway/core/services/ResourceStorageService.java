/**
 * 
 */
package org.brekka.paveway.core.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * @author Andrew Taylor
 *
 */
public interface ResourceStorageService {

    /**
     * @param partId
     * @param backingFile
     */
    void store(UUID id, InputStream is);

    void load(UUID id, OutputStream os);
    
    void remove(UUID id);
}
