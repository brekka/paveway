/**
 * 
 */
package org.brekka.paveway.core.services.impl;


import javax.crypto.SecretKey;

import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Andrew Taylor
 *
 */
@Service
public class PavewayServiceImpl implements PavewayService {

    @Autowired
    private PhalanxService phalanxService;
    
    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Autowired
    private ResourceCryptoService resourceCryptoService;
    
    @Autowired
    private CryptedFileDAO cryptedFileDAO;
    
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
    public CryptedFile complete(KeyPair asymKeyPair, FileBuilder fileBuilder) {
        FileBuilderImpl fileBuilderImpl = narrow(fileBuilder);
        CryptedFile cryptedFile = fileBuilderImpl.getCryptedFile();
        SecretKey secretKey = fileBuilderImpl.getSecretKey();
        CryptedData cryptedData = phalanxService.asymEncrypt(secretKey.getEncoded(), asymKeyPair);
        complete(cryptedFile, cryptedData, fileBuilderImpl);
        return cryptedFile;
    }
    
    private void complete(CryptedFile cryptedFile, CryptedData cryptedData, FileBuilderImpl fileBuilder) {
        cryptedFile.setCryptedDataId(cryptedData.getId());
        cryptedFileDAO.create(cryptedFile);
    }
    
    protected FileBuilderImpl narrow(FileBuilder fileBuilder) {
        if (fileBuilder instanceof FileBuilderImpl == false) {
            throw new PavewayException(PavewayErrorCode.PW300, "Not a managed file builder instance");
        }
        return (FileBuilderImpl) fileBuilder;
    }
}
