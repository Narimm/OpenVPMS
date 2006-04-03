package org.openvpms.web.spring;

import nextapp.echo2.app.ApplicationInstance;
import org.springframework.context.ApplicationContext;

/**
 * <code>ApplicationInstance</code> for integrating Echo with Spring.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision$ $Date$
 */
public abstract class SpringApplicationInstance extends ApplicationInstance {

    /**
     * The application context.
     */
    private ApplicationContext _context;

    /**
     * Sets the application context.
     *
     * @param context the application context
     */
    public void setApplicationContext(ApplicationContext context) {
        _context = context;
    }

    /**
     * Returns the application context.
     *
     * @return the application context
     */
    public ApplicationContext getApplicationContext() {
        return _context;
    }

}
