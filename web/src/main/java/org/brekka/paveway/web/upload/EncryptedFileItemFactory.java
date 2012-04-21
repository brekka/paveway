package org.brekka.paveway.web.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;

/**
 * Can be shared
 * 
 * @author Andrew Taylor
 */
public class EncryptedFileItemFactory extends DiskFileItemFactory {

    protected final transient PavewayService pavewayService;
    
    public EncryptedFileItemFactory(int sizeThreshold, File repository, PavewayService pavewayService) {
        super(sizeThreshold, repository);
        this.pavewayService = pavewayService;
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
        FileBuilder fileBuilder = builderFor(fileName, contentType);
        EncryptedFileItem result = new EncryptedFileItem(fieldName, contentType, 
                fileName, fileBuilder, getSizeThreshold(), getRepository());
        return result;
    }
    
    protected FileBuilder builderFor(String fileName, String contentType) {
        return pavewayService.begin(fileName, contentType);
    }

}
