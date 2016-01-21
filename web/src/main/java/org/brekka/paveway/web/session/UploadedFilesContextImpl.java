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

package org.brekka.paveway.web.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.model.UploadedFileInfo;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.paveway.core.services.impl.FileBuilderImpl;
import org.brekka.paveway.web.model.UploadFileData;
import org.brekka.paveway.web.model.UploadFilesData;
import org.brekka.paveway.web.model.UploadingFilesContext;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.services.DigestCryptoService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;

/**
 * Handles the uploaded files from creation to completion.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class UploadedFilesContextImpl implements UploadingFilesContext, UploadedFiles {

    private final UploadsContext context;

    private final UploadFilesData filesData;


    public UploadedFilesContextImpl(final UploadFilesData filesData, final UploadsContext context) {
        this.filesData = filesData;
        this.context = context;
    }

    @Override
    public synchronized boolean isFileSlotAvailable() {
        if (filesData.isDone()) {
            return false;
        }
        List<UploadFileData> files = filesData.getFiles();
        return filesData.getUploadPolicy().getMaxFiles() > files.size();
    }

    @Override
    public FileBuilder fileBuilder(final String filename, final String mimeType) {
        UploadFileData fileData = fileData(filename);

        FileBuilderImpl fileBuilder;

        if (fileData == null) {
            PavewayService pavewayService = context.getApplicationContext().getBean(PavewayService.class);
            fileBuilder = (FileBuilderImpl) pavewayService.beginUpload(filename, mimeType, filesData.getUploadPolicy());
            CryptedFile cryptedFile = fileBuilder.getCryptedFile();
            fileData = new UploadFileData(filename, mimeType, cryptedFile.getId(), cryptedFile.getSecretKey().getEncoded());
            synchronized (filesData.getFiles()) {
                filesData.getFiles().add(fileData);
            }
        } else {
            fileBuilder = toFileBuilder(fileData);
        }
        context.setDirty(true);
        return fileBuilder;
    }

    @Override
    public void retain(final String fileName, final FileBuilder fileBuilder) {
        FileBuilderImpl xFileBuilder = (FileBuilderImpl) fileBuilder;
        CryptedFile cryptedFile = xFileBuilder.getCryptedFile();
        UploadFileData fileData = new UploadFileData(cryptedFile.getFileName(), cryptedFile.getMimeType(), cryptedFile.getId(),
                cryptedFile.getSecretKey().getEncoded());
        synchronized (filesData.getFiles()) {
            filesData.getFiles().add(fileData);
        }
        context.setDirty(true);
    }

    @Override
    public FileBuilder retrieveFile(final String fileName) {
        FileBuilder fileBuilder = null;
        UploadFileData fileData = fileData(fileName);
        if (fileData != null) {
            fileBuilder = toFileBuilder(fileData);
        }
        return fileBuilder;
    }

    @Override
    public void transferComplete(final String fileName) {
        UploadFileData fileData = fileData(fileName);
        if (fileData != null) {
            fileData.setComplete(true);
            context.setDirty(true);
        } else {
            throw new IllegalArgumentException(String.format("No file '%s' to complete", fileName));
        }
    }


    @Override
    public List<UploadedFileInfo> previewReady() {
        List<UploadedFileInfo> infos = new ArrayList<>();
        for (UploadFileData fileData : filesData.getFiles()) {
            if (fileData.isComplete()) {
                infos.add(fileData);
            }
        }
        return infos;
    }

    @Override
    public synchronized List<CompletableUploadedFile> uploadComplete() {
        List<CompletableUploadedFile> fileBuilders = new ArrayList<>();
        List<UploadFileData> files = filesData.getFiles();
        synchronized (files) {
            Iterator<UploadFileData> fileIterator = files.iterator();
            while (fileIterator.hasNext()) {
                UploadFileData uploadFileData = fileIterator.next();
                FileBuilderImpl fileBuilder = toFileBuilder(uploadFileData);
                if (uploadFileData.isComplete()) {
                    fileBuilders.add(fileBuilder);
                } else {
                    fileBuilder.discard();
                    fileIterator.remove();
                }
            }
        }
        filesData.setDone(true);
        context.free(filesData.getMakerKey());
        return fileBuilders;
    }

    public String getKey() {
        return filesData.getMakerKey();
    }

    @Override
    public List<UploadedFileInfo> files() {
        return previewReady();
    }

    @Override
    public boolean isDone() {
        return filesData.isDone();
    }

    @Override
    public UploadPolicy getPolicy() {
        return filesData.getUploadPolicy();
    }

    @Override
    public synchronized void discard() {
        List<UploadFileData> files = filesData.getFiles();
        synchronized (files) {
            Iterator<UploadFileData> fileIterator = files.iterator();
            while (fileIterator.hasNext()) {
                UploadFileData uploadFileData = fileIterator.next();
                FileBuilderImpl fileBuilder = toFileBuilder(uploadFileData);
                fileBuilder.discard();
                fileIterator.remove();
            }
        }
        context.setDirty(true);
    }

    @Override
    public void addAttribute(final String key, final Object value) {
        filesData.getAttributes().put(key, value);
        context.setDirty(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(final String key, final Class<T> type) {
        Object object = filesData.getAttributes().get(key);
        if (object == null) {
            return null;
        }
        if (type.isAssignableFrom(object.getClass())) {
            return (T) object;
        }
        throw new PavewayException(PavewayErrorCode.PW600,
                "UploadedFiles[%s]: The attribute '%s' value type '%s' does not match the expected '%s'.",
                getKey(), key, object.getClass().getName(), type.getName());
    }

    @Override
    public void removeAttribute(final String key) {
        filesData.getAttributes().remove(key);
        context.setDirty(true);
    }

    @Override
    public void renameFileTo(final UUID id, final String name) {
        for (UploadFileData fileData : filesData.getFiles()) {
            if (fileData.getId().equals(id)) {
                FileBuilderImpl fileBuilder = toFileBuilder(fileData);
                fileBuilder.renameTo(name);
                context.setDirty(true);
                break;
            }
        }
    }


    @Override
    public synchronized boolean discard(final String fileName) {
        UploadFileData fileData = fileData(fileName);
        if (fileData != null) {
            FileBuilderImpl fileBuilder = toFileBuilder(fileData);
            fileBuilder.discard();
            synchronized (filesData.getFiles()) {
                filesData.getFiles().remove(fileData);
            }
            context.setDirty(true);
            return true;
        }
        return false;
    }

    /**
     * @param filename
     * @return
     */
    private UploadFileData fileData(final String filename) {
        UploadFileData fileData = null;
        List<UploadFileData> files = filesData.getFiles();
        for (UploadFileData uploadFileData : files) {
            if (Objects.equals(uploadFileData.getFileName(), filename)) {
                fileData = uploadFileData;
                break;
            }
        }
        return fileData;
    }

    private FileBuilderImpl toFileBuilder(final UploadFileData fileData) {
        PavewayService pavewayService = context.getApplicationContext().getBean(PavewayService.class);
        SymmetricCryptoService symmetricCryptoService = context.getApplicationContext().getBean(SymmetricCryptoService.class);
        DigestCryptoService digestCryptoService = context.getApplicationContext().getBean(DigestCryptoService.class);
        ResourceCryptoService resourceCryptoService = context.getApplicationContext().getBean(ResourceCryptoService.class);
        ResourceStorageService resourceStorageService = context.getApplicationContext().getBean(ResourceStorageService.class);

        CryptedFile cryptedFile = pavewayService.retrieveCryptedFileById(fileData.getCryptedFileId());
        if (cryptedFile == null) {
            throw new IllegalStateException(String.format("Crypted file '%s' not found for '%s'",
                    fileData.getCryptedFileId(), fileData.getFileName()));
        }
        cryptedFile.setMimeType(fileData.getMimeType());
        cryptedFile.setFileName(fileData.getFileName());
        CryptoProfile cryptoProfile = CryptoProfile.Static.of(cryptedFile.getProfile());
        SecretKey secretKey = symmetricCryptoService.toSecretKey(fileData.getSecretKey(), cryptoProfile);
        cryptedFile.setSecretKey(secretKey);

        return new FileBuilderImpl(cryptedFile, cryptoProfile, pavewayService, digestCryptoService,
                resourceCryptoService, resourceStorageService, filesData.getUploadPolicy());
    }
}
