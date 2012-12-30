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
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.PavewayService;

/**
 * {@link FileItemFactory} that creates {@link EncryptedFileItem}s.
 * 
 * @author Andrew Taylor
 */
public class EncryptedFileItemFactory extends DiskFileItemFactory {

    protected final transient PavewayService pavewayService;
    
    private final UploadPolicy uploadPolicy;
    
    public EncryptedFileItemFactory(int sizeThreshold, File repository, 
            PavewayService pavewayService, UploadPolicy uploadPolicy) {
        super(sizeThreshold, repository);
        this.pavewayService = pavewayService;
        this.uploadPolicy = uploadPolicy;
    }

    /**
     * Exactly the same as the supertype, except returns a {@link EncryptedFileItem}.
     */
    @Override
    public FileItem createItem(String fieldName, String contentType,
            boolean isFormField, String fileName) {
        if (isFormField) {
            return super.createItem(fieldName, contentType, isFormField, fileName);
        }
        FileBuilder fileBuilder = pavewayService.beginUpload(fileName, contentType, uploadPolicy);
        EncryptedFileItem result = new EncryptedFileItem(fieldName, contentType, 
                fileName, fileBuilder, getSizeThreshold(), getRepository());
        return result;
    }

}
