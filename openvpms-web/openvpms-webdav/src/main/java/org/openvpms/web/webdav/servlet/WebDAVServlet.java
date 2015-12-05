package org.openvpms.web.webdav.servlet;

import io.milton.http.HttpManager;
import io.milton.servlet.ServletRequest;
import io.milton.servlet.ServletResponse;
import org.openvpms.web.webdav.milton.HttpManagerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        try {
            httpManager.process(new ServletRequest(request, getServletContext()), new ServletResponse(response));
        } finally {
            response.getOutputStream().flush();
            response.flushBuffer();
        }
    }

}
