/**
 * 
 */
package org.brekka.paveway.core.services.impl;


import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.phalanx.api.model.KeyPair;
import org.springframework.stereotype.Service;

/**
 * @author Andrew Taylor
 *
 */
@Service
public class PavewayServiceImpl implements PavewayService {

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#begin(java.lang.String)
     */
    @Override
    public FileBuilder begin(String fileName, String mimeType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#complete(java.lang.String, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    public CryptedFile complete(String password, FileBuilder fileBuilder) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#complete(org.brekka.phalanx.api.model.KeyPair, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    public CryptedFile complete(KeyPair asymKeyPair, FileBuilder fileBuilder) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
