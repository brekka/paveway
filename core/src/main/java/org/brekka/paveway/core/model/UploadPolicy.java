/**
 * 
 */
package org.brekka.paveway.core.model;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface UploadPolicy {

    /**
     * The maximum number of files
     * @return
     */
    int getMaxFiles();
    
    /**
     * The maximum allowed file size
     * @return
     */
    int getMaxFileSize();
    
    /**
     * Maximum request size for DOS protection
     * @return
     */
    int getMaxSize();
    
    /**
     * The cluster size
     * @return
     */
    int getClusterSize();
}
