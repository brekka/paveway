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

import org.brekka.paveway.core.model.UploadPolicy;

/**
 * Basic upload policy
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class BasicUploadPolicy implements UploadPolicy {

    private final int maxFiles;
    
    private final int maxFileSize;
    
    private final int maxSize;
    
    private final int clusterSize;
    
    
    
    public BasicUploadPolicy(int maxFiles, int maxFileSize, int maxSize, int clusterSize) {
        this.maxFiles = maxFiles;
        this.maxFileSize = maxFileSize;
        this.maxSize = maxSize;
        this.clusterSize = clusterSize;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getMaxFiles()
     */
    @Override
    public int getMaxFiles() {
        return maxFiles;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getMaxFileSize()
     */
    @Override
    public int getMaxFileSize() {
        return maxFileSize;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getMaxSize()
     */
    @Override
    public int getMaxSize() {
        return maxSize;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getClusterSize()
     */
    @Override
    public int getClusterSize() {
        return clusterSize;
    }

}
