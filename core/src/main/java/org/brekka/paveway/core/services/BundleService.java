/**
 * 
 */
package org.brekka.paveway.core.services;

import java.util.List;

import org.brekka.paveway.core.model.Bundle;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface BundleService {

    Bundle createBundle(DateTime expires, List<FileBuilder> fileBuilders);
    
    void deallocateBundle(Bundle bundle);
    
    /**
     * Deallocate one of the files from a bundle.
     * @param bundleFile
     */
    void deallocateCryptedFile(CryptedFile cryptedFile);

}
