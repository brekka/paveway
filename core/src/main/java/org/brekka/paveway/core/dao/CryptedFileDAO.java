/**
 * 
 */
package org.brekka.paveway.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.paveway.core.model.Bundle;
import org.brekka.paveway.core.model.CryptedFile;

/**
 * @author Andrew Taylor
 *
 */
public interface CryptedFileDAO extends EntityDAO<UUID, CryptedFile> {

    /**
     * @param bundle
     * @return
     */
    List<CryptedFile> retrieveByBundle(Bundle bundle);

}
