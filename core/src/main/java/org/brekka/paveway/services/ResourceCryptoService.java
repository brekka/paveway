package org.brekka.paveway.services;

import java.io.InputStream;

public interface ResourceCryptoService {

    ResourceEncryptor encryptor();

    InputStream decrypt(int cryptoProfileId, byte[] cryptoIv, byte[] symmetricKey, InputStream inputStream);
}
