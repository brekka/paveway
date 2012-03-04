package org.brekka.paveway.core.services;

import java.io.OutputStream;

import javax.crypto.spec.IvParameterSpec;

public interface ResourceEncryptor {

    /**
     * The thing to write the bytes to
     * @return
     */
    OutputStream encrypt(OutputStream os);
    
    /**
     * The checksum for what was just encrypted
     * @return
     */
    byte[] getChecksum();
    
    /**
     * Retrieve the IV
     * @return
     */
    IvParameterSpec getIV();
}
