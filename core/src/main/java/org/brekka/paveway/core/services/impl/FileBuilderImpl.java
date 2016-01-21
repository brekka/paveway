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

import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.model.ResourceEncryptor;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.services.DigestCryptoService;

public class FileBuilderImpl implements FileBuilder {

    private final PavewayService pavewayService;

    private final ResourceCryptoService resourceCryptoService;

    private final ResourceStorageService resourceStorageService;

    private final DigestCryptoService digestCryptoService;

    private final CryptedFile cryptedFile;

    private final CryptoProfile cryptoProfile;

    /**
     * Length of parts so far
     */
    private final AtomicLong uploadedBytesCount = new AtomicLong();

    private final UploadPolicy policy;

    public FileBuilderImpl(final CryptedFile cryptedFile, final CryptoProfile cryptoProfile, final PavewayService pavewayService,
            final DigestCryptoService digestCryptoService, final ResourceCryptoService resourceCryptoService,
            final ResourceStorageService resourceStorageService, final UploadPolicy policy) {
        this.pavewayService = pavewayService;
        this.digestCryptoService = digestCryptoService;
        this.resourceCryptoService = resourceCryptoService;
        this.resourceStorageService = resourceStorageService;
        this.cryptoProfile = cryptoProfile;
        this.cryptedFile = cryptedFile;
        this.policy = policy;
    }

    @Override
    public UUID getId() {
        return this.cryptedFile.getId();
    }

    @Override
    public void setLength(final long length) {
        pavewayService.setFileLength(cryptedFile, length);
    }

    @Override
    public long getLength() {
        return this.cryptedFile.getOriginalLength();
    }

    @Override
    public String getFileName() {
        return this.cryptedFile.getFileName();
    }

    @Override
    public void renameTo(final String name) {
        if (!isTransferComplete()) {
            throw new IllegalStateException("A file can only be renamed once it has been completed");
        }
        this.cryptedFile.setFileName(name);
    }

    @Override
    public String getMimeType() {
        return this.cryptedFile.getMimeType();
    }

    @Override
    public PartAllocator allocatePart() {
        if (this.uploadedBytesCount.longValue() >= this.policy.getMaxFileSize()) {
            throw new PavewayException(PavewayErrorCode.PW700, "File has grown too large");
        }
        CryptedPart part = new CryptedPart();
        UUID partId = UUID.randomUUID();
        part.setId(partId);
        part.setFile(this.cryptedFile);
        ByteSequence partDestination = this.resourceStorageService.allocate(partId);
        ResourceEncryptor encryptor = this.resourceCryptoService.encryptor(this.cryptedFile.getSecretKey(), this.cryptedFile.getCompression());
        StreamCryptor<OutputStream, DigestResult> digester = this.digestCryptoService.outputDigester(this.cryptoProfile);
        return new PartAllocatorImpl(encryptor, pavewayService, part, digester, partDestination, this.uploadedBytesCount);
    }

    @Override
    public synchronized boolean isTransferComplete() {
        return pavewayService.isTransferComplete(cryptedFile);
    }

    public CryptedFile getCryptedFile() {
        return cryptedFile;
    }

    @Override
    public void discard() {
        pavewayService.removeFile(cryptedFile);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("fileName", this.cryptedFile.getFileName())
            .append("mimeType", this.cryptedFile.getMimeType())
            .toString();
    }
}