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
import java.io.OutputStream;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brekka.paveway.core.model.CryptedFile;

/**
 * Helper class for a download based servlet.
 *
 * @author Andrew Taylor (andrew@brekka.org)
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
