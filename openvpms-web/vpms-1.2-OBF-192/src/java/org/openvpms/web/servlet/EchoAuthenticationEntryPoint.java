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

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * An <tt>AuthenticationEntryPoint</tt> for use with echo2.
 * <p/>
 * This redirects the caller to the login form. However if the caller is
 * an echo2 client, the redirection is initiated by sending an
 * <tt>HttpServletResponse.SC_BAD_REQUEST</tt> status. The echo2 client
 * interprets this as a redirection if
 * <tt>ClientConfiguration.PROPERTY_SESSION_EXPIRATION_URI</tt>
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
     * <tt>HttpServletResponse.SC_BAD_REQUEST</tt> in the response.
     *
     * @param request       that resulted in an <tt>AuthenticationException</tt>
     * @param response      so that the user agent can begin authentication
     * @param authException that caused the invocation
     */
    public void commence(ServletRequest request, ServletResponse response,
                         AuthenticationException authException) throws
                                                                IOException,
                                                                ServletException {
        if (ServletHelper.isEchoRequest((HttpServletRequest) request)) {
            ServletHelper.forceExpiry(((HttpServletResponse) response));
        } else {
            super.commence(request, response, authException);
        }
    }
}
