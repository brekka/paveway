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

/**
 * The compression algorithm being used by a crypted file on its parts prior to encryption.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public enum Compression {
    /**
     * No compression is in use, the file itself is already compressed or a format that does not compress well.
     */
    NONE,
    
    /**
     * Use the GZIP algorithm
     */
    GZIP,
}
