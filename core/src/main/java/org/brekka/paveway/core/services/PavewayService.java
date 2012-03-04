/**
 * 
 */
package org.brekka.paveway.core.services;

import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.phalanx.api.model.KeyPair;

/**
 * @author Andrew Taylor
 *
 */
public interface PavewayService {

    FileBuilder begin(String fileName, String mimeType);
    
    CryptedFile complete(String password, FileBuilder fileBuilder);
    
    CryptedFile complete(KeyPair asymKeyPair, FileBuilder fileBuilder);
}
