/**
 * 
 */
package org.brekka.paveway.web.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brekka.paveway.core.model.CryptedFile;

/**
 * @author Andrew Taylor
 *
 */
public abstract class AbstractDownloadServlet extends AbstractPavewayServlet {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7964395496859717621L;

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        
        CryptedFile cryptedFile = getPavewayService().retrieveCryptedFileById(UUID.fromString(id));
        
        if (cryptedFile == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Content-Length", Long.toString(cryptedFile.getOriginalLength()));
        
        doDownload(cryptedFile, resp.getOutputStream());
    }
    
    protected abstract void doDownload(CryptedFile cryptedFile, OutputStream os);
}
