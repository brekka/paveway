/**
 * Copyright (c) 2016 Digital Shadows Ltd.
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
}
