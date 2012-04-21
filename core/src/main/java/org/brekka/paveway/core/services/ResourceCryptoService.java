package org.brekka.paveway.core.services;

import java.io.InputStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.brekka.paveway.core.model.Compression;

public interface ResourceCryptoService {

    ResourceEncryptor encryptor(SecretKey secretKey, Compression compression);

    InputStream decryptor(int cryptoProfileId, Compression compression, IvParameterSpec iv, SecretKey secretKey, InputStream is);
}
