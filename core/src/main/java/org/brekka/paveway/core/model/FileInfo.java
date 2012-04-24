/**
 * 
 */
package org.brekka.paveway.core.model;

/**
 * @author Andrew Taylor
 *
 */
public interface FileInfo {
    /**
     * File length
     * @return
     */
    long getLength();
    
    /**
     * File name
     * @return
     */
    String getFileName();
}
