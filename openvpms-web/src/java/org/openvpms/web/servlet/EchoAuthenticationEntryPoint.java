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
import org.acegisecurity.intercept.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * An <code>AuthenticationEntryPoint</code> for use with echo2.
 * A typical implementation would send a redirection to a login page.
 * However, when the client is an echo2 app, it has know way of detecting that
 * a redirection has occurred due to a limitation of XmlHttpRequest which
 * automatically follows redirections.<br/>
 * To workaround this, an <code>HttpServletResponse.SC_BAD_REQUEST</code>
 * is sent instead. If echo2's ClientConfiguration.PROPERTY_SESSION_EXPIRATION_URI
 * has been configured correctly, the echo2 client will redirect to that.<br/>
 * The downside is that non echo2 clients will get a page with the message
 * 'Session Expired', rather than being redirected to the login page.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EchoAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

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
        HttpServletResponse req = (HttpServletResponse) response;
        req.setContentType(ContentType.TEXT_HTML.getMimeType());
        req.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        req.getWriter().write("Session Expired");
        req.flushBuffer();
    }
}
