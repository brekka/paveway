/**
 * 
 */
package org.brekka.paveway.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.math.NumberUtils;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.upload.EncryptedFileItem;
import org.brekka.paveway.core.upload.EncryptedFileItemFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Andrew Taylor
 *
 */
public class UploadServlet extends HttpServlet {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4985042386032649934L;
    
    private PavewayService pavewayService;
    
    
    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        pavewayService = applicationContext.getBean(PavewayService.class);
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        HttpSession session = req.getSession();
        EncryptedFileItemFactory factory = (EncryptedFileItemFactory) session.getAttribute(EncryptedFileItemFactory.class.getName());
        if (factory == null) {
            factory = new EncryptedFileItemFactory(50000, null, pavewayService);
            session.setAttribute(EncryptedFileItemFactory.class.getName(), factory);
        }

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        // Parse the request
        try {
            handle(upload, req, resp);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (FileUploadException e) {
            throw new PavewayException(PavewayErrorCode.PW800, e, "");
        }
    }

    /**
     * @param resp 
     * @param req 
     * @param upload 
     * 
     */
    private void handle(ServletFileUpload upload, HttpServletRequest req, HttpServletResponse resp) throws FileUploadException {
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(req);
        
        String xFileName = req.getHeader("X-File-Name");
        
        for (FileItem fileItem : items) {
            if (fileItem instanceof EncryptedFileItem) {
                EncryptedFileItem efi = (EncryptedFileItem) fileItem;
                PartAllocator partAllocator = efi.getPartAllocator();
                if (xFileName != null) {
                    partAllocator.setOffset(NumberUtils.toLong(req.getHeader("X-Part-Offset")));
                    partAllocator.setLength(NumberUtils.toLong(req.getHeader("X-Part-Length")));
                } else {
                    partAllocator.setOffset(0);
                    partAllocator.setLength(fileItem.getSize());
                }
                partAllocator.complete();
                FileBuilder fileBuilder = efi.getFileBuilder();
            }
        }
    }
}
