/**
 * 
 */
package org.brekka.paveway.web.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.FileBuilder;

/**
 * @author Andrew Taylor
 *
 */
public class MultipartFileBuilderCache {
    private transient Map<String, FileBuilder> map;
    
    
    public void add(String fileName, FileBuilder fileBuilder) {
        map().put(fileName, fileBuilder);
    }
    
    public FileBuilder get(String fileName) {
        return map().get(fileName);
    }
    
    public void remove(String fileName) {
        map().remove(fileName);
    }
    
    
    /**
     * Ensures that a map is always available.
     * @return
     */
    protected Map<String, FileBuilder> map() {
        if (map == null) {
            map = new HashMap<String, FileBuilder>();
        }
        return map;
    }
    
    public static MultipartFileBuilderCache get(HttpServletRequest req) {
        return get(req.getSession());
    }
    
    public static MultipartFileBuilderCache get(HttpSession session) {
        String key = MultipartFileBuilderCache.class.getName();
        
        MultipartFileBuilderCache cache = (MultipartFileBuilderCache) session.getAttribute(key);
        if (cache == null) {
            cache = new MultipartFileBuilderCache();
            session.setAttribute(key, cache);
        }
        return cache;
    }

    /**
     * Discard any builders that have not been used
     */
    public synchronized void discard() {
        Collection<FileBuilder> values = map().values();
        for (FileBuilder fileBuilder : values) {
            fileBuilder.discard();
        }
        map = null;
    }
    
    
    /**
     * An attempt is being made to serialize this object. Instead discard the files
     * @param out
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        discard();
        out.defaultWriteObject();
    }
}
