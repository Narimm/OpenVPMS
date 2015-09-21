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

package org.openvpms.web.echo.servlet;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * An {@code AuthenticationEntryPoint} for use with echo2.
 * <p/>
 * This redirects the caller to the login form. However if the caller is
 * an echo2 client, the redirection is initiated by sending an
 * {@code HttpServletResponse.SC_BAD_REQUEST} status. The echo2 client interprets this as a redirection if
 * {@code ClientConfiguration.PROPERTY_SESSION_EXPIRATION_URI} has been configured.
 * <br/>
 * This is required as the echo2 client has no way of detecting that
 * a redirection has occurred as XmlHttpRequest automatically follows
 * redirections.
 *
 * @author Tim Anderson
 */
public class EchoAuthenticationEntryPoint
        extends LoginUrlAuthenticationEntryPoint {

    /**
     * Constructs an {@link EchoAuthenticationEntryPoint}.
     *
     * @param loginFormUrl URL where the login page can be found. Should either be relative to the web-app context path
     *                     (include a leading {@code /}) or an absolute URL.
     */
    public EchoAuthenticationEntryPoint(String loginFormUrl) {
        super(loginFormUrl);
    }

    /**
     * Commences an authentication scheme.
     * <p/>
     * This implemenation simply sends an
     * {@code HttpServletResponse.SC_BAD_REQUEST} in the response.
     *
     * @param request       that resulted in an {@code AuthenticationException}
     * @param response      so that the user agent can begin authentication
     * @param authException that caused the invocation
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        if (ServletHelper.isEchoRequest(request)) {
            ServletHelper.forceExpiry(response);
        } else {
            super.commence(request, response, authException);
        }
    }
}
