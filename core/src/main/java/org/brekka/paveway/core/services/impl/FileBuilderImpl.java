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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.model.ResourceEncryptor;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.DigestResult;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.services.DigestCryptoService;

class FileBuilderImpl implements FileBuilder {
    
    private final ResourceCryptoService resourceCryptoService;
    
    private final ResourceStorageService resourceStorageService;
    
    private final DigestCryptoService digestCryptoService;
    
    private final CryptedFile cryptedFile;
    
    private final CryptoProfile cryptoProfile;
    
    private final List<PartAllocatorImpl> partAllocators = new ArrayList<>();
    
    /**
     * Length of parts so far
     */
    private final AtomicLong uploadedBytesCount = new AtomicLong();
    
    private final UploadPolicy policy;
    
    public FileBuilderImpl(CryptedFile cryptedFile, CryptoProfile cryptoProfile, DigestCryptoService digestCryptoService,
            ResourceCryptoService resourceCryptoService, ResourceStorageService resourceStorageService, UploadPolicy policy) {
        this.digestCryptoService = digestCryptoService;
        this.resourceCryptoService = resourceCryptoService;
        this.resourceStorageService = resourceStorageService;
        this.cryptoProfile = cryptoProfile;
        this.cryptedFile = cryptedFile;
        this.policy = policy;
    }
    
    public void setLength(long length) {
        cryptedFile.setOriginalLength(length);
    }
    
    public long getLength() {
        return cryptedFile.getOriginalLength();
    }
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return cryptedFile.getFileName();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadedFileInfo#getMimeType()
     */
    @Override
    public String getMimeType() {
        return cryptedFile.getMimeType();
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#allocatePart(java.io.OutputStream, long, long)
     */
    @Override
    public PartAllocator allocatePart() {
        if (uploadedBytesCount.longValue() >= policy.getMaxFileSize()) {
            throw new PavewayException(PavewayErrorCode.PW700, "File has grown too large");
        }
        CryptedPart part = new CryptedPart();
        UUID partId = UUID.randomUUID();
        part.setId(partId);
        part.setFile(cryptedFile);
        cryptedFile.getParts().add(part);
        ByteSequence partDestination = resourceStorageService.allocate(partId);
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(
                cryptedFile.getSecretKey(), cryptedFile.getCompression());
        StreamCryptor<OutputStream, DigestResult> digester = digestCryptoService.outputDigester(cryptoProfile);
        PartAllocatorImpl partAllocatorImpl = new PartAllocatorImpl(encryptor, part, 
                digester, partDestination, uploadedBytesCount);
        partAllocators.add(partAllocatorImpl);
        return partAllocatorImpl;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#isComplete()
     */
    @Override
    public synchronized boolean isTransferComplete() {
        boolean complete = false;
        List<CryptedPart> parts = cryptedFile.getParts();
        if (parts.size() == 1) {
            CryptedPart cryptedPart = parts.get(0);
            complete = (cryptedPart.getLength() == cryptedFile.getOriginalLength());
        } else {
            List<CryptedPart> sortedParts = PavewayServiceImpl.sortByOffset(parts);
            long length = 0;
            for (CryptedPart cryptedPart : sortedParts) {
                if (length == cryptedPart.getOffset()) {
                    length += cryptedPart.getLength();
                }
            }
            complete = (length == cryptedFile.getOriginalLength());
        }
        return complete;
    }
    
    /**
     * @return the cryptedFile
     */
    CryptedFile getCryptedFile() {
        return cryptedFile;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FileBuilder#discard()
     */
    @Override
    public void discard() {
        List<PartAllocatorImpl> partAllocators = this.partAllocators;
        for (PartAllocatorImpl partAllocatorImpl : partAllocators) {
            ByteSequence partDestination = partAllocatorImpl.getPartDestination();
            resourceStorageService.remove(partDestination.getId());
        }
        this.partAllocators.clear();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("fileName", cryptedFile.getFileName())
            .append("mimeType", cryptedFile.getMimeType())
            .toString();
    }
}