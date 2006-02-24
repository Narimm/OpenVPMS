package org.openvpms.web.component.app;

import nextapp.echo2.app.ApplicationInstance;

import org.openvpms.web.spring.SpringApplicationInstance;


/**
 * An <code>ApplicationInstance</code> associated with a {@link Context}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class ContextApplicationInstance
        extends SpringApplicationInstance {

    /**
     * Application context.
     */
    private Context _context = new Context();


    /**
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or <code>null</code>
     */
    public static ContextApplicationInstance getInstance() {
        return (ContextApplicationInstance) ApplicationInstance.getActive();
    }

    /**
     * Returns the current context.
     *
     * @return the current context
     */
    public Context getContext() {
        return _context;
    }

    /**
     * Clears the current context.
     */
    protected void clearContext() {
        _context = new Context();
    }
}
