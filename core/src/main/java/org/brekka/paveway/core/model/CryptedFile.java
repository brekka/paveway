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

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.paveway.core.PavewayConstants;
import org.brekka.phoenix.api.SecretKey;
import org.hibernate.annotations.Type;

/**
 * Persistent storage of a an encrypted uploaded file. The file content will be stored in one or more
 * {@link CryptedPart}s. The name, secret key and MIME-type of the file will be stored by some other system.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name = "`CryptedFile`", schema = PavewayConstants.SCHEMA)
public class CryptedFile extends SnapshotEntity<UUID>  {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7742347795078233786L;

    /**
     * Unique ID
     */
    @Id
    @Type(type = "pg-uuid")
    @Column(name = "`ID`")
    private UUID id;

    /**
     * Identifies what compression mechanism is in use for this file, which will apply to all parts.
     */
    @Column(name = "`Compression`", length = 12)
    @Enumerated(EnumType.STRING)
    private Compression compression;

    /**
     * Crypto profile used for this file
     */
    @Column(name = "`Profile`")
    private int profile;

    /**
     * Overall length of the original file
     */
    @Column(name = "`OriginalLength`")
    private long originalLength;

    /**
     * The list of parts that make up this file
     */
    @OneToMany(mappedBy = "file", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<CryptedPart> parts;

    /**
     * The secret key
     */
    @Transient
    private transient SecretKey secretKey;

    @Transient
    private transient String fileName;

    @Transient
    private transient String mimeType;

    public Compression getCompression() {
        return compression;
    }

    public void setCompression(final Compression compression) {
        this.compression = compression;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(final int profile) {
        this.profile = profile;
    }

    public long getOriginalLength() {
        return originalLength;
    }

    public void setOriginalLength(final long originalLength) {
        this.originalLength = originalLength;
    }

    public List<CryptedPart> getParts() {
        return parts;
    }

    public void setParts(final List<CryptedPart> parts) {
        this.parts = parts;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", id)
                .append("fileName", fileName).append("mimeType", mimeType).append("compression", compression)
                .append("originalLength", originalLength)
                .append("secretKey", secretKey != null ? "Yes" : "No").append("profile", profile)
                .append("partsCount", parts != null ? parts.size() : "unknown").append("parts", parts).toString();
    }
}
