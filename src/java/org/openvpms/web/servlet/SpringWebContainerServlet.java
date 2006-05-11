/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.servlet;

import javax.servlet.ServletException;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.WebContainerServlet;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.openvpms.web.spring.SpringApplicationInstance;

/**
 * <code>WebContainerServlet</code> for integrating Echo with Spring.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SpringWebContainerServlet extends WebContainerServlet {

    /**
     * The Spring application context.
     */
    private ApplicationContext _context;

    /**
     * The application name.
     */
    private String _name;


    /**
     * Initialises the servlet.
     *
     * @throws ServletException if the serlvet can't be initialised
     */
    @Override
    public void init() throws ServletException {
        super.init();
        _name = getInitParameter("app-name");
        if (StringUtils.isEmpty(_name)) {
            throw new ServletException("init-param not specified: app-name");
        }
    }

    /**
     * Creates a new <code>ApplicationInstance</code> for a visitor to an
     * application.
     *
     * @return a new <code>ApplicationInstance</code>
     */
    public ApplicationInstance newApplicationInstance() {
        SpringApplicationInstance result = null;
        if (_context == null) {
            _context = WebApplicationContextUtils.getWebApplicationContext(
                    getServletContext());
        }
        result = (SpringApplicationInstance) _context.getBean(_name);
        result.setApplicationContext(_context);
        return result;
    }

}
