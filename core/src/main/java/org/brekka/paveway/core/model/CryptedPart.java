package org.brekka.paveway.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name="\"CryptedPart\"")
public class CryptedPart extends IdentifiableEntity {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 2051914235987306665L;
    

    /**
     * The file that 'this' is a part of.
     */
    @JoinColumn(name="CryptedFileID")
    @ManyToOne()
    private CryptedFile file;
    
    /**
     * The offset from the start of the file that this part represents.
     */
    @Column(name="\"Offset\"")
    private long offset;
    
    /**
     * The length of this part
     */
    @Column(name="\"Length\"")
    private long length;
    
    /**
     * The encryption initialisation vector
     */
    @Column(name="IV")
    private byte[] iv;
    
    /**
     * The overall checksum for the plain version of the file (optional).
     */
    @Column(name="OriginalChecksum")
    private byte[] originalChecksum;
    
    /**
     * Checksum of all parts of the encrypted file.
     */
    @Column(name="EncryptedChecksum")
    private byte[] encryptedChecksum;


    public CryptedFile getFile() {
        return file;
    }

    public void setFile(CryptedFile file) {
        this.file = file;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public byte[] getOriginalChecksum() {
        return originalChecksum;
    }

    public void setOriginalChecksum(byte[] originalChecksum) {
        this.originalChecksum = originalChecksum;
    }

    public byte[] getEncryptedChecksum() {
        return encryptedChecksum;
    }

    public void setEncryptedChecksum(byte[] encryptedChecksum) {
        this.encryptedChecksum = encryptedChecksum;
    }
    
    
}
