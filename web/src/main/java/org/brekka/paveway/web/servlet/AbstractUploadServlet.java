/**
 * 
 */
package org.brekka.paveway.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.FileBuilder;
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
    
    private EncryptedFileItemFactory defaultFileItemFactory;
    
    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init() throws ServletException {
        super.init();
        defaultFileItemFactory = new EncryptedFileItemFactory(0, null, getPavewayService());
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        String xFileName = req.getHeader("X-File-Name");
        String xFileType = req.getHeader("X-File-Type");
        
        FileItemFactory factory = defaultFileItemFactory;
        if (xFileName != null 
                && xFileType != null) {
            // Special factory
            factory = multipartFactory(req, xFileName, xFileType);
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
     * @param req
     * @param xFileName
     * @param xFileType
     * @return
     */
    protected FileItemFactory multipartFactory(HttpServletRequest req, String xFileName, String xFileType) {
        FileBuilder fileBuilder = retrieveFileBuilder(req, xFileName);
        if (fileBuilder == null) {
            fileBuilder = getPavewayService().begin(xFileName, xFileType);
            retainFileBuilder(req, xFileName, fileBuilder);
        }
        return new EncryptedMultipartFileItemFactory(0, null, xFileName, xFileType, fileBuilder);
    }
    
    

    /**
     * @param resp 
     * @param req 
     * @param upload 
     * 
     */
    private void handle(FileItemFactory factory, HttpServletRequest req, HttpServletResponse resp) throws FileUploadException {
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
                    completeFileBuilder(req, fileBuilder);
                }
            }
        }
    }
    
    protected abstract void retainFileBuilder(HttpServletRequest req, String fileName, FileBuilder fileBuilder);
    
    protected abstract FileBuilder retrieveFileBuilder(HttpServletRequest req, String fileName);
    
    protected abstract void completeFileBuilder(HttpServletRequest req, FileBuilder fileBuilder);
}
