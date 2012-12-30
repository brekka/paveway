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

package org.brekka.paveway.web.upload;

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.brekka.paveway.core.model.FileBuilder;

/**
 * A encrypted {@link FileItemFactory} that supports files uploaded in multiple parts. 
 * Must be session-bound.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class EncryptedMultipartFileItemFactory extends DiskFileItemFactory {

    private final FileBuilder fileBuilder;
    private final String fileName;
    private final String contentType;
    
    /**
     * @param sizeThreshold
     * @param repository
     * @param fileName
     * @param contentType
     * @param fileBuilder
     */
    public EncryptedMultipartFileItemFactory(int sizeThreshold, File repository, String fileName, String contentType, FileBuilder fileBuilder) {
        super(sizeThreshold, repository);
        this.fileBuilder = fileBuilder;
        this.fileName = fileName;
        this.contentType = contentType;
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.disk.DiskFileItemFactory#createItem(java.lang.String, java.lang.String, boolean, java.lang.String)
     */
    @Override
    public FileItem createItem(String fieldName, String ignoredContentType, boolean isFormField, String ignoredFileName) {
        if (isFormField) {
            return super.createItem(fieldName, contentType, isFormField, fileName);
        }
        EncryptedFileItem result = new EncryptedFileItem(fieldName, this.contentType, 
                this.fileName, fileBuilder, getSizeThreshold(), getRepository());
        return result;
    }
}
