/**
 * 
 */
package org.brekka.paveway.core.dao.hibernate;

import org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Taylor
 *
 */
public abstract class AbstractPavewayHibernateDAO<Entity extends IdentifiableEntity> extends AbstractIdentifiableEntityHibernateDAO<Entity>  {


    @Autowired
    private SessionFactory pavewaySessionFactory;
    
    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#getCurrentSession()
     */
    @Override
    protected Session getCurrentSession() {
        return pavewaySessionFactory.getCurrentSession();
    }
    
}
