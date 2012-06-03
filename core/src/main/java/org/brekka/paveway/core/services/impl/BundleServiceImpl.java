/**
 * 
 */
package org.brekka.paveway.core.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.brekka.paveway.core.dao.BundleDAO;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.dao.CryptedPartDAO;
import org.brekka.paveway.core.model.Bundle;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.CryptedPart;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.BundleService;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
public class BundleServiceImpl implements BundleService {
    
    @Autowired
    private BundleDAO bundleDAO;
    
    @Autowired
    private CryptedFileDAO cryptedFileDAO;
    
    @Autowired
    private PavewayService pavewayService;
    
    @Autowired
    private ResourceStorageService resourceStorageService;
    
    @Autowired
    private CryptedPartDAO cryptedPartDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleService#createBundle(java.lang.String, java.lang.String, 
     *      org.joda.time.DateTime, javax.crypto.SecretKey, int, java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Bundle createBundle(DateTime expires, List<FileBuilder> fileBuilders) {
        Bundle bundle = new Bundle();
        bundle.setExpires(expires.toDate());
        bundleDAO.create(bundle);
        bundle.setFiles(new HashMap<UUID, CryptedFile>());
        
        for (FileBuilder fileBuilder : fileBuilders) {
            pavewayService.complete(bundle, fileBuilder);
        }
        
        return bundle;
    }

    /**
     * Perform the de-allocation
     * 
     * @param bundle
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void deallocateBundle(Bundle bundle) {
        List<CryptedFile> fileList = cryptedFileDAO.retrieveByBundle(bundle);
        for (CryptedFile bundleFile : fileList) {
            // Bundle file id matches the crypted file id from paveway.
            deallocateCryptedFile(bundleFile);
        }
        
        // Clear the bundle XML
        resourceStorageService.remove(bundle.getId());
        bundleDAO.delete(bundle.getId());
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void deallocateCryptedFile(CryptedFile cryptedFile) {
        cryptedFile = cryptedFileDAO.retrieveById(cryptedFile.getId());
        List<CryptedPart> parts = cryptedFile.getParts();
        for (CryptedPart cryptedPart : parts) {
            UUID partId = cryptedPart.getId();
            resourceStorageService.remove(partId);
            cryptedPartDAO.delete(partId);
        }
        cryptedFileDAO.delete(cryptedFile.getId());
    }
}
