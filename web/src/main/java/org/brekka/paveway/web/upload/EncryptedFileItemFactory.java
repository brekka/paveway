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
import java.util.regex.Pattern;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.PavewayService;

/**
 * {@link FileItemFactory} that creates {@link EncryptedFileItem}s.
 *
 * @author Andrew Taylor
 */
public class EncryptedFileItemFactory extends DiskFileItemFactory {

    public static final Pattern CLEAN_FILENAME = Pattern.compile("[#<>$+%!`&*'|{}?\"=/:\\@]+");

    private static final Log log = LogFactory.getLog(EncryptedFileItemFactory.class);

    protected final transient PavewayService pavewayService;

    private final UploadPolicy uploadPolicy;

    public EncryptedFileItemFactory(final int sizeThreshold, final File repository,
            final PavewayService pavewayService, final UploadPolicy uploadPolicy) {
        super(sizeThreshold, repository);
        this.pavewayService = pavewayService;
        this.uploadPolicy = uploadPolicy;
    }

    /**
     * Exactly the same as the supertype, except returns a {@link EncryptedFileItem}.
     */
    @Override
    public FileItem createItem(final String fieldName, final String contentType, final boolean isFormField, final String fileName) {
        if (isFormField) {
            return super.createItem(fieldName, contentType, isFormField, fileName);
        }
        // Remove any path that may be sent
        String cleanFileName = removePath(fileName);
        // Only allow characters that are deemed 'safe'
        cleanFileName = CLEAN_FILENAME.matcher(cleanFileName).replaceAll("");
        StopWatch sw = new StopWatch();
        sw.start();
        FileBuilder fileBuilder = pavewayService.beginUpload(cleanFileName, contentType, uploadPolicy);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Created file item '%s' of type '%s' from field '%s' in %d ms",
                    cleanFileName, contentType, fieldName, sw.getTime()));
        }
        EncryptedFileItem result = new EncryptedFileItem(fieldName, contentType,
                cleanFileName, fileBuilder, getSizeThreshold(), getRepository());
        return result;
    }

    private static String removePath(final String fileName) {
        if (fileName == null) {
            return fileName;
        }
        String cleanFileName = fileName;
        int backslashIndex = cleanFileName.lastIndexOf('\\');
        if (backslashIndex != -1) {
            cleanFileName = cleanFileName.substring(backslashIndex + 1);
        }
        int slashIndex = cleanFileName.lastIndexOf('/');
        if (slashIndex != -1) {
            cleanFileName = cleanFileName.substring(slashIndex + 1);
        }
        return cleanFileName;
    }
}
