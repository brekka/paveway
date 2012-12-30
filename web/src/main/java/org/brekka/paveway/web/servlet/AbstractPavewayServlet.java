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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.brekka.paveway.core.services.PavewayService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Provides lookup of the {@link PavewayService} from the spring context.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class AbstractPavewayServlet extends HttpServlet {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 6945159491981031600L;
    
    private PavewayService pavewayService;
    
    
    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init() throws ServletException {
        pavewayService = initPavewayService();
    }
    
    /**
     * @return the pavewayService
     */
    protected final PavewayService getPavewayService() {
        return pavewayService;
    }
    
    protected PavewayService initPavewayService() {
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        return applicationContext.getBean(PavewayService.class);
    }
}
