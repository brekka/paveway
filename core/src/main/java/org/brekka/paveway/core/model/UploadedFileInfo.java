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

import java.util.UUID;

/**
 * Information about a recently uploaded file.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface UploadedFileInfo {

    /**
     * ID
     * @return
     */
    UUID getId();

    /**
     * File name
     * @return
     */
    String getFileName();

    /**
     * File length in bytes
     * @return
     */
    long getLength();

    /**
     * The MIME type
     * @return
     */
    String getMimeType();
}
