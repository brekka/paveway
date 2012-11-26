/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.SymmetricCryptoSpec;

class MultipartInputStream extends InputStream {
    
    private final CryptedFile cryptedFile;
    private final Iterator<CryptedPart> partsIterator;
    
    private final ResourceStorageService resourceStorageService;
    private final ResourceCryptoService resourceCryptoService;
    
    private InputStream current = null;
    
    public MultipartInputStream(CryptedFile cryptedFile,
            ResourceStorageService resourceStorageService, ResourceCryptoService resourceCryptoService) {
        this.cryptedFile = cryptedFile;
        this.resourceCryptoService = resourceCryptoService;
        this.resourceStorageService = resourceStorageService;
        List<CryptedPart> parts = PavewayServiceImpl.sortByOffset(cryptedFile.getParts());
        this.partsIterator = parts.iterator();
        toNextStream();
    }



    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int i = current.read(b, off, len);
        if (i == -1) {
            toNextStream();
            if (current != null) {
                i = current.read(b, off, len);
            }
        }
        return i;
    }
    
    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(current);
    }
    
    /**
     * @return
     */
    private void toNextStream() {
        // Close any existing stream
        IOUtils.closeQuietly(current);
        if (!partsIterator.hasNext()) {
            this.current = null;
            return;
        }
        CryptedPart cryptedPart = partsIterator.next();
        UUID partId = cryptedPart.getId();
        ByteSequence byteSequence = resourceStorageService.retrieve(partId);
        InputStream is = byteSequence.getInputStream();
        StreamCryptor<InputStream, SymmetricCryptoSpec> decryptor = resourceCryptoService.decryptor(cryptedPart, cryptedFile.getCompression());
        this.current = decryptor.getStream(is);
    }
}