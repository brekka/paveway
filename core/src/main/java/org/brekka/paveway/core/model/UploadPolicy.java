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

/**
 * An upload policy determines the number and size of files a given user can send.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface UploadPolicy {

    /**
     * The maximum number of files
     * @return
     */
    int getMaxFiles();
    
    /**
     * The maximum allowed file size
     * @return
     */
    int getMaxFileSize();
    
    /**
     * Maximum request size for DOS protection
     * @return
     */
    int getMaxSize();
    
    /**
     * The cluster size
     * @return
     */
    int getClusterSize();
}
