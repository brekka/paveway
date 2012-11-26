package org.brekka.paveway.core.services;

import java.io.OutputStream;

import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.SymmetricCryptoSpec;

public interface ResourceEncryptor {

    /**
     * The thing to write the bytes to
     * @return
     */
    OutputStream encrypt(OutputStream os);
    
    DigestResult getDigestResult();
    
    SymmetricCryptoSpec getSpec();
}
