/*
 * Copyright 2016 the original author or authors.
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

package org.brekka.paveway.web.model;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.brekka.paveway.core.model.UploadedFileInfo;

/**
 *
 */
public class UploadFileData implements Serializable, UploadedFileInfo {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 5194754497442897425L;

    private final String fileName;

    /**
     * The secret key
     */
    private final byte[] secretKey;

    private final String mimeType;

    private final UUID cryptedFileId;

    private final AtomicLong uploadedBytesCount = new AtomicLong();

    private long length;

    private boolean complete;

    public UploadFileData(final String fileName, final String mimeType, final UUID cryptedFileId, final byte[] secretKey) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.cryptedFileId = cryptedFileId;
        this.secretKey = secretKey;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }

    @Override
    public UUID getId() {
        return cryptedFileId;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    public UUID getCryptedFileId() {
        return cryptedFileId;
    }

    public AtomicLong getUploadedBytesCount() {
        return uploadedBytesCount;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(final boolean complete) {
        this.complete = complete;
    }

    @Override
    public long getLength() {
        return length;
    }

    public void setLength(final long length) {
        this.length = length;
    }

    public UploadFileData renameTo(final String name) {
        if (!complete) {
            throw new IllegalStateException("A file can only be renamed once it has been completed");
        }
        UploadFileData newFileData = new UploadFileData(name, mimeType, cryptedFileId, secretKey);
        newFileData.length = length;
        newFileData.complete = complete;
        newFileData.uploadedBytesCount.set(uploadedBytesCount.get());
        return newFileData;
    }
}
