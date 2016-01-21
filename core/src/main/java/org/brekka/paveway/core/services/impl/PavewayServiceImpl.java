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


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.dao.CryptedPartDAO;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.api.services.DigestCryptoService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Paveway service implementation
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
public class PavewayServiceImpl implements PavewayService {

    @Autowired
    private CryptoProfileService cryptoProfileService;

    @Autowired
    private SymmetricCryptoService symmetricCryptoService;

    @Autowired
    private DigestCryptoService digestCryptoService;

    @Autowired
    private ResourceCryptoService resourceCryptoService;

    @Autowired
    private ResourceStorageService resourceStorageService;

    @Autowired
    private CryptedFileDAO cryptedFileDAO;

    @Autowired
    private CryptedPartDAO cryptedPartDAO;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public FileBuilder beginUpload(final String fileName, final String mimeType, final UploadPolicy uploadPolicy) {
        Compression compression = Compression.NONE;
        // TODO more mime types that can be compressed
        if (mimeType.startsWith("text")) {
            compression = Compression.GZIP;
        }
        CryptoProfile cryptoProfile = this.cryptoProfileService.retrieveDefault();

        CryptedFile cryptedFile = new CryptedFile();
        // Set early
        cryptedFile.setId(UUID.randomUUID());
        cryptedFile.setParts(new ArrayList<CryptedPart>());
        cryptedFile.setCompression(compression);
        cryptedFile.setProfile(cryptoProfile.getNumber());
        cryptedFile.setFileName(fileName);
        cryptedFile.setMimeType(mimeType);
        SecretKey secretKey = this.symmetricCryptoService.createSecretKey(cryptoProfile);
        cryptedFile.setSecretKey(secretKey);
        cryptedFile.setStaged(Boolean.TRUE);
        cryptedFileDAO.create(cryptedFile);

        return new FileBuilderImpl(cryptedFile, cryptoProfile, applicationContext.getBean(PavewayService.class), this.digestCryptoService,
                this.resourceCryptoService, this.resourceStorageService, uploadPolicy);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void createPart(final CryptedPart cryptedPart) {
        cryptedPartDAO.create(cryptedPart);
    }

    @Override
    @Transactional()
    public CryptedFile complete(final CompletableUploadedFile completableFile) {
        FileBuilderImpl fileBuilderImpl = narrow(completableFile);
        CryptedFile cryptedFile = cryptedFileDAO.retrieveById(fileBuilderImpl.getCryptedFile().getId());
        cryptedFile.setStaged(null);
        this.cryptedFileDAO.update(cryptedFile);
        return cryptedFile;
    }

    @Override
    @Transactional()
    public void removeFile(final CryptedFile cryptedFile) {
        CryptedFile managedCryptedFile = this.cryptedFileDAO.retrieveById(cryptedFile.getId());
        List<CryptedPart> parts = managedCryptedFile.getParts();
        for (CryptedPart part : parts) {
            this.cryptedPartDAO.delete(part.getId());
            this.resourceStorageService.remove(part.getId());
        }
        this.cryptedFileDAO.delete(managedCryptedFile.getId());
    }

    @Override
    @Transactional()
    public void setFileLength(final CryptedFile cryptedFile, final long length) {
        CryptedFile managedCryptedFile = this.cryptedFileDAO.retrieveById(cryptedFile.getId());
        managedCryptedFile.setOriginalLength(length);
        cryptedFileDAO.update(managedCryptedFile);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public boolean isTransferComplete(final CryptedFile cryptedFile) {
        CryptedFile file = cryptedFileDAO.retrieveById(cryptedFile.getId());
        boolean complete = false;
        List<CryptedPart> parts = file.getParts();
        if (parts.size() == 1) {
            CryptedPart cryptedPart = parts.get(0);
            complete = (cryptedPart.getLength() == file.getOriginalLength());
        } else {
            List<CryptedPart> sortedParts = PavewayServiceImpl.sortByOffset(parts);
            long length = 0;
            for (CryptedPart cryptedPart : sortedParts) {
                if (length == cryptedPart.getOffset()) {
                    length += cryptedPart.getLength();
                }
            }
            complete = (length == file.getOriginalLength());
        }
        return complete;
    }

    @Override
    @Transactional(readOnly=true)
    public CryptedFile retrieveCryptedFileById(final UUID id) {
        return this.cryptedFileDAO.retrieveById(id);
    }

    @Override
    @Transactional()
    public InputStream download(final CryptedFile cryptedFile) {
        CryptedFile managedFile = this.cryptedFileDAO.retrieveById(cryptedFile.getId());
        if (cryptedFile.getSecretKey() == null) {
            throw new PavewayException(PavewayErrorCode.PW673, "The secret key must be set on the crypted file '%s'",
                    managedFile.getId());
        }
        return new MultipartInputStream(managedFile, this.resourceStorageService, this.resourceCryptoService);
    }

    protected FileBuilderImpl narrow(final CompletableUploadedFile file) {
        if (file instanceof FileBuilderImpl == false) {
            throw new PavewayException(PavewayErrorCode.PW300, "Not a managed file builder instance");
        }
        return (FileBuilderImpl) file;
    }


    static List<CryptedPart> sortByOffset(final Collection<CryptedPart> parts) {
        List<CryptedPart> sortedParts = new ArrayList<>(parts);
        Collections.sort(sortedParts, new Comparator<CryptedPart>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final CryptedPart o1, final CryptedPart o2) {
                return Long.valueOf(o1.getOffset()).compareTo(o2.getOffset());
            }
        });
        return sortedParts;
    }
}
