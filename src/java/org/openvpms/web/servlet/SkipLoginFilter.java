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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.servlet;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * A login filter that skips the login process if the user is already logged
 * in. This is necessary as the http session is shared - logging in again would
 * replace the existing credentials.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see SpringWebContainerServlet
 */
public class SkipLoginFilter implements Filter {


    /**
     * Initialises the filter.
     *
     * @param config the filter configuration
     */
    public void init(FilterConfig config) {
    }

    /**
     * Filters the request.
     *
     * @param request  the request
     * @param response the response
     * @param chain    the filter chain
     * @throws IOException      for any I/O error
     * @throws ServletException for any servlet error
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException,
                                                   ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Destroys the filter.
     */
    public void destroy() {
    }

    /**
     * Filters the request.
     * <p/>
     * If the request is to the login servlet, and the current user is already
     * logged in, redirects to the app servlet. If the request is an echo2 AJAX
     * request, then it uses {@link ServletHelper#forceExpiry} to force
     * redirection.
     *
     * @param request  the request
     * @param response the response
     * @param chain    the filter chain
     * @throws IOException      for any I/O error
     * @throws ServletException for any servlet error
     */
    private void doFilter(HttpServletRequest request,
                          HttpServletResponse response,
                          FilterChain chain) throws IOException,
                                                    ServletException {
        if (request.getServletPath().startsWith("/login") && isAuthenticated()) {
            if (ServletHelper.isEchoRequest(request)) {
                ServletHelper.forceExpiry(response);
            } else {
                String uri = ServletHelper.getRedirectURI(request, "app");
                response.sendRedirect(response.encodeRedirectURL(uri));
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Determines if the user is already authenticated.
     *
     * @return <tt>true</tt> if the user is already authenticated
     */
    private boolean isAuthenticated() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
               && authentication.isAuthenticated();
    }
}

