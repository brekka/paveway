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
    
    

    public AbstractFileItem(String fileName, String contentType, String fieldName) {
        this.contentType = contentType;
        this.fileName = fileName;
        this.fieldName = fieldName;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#getContentType()
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#getName()
     */
    @Override
    public String getName() {
        return Streams.checkFileName(fileName);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#isInMemory()
     */
    @Override
    public boolean isInMemory() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#getSize()
     */
    @Override
    public long getSize() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#get()
     */
    @Override
    public byte[] get() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#getString(java.lang.String)
     */
    @Override
    public String getString(String encoding) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#getString()
     */
    @Override
    public String getString() {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#write(java.io.File)
     */
    @Override
    public void write(File file) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#delete()
     */
    @Override
    public void delete() {
        // Ignore, handled internally
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#getFieldName()
     */
    @Override
    public String getFieldName() {
        return fieldName;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#setFieldName(java.lang.String)
     */
    @Override
    public void setFieldName(String name) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#isFormField()
     */
    @Override
    public boolean isFormField() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#setFormField(boolean)
     */
    @Override
    public void setFormField(boolean state) {
        throw new UnsupportedOperationException();
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.fileupload.FileItem#getInputStream()
     */
    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
}
