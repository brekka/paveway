/**
 * 
 */
package org.brekka.paveway.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andrew Taylor
 */
public interface FilePart {

    /**
     * Used when allocating the part
     * @return
     * @throws IOException
     */
    OutputStream getOutputStream() throws IOException;
    
    /**
     * Used to retrieve the part content (encrypted).
     * @return
     * @throws IOException
     */
    InputStream getInputStream() throws IOException;
}
