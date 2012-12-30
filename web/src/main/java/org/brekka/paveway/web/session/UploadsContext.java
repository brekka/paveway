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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.web.model.UploadingFilesContext;
import org.brekka.paveway.web.support.PolicyHelper;

/**
 * A context for file uploads that will be bound to a session. Supports multiple separate uploads via "makerkeys" which
 * allow individual page requests (or components with a single page) to have their own context.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class UploadsContext {
    public static final String SESSION_KEY = UploadsContext.class.getName();

    private transient Map<String, UploadedFilesContextImpl> makers;

    private final UploadPolicy policy;

    private UploadsContext(UploadPolicy policy) {
        this.policy = policy;
    }

    public synchronized boolean contains(String makerKey) {
        return map().containsKey(makerKey);
    }
    
    public synchronized <T extends UploadingFilesContext & UploadedFiles> T get(String makerKey) {
        return get(makerKey, null);
    }

    public synchronized <T extends UploadingFilesContext & UploadedFiles> T get(String makerKey, UploadPolicy policy) {
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
        makers.clear();
    }

    synchronized void free(String makerKey) {
        makers.remove(makerKey);
    }

    private synchronized Map<String, UploadedFilesContextImpl> map() {
        Map<String, UploadedFilesContextImpl> map = this.makers;
        if (map == null) {
            map = new HashMap<>();
        }
        return (this.makers = map);
    }

    public static UploadsContext get(HttpServletRequest req, boolean create) {
        HttpSession session = req.getSession(create);
        if (session == null) {
            return null;
        }
        return get(session);
    }

    public static UploadsContext get(HttpSession session) {
        UploadsContext content = (UploadsContext) session.getAttribute(SESSION_KEY);
        UploadPolicy policy = PolicyHelper.identifyPolicy(session.getServletContext());
        if (content == null) {
            content = new UploadsContext(policy);
            session.setAttribute(SESSION_KEY, content);
        }
        return content;
    }
}
