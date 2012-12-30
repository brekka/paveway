/**
 * 
 */
package org.brekka.paveway.web.model;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;

/**
 * Operations for files in the process of being uploaded.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface UploadingFilesContext {

    /**
     * Retrieve the policy
     * 
     * @return the policy
     */
    UploadPolicy getPolicy();

    /**
     * Bind the file builder to this context.
     * 
     * @param name
     *            the name to bind the file as.
     * @param fileBuilder
     *            the file builder to bind.
     */
    void retain(String name, FileBuilder fileBuilder);

    /**
     * Retrieve a file previously retained under the name <code>name</code>
     * 
     * @param name
     *            the name the {@link FileBuilder} was previously bound under.
     * @return the {@link FileBuilder} or null if it cannot be found.
     */
    FileBuilder retrieve(String name);

    /**
     * Mark this {@link FileBuilder} as having had all its parts transferred.
     * 
     * @param fileBuilder
     *            the now fully transferred {@link FileBuilder}.
     */
    void transferComplete(FileBuilder fileBuilder);

    /**
     * Can another file be added?
     * 
     * @return true if the policy allows more files to be added.
     */
    boolean isFileSlotAvailable();

    /**
     * Is this files context completed, ie can more files be uploaded? Once the uploaded files have been persisted, no
     * more can be added.
     * 
     * @return true if no more files can be uploaded.
     */
    boolean isDone();

    /**
     * Discard all files currently associated with this context.
     */
    void discard();
}
