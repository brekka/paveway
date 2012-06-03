/**
 * 
 */
package org.brekka.paveway.core.services;

import java.io.InputStream;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.brekka.paveway.core.model.AllocatedFile;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;

/**
 * @author Andrew Taylor
 *
 */
public interface PavewayService {

    FileBuilder begin(String fileName, String mimeType, UploadPolicy uploadPolicy);
    
    AllocatedFile complete(FileBuilder fileBuilder);
    
    CryptedFile retrieveCryptedFileById(UUID id);
    
    InputStream download(CryptedFile file, SecretKey secretKey);
    
    void remove(UUID cryptedFileId);
}
