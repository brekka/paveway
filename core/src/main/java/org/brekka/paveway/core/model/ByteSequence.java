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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.brekka.paveway.core.services.ResourceStorageService;

/**
 * Represents a basic byte storage entry that supports writing and reading. 
 *
 * @author Andrew Taylor (andrew@brekka.org)
 * @see ResourceStorageService
 */
public interface ByteSequence {
    /**
     * Unique ID assigned to this sequence
     * @return
     */
    UUID getId();
    
    /**
     * Used when allocating the part
     * @return
     * @throws IOException
     */
    OutputStream getOutputStream();
    
    /**
     * Used to retrieve the part content (encrypted).
     * @return
     * @throws IOException
     */
    InputStream getInputStream();
    
    /**
     * Indicate that this byte sequence should be stored more permanently.
     */
    void persist();
    
    /**
     * Discard this byte sequence, it is no longer required.
     */
    void discard();
}
