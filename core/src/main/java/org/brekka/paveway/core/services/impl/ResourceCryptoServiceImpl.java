package org.brekka.paveway.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
import org.brekka.phoenix.api.services.DigestCryptoService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceCryptoServiceImpl implements ResourceCryptoService {

    @Autowired
    private SymmetricCryptoService symmetricCryptoService;
    
    @Autowired
    private DigestCryptoService digestCryptoService;
    
    @Override
    public ResourceEncryptor encryptor(SecretKey secretKey, Compression compression) {
        StreamCryptor<OutputStream, SymmetricCryptoSpec> symCryptor = symmetricCryptoService.encryptor(secretKey);
        StreamCryptor<OutputStream, DigestResult> digestCryptor = digestCryptoService.outputDigester(secretKey.getCryptoProfile());
        return new ResourceEncryptorImpl(symCryptor, digestCryptor, compression);
    }
    
    
    @Override
    public StreamCryptor<InputStream, SymmetricCryptoSpec> decryptor(SymmetricCryptoSpec spec, Compression compression) {
        StreamCryptor<InputStream, SymmetricCryptoSpec> symDecryptor = symmetricCryptoService.decryptor(spec);
        return new ResourceDecryptorImpl(symDecryptor, compression);
    }
    
    private class ResourceDecryptorImpl implements StreamCryptor<InputStream, SymmetricCryptoSpec> {
        private final StreamCryptor<InputStream, SymmetricCryptoSpec> symDecryptor;
        private final Compression compression;
        /**
         * @param symDecryptor
         * @param compression
         */
        public ResourceDecryptorImpl(StreamCryptor<InputStream, SymmetricCryptoSpec> symDecryptor,
                Compression compression) {
            this.symDecryptor = symDecryptor;
            this.compression = compression;
        }
        
        /* (non-Javadoc)
         * @see org.brekka.phoenix.api.StreamCryptor#getStream(java.lang.Object)
         */
        @Override
        public InputStream getStream(InputStream stream) {
            InputStream cipherOutputStream = symDecryptor.getStream(stream);
            InputStream is;
            switch (compression) {
                case GZIP:
                    try {
                        is = new GZIPInputStream(cipherOutputStream);
                    } catch (IOException e) {
                        // TODO
                        throw new PavewayException(PavewayErrorCode.PW400, e, 
                                "GZip problem");
                    }
                    break;
                case NONE:
                default:
                    is = cipherOutputStream;
                    break;
            }
            return is;
        }
        
        /* (non-Javadoc)
         * @see org.brekka.phoenix.api.StreamCryptor#getSpec()
         */
        @Override
        public SymmetricCryptoSpec getSpec() {
            return symDecryptor.getSpec();
        }
        
    }
    
    private class ResourceEncryptorImpl implements ResourceEncryptor {
        
        private final StreamCryptor<OutputStream, SymmetricCryptoSpec> symCryptor;
        private final StreamCryptor<OutputStream, DigestResult> digestCryptor;
        private final Compression compression;
        
        public ResourceEncryptorImpl(StreamCryptor<OutputStream, SymmetricCryptoSpec> symCryptor,
                StreamCryptor<OutputStream, DigestResult> digestCryptor, Compression compression) {
            this.symCryptor = symCryptor;
            this.digestCryptor = digestCryptor;
            this.compression = compression;
        }
        
        /**
         * - GZIP
         * - Encrypt
         */
        @Override
        public OutputStream encrypt(OutputStream finalOs) {
            OutputStream dos = digestCryptor.getStream(finalOs);
            OutputStream cos = symCryptor.getStream(dos);
            OutputStream os;
            switch (compression) {
                case GZIP:
                    try {
                        os = new GZIPOutputStream(cos);
                    } catch (IOException e) {
                        throw new PavewayException(PavewayErrorCode.PW401, e, 
                                "Failed to create GZIP instance for encryption stream");
                    }
                    break;
                case NONE:
                default:
                    // None;
                    os = cos;
                    break;
            }
            return os;
        }
        
        /* (non-Javadoc)
         * @see org.brekka.paveway.core.services.ResourceEncryptor#getDigestResult()
         */
        @Override
        public DigestResult getDigestResult() {
            return digestCryptor.getSpec();
        }
        
        /* (non-Javadoc)
         * @see org.brekka.paveway.core.services.ResourceEncryptor#getSpec()
         */
        @Override
        public SymmetricCryptoSpec getSpec() {
            return symCryptor.getSpec();
        }
    }
}
