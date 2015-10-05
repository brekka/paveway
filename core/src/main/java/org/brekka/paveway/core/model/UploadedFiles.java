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

import java.util.List;
import java.util.UUID;

import org.brekka.paveway.core.services.PavewayService;

/**
 * A bundle of files that were uploaded together. For example they would all have been applied to one selection box.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface UploadedFiles {

    /**
     * Can this bundle receive any more files? Returns true once {@link #uploadComplete()} is called.
     *
     * @return true if {@link #uploadComplete()} has been invoked.
     */
    boolean isDone();

    /**
     * Complete this upload and return the list of individual files that need to be passed to a transactional context of
     * {@link PavewayService#complete(CompletableUploadedFile)} to be completed. Once this method is called, responsibility for
     * discarding the files falls to the caller.
     *
     * @return the list of files that are awaiting completion.
     */
    List<CompletableUploadedFile> uploadComplete();

    /**
     * View the list of files that have been uploaded without completing the upload (ie more files can be uploaded).
     *
     * @return the list of uploaded files.
     */
    List<UploadedFileInfo> previewReady();

    /**
     * Retrieve the policy that was applied to these uploaded files.
     *
     * @return the policy
     */
    UploadPolicy getPolicy();

    /**
     * Discard all uploaded files, releasing the underlying storage.
     */
    void discard();

    /**
     * Add an attribute to this uploaded file for temporary storage during the upload process.
     * @param key
     * @param value
     */
    void addAttribute(String key, Object value);

    /**
     * Retrieve an attribute.
     * @param key
     * @param type
     * @return
     */
    <T> T getAttribute(String key, Class<T> type);

    /**
     * Remove a previously added attribute.
     * @param key
     */
    void removeAttribute(String key);

    /**
     * @param id
     * @param name
     */
    void renameFileTo(UUID id, String name);
    
    /**
     * Once completed, returns the read-only information about the uploaded files.
     * @return
     */
    List<UploadedFileInfo> files();
}