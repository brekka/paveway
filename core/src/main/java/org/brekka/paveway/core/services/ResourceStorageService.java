/**
 * 
 */
package org.brekka.paveway.core.services;

import java.util.UUID;

import org.brekka.paveway.core.model.ByteSequence;

/**
 * @author Andrew Taylor
 *
 */
public interface ResourceStorageService {

//    /**
//     * @param partId
//     * @param backingFile
//     */
//    OutputStream store(UUID id);
//
//    InputStream load(UUID id);
    
    ByteSequence allocate(UUID id);
    
    ByteSequence retrieve(UUID id);
    
    void remove(UUID id);
}
