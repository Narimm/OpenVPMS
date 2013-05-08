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

import nextapp.echo2.webcontainer.ContainerInstance;
import nextapp.echo2.webrender.Connection;
import nextapp.echo2.webrender.ContentType;
import nextapp.echo2.webrender.WebRenderServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;


/**
 * Servlet helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ServletHelper {

    /**
     * Determines if a request is from echo2.
     *
     * @param request the request
     * @return <tt>true</tt> if the request came from echo2
     */
    public static boolean isEchoRequest(HttpServletRequest request) {
        String query = request.getQueryString();
        return (query != null && query.contains("service"));
    }

    /**
     * Helper to return a URI suitable for redirecting to a servlet.
     *
     * @param servlet the servlet name
     * @return a URI to redirect to the servlet
     */
    public static String getRedirectURI(String servlet) {
        Connection connection = WebRenderServlet.getActiveConnection();
        if (connection != null) {
            return getRedirectURI(connection.getRequest(), servlet);
        }
        return servlet;
    }

    /**
     * Helper to return a URI suitable for redirecting to a servlet.
     *
     * @param request contains the path to redirect from
     * @param servlet the servlet name
     * @return a URI to redirect to the servlet
     */
    public static String getRedirectURI(HttpServletRequest request,
                                        String servlet) {
        String uri = request.getRequestURI();
        int start = (uri.startsWith("/")) ? 1 : 0;
        int index = uri.indexOf("/", start);
        if (index != -1) {
            return uri.substring(0, index + 1) + servlet;
        }
        return uri + "/" + servlet;
    }

    /**
     * Returns the no. of instances of a particular echo2 application for
     * the current http session.
     *
     * @param servlet the application servlet
     * @return the no. of instance
     */
    public static int getApplicationInstanceCount(String servlet) {
        String instance = "Echo2UserInstance:/" + servlet;
        int count = 0;
        Connection connection = WebRenderServlet.getActiveConnection();
        if (connection != null) {
            HttpSession session = connection.getRequest().getSession();
            Enumeration attributes = session.getAttributeNames();
            while (attributes.hasMoreElements()) {
                String name = (String) attributes.nextElement();
                if (name.startsWith(instance)) {
                    if (session.getAttribute(name)
                            instanceof ContainerInstance) {
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Forces an echo2 client to redirect to the
     * <tt>ClientConfiguration.PROPERTY_SESSION_EXPIRATION_URI</tt> (if
     * it has been configured).
     * <p/>
     * This is done by sending an sending an
     * <tt>HttpServletResponse.SC_BAD_REQUEST</tt> status. The echo2 client
     * interprets this as a redirection when the above property is set.
     * <p/>
     * This is the only mechanism available to perform redirections when the
     * caller is an echo2 client. The <tt>HttpServletResponse.sendRedirect()</tt>
     * method doesn't work as XmlHttpRequest automatically follows redirections.
     *
     * @param response the response
     * @throws IOException
     */
    public static void forceExpiry(HttpServletResponse response) throws
                                                                 IOException {
        response.setContentType(ContentType.TEXT_HTML.getMimeType());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Session Expired");
        response.flushBuffer();
    }
}
