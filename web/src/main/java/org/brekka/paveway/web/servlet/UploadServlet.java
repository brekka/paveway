/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.apache.commons.lang3.StringUtils;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.web.model.UploadingFilesContext;
import org.brekka.paveway.web.support.ContentDisposition;
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.paveway.web.upload.EncryptedFileItemFactory;
import org.brekka.paveway.web.upload.EncryptedMultipartFileItemFactory;
import org.brekka.paveway.web.session.UploadsContext;

/**
 * Servlet for handling file uploads
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class UploadServlet extends AbstractPavewayServlet {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4985042386032649934L;
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UploadingFilesContext filesContext = getFilesContext(req);
        
        String xFileName = null;
        String contentDispositionStr = req.getHeader(ContentDisposition.HEADER);
        if (contentDispositionStr != null) {
            String encoding = req.getCharacterEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            ContentDisposition contentDisposition = ContentDisposition.valueOf(contentDispositionStr, encoding);
            xFileName = contentDisposition.getFilename();
        }
        String xFileType = req.getHeader("Content-Description");
        
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
            switch ((PavewayErrorCode) e.getErrorCode()) {
                case PW700:
                    resp.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                    return;
                case PW701:
                    resp.sendError(HttpServletResponse.SC_CONFLICT);
                    return;
                default:
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
    protected FileItemFactory multipartFactory(UploadingFilesContext filesContext, String xFileName, String xFileType) {
        FileBuilder fileBuilder = filesContext.retrieve(xFileName);
        if (fileBuilder == null) {
            // Allocate a new file
            if (filesContext.isDone()) {
                throw new PavewayException(PavewayErrorCode.PW701, "This maker has already been completed");
            }
            if (filesContext.isFileSlotAvailable()) {
                fileBuilder = getPavewayService().beginUpload(xFileName, xFileType, filesContext.getPolicy());
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
    private static void handle(FileItemFactory factory, UploadingFilesContext filesContext, HttpServletRequest req, HttpServletResponse resp) 
                throws FileUploadException {
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
                    filesContext.transferComplete(fileBuilder);
                }
            }
        }
    }
    
    protected UploadingFilesContext getFilesContext(HttpServletRequest req) {
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        requestURI = requestURI.substring(contextPath.length());
        String makerKey = StringUtils.substringAfterLast(requestURI, "/");
        UploadsContext bundleMakerContext = UploadsContext.get(req, true);
        return (UploadingFilesContext) bundleMakerContext.get(makerKey);
    }
}
