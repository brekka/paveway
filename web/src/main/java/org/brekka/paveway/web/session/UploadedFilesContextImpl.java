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

package org.brekka.paveway.web.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.model.UploadedFileInfo;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.web.model.UploadingFilesContext;

/**
 * Handles the uploaded files from creation to completion.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class UploadedFilesContextImpl implements UploadingFilesContext, UploadedFiles {

    private final UploadsContext context;

    private final String makerKey;

    private final UploadPolicy policy;

    private final List<FileBuilder> completed = new ArrayList<>();

    private final Map<String, FileBuilder> inProgress = new HashMap<>();

    private boolean done = false;

    private final Map<String, Object> attributes = new HashMap<>();


    /**
     * @param makerKey
     */
    public UploadedFilesContextImpl(final String makerKey, final UploadPolicy policy, final UploadsContext context) {
        this.makerKey = makerKey;
        this.policy = policy;
        this.context = context;
    }

    @Override
    public synchronized boolean isFileSlotAvailable() {
        if (this.done) {
            return false;
        }
        return this.policy.getMaxFiles() > (this.completed.size() + this.inProgress.size());
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#retain(java.lang.String, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    public void retain(final String fileName, final FileBuilder fileBuilder) {
        this.inProgress.put(fileName, fileBuilder);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#retrieve(java.lang.String)
     */
    @Override
    public FileBuilder retrieve(final String fileName) {
        return this.inProgress.get(fileName);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#complete(org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    public void transferComplete(final FileBuilder fileBuilder) {
        this.inProgress.remove(fileBuilder.getFileName());
        this.completed.add(fileBuilder);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#getPolicy()
     */
    @Override
    public UploadPolicy getPolicy() {
        return this.policy;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.session.Files#isDone()
     */
    @Override
    public boolean isDone() {
        return this.done;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.session.Files#previewCompleted()
     */
    @Override
    public List<UploadedFileInfo> previewReady() {
        return new ArrayList<UploadedFileInfo>(this.completed);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.session.Files#complete()
     */
    @Override
    public synchronized List<CompletableUploadedFile> uploadComplete() {
        // Deallocate the files that were never completed
        Collection<FileBuilder> values = this.inProgress.values();
        discardAll(values);
        this.inProgress.clear();

        List<CompletableUploadedFile> fileBuilders = new ArrayList<CompletableUploadedFile>(this.completed);
        this.completed.clear();
        this.done = true;
        this.context.free(this.makerKey);
        return fileBuilders;
    }

    /**
     * @return the makerKey
     */
    public String getKey() {
        return this.makerKey;
    }


    @Override
    public synchronized void discard() {
        Collection<FileBuilder> values = this.inProgress.values();
        discardAll(values);
        this.inProgress.clear();
        discardAll(this.completed);
        this.completed.clear();
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.model.Files#addAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void addAttribute(final String key, final Object value) {
        this.attributes.put(key, value);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.model.Files#getAttribute(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(final String key, final Class<T> type) {
        Object object = this.attributes.get(key);
        if (object == null) {
            return null;
        }
        if (type.isAssignableFrom(object.getClass())) {
            return (T) object;
        }
        throw new PavewayException(PavewayErrorCode.PW600,
                "UploadedFiles[%s]: The attribute '%s' value type '%s' does not match the expected '%s'.",
                getKey(), key, object.getClass().getName(), type.getName());
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.model.Files#removeAttribute(java.lang.String)
     */
    @Override
    public void removeAttribute(final String key) {
        this.attributes.remove(key);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadedFiles#renameFileTo(java.util.UUID, java.lang.String)
     */
    @Override
    public void renameFileTo(final UUID id, final String name) {
        for (FileBuilder fileBuilder : this.completed) {
            if (fileBuilder.getId().equals(id)) {
                fileBuilder.renameTo(name);
                break;
            }
        }
    }

    private static void discardAll(final Collection<FileBuilder> fileBuilders) {
        for (FileBuilder fileBuilder : fileBuilders) {
            fileBuilder.discard();
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.web.model.UploadingFilesContext#discard(org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    public synchronized boolean discard(final String fileName) {
        FileBuilder found = null;
        for (FileBuilder fileBuilder : this.completed) {
            if (fileBuilder.getFileName().equals(fileName)) {
                found = fileBuilder;
                break;
            }
        }
        for (FileBuilder fileBuilder : this.inProgress.values()) {
            if (fileBuilder.getFileName().equals(fileName)) {
                found = fileBuilder;
                break;
            }
        }
        if (found != null) {
            boolean removed = this.completed.remove(found);
            removed |= this.inProgress.remove(found.getFileName()) != null;
            found.discard();
            return removed;
        }
        return false;
    }

}
