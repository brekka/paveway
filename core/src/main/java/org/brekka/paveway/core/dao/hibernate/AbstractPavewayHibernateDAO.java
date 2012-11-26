/**
 * 
 */
package org.brekka.paveway.core.dao.hibernate;

import java.util.UUID;

import org.brekka.commons.persistence.dao.hibernate.AbstractUniversallyIdentifiableEntityHibernateDAO;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Taylor
 *
 */
public abstract class AbstractPavewayHibernateDAO<T extends IdentifiableEntity<UUID>> 
extends AbstractUniversallyIdentifiableEntityHibernateDAO<T>{

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
