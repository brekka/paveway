/**
 * 
 */
package org.brekka.paveway.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.brekka.paveway.core.services.PavewayService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Andrew Taylor
 *
 */
public class AbstractPavewayServlet extends HttpServlet {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 6945159491981031600L;
    
    private PavewayService pavewayService;
    
    
    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        pavewayService = initPavewayService();
    }
    
    /**
     * @return the pavewayService
     */
    protected final PavewayService getPavewayService() {
        return pavewayService;
    }
    
    protected PavewayService initPavewayService() {
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        return applicationContext.getBean(PavewayService.class);
    }
}
