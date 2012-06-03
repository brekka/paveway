/**
 * 
 */
package org.brekka.paveway.core.model;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.paveway.core.PavewayConstants;

/**
 * A bundle represents a collection of files that should be transferred from one entity to another.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Bundle`", schema=PavewayConstants.SCHEMA)
public class Bundle extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5103824363711199962L;
    
    /**
     * When does this bundle expire?
     */
    @Column(name="`Expires`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;
    
    /**
     * The list of files containened in the bundle
     */
    @OneToMany(mappedBy="bundle")
    @MapKey
    private Map<UUID, CryptedFile> files;
    
    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Map<UUID, CryptedFile> getFiles() {
        return files;
    }

    public void setFiles(Map<UUID, CryptedFile> files) {
        this.files = files;
    }
}
