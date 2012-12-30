/**
 * 
 */
package org.brekka.paveway.core.model;


/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface FilesContext {

    
    
    UploadPolicy getPolicy();
    
    void retain(String fileName, FileBuilder fileBuilder);
    
    FileBuilder retrieve(String fileName);
    
    void complete( FileBuilder fileBuilder);
    
    /**
     * Can another file be added?
     * @return
     */
    boolean isFileSlotAvailable();
    
    boolean isDone();
}
