/**
 * 
 */
package org.brekka.paveway.core.services;

/**
 * Remove bundles that have passed their expiry date.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ReaperService {

    /**
     * Perform background de-allocation of bundles
     */
    void deallocateBundles();
}
