/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.output.CountingOutputStream;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.StreamCryptor;

/**
 * @author Andrew Taylor
 *
 */
public class PartAllocatorImpl implements PartAllocator {

    private final ResourceEncryptor resourceEncryptor;
    
    private final CryptedPart cryptedPart;
    
    private final StreamCryptor<OutputStream, DigestResult> digester;
    
    /**
     * This is where the encrypted data will be stored.
     */
    private final ByteSequence partDestination;
    
    private final AtomicLong uploadedBytesCount;
    
    private CountingOutputStream counter;
    
    
    
    public PartAllocatorImpl(ResourceEncryptor resourceEncryptor, CryptedPart cryptedPart, 
            StreamCryptor<OutputStream, DigestResult> digester, ByteSequence partDestination, 
            AtomicLong uploadedBytesCount) {
        this.resourceEncryptor = resourceEncryptor;
        this.cryptedPart = cryptedPart;
        this.digester = digester;
        this.partDestination = partDestination;
        this.uploadedBytesCount = uploadedBytesCount;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#encrypt(java.io.OutputStream)
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream encryptingOs = resourceEncryptor.encrypt(partDestination.getOutputStream());
        OutputStream dos = digester.getStream(encryptingOs);
        counter = new CountingOutputStream(dos);
        return counter;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.PartAllocator#complete()
     */
    @Override
    public void complete(long offset) {
        cryptedPart.setIv(resourceEncryptor.getSpec().getIV());
        cryptedPart.setOriginalChecksum(digester.getSpec().getDigest());
        cryptedPart.setEncryptedChecksum(resourceEncryptor.getDigestResult().getDigest());
        cryptedPart.setLength(counter.getByteCount());
        cryptedPart.setOffset(offset);
        uploadedBytesCount.addAndGet(counter.getByteCount());
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
     * @return the partDestination
     */
    ByteSequence getPartDestination() {
        return partDestination;
    }
    
    /**
     * @return the cryptedPart
     */
    CryptedPart getCryptedPart() {
        return cryptedPart;
    }

}
