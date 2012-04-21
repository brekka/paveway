/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import org.apache.commons.io.output.CountingOutputStream;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FilePart;
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
    
    /**
     * Need to keep a strong reference to the file item to prevent the underlying
     * temp file from being deleted during garbage collection.
     */
    private final FilePart partDestination;
    
    private CountingOutputStream counter;
    
    
    
    public PartAllocatorImpl(ResourceEncryptor resourceEncryptor, CryptedPart cryptedPart, MessageDigest messageDigest, FilePart partDestination) {
        this.resourceEncryptor = resourceEncryptor;
        this.cryptedPart = cryptedPart;
        this.messageDigest = messageDigest;
        this.partDestination = partDestination;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#encrypt(java.io.OutputStream)
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream encryptingOs = resourceEncryptor.encrypt(partDestination.getOutputStream());
        DigestOutputStream dos = new DigestOutputStream(encryptingOs, messageDigest);
        counter = new CountingOutputStream(dos);
        return counter;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#complete()
     */
    @Override
    public void complete(long offset) {
        cryptedPart.setIv(resourceEncryptor.getIV().getIV());
        cryptedPart.setOriginalChecksum(messageDigest.digest());
        cryptedPart.setEncryptedChecksum(resourceEncryptor.getChecksum());
        cryptedPart.setLength(counter.getByteCount());
        cryptedPart.setOffset(offset);
    }
    
    /*
     * (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#getLength()
     */
    @Override
    public long getLength() {
        return cryptedPart.getLength();
    }
    
    
    InputStream getInputStream() throws IOException {
        return partDestination.getInputStream();
    }
    
    /**
     * @return the cryptedPart
     */
    CryptedPart getCryptedPart() {
        return cryptedPart;
    }

}
