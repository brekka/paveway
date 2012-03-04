/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.io.File;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.output.CountingOutputStream;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.paveway.core.upload.EncryptedFileItem;

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
    private FileItem fileItem;
    
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
    public void complete(FileItem fileItem, long offset) {
        cryptedPart.setIv(resourceEncryptor.getIV().getIV());
        cryptedPart.setOriginalChecksum(messageDigest.digest());
        cryptedPart.setEncryptedChecksum(resourceEncryptor.getChecksum());
        cryptedPart.setLength(counter.getByteCount());
        cryptedPart.setOffset(offset);
        this.fileItem = fileItem;
    }
    
    /*
     * (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#getLength()
     */
    @Override
    public long getLength() {
        return cryptedPart.getLength();
    }
    
    /**
     * @return the backingFile
     */
    File getBackingFile() {
        return ((EncryptedFileItem) fileItem).getStoreLocation();
    }
    
    /**
     * @return the cryptedPart
     */
    CryptedPart getCryptedPart() {
        return cryptedPart;
    }

}
