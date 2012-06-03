package org.brekka.paveway.web.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.PavewayService;

/**
 * Can be shared
 * 
 * @author Andrew Taylor
 */
public class EncryptedFileItemFactory extends DiskFileItemFactory {

    protected final transient PavewayService pavewayService;
    
    private final UploadPolicy uploadPolicy;
    
    public EncryptedFileItemFactory(int sizeThreshold, File repository, 
            PavewayService pavewayService, UploadPolicy uploadPolicy) {
        super(sizeThreshold, repository);
        this.pavewayService = pavewayService;
        this.uploadPolicy = uploadPolicy;
    }

    /**
     * Exactly the same as the supertype, except returns a {@link EncryptedFileItem}.
     */
    @Override
    public FileItem createItem(String fieldName, String contentType,
            boolean isFormField, String fileName) {
        if (isFormField) {
            return super.createItem(fieldName, contentType, isFormField, fileName);
        }
        FileBuilder fileBuilder = pavewayService.begin(fileName, contentType, uploadPolicy);
        EncryptedFileItem result = new EncryptedFileItem(fieldName, contentType, 
                fileName, fileBuilder, getSizeThreshold(), getRepository());
        return result;
    }

}
