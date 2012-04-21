/**
 * 
 */
package org.brekka.paveway.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
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
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.paveway.web.upload.EncryptedFileItemFactory;
import org.brekka.paveway.web.upload.EncryptedMultipartFileItemFactory;

/**
 * @author Andrew Taylor
 *
 */
public abstract class AbstractUploadServlet extends AbstractPavewayServlet {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4985042386032649934L;

    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        EncryptedMultipartFileItemFactory factory = (EncryptedMultipartFileItemFactory) session.getAttribute(EncryptedMultipartFileItemFactory.class.getName());
        if (factory == null) {
            factory = new EncryptedMultipartFileItemFactory(0, null, getPavewayService());
            session.setAttribute(EncryptedFileItemFactory.class.getName(), factory);
        }

        // Parse the request
        try {
            handle(factory, req, resp);
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
    private void handle(EncryptedMultipartFileItemFactory factory, HttpServletRequest req, HttpServletResponse resp) throws FileUploadException {
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(req);
        
        for (FileItem fileItem : items) {
            if (fileItem instanceof EncryptedFileItem) {
                EncryptedFileItem efi = (EncryptedFileItem) fileItem;
                FileBuilder fileBuilder = efi.complete(req);
                if (fileBuilder != null) {
                    // file is complete
                    factory.remove(efi.getName());
                    handleCompletedFile(req, fileBuilder);
                }
            }
        }
    }
    
    protected abstract void handleCompletedFile(HttpServletRequest req, FileBuilder fileBuilder);
}