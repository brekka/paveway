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

package org.brekka.paveway.web.support;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Content-Disposition  attachment; filename="file.jpg"
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ContentDisposition implements Serializable {
    /**
     * The HTTP header key for Content range.
     */
    public static final String HEADER = "Content-Disposition";
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4649491401779278592L;
    
    /**
     * Pattern for extraction
     */
    private static final Pattern EXTRACT_PATTERN = Pattern.compile("^([^;]+); filename=\"([^\"]+)\"$", Pattern.UNICODE_CHARACTER_CLASS);
    
    /**
     * The disposition type
     */
    private final String type;
    
    /**
     * File name
     */
    private final String filename;

    /**
     * @param type
     * @param filename
     */
    public ContentDisposition(String type, String filename) {
        this.type = type;
        this.filename = filename;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s; filename=\"%s\"", type, filename);
    }
    
    /**
     * Create a new ContentDisposition for the specified string
     * 
     * @param contentDispositionStr
     *            the disposition string to parse
     * @return a new ContentDisposition (never null).
     * @throws IllegalArgumentException
     *             if the input is null or the string does not appear to be a Content-Disposition.
     */
    public static ContentDisposition valueOf(String contentDispositionStr, String encoding) {
        if (contentDispositionStr == null) {
            throw new IllegalArgumentException("No content disposition specified");
        }
        Matcher m = EXTRACT_PATTERN.matcher(contentDispositionStr);
        if (m.matches()) {
            String filename = m.group(2);
            try {
                filename = URLDecoder.decode(filename, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
            return new ContentDisposition(m.group(1), filename);
        }
        throw new IllegalArgumentException(String.format("The content disposition string '%s' is not valid", contentDispositionStr));
    }
}
