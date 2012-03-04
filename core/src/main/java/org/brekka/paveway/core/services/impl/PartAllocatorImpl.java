/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.io.File;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import org.apache.commons.io.output.CountingOutputStream;
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
    
    private File backingFile;
    
    private CountingOutputStream counter;
    
    
    
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
        counter = new CountingOutputStream(dos);
        return counter;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#complete()
     */
    @Override
    public void complete() {
        cryptedPart.setIv(resourceEncryptor.getIV().getIV());
        cryptedPart.setOriginalChecksum(messageDigest.digest());
        cryptedPart.setEncryptedChecksum(resourceEncryptor.getChecksum());
        cryptedPart.setLength(counter.getByteCount());
    }
    
    /*
     * (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#getLength()
     */
    @Override
    public long getLength() {
        return cryptedPart.getLength();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#setLength(long)
     */
    @Override
    public void setBackingFile(File backingFile) {
        this.backingFile = backingFile;
    }
    
    /**
     * @return the backingFile
     */
    File getBackingFile() {
        return backingFile;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#setOffset(long)
     */
    @Override
    public void setOffset(long offset) {
        cryptedPart.setOffset(offset);
    }

}
