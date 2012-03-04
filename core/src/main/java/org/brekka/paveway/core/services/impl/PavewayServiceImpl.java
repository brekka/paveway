/**
 * 
 */
package org.brekka.paveway.core.services.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.dao.CryptedPartDAO;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.brekka.phalanx.api.services.PhalanxService;
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
    private PhalanxService phalanxService;
    
    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Autowired
    private ResourceCryptoService resourceCryptoService;
    
    @Autowired
    private CryptedFileDAO cryptedFileDAO;
    
    @Autowired
    private CryptedPartDAO cryptedPartDAO;
    
    // TODO very temporary
    private File repo = new File("/large/pavewayrepo");
    
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

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#complete(java.lang.String, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public CryptedFile complete(String password, FileBuilder fileBuilder) {
        FileBuilderImpl fileBuilderImpl = narrow(fileBuilder);
        CryptedFile cryptedFile = fileBuilderImpl.getCryptedFile();
        SecretKey secretKey = fileBuilderImpl.getSecretKey();
        CryptedData cryptedData = phalanxService.pbeEncrypt(secretKey.getEncoded(), password);
        complete(cryptedFile, cryptedData, fileBuilderImpl);
        return cryptedFile;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#complete(org.brekka.phalanx.api.model.KeyPair, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public CryptedFile complete(KeyPair asymKeyPair, FileBuilder fileBuilder) {
        FileBuilderImpl fileBuilderImpl = narrow(fileBuilder);
        CryptedFile cryptedFile = fileBuilderImpl.getCryptedFile();
        SecretKey secretKey = fileBuilderImpl.getSecretKey();
        CryptedData cryptedData = phalanxService.asymEncrypt(secretKey.getEncoded(), asymKeyPair);
        complete(cryptedFile, cryptedData, fileBuilderImpl);
        return cryptedFile;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#download(org.brekka.paveway.core.model.CryptedFile, java.lang.String, java.io.OutputStream)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void download(CryptedFile cryptedFile, String password, OutputStream os) {
        UUID cryptedDataId = cryptedFile.getCryptedDataId();
        byte[] secretKeyBytes = phalanxService.pbeDecrypt(new IdentityCryptedData(cryptedDataId), password);
        download(cryptedFile, secretKeyBytes, os);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#download(org.brekka.paveway.core.model.CryptedFile, org.brekka.phalanx.api.model.PrivateKeyToken, java.io.OutputStream)
     */
    @Override
    public void download(CryptedFile cryptedFile, PrivateKeyToken privateKeyToken, OutputStream os) {
        UUID cryptedDataId = cryptedFile.getCryptedDataId();
        byte[] secretKeyBytes = phalanxService.asymDecrypt(new IdentityCryptedData(cryptedDataId), privateKeyToken);
        download(cryptedFile, secretKeyBytes, os);
    }
    

    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#retrieveCryptedFileById(java.util.UUID)
     */
    @Override
    public CryptedFile retrieveCryptedFileById(UUID id) {
        return cryptedFileDAO.retrieveById(id);
    }
    
    
    /**
     * TODO Temporary
     * @param cryptedPart
     * @return
     */
    protected InputStream retrieveFromStorage(CryptedPart cryptedPart) {
        try {
            return new FileInputStream(new File(repo, cryptedPart.getId().toString()));
        } catch (FileNotFoundException e) {
            // TODO
            throw new IllegalStateException(e);
        }
    }

    protected void complete(CryptedFile cryptedFile, CryptedData cryptedData, FileBuilderImpl fileBuilder) {
        cryptedFile.setCryptedDataId(cryptedData.getId());
        List<CryptedPart> parts = cryptedFile.getParts();
        for (CryptedPart cryptedPart : parts) {
            cryptedPartDAO.create(cryptedPart);
        }
        cryptedFileDAO.create(cryptedFile);
        
        // Needs to take place after the part ids have been assigned.
        List<PartAllocatorImpl> partAllocators = fileBuilder.getPartAllocators();
        for (PartAllocatorImpl partAllocatorImpl : partAllocators) {
            File backingFile = partAllocatorImpl.getBackingFile();
            File repoFile = new File(repo, partAllocatorImpl.getCryptedPart().getId().toString());
            try {
                FileUtils.moveFile(backingFile, repoFile);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                throw new IllegalStateException(e);
            }
        }
    }
    
    protected void download(CryptedFile cryptedFile, byte[] secretKeyBytes, OutputStream os) {
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(cryptedFile.getProfile());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
        List<CryptedPart> parts = sortByOffset(cryptedFile.getParts());
        for (CryptedPart cryptedPart : parts) {
            try (InputStream is = retrieveFromStorage(cryptedPart)) {
                IvParameterSpec iv = new IvParameterSpec(cryptedPart.getIv());
                InputStream partIs = resourceCryptoService.decrypt(cryptedFile.getProfile(), cryptedFile.getCompression(), iv, secretKey, is);
                IOUtils.copy(partIs, os);
            } catch (IOException e) {
                throw new PavewayException(PavewayErrorCode.PW500, e,
                        "Failed to read/decrypt part '%s' of file '%s'", cryptedPart.getId(), cryptedFile.getId());
            }
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
