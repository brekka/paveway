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

import java.io.InputStream;
import java.util.UUID;

import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;

/**
 * Paveway is an encrypted file upload service and retrieval service. When a file is uploaded the transferred bytes are
 * immediately encrypted. There is never a plaintext version of the file written to any persistence storage medium (and
 * only a small buffer of the file in memory at any one time.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface PavewayService {

    /**
     * Begin a new file upload. All objects created during the upload process are non-committed.
     *
     * @param fileName
     *            the name of the file
     * @param mimeType
     *            the MIME type of the file
     * @param uploadPolicy
     *            the policy to apply to the upload.
     * @return a new {@link FileBuilder} that will
     */
    FileBuilder beginUpload(String fileName, String mimeType, UploadPolicy uploadPolicy);

    /**
     * Complete the uploaded file, allocating a {@link CryptedFile} instance to make the file persistent.
     *
     * @param completableUploadedFile
     *            the uploaded file to complete.
     * @return the now committed crypted file.
     */
    CryptedFile complete(CompletableUploadedFile completableUploadedFile);

    /**
     * Retrieve a crypted file by it's id.
     *
     * @param id
     *            the id of the file
     * @return the {@link CryptedFile} instance or null if it cannot be found.
     */
    CryptedFile retrieveCryptedFileById(UUID id);

    /**
     * Retrieve the plaintext content of the specified file. The
     * {@link CryptedFile#setSecretKey(org.brekka.phoenix.api.SecretKey)} method must be set with the secret key of the
     * file.
     *
     * @param file
     *            the file to download
     * @return a stream of the downloaded file.
     * @throws PavewayException if the secret key has not been set or is incorrect for this file.
     */
    InputStream download(CryptedFile file);

    /**
     * Request the removal of the specified file, deallocating the database and stored bytes.
     *
     * @param cryptedFile
     *            the file to remove.
     */
    void removeFile(CryptedFile cryptedFile);

    /**
     * @param cryptedPart
     */
    void createPart(CryptedPart cryptedPart);

    /**
     * @param cryptedFile
     * @return
     */
    boolean isTransferComplete(CryptedFile cryptedFile);

    /**
     * @param cryptedFile
     * @param length
     */
    void setFileLength(CryptedFile cryptedFile, long length);
}
