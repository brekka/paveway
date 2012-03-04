package org.brekka.paveway.core.upload;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;

/**
 * @author Andrew Taylor
 *
 */
public class EncryptedFileItemFactory extends DiskFileItemFactory {

    private final PavewayService pavewayService;
    
    private Map<String, FileBuilder> fileBuilders = new HashMap<String, FileBuilder>();
    
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
        
        if (!fileBuilders.containsKey(fileName)) {
            FileBuilder fileBuilder = pavewayService.begin(fileName, contentType);
            fileBuilders.put(fileName, fileBuilder);
        }
        FileBuilder fileBuilder = fileBuilders.get(fileName);
        EncryptedFileItem result = new EncryptedFileItem(fieldName,
                fileBuilder, getSizeThreshold(), getRepository());
        return result;
    }
}
