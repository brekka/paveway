package org.brekka.paveway.services.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.brekka.paveway.services.ResourceCryptoService;
import org.brekka.paveway.services.ResourceEncryptor;
import org.brekka.phalanx.crypto.CryptoErrorCode;
import org.brekka.phalanx.crypto.CryptoException;
import org.brekka.phalanx.crypto.CryptoFactory;
import org.brekka.phalanx.crypto.CryptoFactoryRegistry;
import org.brekka.xml.v1.paveway.ResourceInfoDocument.ResourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceCryptoServiceImpl implements ResourceCryptoService {

    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Override
    public ResourceEncryptor encryptor() {
        CryptoFactory factory = cryptoFactoryRegistry.getDefault();
        return new ResourceEncryptorImpl(factory);
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
            throw new CryptoException(CryptoErrorCode.CP102, e, 
                    "Problem initializing symmetric cipher");
        }
        return cipher;
    }
    
    private class ResourceEncryptorImpl implements ResourceEncryptor {
        
        private final int profileId;
        private final SecretKey secretKey;
        private final IvParameterSpec initializationVector;
        private final Cipher cipher;
        
        public ResourceEncryptorImpl(CryptoFactory factory) {
            CryptoFactory.Symmetric synchronousFactory = factory.getSymmetric();
            this.secretKey = synchronousFactory.getKeyGenerator().generateKey();
            this.initializationVector = generateInitializationVector(factory);
            this.cipher = getCipher(Cipher.ENCRYPT_MODE, secretKey, initializationVector, synchronousFactory);
            this.profileId = factory.getProfileId();
        }
        
        /**
         * - GZIP
         * - Encrypt
         */
        @Override
        public OutputStream encrypt(OutputStream os) {
            CipherOutputStream cos = new CipherOutputStream(os, cipher);
            GZIPOutputStream zos;
            try {
                zos = new GZIPOutputStream(cos);
            } catch (IOException e) {
                throw new CryptoException(CryptoErrorCode.CP700, e, 
                        "Failed to create GZIP instance for encryption stream");
            }
            return zos;
        }
        
        @Override
        public ResourceInfo complete() {
            ResourceInfo resourceInfo = ResourceInfo.Factory.newInstance();
            resourceInfo.setProfile(profileId);
            resourceInfo.setIV(initializationVector.getIV());
            resourceInfo.setKey(secretKey.getEncoded());
            return resourceInfo;
        }
    }
}
