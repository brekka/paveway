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

package org.brekka.paveway.core.model;

import java.io.Serializable;

/**
 * Basic upload policy
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class BasicUploadPolicy implements UploadPolicy, Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6785749481139889238L;

    private final int maxFiles;

    private final int maxFileSize;

    private final int maxSize;

    private final int clusterSize;



    public BasicUploadPolicy(final int maxFiles, final int maxFileSize, final int maxSize, final int clusterSize) {
        this.maxFiles = maxFiles;
        this.maxFileSize = maxFileSize;
        this.maxSize = maxSize;
        this.clusterSize = clusterSize;
    }

    @Override
    public int getMaxFiles() {
        return maxFiles;
    }

    @Override
    public int getMaxFileSize() {
        return maxFileSize;
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public int getClusterSize() {
        return clusterSize;
    }
}
