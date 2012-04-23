/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.paveway.core.model.AllocatedFile;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.CryptoFactory;

class FileBuilderImpl implements FileBuilder {
    
    private final String fileName;
    
    private final String mimeType;
    
    private final ResourceCryptoService resourceCryptoService;
    
    private final ResourceStorageService resourceStorageService;
    
    private final CryptedFile cryptedFile;
    
    private final CryptoFactory cryptoFactory;
    
    private final SecretKey secretKey;
    
    private final List<PartAllocatorImpl> partAllocators = new ArrayList<>();
    
    public FileBuilderImpl(String fileName, String mimeType, Compression compression, CryptoFactory cryptoFactory, 
            ResourceCryptoService resourceCryptoService, ResourceStorageService resourceStorageService) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.resourceCryptoService = resourceCryptoService;
        this.resourceStorageService = resourceStorageService;
        this.cryptoFactory = cryptoFactory;
        CryptedFile cryptedFile = new CryptedFile();
        cryptedFile.setParts(new ArrayList<CryptedPart>());
        cryptedFile.setCompression(compression);
        cryptedFile.setProfile(cryptoFactory.getProfileId());
        this.cryptedFile = cryptedFile;
        this.secretKey = cryptoFactory.getSymmetric().getKeyGenerator().generateKey();
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
        return fileName;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#allocatePart(java.io.OutputStream, long, long)
     */
    @Override
    public PartAllocator allocatePart() {
        CryptedPart part = new CryptedPart();
        UUID partId = UUID.randomUUID();
        part.setId(partId);
        part.setFile(cryptedFile);
        cryptedFile.getParts().add(part);
        ByteSequence partDestination = resourceStorageService.allocate(partId);
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, cryptedFile.getCompression());
        MessageDigest digestInstance = cryptoFactory.getDigestInstance();
        PartAllocatorImpl partAllocatorImpl = new PartAllocatorImpl(encryptor, part, digestInstance, partDestination);
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
    
    public AllocatedFile getAllocatedFile() {
        return new AllocatedFileImpl(fileName, mimeType, cryptedFile, secretKey);
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
            .append("fileName", fileName)
            .append("mimeType", mimeType)
            .toString();
    }
}