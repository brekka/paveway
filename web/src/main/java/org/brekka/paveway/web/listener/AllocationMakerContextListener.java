/**
 * 
 */
package org.brekka.paveway.web.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.brekka.paveway.web.session.UploadsContext;

/**
 * @author Andrew Taylor
 *
 */
public class AllocationMakerContextListener implements HttpSessionListener {

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Not required
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        UploadsContext bundleMakerContext = UploadsContext.get(session);
        bundleMakerContext.discard();
    }

}
