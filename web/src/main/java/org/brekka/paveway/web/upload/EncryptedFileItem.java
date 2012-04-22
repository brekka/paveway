package org.brekka.paveway.web.upload;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;

/**
 * @author Andrew Taylor
 *
 */
public class EncryptedFileItem extends AbstractFileItem {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -609134533128275214L;
    
    private transient final FileBuilder fileBuilder;
    
    private transient PartAllocator partAllocator;
    
    private final String originalFileName;
    
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
        super(fileName, contentType, fieldName);
        this.fileBuilder = fileBuilder;
        this.originalFileName = fileName;
    }
    
    
    @Override
    public OutputStream getOutputStream() throws IOException {
        partAllocator = fileBuilder.allocatePart();
        OutputStream encryptionOutputStream = partAllocator.getOutputStream();
        return encryptionOutputStream;
    }
    
    
    /**
     * @param req does not have to be set
     */
    public FileBuilder complete(HttpServletRequest req) {
        long length = 0;
        String xFileName = null;
        if (req != null) {
            xFileName = req.getHeader("X-File-Name");
        }
        if (xFileName != null) {
            long offset = NumberUtils.toLong(req.getHeader("X-Part-Offset"));
            partAllocator.complete(offset);
            length = NumberUtils.toLong(req.getHeader("X-File-Size"));
        } else {
            partAllocator.complete(0);
            length = partAllocator.getLength();
        }
        fileBuilder.setLength(length);
        if (fileBuilder.isComplete()) {
            return fileBuilder;
        }
        return null;
    }
    
    /**
     * @return the originalFileName
     */
    public String getOriginalFileName() {
        return originalFileName;
    }
}
