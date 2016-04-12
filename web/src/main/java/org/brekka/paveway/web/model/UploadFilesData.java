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
