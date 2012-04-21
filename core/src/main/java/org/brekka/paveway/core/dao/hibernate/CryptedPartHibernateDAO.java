/**
 * 
 */
package org.brekka.paveway.core.dao.hibernate;

import org.brekka.paveway.core.dao.CryptedPartDAO;
import org.brekka.paveway.core.model.CryptedPart;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class CryptedPartHibernateDAO extends AbstractPavewayHibernateDAO<CryptedPart> implements CryptedPartDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<CryptedPart> type() {
        return CryptedPart.class;
    }
}
