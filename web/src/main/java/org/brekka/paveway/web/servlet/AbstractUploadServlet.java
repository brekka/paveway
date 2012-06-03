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
import org.brekka.paveway.core.model.FilesContext;
import org.brekka.paveway.core.model.UploadPolicy;
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
        FilesContext filesContext = getFilesContext(req);
        
        String xFileName = req.getHeader("X-File-Name");
        String xFileType = req.getHeader("X-File-Type");
        
        FileItemFactory factory;
        try {
            if (xFileName != null 
                    && xFileType != null) {
                // Special factory
                factory = multipartFactory(filesContext, xFileName, xFileType);
            } else {
                factory = new EncryptedFileItemFactory(0, null, getPavewayService(), filesContext.getPolicy());
            }
        } catch (PavewayException e) {
            if (e.getErrorCode() == PavewayErrorCode.PW700) {
                // Too many files
                resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                return;
            } else {
                throw e;
            }
        }

        // Parse the request
        try {
            handle(factory, filesContext, req, resp);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (FileUploadException e) {
            throw new PavewayException(PavewayErrorCode.PW800, e, "");
        }
    }
    
    

    /**
     * @param xFileName
     * @param xFileType
     * @return
     */
    protected FileItemFactory multipartFactory(FilesContext filesContext, String xFileName, String xFileType) {
        FileBuilder fileBuilder = filesContext.retrieve(xFileName);
        if (fileBuilder == null) {
            // Allocate a new file
            if (filesContext.isFileSlotAvailable()) {
                fileBuilder = getPavewayService().begin(xFileName, xFileType, filesContext.getPolicy());
                filesContext.retain(xFileName, fileBuilder);
            } else {
                throw new PavewayException(PavewayErrorCode.PW700, "Unable to add any more files to this maker");
            }
        }
        return new EncryptedMultipartFileItemFactory(0, null, xFileName, xFileType, fileBuilder);
    }
    
    

    /**
     * @param resp 
     * @param req 
     * @param upload 
     * 
     */
    private void handle(FileItemFactory factory, FilesContext filesContext, HttpServletRequest req, HttpServletResponse resp) 
                throws FileUploadException, IOException {
        UploadPolicy policy = filesContext.getPolicy();
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setFileSizeMax(policy.getMaxFileSize());
        
        @SuppressWarnings("unchecked")
        List<FileItem> items = upload.parseRequest(req);
        for (FileItem fileItem : items) {
            if (fileItem instanceof EncryptedFileItem) {
                EncryptedFileItem efi = (EncryptedFileItem) fileItem;
                FileBuilder fileBuilder = efi.complete(req);
                if (fileBuilder != null) {
                    // file is complete
                    filesContext.complete(fileBuilder);
                }
            }
        }
    }
    
    protected abstract FilesContext getFilesContext(HttpServletRequest req);
}
