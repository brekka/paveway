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

package org.brekka.paveway.web.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.web.model.UploadFilesData;
import org.brekka.paveway.web.model.UploadingFilesContext;
import org.brekka.paveway.web.support.PolicyHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A context for file uploads that will be bound to a session. Supports multiple separate uploads via "makerkeys" which
 * allow individual page requests (or components with a single page) to have their own context.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class UploadsContext implements Serializable {
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 5795369149104679209L;

    public static final String SESSION_KEY = UploadsContext.class.getName();

    private final Map<String, UploadFilesData> makers = new ConcurrentHashMap<>();

    private transient ApplicationContext applicationContext;

    private final UploadPolicy policy;

    private transient boolean dirty;

    private UploadsContext(final UploadPolicy policy) {
        this.policy = policy;
    }

    public synchronized boolean contains(final String makerKey) {
        return makers.containsKey(makerKey);
    }

    public synchronized <T extends UploadingFilesContext & UploadedFiles> T get(final String makerKey) {
        Map<String, UploadFilesData> map = makers;
        UploadFilesData filesData = map.get(makerKey);
        if (filesData == null) {
            filesData = new UploadFilesData(makerKey, policy);
            map.put(makerKey, filesData);
        }
        return (T) new UploadedFilesContextImpl(filesData, this);
    }

    public synchronized void discard() {
        Collection<UploadFilesData> values = makers.values();
        for (UploadFilesData filesData : values) {
            new UploadedFilesContextImpl(filesData, this).discard();
        }
        makers.clear();
        setDirty(true);
    }

    synchronized void free(final String makerKey) {
        makers.remove(makerKey);
        setDirty(true);
    }

    public static void init(final HttpServletRequest req, final UploadPolicy policy) {
        HttpSession session = req.getSession(true);
        Object existing = session.getAttribute(SESSION_KEY);
        if (existing != null) {
            UploadsContext context = new UploadsContext(policy);
            context.setApplicationContext(WebApplicationContextUtils.getWebApplicationContext(req.getServletContext()));
            session.setAttribute(SESSION_KEY, context);
        }
    }

    public static UploadsContext get(final HttpServletRequest req, final boolean create) {
        HttpSession session = req.getSession(create);
        if (session == null) {
            return null;
        }
        return get(session);
    }

    public static UploadsContext get(final HttpSession session) {
        UploadsContext context = (UploadsContext) session.getAttribute(SESSION_KEY);
        UploadPolicy policy = PolicyHelper.identifyPolicy(session.getServletContext());
        if (context == null) {
            context = new UploadsContext(policy);
            session.setAttribute(SESSION_KEY, context);
        }
        context.setApplicationContext(WebApplicationContextUtils.getWebApplicationContext(session.getServletContext()));
        return context;
    }

    public static void sync(final HttpServletRequest req) {
        UploadsContext uploadsContext = get(req, false);
        if (uploadsContext != null
                && uploadsContext.isDirty()) {
            HttpSession session = req.getSession(true);
            // Force context to be serialized.
            session.setAttribute(SESSION_KEY, uploadsContext);
            uploadsContext.setDirty(false);
        }
    }

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public UploadPolicy getDefaultPolicy() {
        return this.policy;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(final boolean dirty) {
        this.dirty = dirty;
    }
}
