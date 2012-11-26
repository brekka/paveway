package org.brekka.paveway.core.services;

import java.io.InputStream;

import org.brekka.paveway.core.model.Compression;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.SymmetricCryptoSpec;

public interface ResourceCryptoService {

    ResourceEncryptor encryptor(SecretKey secretKey, Compression compression);

    StreamCryptor<InputStream, SymmetricCryptoSpec> decryptor(SymmetricCryptoSpec spec, Compression compression);
}
