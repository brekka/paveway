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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.brekka.paveway.core.PavewayErrorCode;
import org.brekka.paveway.core.PavewayException;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.PartAllocator;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.web.model.UploadingFilesContext;
import org.brekka.paveway.web.session.UploadsContext;
import org.brekka.paveway.web.support.ContentDisposition;
import org.brekka.paveway.web.support.ContentRange;
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.paveway.web.upload.EncryptedFileItemFactory;
import org.brekka.paveway.web.upload.EncryptedMultipartFileItemFactory;

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
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        UploadingFilesContext filesContext = getFilesContext(req);

        String fileName = null;
        String contentDispositionStr = req.getHeader(ContentDisposition.HEADER);
        if (contentDispositionStr != null) {
            String encoding = req.getCharacterEncoding();
            if (encoding == null) {
                encoding = "UTF-8";
            }
            ContentDisposition contentDisposition = ContentDisposition.valueOf(contentDispositionStr, encoding);
            fileName = contentDisposition.getFilename();
        }
        String accept = req.getHeader("Accept");
        String contentType = req.getHeader("Content-Type");
        String contentLengthStr = req.getHeader("Content-Length");
        String contentRangeStr = req.getHeader(ContentRange.HEADER);

        if (contentType == null) {
            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        FileItemFactory factory;
        try {
            if (contentType.toLowerCase().startsWith("multipart/form-data")) {
                if (fileName == null) {
                    // Handles a standard file upload
                    factory = new EncryptedFileItemFactory(0, null, getPavewayService(), filesContext.getPolicy());
                    handle(factory, filesContext, req, resp);
                } else {
                    throw new UnsupportedOperationException("This mode is no longer supported");
                }
            } else {
                ContentRange contentRange;
                if (contentRangeStr == null && contentLengthStr == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Content-Range or Content-Length must be specified");
                    return;
                } else if (contentRangeStr != null) {
                    contentRange = ContentRange.valueOf(contentRangeStr);
                } else if (contentLengthStr != null) {
                    Long contentLength = Long.valueOf(contentLengthStr);
                    contentRange = new ContentRange(0, contentLength.longValue() - 1, contentLength.longValue());
                } else {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Only one of Content-Range or Content-Length must be specified");
                    return;
                }
                UUID id = processUpload(fileName, contentRange, contentType, req);
                if (accept != null
                        && accept.contains("application/json")) {
                    resp.setContentType("application/json");
                    resp.getWriter().printf("{\"id\": \"%s\"}", id);
                }
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
        } catch (FileUploadException e) {
            throw new PavewayException(PavewayErrorCode.PW800, e, "");
        }

        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doDelete(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        UploadingFilesContext filesContext = getFilesContext(req);
        String requestUri = req.getRequestURI();
        String fileName = StringUtils.substringAfterLast(requestUri, "/");
        fileName = URLDecoder.decode(fileName, "UTF-8");
        if (!filesContext.discard(fileName)) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }


    /**
     * @param fileName
     * @param contentRange
     * @param contentType
     * @param req
     */
    private UUID processUpload(final String fileName, final ContentRange contentRange, final String contentType, final HttpServletRequest req) throws IOException {
        UploadingFilesContext uploadingFilesContext = getFilesContext(req);
        PavewayService pavewayService = getPavewayService();
        FileBuilder fileBuilder = uploadingFilesContext.retrieve(fileName);
        UploadPolicy policy = uploadingFilesContext.getPolicy();
        if (fileBuilder == null) {
            if (!uploadingFilesContext.isFileSlotAvailable()) {
                throw new PavewayException(PavewayErrorCode.PW700, "Unable to add any more files, maximum %d reached", policy.getMaxFiles());
            }
            if (contentRange.getLength() > policy.getMaxFileSize()) {
                throw new PavewayException(PavewayErrorCode.PW700, "Content length %d exceeds maximum %d", contentRange.getLength(), policy.getMaxFileSize());
            }
            long clusterSize = contentRange.getLastBytePosition() - contentRange.getFirstBytePosition();
            if (clusterSize > policy.getClusterSize()) {
                throw new PavewayException(PavewayErrorCode.PW700, "Cluster size too large, maximum %d, found %d", policy.getClusterSize(), clusterSize);
            }
            fileBuilder = pavewayService.beginUpload(fileName, contentType, policy);
            fileBuilder.setLength(contentRange.getLength());
            uploadingFilesContext.retain(fileName, fileBuilder);
        }
        PartAllocator partAllocator = fileBuilder.allocatePart();
        try (InputStream is = req.getInputStream(); OutputStream os = partAllocator.getOutputStream()) {
            IOUtils.copy(is, os);
        }
        partAllocator.complete(contentRange.getFirstBytePosition());
        if (fileBuilder.isTransferComplete()) {
            uploadingFilesContext.transferComplete(fileBuilder);
        }
        return fileBuilder.getId();
    }



    /**
     * @param xFileName
     * @param xFileType
     * @return
     */
    protected FileItemFactory multipartFactory(final UploadingFilesContext filesContext, final String xFileName) {
        FileBuilder fileBuilder = filesContext.retrieve(xFileName);
        if (fileBuilder == null) {
            // Allocate a new file
            if (filesContext.isDone()) {
                throw new PavewayException(PavewayErrorCode.PW701, "This maker has already been completed");
            }
            if (filesContext.isFileSlotAvailable()) {
                fileBuilder = getPavewayService().beginUpload(xFileName, null, filesContext.getPolicy());
                filesContext.retain(xFileName, fileBuilder);
            } else {
                throw new PavewayException(PavewayErrorCode.PW700, "Unable to add any more files to this maker");
            }
        }
        return new EncryptedMultipartFileItemFactory(0, null, xFileName, fileBuilder);
    }



    /**
     * @param resp
     * @param req
     * @param upload
     *
     */
    private static void handle(final FileItemFactory factory, final UploadingFilesContext filesContext, final HttpServletRequest req, final HttpServletResponse resp)
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

    protected UploadingFilesContext getFilesContext(final HttpServletRequest req) {
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        requestURI = requestURI.substring(contextPath.length());
        String remainder = requestURI.substring(req.getServletPath().length() + 1);
        String makerKey = StringUtils.substringBefore(remainder, "/");
        UploadsContext bundleMakerContext = UploadsContext.get(req, true);
        return bundleMakerContext.get(makerKey);
    }
}
