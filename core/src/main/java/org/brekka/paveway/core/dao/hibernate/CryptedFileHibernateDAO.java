/**
 * 
 */
package org.brekka.paveway.core.dao.hibernate;

import java.util.List;

import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.model.Bundle;
import org.brekka.paveway.core.model.CryptedFile;
import org.hibernate.criterion.Restrictions;
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
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.dao.CryptedFileDAO#retrieveByBundle(org.brekka.paveway.core.model.Bundle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<CryptedFile> retrieveByBundle(Bundle bundle) {
        return getCurrentSession().createCriteria(Bundle.class)
                .add(Restrictions.eq("bundle", bundle))
                .list();
    }
    
}
