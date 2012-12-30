/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.paveway.core.model;

import java.io.OutputStream;

import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.SymmetricCryptoSpec;

/**
 * Encapsulates an operation to encrypt a resource.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ResourceEncryptor {

    /**
     * Wraps the specified {@link OutputStream} with out own combination of {@link OutputStream}s that perform
     * compression, encryption, digesting prior to the bytes being written to <code>os</code>.
     * 
     * @param os
     *            the ulitmate destination of the bytes being transformed by this {@link ResourceEncryptor}.
     * @return the wrapping {@link OutputStream} that the bytes to be encrypted should be written to.
     */
    OutputStream encrypt(OutputStream os);

    /**
     * The digest of the ciphertext. Must only be called once the {@link OutputStream} returned by
     * {@link #encrypt(OutputStream)} has been closed.
     * 
     * @return the cipertext digest.
     */
    DigestResult getDigestResult();

    /**
     * The symmetric crypto specification containing the secret key and IV used in this encryption operation.
     * 
     * @return the crypto spec.
     */
    SymmetricCryptoSpec getSpec();
}
