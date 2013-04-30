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

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.WebContainerServlet;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.system.SpringApplicationInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;


/**
 * A <tt>WebContainerServlet</tt> that integrates Echo with Spring.
 * <p/>
 * This supports multiple client browser windows/tabs via a single http session
 * by appending a unique identifier to the request URI when the servlet is
 * first accessed.
 * <p/>
 * This approach has a few drawbacks:
 * <ul>
 * <li>different logins from the same client browser are not supported.
 * This is due to the fact that Acegi doesn't bind authentication to a
 * particular path, but has a global instance per session.
 * <li>terminating the session logs out all instances
 * </ul>
 * An alternative approach would be to use URL rewriting. This is not supported
 * by echo2, as it does not encode the JSESSIONID in URLs.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SpringWebContainerServlet extends WebContainerServlet {

    /**
     * The Spring application context.
     */
    private transient ApplicationContext context;

    /**
     * The application name.
     */
    private String name;

    /**
     * Caches the servlet name for the current thread.
     */
    private transient ThreadLocal<String> servletName
        = new ThreadLocal<String>();

    /**
     * The locale of the current thread.
     */
    private transient ThreadLocal<Locale> locale = new ThreadLocal<Locale>();

    /**
     * Serialisation ID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Used to allocate the next path instance.
     */
    private static final String NEXT_INSTANCE = "__EchoNextApplicationInstance";


    /**
     * Initialises the servlet.
     *
     * @throws ServletException if the serlvet can't be initialised
     */
    @Override
    public void init() throws ServletException {
        super.init();
        name = getInitParameter("app-name");
        if (StringUtils.isEmpty(name)) {
            throw new ServletException("init-param not specified: app-name");
        }
    }

    /**
     * Creates a new <tt>ApplicationInstance</tt> for a visitor to an
     * application.
     *
     * @return a new <tt>ApplicationInstance</tt>
     */
    public ApplicationInstance newApplicationInstance() {
        SpringApplicationInstance result;
        if (context == null) {
            context = WebApplicationContextUtils.getWebApplicationContext(
                getServletContext());
        }
        result = (SpringApplicationInstance) context.getBean(name);
        result.setApplicationContext(context);
        Locale current = locale.get();
        if (current != null) {
            result.setLocale(current);
        }
        return result;
    }

    /**
     * Processes a HTTP request and generates a response.
     *
     * @param request  the incoming <tt>HttpServletRequest</tt>
     * @param response the outgoing <tt>HttpServletResponse</tt>
     */
    @Override
    protected void process(HttpServletRequest request,
                           HttpServletResponse response)
        throws IOException, ServletException {
        HttpSession session = request.getSession();

        // get next instance-counter
        Integer nextInstance = (Integer) session.getAttribute(NEXT_INSTANCE);
        if (nextInstance == null) {
            nextInstance = 1;
        }

        ServletInstance instance = new ServletInstance(request);

        if (instance.getId() != -1 && instance.getId() < nextInstance) {
            servletName.set(instance.getServletName());
            locale.set(request.getLocale());
            super.process(request, response);
        } else {
            // increase instance-counter
            synchronized (session) {
                nextInstance = (Integer) session.getAttribute(NEXT_INSTANCE);
                if (nextInstance == null) {
                    nextInstance = 1;
                }
                instance.setId(nextInstance);
                nextInstance += 1;
                session.setAttribute(NEXT_INSTANCE, nextInstance);
            }

            servletName.set(instance.getServletName());

            // redirect to new servlet-path including request parameters
            String url = instance.getURI();
            String queryString = request.getQueryString();
            if (queryString != null) {
                url += "?" + queryString;
            }
            response.sendRedirect(response.encodeRedirectURL(url));
        }
    }

    /**
     * Returns the servlet name.
     *
     * @return the servlet name
     */
    public String getServletName() {
        return servletName.get();
    }

    /**
     * Helper for manipulating servlet URI paths.
     */
    private static class ServletInstance {

        /**
         * The servlet path.
         */
        String path;

        /**
         * The servlet URI
         */
        String uri;

        /**
         * The servlet instance identifier.
         */
        int id = -1;


        /**
         * Creates a new <tt>ServletInstance</tt>.
         *
         * @param request the request
         */
        public ServletInstance(HttpServletRequest request) {
            // remove jsessionid if present
            String tmp = request.getRequestURI();
            int index = tmp.indexOf(";");
            if (index != -1) {
                tmp = tmp.substring(0, index);
            }
            path = request.getServletPath();
            uri = tmp.substring(0, tmp.indexOf(path, 1) + path.length());

            if (uri.length() != tmp.length()) {
                try {
                    id = Integer.valueOf(tmp.substring(uri.length() + 1));
                } catch (NumberFormatException ignore) {
                    // no-op
                }
            }
        }

        /**
         * Returns the servlet URI.
         *
         * @return the servlet URI
         */
        public String getURI() {
            return uri + "/" + id;
        }

        /**
         * Returns the servlet name.
         *
         * @return the servlet name
         */
        public String getServletName() {
            return path + "/" + id;
        }

        /**
         * Returns the servlet instance identifier.
         *
         * @return the servlet instance identifier, or <tt>-1</tt> if none has
         *         been set
         */
        public int getId() {
            return id;
        }

        /**
         * Sets the servlet instance identifier.
         *
         * @param id the instance identifier
         */
        public void setId(int id) {
            this.id = id;
        }

    }
}
