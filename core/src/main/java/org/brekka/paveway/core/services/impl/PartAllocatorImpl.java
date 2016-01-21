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

package org.brekka.paveway.core.services.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.output.CountingOutputStream;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.model.ResourceEncryptor;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.StreamCryptor;

/**
 * @author Andrew Taylor
 *
 */
public class PartAllocatorImpl implements PartAllocator {

    private final ResourceEncryptor resourceEncryptor;

    private final PavewayService pavewayService;

    private final CryptedPart cryptedPart;

    private final StreamCryptor<OutputStream, DigestResult> digester;

    /**
     * This is where the encrypted data will be stored.
     */
    private final ByteSequence partDestination;

    private final AtomicLong uploadedBytesCount;

    private CountingOutputStream counter;


    public PartAllocatorImpl(final ResourceEncryptor resourceEncryptor, final PavewayService pavewayService, final CryptedPart cryptedPart,
            final StreamCryptor<OutputStream, DigestResult> digester, final ByteSequence partDestination, final AtomicLong uploadedBytesCount) {
        this.resourceEncryptor = resourceEncryptor;
        this.pavewayService = pavewayService;
        this.cryptedPart = cryptedPart;
        this.digester = digester;
        this.partDestination = partDestination;
        this.uploadedBytesCount = uploadedBytesCount;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStream encryptingOs = resourceEncryptor.encrypt(partDestination.getOutputStream());
        OutputStream dos = digester.getStream(encryptingOs);
        counter = new CountingOutputStream(dos);
        return counter;
    }

    @Override
    public void complete(final long offset) {
        cryptedPart.setIv(resourceEncryptor.getSpec().getIV());
        cryptedPart.setOriginalChecksum(digester.getSpec().getDigest());
        cryptedPart.setEncryptedChecksum(resourceEncryptor.getDigestResult().getDigest());
        cryptedPart.setLength(counter.getByteCount());
        cryptedPart.setOffset(offset);
        pavewayService.createPart(cryptedPart);
        uploadedBytesCount.addAndGet(counter.getByteCount());
    }

    @Override
    public long getLength() {
        return cryptedPart.getLength();
    }

    /**
     * @return the partDestination
     */
    ByteSequence getPartDestination() {
        return partDestination;
    }

    /**
     * @return the cryptedPart
     */
    CryptedPart getCryptedPart() {
        return cryptedPart;
    }

}
