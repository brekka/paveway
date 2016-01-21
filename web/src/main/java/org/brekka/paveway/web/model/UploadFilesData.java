/**
 * Copyright (c) 2016 Digital Shadows Ltd.
 */

package org.brekka.paveway.web.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.brekka.paveway.core.model.UploadPolicy;

/**
 *
 */
public class UploadFilesData implements Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8181336445421395741L;


    private final String makerKey;

    private final UploadPolicy uploadPolicy;

    private final List<UploadFileData> files = new LinkedList<>();

    private final Map<String, Object> attributes = new HashMap<>();

    private boolean done = false;

    public UploadFilesData(final String makerKey, final UploadPolicy uploadPolicy) {
        this.makerKey = makerKey;
        this.uploadPolicy = uploadPolicy;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(final boolean done) {
        this.done = done;
    }

    public String getMakerKey() {
        return makerKey;
    }

    public List<UploadFileData> getFiles() {
        return files;
    }

    public UploadPolicy getUploadPolicy() {
        return uploadPolicy;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
