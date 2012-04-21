/**
 * 
 */
package org.brekka.paveway.core.dao.hibernate;

import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.model.CryptedFile;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class CryptedFileHibernateDAO extends AbstractPavewayHibernateDAO<CryptedFile> implements CryptedFileDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<CryptedFile> type() {
        return CryptedFile.class;
    }
}
