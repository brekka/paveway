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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.model.ResourceEncryptor;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.StreamCryptor;

/**
 * @author Andrew Taylor
 *
 */
public class PartAllocatorImpl implements PartAllocator {

    private static final Log log = LogFactory.getLog(PartAllocatorImpl.class);

    private final ResourceEncryptor resourceEncryptor;

    private final PavewayService pavewayService;

    private final CryptedPart cryptedPart;

    private final StreamCryptor<OutputStream, DigestResult> digester;

    private final ResourceStorageService resourceStorageService;

    /**
     * This is where the encrypted data will be stored.
     */
    private final ByteSequence partDestination;

    private final AtomicLong uploadedBytesCount;

    private CountingOutputStream counter;


    public PartAllocatorImpl(final CryptedFile cryptedFile, final ResourceEncryptor resourceEncryptor, final PavewayService pavewayService,
            final ResourceStorageService resourceStorageService, final StreamCryptor<OutputStream, DigestResult> digester,
            final AtomicLong uploadedBytesCount) {
        CryptedPart part = new CryptedPart();
        part.setId(UUID.randomUUID());
        part.setFile(cryptedFile);

        this.cryptedPart = part;
        this.partDestination = resourceStorageService.allocate(part.getId());
        this.resourceEncryptor = resourceEncryptor;
        this.pavewayService = pavewayService;
        this.resourceStorageService = resourceStorageService;
        this.digester = digester;
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
        cryptedPart.setIv(resourceEncryptor.getSpec().getIv());
        cryptedPart.setOriginalChecksum(digester.getSpec().getDigest());
        cryptedPart.setEncryptedChecksum(resourceEncryptor.getDigestResult().getDigest());
        cryptedPart.setLength(counter.getByteCount());
        cryptedPart.setOffset(offset);
        StopWatch sw = new StopWatch();
        sw.start();
        pavewayService.createPart(cryptedPart);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Part offset %d created in %d ms for file '%s'", offset, sw.getTime(), cryptedPart.getFile().getFileName()));
        }
        uploadedBytesCount.addAndGet(counter.getByteCount());
    }

    @Override
    public void discard() {
        resourceStorageService.remove(cryptedPart.getId());
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
