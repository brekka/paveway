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

package org.brekka.paveway.web.model;

import java.util.List;

import org.brekka.paveway.core.model.CompletableFile;
import org.brekka.paveway.core.model.FileInfo;
import org.brekka.paveway.core.model.UploadPolicy;

/**
 * TODO Description of Files
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface Files {

    boolean isDone();

    List<FileInfo> previewReady();

    List<CompletableFile> retrieveReady();
    
    void discard();

    UploadPolicy getPolicy();
    
    void addAttribute(String key, Object value);
    
    <T> T getAttribute(String key, Class<T> type);
    
    void removeAttribute(String key);
}