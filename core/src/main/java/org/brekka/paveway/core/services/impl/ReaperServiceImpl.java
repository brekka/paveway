/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.util.Collections;
import java.util.List;

import org.brekka.paveway.core.dao.BundleDAO;
import org.brekka.paveway.core.model.Bundle;
import org.brekka.paveway.core.services.BundleService;
import org.brekka.paveway.core.services.ReaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Transactional
@Service
public class ReaperServiceImpl implements ReaperService {

    // TODO configured
    private int maxBundleCount = 20;
    
    @Autowired
    private BundleDAO bundleDAO;
    
    @Autowired
    private BundleService bundleService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ReaperService#deallocate(int)
     */
    @Override
    @Scheduled(fixedDelay=10000) // Gap of ten seconds between each invocation
    @Transactional(propagation=Propagation.REQUIRED)
    public void deallocateBundles() {
        List<Bundle> bundleList = Collections.emptyList();
        do {
            bundleList = bundleDAO.retrieveOldestExpired(maxBundleCount);
            for (Bundle bundle : bundleList) {
                bundleService.deallocateBundle(bundle);
            }
            // Keep looping until there are no more entries to expire
        } while (!bundleList.isEmpty());
        
    }
}
