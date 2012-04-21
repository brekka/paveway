package org.brekka.paveway.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceCryptoServiceImpl implements ResourceCryptoService {

    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Override
    public ResourceEncryptor encryptor(SecretKey secretKey, Compression compression) {
        CryptoFactory factory = cryptoFactoryRegistry.getDefault();
        return new ResourceEncryptorImpl(factory, secretKey, compression);
    }
    
    
    
    @Override
    public InputStream decryptor(int cryptoProfileId, Compression compression, IvParameterSpec iv, SecretKey secretKey, InputStream inputStream) {
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(cryptoProfileId);
        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, secretKey, iv, cryptoFactory.getSymmetric());
        InputStream cipherOutputStream = new CipherInputStream(inputStream, cipher);
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
                
            default:
                is = cipherOutputStream;
                break;
        }
        return is;
    }

    
    protected IvParameterSpec generateInitializationVector(CryptoFactory profile) {
        byte[] ivBytes = new byte[profile.getSymmetric().getIvLength()];
        profile.getSecureRandom().nextBytes(ivBytes);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        return iv;
    }
    
    protected Cipher getCipher(int mode, Key key, AlgorithmParameterSpec parameter, CryptoFactory.Symmetric symmetricProfile) {
        Cipher cipher = symmetricProfile.getInstance();
        try {
            cipher.init(mode, key, parameter);
        } catch (GeneralSecurityException e) {
            throw new PavewayException(PavewayErrorCode.PW200, e, 
                    "Problem initializing symmetric cipher");
        }
        return cipher;
    }
    
    private class ResourceEncryptorImpl implements ResourceEncryptor {
        
        private final IvParameterSpec initializationVector;
        private final Cipher cipher;
        private final MessageDigest messageDigest;
        private final Compression compression;
        
        public ResourceEncryptorImpl(CryptoFactory factory, SecretKey secretKey, Compression compression) {
            CryptoFactory.Symmetric synchronousFactory = factory.getSymmetric();
            this.initializationVector = generateInitializationVector(factory);
            this.cipher = getCipher(Cipher.ENCRYPT_MODE, secretKey, initializationVector, synchronousFactory);
            this.messageDigest = factory.getDigestInstance();
            this.compression = compression;
        }
        
        /**
         * - GZIP
         * - Encrypt
         */
        @Override
        public OutputStream encrypt(OutputStream finalOs) {
            DigestOutputStream dos = new DigestOutputStream(finalOs, messageDigest);
            CipherOutputStream cos = new CipherOutputStream(dos, cipher);
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
                default:
                    // None;
                    os = cos;
                    break;
            }
            return os;
        }
        
        /* (non-Javadoc)
         * @see org.brekka.paveway.core.services.ResourceEncryptor#getChecksum()
         */
        @Override
        public byte[] getChecksum() {
            return messageDigest.digest();
        }
        
        /* (non-Javadoc)
         * @see org.brekka.paveway.core.services.ResourceEncryptor#getIV()
         */
        @Override
        public IvParameterSpec getIV() {
            return initializationVector;
        }
    }
}
