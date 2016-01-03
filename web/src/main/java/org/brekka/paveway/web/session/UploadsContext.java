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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.web.model.UploadingFilesContext;
import org.brekka.paveway.web.support.PolicyHelper;

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

    private transient Map<String, UploadedFilesContextImpl> makers;

    private final UploadPolicy policy;

    private UploadsContext(final UploadPolicy policy) {
        this.policy = policy;
    }

    public synchronized boolean contains(final String makerKey) {
        return map().containsKey(makerKey);
    }

    public synchronized <T extends UploadingFilesContext & UploadedFiles> T get(final String makerKey) {
        return get(makerKey, null);
    }

    public synchronized <T extends UploadingFilesContext & UploadedFiles> T get(final String makerKey, UploadPolicy policy) {
        if (policy == null) {
            policy = this.policy;
        }
        Map<String, UploadedFilesContextImpl> map = map();
        UploadedFilesContextImpl files = map.get(makerKey);
        if (files == null) {
            files = new UploadedFilesContextImpl(makerKey, policy, this);
            map.put(makerKey, files);
        }
        return (T) files;
    }

    public synchronized void discard() {
        Collection<UploadedFilesContextImpl> values = map().values();
        for (UploadedFilesContextImpl files : values) {
            files.discard();
        }
        this.makers.clear();
    }

    synchronized void free(final String makerKey) {
        this.makers.remove(makerKey);
    }

    private synchronized Map<String, UploadedFilesContextImpl> map() {
        Map<String, UploadedFilesContextImpl> map = this.makers;
        if (map == null) {
            map = new HashMap<>();
        }
        return (this.makers = map);
    }

    public static void init(final HttpServletRequest req, final UploadPolicy policy) {
        HttpSession session = req.getSession(true);
        UploadsContext context = new UploadsContext(policy);
        session.setAttribute(SESSION_KEY, context);
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
        return context;
    }

    public UploadPolicy getDefaultPolicy() {
        return this.policy;
    }
}
