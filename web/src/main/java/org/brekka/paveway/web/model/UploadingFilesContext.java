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

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;

/**
 * Operations for files in the process of being uploaded.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface UploadingFilesContext {

    /**
     * Retrieve the policy
     *
     * @return the policy
     */
    UploadPolicy getPolicy();


    FileBuilder fileBuilder(String filename, String mimeType);

    /**
     * Bind the file builder to this context.
     *
     * @param name
     *            the name to bind the file as.
     * @param fileBuilder
     *            the file builder to bind.
     */
    void retain(String name, FileBuilder fileBuilder);

    /**
     * Mark the file as having had all its parts transferred.
     *
     * @param fileName
     *            the now fully transferred file.
     */
    void transferComplete(final String fileName);

    /**
     * Can another file be added?
     *
     * @return true if the policy allows more files to be added.
     */
    boolean isFileSlotAvailable();

    /**
     * Is this files context completed, ie can more files be uploaded? Once the uploaded files have been persisted, no
     * more can be added.
     *
     * @return true if no more files can be uploaded.
     */
    boolean isDone();

    /**
     * Discard all files currently associated with this context.
     */
    void discard();

    /**
     * Discard a single file from this builder.
     *
     * @param fileName
     * @return true if the file was found and removed.
     */
    boolean discard(String fileName);


    /**
     * @param fileName
     * @return
     */
    FileBuilder retrieveFile(String fileName);

}
