package org.brekka.paveway.core.upload;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;

/**
 * @author Andrew Taylor
 *
 */
public class EncryptedFileItem extends DiskFileItem {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -609134533128275214L;
    
    private transient final FileBuilder fileBuilder;
    
    private transient PartAllocator partAllocator;
    
    /**
     * @param fieldName
     * @param contentType
     * @param fileName
     * @param fileBuilder
     * @param sizeThreshold
     * @param repository
     */
    public EncryptedFileItem(String fieldName, String contentType, String fileName, FileBuilder fileBuilder,
            int sizeThreshold, File repository) {
        super(fieldName, contentType, false, fileName, sizeThreshold, repository);
        this.fileBuilder = fileBuilder;
    }
    
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        if (isFormField()) {
            return super.getOutputStream();
        }
        partAllocator = fileBuilder.allocatePart(super.getOutputStream());
        OutputStream encryptionOutputStream = partAllocator.allocate(super.getOutputStream());
        return encryptionOutputStream;
    }
    
    /**
     * @return the partAllocator
     */
    public PartAllocator getPartAllocator() {
        return partAllocator;
    }
    
    /**
     * @return the fileBuilder
     */
    public FileBuilder getFileBuilder() {
        return fileBuilder;
    }
}
