package org.openvpms.web.webdav.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.webdav.launch.EditorLauncher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * Servlet to generate a JNLP to launch the {@link EditorLauncher} for a given document URL.
 *
 * @author Tim Anderson
 */
public class WebDAVEditorLauncherServlet extends HttpServlet {

    /**
     * Handles a GET request.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException for any I/O exception
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/x-java-jnlp-file");
        response.setHeader("Cache-Control", "no-cache");
        String output = generateJNLP(request);
        response.getWriter().println(output);
    }

    /**
     * Generates the JNLP for a request.
     *
     * @param request the request. The document URL must be passed in the query string
     * @return the JNLP
     * @throws IOException for any I/O error
     */
    private String generateJNLP(HttpServletRequest request) throws IOException {
        String jnlp = getTemplate();
        String baseURL = getServerURL(request);
        String requestURI = request.getRequestURI();

        int index = requestURI.lastIndexOf('/');
        String name = requestURI.substring(index + 1);       // exclude /
        String context = requestURI.substring(0, index + 1); // Include
        String codebase = context + "webstart";
        jnlp = StringUtils.replace(jnlp, "$$name", name);
        jnlp = StringUtils.replace(jnlp, "$$hostname", request.getServerName());
        jnlp = StringUtils.replace(jnlp, "$$codebase", baseURL + codebase);
        jnlp = StringUtils.replace(jnlp, "$$context", baseURL + context);
        jnlp = StringUtils.replace(jnlp, "$$site", baseURL);
        jnlp = StringUtils.replace(jnlp, "$$doc", request.getQueryString());
        return jnlp;
    }

    /**
     * Returns the JNLP template.
     *
     * @return the JNLP template as a string
     * @throws IOException for any I/O error
     */
    private String getTemplate() throws IOException {
        String template;
        try (InputStream stream = getClass().getResourceAsStream("/webstart/launch.jnlp")) {
            template = IOUtils.toString(stream);
        }
        return template;
    }

    /**
     * Returns the server URL.
     *
     * @param request the request
     * @return the server URL
     */
    private String getServerURL(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        return url.substring(0, url.length() - request.getRequestURI().length());
    }

}
