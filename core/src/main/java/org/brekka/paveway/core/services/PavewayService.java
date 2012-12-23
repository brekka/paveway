/**
 * 
 */
package org.brekka.paveway.core.services;

import java.io.InputStream;
import java.util.UUID;

import org.brekka.paveway.core.model.CompletableFile;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.EncryptedFileImporter;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.phoenix.api.SecretKey;

/**
 * @author Andrew Taylor
 *
 */
public interface PavewayService {

    FileBuilder begin(String fileName, String mimeType, UploadPolicy uploadPolicy);
    
    CryptedFile complete(CompletableFile fileBuilder);
    
    CryptedFile retrieveCryptedFileById(UUID id);
    
    InputStream download(CryptedFile file);
    
    EncryptedFileImporter importEncryptedFile(String fileName, long originalLength, 
            byte[] originalChecksum, Compression compression, SecretKey secretKey);

    /**
     * @param cryptedFileId
     */
    void removeFile(UUID cryptedFileId);
}
