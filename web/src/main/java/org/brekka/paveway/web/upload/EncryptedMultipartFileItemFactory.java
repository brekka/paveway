package org.brekka.paveway.web.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.brekka.paveway.core.model.FileBuilder;

/**
 * Must be session-bound
 * 
 * @author Andrew Taylor
 */
public class EncryptedMultipartFileItemFactory extends DiskFileItemFactory {

    private final FileBuilder fileBuilder;
    private final String fileName;
    private final String contentType;
    
    /**
     * 
     */
    public EncryptedMultipartFileItemFactory(int sizeThreshold, File repository, String fileName, String contentType, FileBuilder fileBuilder) {
        super(sizeThreshold, repository);
        this.fileBuilder = fileBuilder;
        this.fileName = fileName;
        this.contentType = contentType;
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.disk.DiskFileItemFactory#createItem(java.lang.String, java.lang.String, boolean, java.lang.String)
     */
    @Override
    public FileItem createItem(String fieldName, String ignoredContentType, boolean isFormField, String ignoredFileName) {
        if (isFormField) {
            return super.createItem(fieldName, contentType, isFormField, fileName);
        }
        EncryptedFileItem result = new EncryptedFileItem(fieldName, this.contentType, 
                this.fileName, fileBuilder, getSizeThreshold(), getRepository());
        return result;
    }
}
