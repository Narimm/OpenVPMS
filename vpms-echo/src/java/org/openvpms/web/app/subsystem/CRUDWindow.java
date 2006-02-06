package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;


/**
 * Window for peforming CRUD operations.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface CRUDWindow {

    /**
     * Sets a listener for events.
     *
     * @param listener the listener
     */
    void setCRUDWindowListener(CRUDWindowListener listener);

    /**
     * Returns the CRUD component.
     *
     * @return the CRUD component
     */
    Component getComponent();
}
