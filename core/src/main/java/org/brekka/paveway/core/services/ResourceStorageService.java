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
    OutputStream store(UUID id);

    InputStream load(UUID id);
    
    void remove(UUID id);
}
