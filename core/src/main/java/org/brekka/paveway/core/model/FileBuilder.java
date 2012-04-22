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
    PartAllocator allocatePart();

    /**
     * Set the length of the file, when known
     * @param length
     */
    void setLength(long length);
    
    /**
     * @return
     */
    boolean isComplete();
    
    /**
     * Discard this builder, removing any byteStreams that may have been created.
     */
    void discard();

}
