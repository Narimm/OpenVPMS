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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.webdav.servlet;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.security.UserService;
import org.openvpms.web.webdav.session.Session;
import org.openvpms.web.webdav.session.SessionManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A {@code Filter} that authenticates a request if the requested URL corresponds to an existing WebDAV {@link Session}.
 *
 * @author Tim Anderson
 */
public class WebDAVAuthenticationFilter extends GenericFilterBean {

    /**
     * The WebDAV sessions.
     */
    private final SessionManager sessions;

    /**
     * The user service.
     */
    private final UserService users;

    /**
     * Constructs a {@link WebDAVAuthenticationFilter}.
     *
     * @param sessions the sessions
     * @param users    the user service
     */
    public WebDAVAuthenticationFilter(SessionManager sessions, UserService users) {
        this.sessions = sessions;
        this.users = users;
    }


    /**
     * Authenticates a request if the requested URL corresponds to an existing WebDAV {@link Session}.
     *
     * @param request     the servlet request
     * @param response    the servlet response
     * @param filterChain the filter chain
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        authenticate((HttpServletRequest) request);
        filterChain.doFilter(request, response);
    }

    /**
     * Authenticates a request if it the URL contains a valid session identifier.
     *
     * @param request the request
     */
    protected void authenticate(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = StringUtils.substringAfter(path, "document");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String sessionId = StringUtils.substringBefore(path, "/");
        Session session = sessions.get(sessionId);
        if (session != null) {
            String userName = session.getUserName();
            User user = (User) users.loadUserByUsername(userName);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    user, user.getPassword(), user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
        }
    }

}
