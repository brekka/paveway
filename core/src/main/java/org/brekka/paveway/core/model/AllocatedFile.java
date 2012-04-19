/**
 * 
 */
package org.brekka.paveway.core.model;

import javax.crypto.SecretKey;

/**
 * @author Andrew Taylor
 *
 */
public interface AllocatedFile {

    /**
     * Retrieve the file name.
     * @return
     */
    String getFileName();
    
    String getMimeType();
    
    /**
     * Get the secret key for this file
     * @return
     */
    SecretKey getSecretKey();
    
    CryptedFile getCryptedFile();
}
