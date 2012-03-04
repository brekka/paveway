/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.services.ResourceEncryptor;

/**
 * @author Andrew Taylor
 *
 */
public class PartAllocatorImpl implements PartAllocator {

    private final ResourceEncryptor resourceEncryptor;
    
    private final CryptedPart cryptedPart;
    
    private final MessageDigest messageDigest;
    
    
    
    public PartAllocatorImpl(ResourceEncryptor resourceEncryptor, CryptedPart cryptedPart, MessageDigest messageDigest) {
        this.resourceEncryptor = resourceEncryptor;
        this.cryptedPart = cryptedPart;
        this.messageDigest = messageDigest;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#encrypt(java.io.OutputStream)
     */
    @Override
    public OutputStream allocate(OutputStream os) {
        OutputStream encryptingOs = resourceEncryptor.encrypt(os);
        DigestOutputStream dos = new DigestOutputStream(encryptingOs, messageDigest);
        return dos;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#complete()
     */
    @Override
    public void complete() {
        cryptedPart.setIv(resourceEncryptor.getIV().getIV());
        cryptedPart.setOriginalChecksum(messageDigest.digest());
        cryptedPart.setEncryptedChecksum(resourceEncryptor.getChecksum());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#setLength(long)
     */
    @Override
    public void setLength(long length) {
        cryptedPart.setLength(length);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#setOffset(long)
     */
    @Override
    public void setOffset(long offset) {
        cryptedPart.setOffset(offset);
    }

}
