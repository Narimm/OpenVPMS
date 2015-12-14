package org.openvpms.web.webdav.servlet;

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
import java.util.Enumeration;

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
            StringBuilder builder = new StringBuilder();
            builder.append("WebDAV request: ").append(request.getMethod()).append(" ").append(request.getRequestURL());
            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                String name = headers.nextElement();
                builder.append("\n header: ").append(name).append("=").append(request.getHeader(name));
            }
            log.debug(builder.toString());
        }
        try {
            httpManager.process(new ServletRequest(request, getServletContext()), new ServletResponse(response));
        } finally {
            response.getOutputStream().flush();
            response.flushBuffer();
        }
        if (log.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            Response.Status status = ServletResponse.Status.fromCode(response.getStatus());
            builder.append("WebDAV response: ").append(request.getMethod()).append(" ").append(
                    request.getRequestURL()).append(" - ").append(status);
            for (String name : response.getHeaderNames()) {
                builder.append("\n header: ").append(name).append("=").append(response.getHeader(name));
            }
            log.debug(builder.toString());
        }
    }
}
