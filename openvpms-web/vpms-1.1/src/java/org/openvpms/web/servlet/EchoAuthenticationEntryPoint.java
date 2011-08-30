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

import nextapp.echo2.webrender.ContentType;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * An <code>AuthenticationEntryPoint</code> for use with echo2.
 * <p/>
 * This redirects the caller to the login form. However if the caller is
 * an echo2 client, the redirection is initiated by sending an
 * <code>HttpServletResponse.SC_BAD_REQUEST</code> status. The echo2 client
 * interprets this as a redirection if
 * <code>ClientConfiguration.PROPERTY_SESSION_EXPIRATION_URI</code>
 * has been configured.
 * <br/>
 * This is required as the echo2 client has no way of detecting that
 * a redirection has occurred as XmlHttpRequest automatically follows
 * redirections.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EchoAuthenticationEntryPoint
        extends AuthenticationProcessingFilterEntryPoint {

    /**
     * Commences an authentication scheme.
     * <p/>
     * This implemenation simply sends an
     * <code>HttpServletResponse.SC_BAD_REQUEST</code> in the response.
     *
     * @param request       that resulted in an <code>AuthenticationException</code>
     * @param response      so that the user agent can begin authentication
     * @param authException that caused the invocation
     */
    public void commence(ServletRequest request, ServletResponse response,
                         AuthenticationException authException) throws
                                                                IOException,
                                                                ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String query = req.getQueryString();
        if (query != null && query.contains("service")) {
            // probably an echo2 client
            resp.setContentType(ContentType.TEXT_HTML.getMimeType());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Session Expired");
            resp.flushBuffer();
        } else {
            super.commence(request, response, authException);
        }
    }
}
