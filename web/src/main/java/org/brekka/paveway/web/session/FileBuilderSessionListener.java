/**
 * 
 */
package org.brekka.paveway.web.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.brekka.paveway.web.servlet.MultipartFileBuilderCache;

/**
 * @author Andrew Taylor
 *
 */
public class FileBuilderSessionListener implements HttpSessionListener {

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
        MultipartFileBuilderCache cache = MultipartFileBuilderCache.get(session);
        cache.discard();
    }

}
