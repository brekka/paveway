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

package org.brekka.paveway.core.services;

import java.util.UUID;

import org.brekka.paveway.core.model.ByteSequence;

/**
 * For allocating {@link ByteSequence} to some underlying storage, be it the local file system or Amazon S3, etc.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ResourceStorageService {

    /**
     * Allocate a new {@link ByteSequence}
     * 
     * @param id
     *            the ID to use
     * @return the new byte sequence
     */
    ByteSequence allocate(UUID id);

    /**
     * Retrieve an existing byte sequence.
     * @param id the id of the sequence to retrieve
     * @return the {@link ByteSequence} instance, or null if it cannot be found.
     */
    ByteSequence retrieve(UUID id);

    /**
     * Remove the byte sequence with the specified ID.
     * @param id the id of the sequence to remove.
     */
    void remove(UUID id);
}
