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
 * Constructs a file from one or more parts.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface FileBuilder extends CompletableUploadedFile {

    /**
     * Allocate a new part for the file.
     * 
     * @return a new part allocator.
     */
    PartAllocator allocatePart();

    /**
     * Set the length of the file, when known
     * 
     * @param length
     */
    void setLength(long length);

    /**
     * Has the file been fully uploaded (all parts accounted for).
     * 
     * @return true if all parts have been uploaded
     */
    boolean isTransferComplete();
}
