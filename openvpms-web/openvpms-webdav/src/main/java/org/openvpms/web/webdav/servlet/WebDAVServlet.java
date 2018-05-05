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

import com.github.isrsal.logging.RequestWrapper;
import com.github.isrsal.logging.ResponseWrapper;
import io.milton.http.HttpManager;
import io.milton.http.Response;
import io.milton.servlet.ServletRequest;
import io.milton.servlet.ServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.webdav.milton.HttpManagerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Servlet to allow editing of OpenOffice/Word documents in OpenVPMS via WebDAV.
 *
 * @author Tim Anderson
 */
public class WebDAVServlet extends HttpServlet {

    /**
     * The http manager.
     */
    private HttpManager httpManager;

    /**
     * Used for debugging.
     */
    private AtomicLong id = new AtomicLong(1L);

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(WebDAVServlet.class);

    /**
     * Initialise the servlet.
     *
     * @throws ServletException if an exception occurs that interrupts the servlet's normal operation
     */
    @Override
    public void init() throws ServletException {
        super.init();
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        HttpManagerFactory factory = context.getBean(HttpManagerFactory.class);
        httpManager = factory.create(getServletContext());
    }

    /**
     * Returns information about the servlet, such as author, version, and copyright.
     *
     * @return the servlet information
     */
    @Override
    public String getServletInfo() {
        return "WebDAVServlet";
    }

    /**
     * Services a request.
     *
     * @param request  the client request
     * @param response the servlet response
     * @throws IOException      if an input or output error occurs while the servlet is handling the HTTP request
     * @throws ServletException if the HTTP request cannot be handled
     * @see Servlet#service
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            long requestId = id.incrementAndGet();
            request = new RequestWrapper(requestId, request);
            response = new ResponseWrapper(requestId, response);
            log((RequestWrapper) request);
        }
        try {
            httpManager.process(new ServletRequest(request, getServletContext()), new ServletResponse(response));
        } finally {
            response.getOutputStream().flush();
            response.flushBuffer();
        }
        if (log.isDebugEnabled()) {
            log((RequestWrapper) request, (ResponseWrapper) response);
        }
    }

    /**
     * Logs a request.
     *
     * @param request the request
     */
    private void log(RequestWrapper request) {
        StringBuilder builder = new StringBuilder();
        builder.append("WebDAV request: id=").append(request.getId())
                .append(", ")
                .append(request.getMethod())
                .append(" ")
                .append(request.getRequestURL());
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            builder.append("\n header: ").append(name).append("=").append(request.getHeader(name));
        }
        log.debug(builder.toString());
    }

    /**
     * Logs the request body and response, after they have been processed.
     *
     * @param request  the request
     * @param response the response
     */
    private void log(RequestWrapper request, ResponseWrapper response) {
        StringBuilder builder = new StringBuilder();
        if (isLoggable(request.getContentType())) {
            builder.append("WebDAV request: id=").append(request.getId())
                    .append(", ")
                    .append(request.getMethod())
                    .append(" ")
                    .append(request.getRequestURL());
            builder.append("\n body: ").append(getBody(request.toByteArray(), request.getCharacterEncoding()));
        }
        Response.Status status = ServletResponse.Status.fromCode(response.getStatus());
        builder.append("WebDAV response: id=").append(response.getId())
                .append(", ")
                .append(request.getMethod())
                .append(request.getRequestURL())
                .append(" - ")
                .append(status);
        for (String name : response.getHeaderNames()) {
            builder.append("\n header: ").append(name).append("=").append(response.getHeader(name));
        }
        if (isLoggable(response.getContentType())) {
            builder.append("\n body: ").append(getBody(response.toByteArray(), response.getCharacterEncoding()));
        }
        log.debug(builder.toString());
    }

    /**
     * Returns the request/response body as a string.
     *
     * @param body              the request/response body
     * @param characterEncoding the body encoding. May be {@code null}
     * @return the string ofrm of the body
     */
    private String getBody(byte[] body, String characterEncoding) {
        String result = "";
        if (characterEncoding == null) {
            characterEncoding = "UTF-8";
        }
        try {
            result = new String(body, characterEncoding);
        } catch (UnsupportedEncodingException exception) {
            log.warn("Failed to parse body: ", exception);
        }
        return result;
    }

    /**
     * Determines if a content type is loggable.
     *
     * @param contentType the content type
     * @return {@code true} if the content type is loggable
     */
    private boolean isLoggable(String contentType) {
        return contentType != null && (contentType.contains("application/xml") || contentType.contains("text/xml"));
    }

}
