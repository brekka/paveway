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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.paveway.core.PavewayConstants;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
import org.hibernate.annotations.Type;

import net.iharder.Base64;

/**
 * Persistent storage of a part of an uploaded file. All file parts will be encrypted with the same symmetric key though
 * each part gets its own IV. The actual bytes for the part are not stored in this table, instead they are allocated by
 * {@link ResourceStorageService}.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name = "`CryptedPart`", schema = PavewayConstants.SCHEMA)
public class CryptedPart extends SnapshotEntity<UUID> implements SymmetricCryptoSpec {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 2051914235987306665L;

    /**
     * Unique ID
     */
    @Id
    @Type(type = "pg-uuid")
    @Column(name = "`ID`")
    private UUID id;

    /**
     * The file that 'this' is a part of.
     */
    @JoinColumn(name = "`CryptedFileID`")
    @ManyToOne()
    private CryptedFile file;

    /**
     * The offset from the start of the file that this part represents based on the plaintext form.
     */
    @Column(name = "`Offset`")
    private long offset;

    /**
     * The length of this part in its plaintext form.
     */
    @Column(name = "`Length`")
    private long length;

    /**
     * The encryption initialisation vector
     */
    @Column(name = "`IV`")
    private byte[] iv;

    /**
     * The overall checksum for the plain version of the file (optional).
     */
    @Column(name = "`OriginalChecksum`")
    private byte[] originalChecksum;

    /**
     * Checksum of this encrypted part
     */
    @Column(name = "`EncryptedChecksum`")
    private byte[] encryptedChecksum;


    public CryptedFile getFile() {
        return file;
    }

    public void setFile(final CryptedFile file) {
        this.file = file;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(final long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(final long length) {
        this.length = length;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(final byte[] iv) {
        this.iv = iv;
    }

    public byte[] getOriginalChecksum() {
        return originalChecksum;
    }

    public void setOriginalChecksum(final byte[] originalChecksum) {
        this.originalChecksum = originalChecksum;
    }

    public byte[] getEncryptedChecksum() {
        return encryptedChecksum;
    }

    public void setEncryptedChecksum(final byte[] encryptedChecksum) {
        this.encryptedChecksum = encryptedChecksum;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    @Override
    public byte[] getIV() {
        return iv;
    }

    @Override
    public SecretKey getSecretKey() {
        return getFile().getSecretKey();
    }

    @Override
    public CryptoProfile getCryptoProfile() {
        return CryptoProfile.Static.of(getFile().getProfile());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", id)
        .append("length", length)
        .append("offset", offset)
        .append("IV", iv != null ? Base64.encodeBytes(iv) : null)
        .append("originalChecksum", originalChecksum != null ? Base64.encodeBytes(originalChecksum) : null)
        .append("encryptedChecksum", encryptedChecksum != null ? Base64.encodeBytes(encryptedChecksum) : null)
        .toString();
    }
}
