/**
 * 
 */
package org.brekka.paveway.core.dao.hibernate;

import java.sql.Date;
import java.util.List;

import org.brekka.paveway.core.dao.BundleDAO;
import org.brekka.paveway.core.model.Bundle;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class BundleHibernateDAO extends AbstractPavewayHibernateDAO<Bundle> implements BundleDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Bundle> type() {
        return Bundle.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.BundleDAO#retrieveOldestExpired(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Bundle> retrieveOldestExpired(int maxBundleCount) {
        return getCurrentSession().createCriteria(Bundle.class)
                .add(Restrictions.isNull("deleted"))
                .add(Restrictions.lt("expires", new Date(System.currentTimeMillis())))
                .setMaxResults(maxBundleCount)
                .addOrder(Order.asc("expires"))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.BundleDAO#refresh(org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    public void refresh(Bundle bundle) {
        getCurrentSession().refresh(bundle);
    }
}
