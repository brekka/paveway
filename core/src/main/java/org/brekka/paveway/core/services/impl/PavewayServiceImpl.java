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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Paveway service implementation
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
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

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#begin(java.lang.String)
     */
    @Override
    public FileBuilder beginUpload(final String fileName, final String mimeType, final UploadPolicy uploadPolicy) {
        Compression compression = Compression.NONE;
        // TODO more mime types that can be compressed
        if (mimeType.startsWith("text")) {
            compression = Compression.GZIP;
        }
        CryptoProfile cryptoProfile = cryptoProfileService.retrieveDefault();

        CryptedFile cryptedFile = new CryptedFile();
        cryptedFile.setParts(new ArrayList<CryptedPart>());
        cryptedFile.setCompression(compression);
        cryptedFile.setProfile(cryptoProfile.getNumber());
        cryptedFile.setFileName(fileName);
        cryptedFile.setMimeType(mimeType);
        SecretKey secretKey = symmetricCryptoService.createSecretKey(cryptoProfile);
        cryptedFile.setSecretKey(secretKey);

        return new FileBuilderImpl(cryptedFile, cryptoProfile, digestCryptoService,
                resourceCryptoService, resourceStorageService, uploadPolicy);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public CryptedFile complete(final CompletableUploadedFile completableFile) {
        FileBuilderImpl fileBuilderImpl = narrow(completableFile);
        CryptedFile cryptedFile = fileBuilderImpl.getCryptedFile();
        List<CryptedPart> parts = cryptedFile.getParts();
        for (CryptedPart cryptedPart : parts) {
            cryptedPartDAO.create(cryptedPart);
        }
        cryptedFileDAO.create(cryptedFile);
        return cryptedFile;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#removeFile(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void removeFile(final CryptedFile cryptedFile) {
        CryptedFile managedCryptedFile = cryptedFileDAO.retrieveById(cryptedFile.getId());
        List<CryptedPart> parts = managedCryptedFile.getParts();
        for (CryptedPart part : parts) {
            cryptedPartDAO.delete(part.getId());
            resourceStorageService.remove(part.getId());
        }
        cryptedFileDAO.delete(managedCryptedFile.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.PavewayService#retrieveCryptedFileById(java.util.UUID)
     */
    @Override
    public CryptedFile retrieveCryptedFileById(final UUID id) {
        return cryptedFileDAO.retrieveById(id);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public InputStream download(final CryptedFile cryptedFile) {
        CryptedFile managedFile = cryptedFileDAO.retrieveById(cryptedFile.getId());
        if (cryptedFile.getSecretKey() == null) {
            throw new PavewayException(PavewayErrorCode.PW673, "The secret key must be set on the crypted file '%s'",
                    managedFile.getId());
        }
        return new MultipartInputStream(managedFile, resourceStorageService, resourceCryptoService);
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
