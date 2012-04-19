/**
 * 
 */
package org.brekka.paveway.core.services.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.dao.CryptedPartDAO;
import org.brekka.paveway.core.model.AllocatedFile;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor
 *
 */
@Service
@Transactional
public class PavewayServiceImpl implements PavewayService {

    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Autowired
    private ResourceCryptoService resourceCryptoService;
    
    @Autowired
    private ResourceStorageService resourceStorageService;
    
    @Autowired
    private CryptedFileDAO cryptedFileDAO;
    
    @Autowired
    private CryptedPartDAO cryptedPartDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#begin(java.lang.String)
     */
    @Override
    public FileBuilder begin(String fileName, String mimeType) {
        Compression compression = Compression.NONE;
        // TODO more mime types that can be compressed
        if (mimeType.startsWith("text")) {
            compression = Compression.GZIP;
        }
        CryptoFactory defaultFactory = cryptoFactoryRegistry.getDefault();
        return new FileBuilderImpl(fileName, mimeType, compression, defaultFactory, resourceCryptoService);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public AllocatedFile complete(FileBuilder fileBuilder) {
        FileBuilderImpl fileBuilderImpl = narrow(fileBuilder);
        AllocatedFile allocatedFile = fileBuilderImpl.getAllocatedFile();
        CryptedFile cryptedFile = allocatedFile.getCryptedFile();
        List<CryptedPart> parts = cryptedFile.getParts();
        for (CryptedPart cryptedPart : parts) {
            cryptedPartDAO.create(cryptedPart);
        }
        cryptedFileDAO.create(cryptedFile);
        
        // Needs to take place after the part ids have been assigned.
        List<PartAllocatorImpl> partAllocators = fileBuilderImpl.getPartAllocators();
        for (PartAllocatorImpl partAllocatorImpl : partAllocators) {
            UUID partId = partAllocatorImpl.getCryptedPart().getId();
            File backingFile = partAllocatorImpl.getBackingFile();
            try (FileInputStream is = new FileInputStream(backingFile)) {
                resourceStorageService.store(partId, is);
            } catch (IOException e) {
                throw new PavewayException(PavewayErrorCode.PW500, e,
                        "Failed to transfer backing file of part '%s' to storage", partId);
            }
            
        }
        return allocatedFile;
    }

    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#retrieveCryptedFileById(java.util.UUID)
     */
    @Override
    public CryptedFile retrieveCryptedFileById(UUID id) {
        return cryptedFileDAO.retrieveById(id);
    }
    
    @Override
    public void download(CryptedFile cryptedFile, SecretKey secretKey, OutputStream os) {
        List<CryptedPart> parts = sortByOffset(cryptedFile.getParts());
        for (CryptedPart cryptedPart : parts) {
            UUID partId = cryptedPart.getId();
            IvParameterSpec iv = new IvParameterSpec(cryptedPart.getIv());
            os = resourceCryptoService.decryptor(cryptedFile.getProfile(), cryptedFile.getCompression(), iv, secretKey, os);
            resourceStorageService.load(partId, os);
        }
    }
    
    protected FileBuilderImpl narrow(FileBuilder fileBuilder) {
        if (fileBuilder instanceof FileBuilderImpl == false) {
            throw new PavewayException(PavewayErrorCode.PW300, "Not a managed file builder instance");
        }
        return (FileBuilderImpl) fileBuilder;
    }
    
    static List<CryptedPart> sortByOffset(Collection<CryptedPart> parts) {
        List<CryptedPart> sortedParts = new ArrayList<>(parts);
        Collections.sort(sortedParts, new Comparator<CryptedPart>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(CryptedPart o1, CryptedPart o2) {
                return Long.valueOf(o1.getOffset()).compareTo(o2.getOffset());
            }
        });
        return sortedParts;
    }
}
