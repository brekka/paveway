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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.util.Streams;

/**
 * Base implementation of {@link FileItem}.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public abstract class AbstractFileItem implements FileItem {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3851768581810069924L;

    private final String contentType;
    private final String fileName;
    private final String fieldName;
    private FileItemHeaders headers;



    public AbstractFileItem(final String fileName, final String contentType, final String fieldName) {
        this.contentType = contentType;
        this.fileName = fileName;
        this.fieldName = fieldName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return Streams.checkFileName(fileName);
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public byte[] get() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(final String encoding) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(final File file) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        // Ignore, handled internally
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFormField() {
        return false;
    }

    @Override
    public void setFormField(final boolean state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }


    @Override
    public FileItemHeaders getHeaders() {
        return headers;
    }


    @Override
    public void setHeaders(final FileItemHeaders headers) {
        this.headers = headers;
    }
}
