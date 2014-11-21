/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.servlet;

import org.apache.log4j.MDC;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * A filter that populates the log4j {@code MDC} with the current user, for logging purposes.
 *
 * @author Tim Anderson
 */
public class Log4JMDCUserFilter implements Filter {

    /**
     * Called by the web container to indicate to a filter that it is being placed into service.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * The <code>doFilter</code> method of the Filter is called by the container each time a request/response pair is
     * passed through the chain due to a client request for a resource at the end of the chain.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                                     ServletException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            principal = ((User) principal).getUsername();
        }
        MDC.put("user", principal);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("user");
        }
    }

    /**
     * Called by the web container to indicate to a filter that it is being taken out of service.
     */
    @Override
    public void destroy() {
    }


}
