package org.brekka.paveway.core.services;

import java.io.OutputStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.brekka.paveway.core.model.Compression;

public interface ResourceCryptoService {

    ResourceEncryptor encryptor(SecretKey secretKey, Compression compression);

    OutputStream decryptor(int cryptoProfileId, Compression compression, IvParameterSpec iv, SecretKey secretKey, OutputStream os);
}
