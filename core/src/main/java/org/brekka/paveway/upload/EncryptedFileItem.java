package org.brekka.paveway.upload;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.output.CountingOutputStream;
import org.brekka.paveway.services.ResourceEncryptor;
import org.brekka.xml.v1.paveway.ResourceInfoDocument.ResourceInfo;

/**
 * @author Andrew Taylor
 *
 */
public class EncryptedFileItem extends DiskFileItem {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -609134533128275214L;
    
    private transient final ResourceEncryptor encryptor;
    
    private transient final MessageDigest messageDigest;
    
    private transient CountingOutputStream countingOutputStream;
    
    /**
     * @param fieldName
     * @param contentType
     * @param isFormField
     * @param fileName
     * @param sizeThreshold
     * @param repository
     * @param encryptor
     */
    public EncryptedFileItem(String fieldName, String contentType, boolean isFormField, String fileName,
            int sizeThreshold, File repository, ResourceEncryptor encryptor, MessageDigest messageDigest) {
        super(fieldName, contentType, isFormField, fileName, sizeThreshold, repository);
        this.encryptor = encryptor;
        this.messageDigest = messageDigest;
    }
    
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (isFormField()) {
            return super.getOutputStream();
        }
        OutputStream outputStream = super.getOutputStream();
        OutputStream encryptionOutputStream = encryptor.encrypt(outputStream);
        this.countingOutputStream = new CountingOutputStream(encryptionOutputStream);
        return countingOutputStream;
    }
    
    public ResourceInfo getEncryptedResource() {
        return encryptor.complete();
    }
    
    public byte[] getDigest() {
        return messageDigest.digest();
    }
    
    public String getDigestAlgorithm() {
        return messageDigest.getAlgorithm();
    }
    
    public long getOriginalLength() {
        return countingOutputStream.getByteCount();
    }
}
