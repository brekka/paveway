/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.CryptoFactory;

class FileBuilderImpl implements FileBuilder {
    
    private final ResourceCryptoService resourceCryptoService;
    
    private final ResourceStorageService resourceStorageService;
    
    private final CryptedFile cryptedFile;
    
    private final CryptoFactory cryptoFactory;
    
    private final List<PartAllocatorImpl> partAllocators = new ArrayList<>();
    
    /**
     * Length of parts so far
     */
    private final AtomicLong uploadedBytesCount = new AtomicLong();
    
    private final UploadPolicy policy;
    
    public FileBuilderImpl(String fileName, String mimeType, Compression compression, CryptoFactory cryptoFactory, 
            ResourceCryptoService resourceCryptoService, ResourceStorageService resourceStorageService, UploadPolicy policy) {
        this.resourceCryptoService = resourceCryptoService;
        this.resourceStorageService = resourceStorageService;
        this.cryptoFactory = cryptoFactory;
        CryptedFile cryptedFile = new CryptedFile();
        cryptedFile.setParts(new ArrayList<CryptedPart>());
        cryptedFile.setCompression(compression);
        cryptedFile.setProfile(cryptoFactory.getProfileId());
        cryptedFile.setFileName(fileName);
        cryptedFile.setMimeType(mimeType);
        cryptedFile.setSecretKey(cryptoFactory.getSymmetric().getKeyGenerator().generateKey());
        this.cryptedFile = cryptedFile;
        this.policy = policy;
    }
    
    public void setLength(long length) {
        cryptedFile.setOriginalLength(length);
    }
    
    public long getLength() {
        return cryptedFile.getOriginalLength();
    }
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return cryptedFile.getFileName();
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#allocatePart(java.io.OutputStream, long, long)
     */
    @Override
    public PartAllocator allocatePart() {
        if (uploadedBytesCount.longValue() >= policy.getMaxFileSize()) {
            throw new PavewayException(PavewayErrorCode.PW700, "File has grown too large");
        }
        CryptedPart part = new CryptedPart();
        UUID partId = UUID.randomUUID();
        part.setId(partId);
        part.setFile(cryptedFile);
        cryptedFile.getParts().add(part);
        ByteSequence partDestination = resourceStorageService.allocate(partId);
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(
                cryptedFile.getSecretKey(), cryptedFile.getCompression());
        MessageDigest digestInstance = cryptoFactory.getDigestInstance();
        PartAllocatorImpl partAllocatorImpl = new PartAllocatorImpl(encryptor, part, 
                digestInstance, partDestination, uploadedBytesCount);
        partAllocators.add(partAllocatorImpl);
        return partAllocatorImpl;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#isComplete()
     */
    @Override
    public synchronized boolean isComplete() {
        boolean complete = false;
        List<CryptedPart> parts = cryptedFile.getParts();
        if (parts.size() == 1) {
            CryptedPart cryptedPart = parts.get(0);
            complete = (cryptedPart.getLength() == cryptedFile.getOriginalLength());
        } else {
            List<CryptedPart> sortedParts = PavewayServiceImpl.sortByOffset(parts);
            long length = 0;
            for (CryptedPart cryptedPart : sortedParts) {
                if (length == cryptedPart.getOffset()) {
                    length += cryptedPart.getLength();
                }
            }
            complete = (length == cryptedFile.getOriginalLength());
        }
        return complete;
    }
    
    /**
     * @return the cryptedFile
     */
    CryptedFile getCryptedFile() {
        return cryptedFile;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#discard()
     */
    @Override
    public void discard() {
        List<PartAllocatorImpl> partAllocators = this.partAllocators;
        for (PartAllocatorImpl partAllocatorImpl : partAllocators) {
            ByteSequence partDestination = partAllocatorImpl.getPartDestination();
            resourceStorageService.remove(partDestination.getId());
        }
        this.partAllocators.clear();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("fileName", cryptedFile.getFileName())
            .append("mimeType", cryptedFile.getMimeType())
            .toString();
    }
}