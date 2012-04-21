/**
 * 
 */
package org.brekka.paveway.core.model;



/**
 * @author Andrew Taylor
 *
 */
public interface FileBuilder {

    /**
     * The thing to write the bytes to
     * @return
     */
    PartAllocator allocatePart(FilePart partDestination);

    void setLength(long length);
    
    /**
     * @return
     */
    boolean isComplete();

}
