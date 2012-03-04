package org.brekka.paveway.core.services;

import java.io.InputStream;

import javax.crypto.SecretKey;

import org.brekka.paveway.core.model.Compression;

public interface ResourceCryptoService {

    ResourceEncryptor encryptor(SecretKey secretKey, Compression compression);

    InputStream decrypt(int cryptoProfileId, byte[] cryptoIv, byte[] symmetricKey, InputStream inputStream);
}
