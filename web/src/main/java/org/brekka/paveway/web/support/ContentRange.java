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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a content range as defined by http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public final class ContentRange implements Serializable {
    /**
     * The HTTP header key for Content range.
     */
    public static final String HEADER = "Content-Range";

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 790209130139202745L;

    /**
     * Pattern for extraction
     */
    private static final Pattern EXTRACT_PATTERN = Pattern.compile("^bytes (\\d+)-(\\d+)/(\\d+)$");

    /**
     * The index of the first byte in this range
     */
    private final long firstBytePosition;

    /**
     * the index of the last byte in this range
     */
    private final long lastBytePosition;

    /**
     * The total length of the resource being sent
     */
    private final long length;

    /**
     * @param firstBytePosition
     * @param lastBytePosition
     * @param length
     */
    public ContentRange(final long firstBytePosition, final long lastBytePosition, final long length) {
        this.firstBytePosition = firstBytePosition;
        this.lastBytePosition = lastBytePosition;
        this.length = length;
    }

    /**
     * @return the firstBytePosition
     */
    public long getFirstBytePosition() {
        return firstBytePosition;
    }

    /**
     * @return the lastBytePosition
     */
    public long getLastBytePosition() {
        return lastBytePosition;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    @Override
    public String toString() {
        return String.format("bytes %d-%d/%d", firstBytePosition, lastBytePosition, length);
    }

    /**
     * Create a new ContentRange for the specified string
     *
     * @param contentRangeStr
     *            the range string to parse
     * @return a new ContentRange (never null).
     * @throws IllegalArgumentException
     *             if the input is null or the string does not appear to be a Content-Range.
     */
    public static ContentRange valueOf(final String contentRangeStr) {
        if (contentRangeStr == null) {
            throw new IllegalArgumentException("No content range specified");
        }
        Matcher m = EXTRACT_PATTERN.matcher(contentRangeStr);
        if (m.matches()) {
            return new ContentRange(Long.valueOf(m.group(1)), Long.valueOf(m.group(2)), Long.valueOf(m.group(3)));
        }
        throw new IllegalArgumentException(String.format("The content range string '%s' is not valid", contentRangeStr));
    }
}
