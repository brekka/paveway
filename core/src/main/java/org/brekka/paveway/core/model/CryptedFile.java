package org.brekka.paveway.core.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name="\"CryptedFile\"")
public class CryptedFile extends IdentifiableEntity {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7742347795078233786L;

    /**
     * Identifies what compression mechanism is in use for this file, which will apply to all parts.
     */
    @Column(name="Compression", length=12)
    @Enumerated(EnumType.STRING)
    private Compression compression;
    
    /**
     * Crypto profile used for this file
     */
    @Column(name="Profile")
    private int profile;
    
    /**
     * Overall length of the original file
     */
    @Column(name="OriginalLength")
    private long originalLength;
    
    /**
     * The overall checksum for the plain version of the file (optional).
     */
    @Column(name="OriginalChecksum")
    private byte[] originalChecksum;
    
    /**
     * Length of the compressed/encrypted file.
     */
    @Column(name="EncryptedLength")
    private long encryptedLength;
    
    /**
     * Checksum of all parts of the encrypted file.
     */
    @Column(name="EncryptedChecksum")
    private byte[] encryptedChecksum;
    
    /**
     * The list of parts that make up this file
     */
    @OneToMany(fetch=FetchType.EAGER, mappedBy="file")
    private List<CryptedPart> parts;

    
    public Compression getCompression() {
        return compression;
    }

    public void setCompression(Compression compression) {
        this.compression = compression;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public long getOriginalLength() {
        return originalLength;
    }

    public void setOriginalLength(long originalLength) {
        this.originalLength = originalLength;
    }

    public byte[] getOriginalChecksum() {
        return originalChecksum;
    }

    public void setOriginalChecksum(byte[] originalChecksum) {
        this.originalChecksum = originalChecksum;
    }

    public long getEncryptedLength() {
        return encryptedLength;
    }

    public void setEncryptedLength(long encryptedLength) {
        this.encryptedLength = encryptedLength;
    }

    public byte[] getEncryptedChecksum() {
        return encryptedChecksum;
    }

    public void setEncryptedChecksum(byte[] encryptedChecksum) {
        this.encryptedChecksum = encryptedChecksum;
    }

    public List<CryptedPart> getParts() {
        return parts;
    }

    public void setParts(List<CryptedPart> parts) {
        this.parts = parts;
    }
}
