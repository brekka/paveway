/**
 * 
 */
package org.brekka.paveway.web.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.web.model.Files;
import org.brekka.paveway.web.support.PolicyHelper;

/**
 * @author Andrew Taylor
 *
 */
public class UploadsContext {
    public static final String SESSION_KEY = UploadsContext.class.getName();

    private transient Map<String, FilesImpl> makers;
    
    private final UploadPolicy policy;
    
    private UploadsContext(UploadPolicy policy) {
        this.policy = policy;
    }

    public synchronized boolean contains(String makerKey) {
        return map().containsKey(makerKey);
    }
    
    public synchronized Files get(String makerKey) {
        Map<String, FilesImpl> map = map();
        FilesImpl files = map.get(makerKey);
        if (files == null) {
            files = new FilesImpl(makerKey, policy);
            map.put(makerKey, files);
        }
        return files;
    }
    
    public synchronized void discard() {
        Collection<FilesImpl> values = map().values();
        for (FilesImpl files : values) {
            files.discard();
        }
        makers.clear();
    }
    
    private synchronized Map<String, FilesImpl> map() {
        Map<String, FilesImpl> map = this.makers;
        if (map == null) {
            map = new HashMap<>();
        }
        return (this.makers = map);
    }
    

    public static UploadsContext get(HttpServletRequest req, boolean create) {
        HttpSession session = req.getSession(create);
        if (session == null) {
            return null;
        }
        return get(session);
    }
    
    public static UploadsContext get(HttpSession session) {
        UploadsContext content = (UploadsContext) session.getAttribute(SESSION_KEY);
        UploadPolicy policy = PolicyHelper.identifyPolicy(session.getServletContext());
        if (content == null) {
            content = new UploadsContext(policy);
            session.setAttribute(SESSION_KEY, content);
        }
        return content;
    }
}
