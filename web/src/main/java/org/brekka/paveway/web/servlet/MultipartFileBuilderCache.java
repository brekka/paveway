/**
 * 
 */
package org.brekka.paveway.web.servlet;

import java.util.HashMap;
import java.util.Map;

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
}
