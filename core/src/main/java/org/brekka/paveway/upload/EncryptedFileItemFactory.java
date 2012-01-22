package org.brekka.paveway.upload;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.brekka.paveway.services.ResourceCryptoService;
import org.brekka.paveway.services.ResourceEncryptor;

/**
 * @author Andrew Taylor
 *
 */
public class EncryptedFileItemFactory extends DiskFileItemFactory {

    private final ResourceCryptoService resourceCryptoService;
    
    private final String messageDigestAlgorithm;
    
    public EncryptedFileItemFactory(int sizeThreshold, File repository, ResourceCryptoService resourceCryptoService, String messageDigestAlgorithm) {
        super(sizeThreshold, repository);
        this.resourceCryptoService = resourceCryptoService;
        this.messageDigestAlgorithm = messageDigestAlgorithm;
    }

    /**
     * Exactly the same as the supertype, except returns a {@link EncryptedFileItem}.
     */
    @Override
    public FileItem createItem(String fieldName, String contentType,
            boolean isFormField, String fileName) {
        ResourceEncryptor encryptor = resourceCryptoService.encryptor();
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(messageDigestAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        EncryptedFileItem result = new EncryptedFileItem(fieldName, contentType,
                isFormField, fileName, getSizeThreshold(), getRepository(), encryptor, messageDigest);
        return result;
    }
}
