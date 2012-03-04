/**
 * 
 */
package org.brekka.paveway.core.services;

import java.io.OutputStream;
import java.util.UUID;

import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;

/**
 * @author Andrew Taylor
 *
 */
public interface PavewayService {

    FileBuilder begin(String fileName, String mimeType);
    
    CryptedFile complete(String password, FileBuilder fileBuilder);
    
    CryptedFile complete(KeyPair asymKeyPair, FileBuilder fileBuilder);
    
    CryptedFile retrieveCryptedFileById(UUID id);
    
    void download(CryptedFile file, String password, OutputStream os);
    
    void download(CryptedFile file, PrivateKeyToken privateKeyToken, OutputStream os);
    
}
