/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import javax.crypto.SecretKey;

import org.brekka.paveway.core.model.AllocatedFile;
import org.brekka.paveway.core.model.CryptedFile;

/**
 * @author Andrew Taylor
 *
 */
public class AllocatedFileImpl implements AllocatedFile {

    private final String fileName;
    
    private final String mimeType;
    
    private final CryptedFile cryptedFile;
    
    private final SecretKey secretKey;
    
    
    
    public AllocatedFileImpl(String fileName, String mimeType, CryptedFile cryptedFile, SecretKey secretKey) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.cryptedFile = cryptedFile;
        this.secretKey = secretKey;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.AllocatedFile#getFileName()
     */
    @Override
    public String getFileName() {
        return fileName;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.AllocatedFile#getMimeType()
     */
    @Override
    public String getMimeType() {
        return mimeType;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.AllocatedFile#getSecretKey()
     */
    @Override
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.AllocatedFile#getCryptedFile()
     */
    @Override
    public CryptedFile getCryptedFile() {
        return cryptedFile;
    }

}
