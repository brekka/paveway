/**
 * 
 */
package org.brekka.paveway.web.support;

import javax.servlet.ServletContext;

import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.UploadPolicyService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class PolicyHelper {
    
    public static UploadPolicy identifyPolicy(ServletContext servletContext) {
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        UploadPolicyService uploadPolicyService = webApplicationContext.getBean(UploadPolicyService.class);
        return uploadPolicyService.identifyUploadPolicy();
    }
}
