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

import org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Logout servlet. Cleans up the session and any acegi 'remember me' cookie, and
 * redirects to the login page.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LogoutServlet extends HttpServlet {

    /**
     * Processes a request and generates a response.
     *
     * @param request  the incoming request
     * @param response the outgoing response
     * @throws ServletException for any servlet exception
     * @throws IOException      for any I/O error
     */
    @Override
    protected void service(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {
        request.getSession().invalidate();
        Cookie terminate = new Cookie(
                TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY,
                null);
        terminate.setMaxAge(0);
        response.addCookie(terminate);
        response.sendRedirect(ServletHelper.getRedirectURI(request, "logout"));
    }

}
