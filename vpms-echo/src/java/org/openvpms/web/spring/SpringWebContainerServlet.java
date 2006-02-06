package org.openvpms.web.spring;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.WebContainerServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <code>WebContainerServlet</code> for integrating Echo with Spring.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class SpringWebContainerServlet extends WebContainerServlet {

    /**
     * The Spring application context.
     */
    private ApplicationContext _context;

    /**
     * The application name.
     */
    private String _name;

    /**
     * Creates a new <code>ApplicationInstance</code> for a visitor to an
     * application.
     *
     * @return a new <code>ApplicationInstance</code>
     */
    public ApplicationInstance newApplicationInstance() {
        SpringApplicationInstance result = null;
        if (_context == null) {
            _context = WebApplicationContextUtils.getWebApplicationContext(
                    getServletContext());

            String[] names = _context.getBeanNamesForType(
                    SpringApplicationInstance.class);
            if (names == null || names.length == 0) {
                throw new IllegalStateException(
                        "A bean of type "
                        + SpringApplicationInstance.class.getName()
                        + "must be registered in the application context.");
            }
            if (names.length > 1) {
                throw new IllegalStateException(
                        "Multiple beans of type "
                        + SpringApplicationInstance.class.getName()
                        + "registered in the application context.");
            }
            _name = names[0];
        }
        result = (SpringApplicationInstance) _context.getBean(_name);
        result.setApplicationContext(_context);
        return result;
    }

}
