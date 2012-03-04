/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.phoenix.CryptoFactory;

class FileBuilderImpl implements FileBuilder {
    
    private final String fileName;
    
    private final String mimeType;
    
    private final ResourceCryptoService resourceCryptoService;
    
    private final CryptedFile cryptedFile;
    
    private final CryptoFactory cryptoFactory;
    
    private final SecretKey secretKey;
    
    private final List<PartAllocatorImpl> partAllocators = new ArrayList<>();
    
    
    public FileBuilderImpl(String fileName, String mimeType, Compression compression, CryptoFactory cryptoFactory, 
            ResourceCryptoService resourceCryptoService) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.resourceCryptoService = resourceCryptoService;
        this.cryptoFactory = cryptoFactory;
        CryptedFile cryptedFile = new CryptedFile();
        cryptedFile.setParts(new ArrayList<CryptedPart>());
        cryptedFile.setCompression(compression);
        cryptedFile.setProfile(cryptoFactory.getProfileId());
        this.cryptedFile = cryptedFile;
        this.secretKey = cryptoFactory.getSymmetric().getKeyGenerator().generateKey();
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#getFileName()
     */
    @Override
    public String getFileName() {
        return fileName;
    }
    
    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }
    
    public void setLength(long length) {
        cryptedFile.setOriginalLength(length);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#allocatePart(java.io.OutputStream, long, long)
     */
    @Override
    public PartAllocator allocatePart(OutputStream os) {
        CryptedPart part = new CryptedPart();
        part.setFile(cryptedFile);
        cryptedFile.getParts().add(part);
        
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, cryptedFile.getCompression());
        MessageDigest digestInstance = cryptoFactory.getDigestInstance();
        PartAllocatorImpl partAllocatorImpl = new PartAllocatorImpl(encryptor, part, digestInstance);
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
    
    /**
     * @return the partAllocators
     */
    List<PartAllocatorImpl> getPartAllocators() {
        return partAllocators;
    }
    
    /**
     * @return the secretKey
     */
    SecretKey getSecretKey() {
        return secretKey;
    }

}