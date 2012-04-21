package org.brekka.paveway.web.upload;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;

/**
 * Must be session-bound
 * 
 * @author Andrew Taylor
 */
public class EncryptedMultipartFileItemFactory extends EncryptedFileItemFactory {

    private transient Map<String, FileBuilder> fileBuilders = new HashMap<String, FileBuilder>();
    
    public EncryptedMultipartFileItemFactory(int sizeThreshold, File repository, PavewayService pavewayService) {
        super(sizeThreshold, repository, pavewayService);
    }

    protected FileBuilder builderFor(String fileName, String contentType) {
        if (!fileBuilders.containsKey(fileName)) {
            FileBuilder fileBuilder = pavewayService.begin(fileName, contentType);
            fileBuilders.put(fileName, fileBuilder);
        }
        return fileBuilders.get(fileName);
    }
    
    /**
     * @param fileBuilder
     */
    public void remove(String fileName) {
        fileBuilders.remove(fileName);
    }
}
