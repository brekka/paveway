package org.brekka.paveway.services;

import java.io.OutputStream;

import org.brekka.xml.v1.paveway.ResourceInfoDocument.ResourceInfo;

public interface ResourceEncryptor {

    /**
     * The thing to write the bytes to
     * @return
     */
    OutputStream encrypt(OutputStream os);
    
    /**
     * 
     * @return
     */
    ResourceInfo complete();
}