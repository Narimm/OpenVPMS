package org.openvpms.web.app.subsystem;

import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * Listener for {@link CRUDWindow} events.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface CRUDWindowListener {

    /**
     * Invoked when a new object is selected.
     *
     * @param object the selected object
     */
    void selected(IMObject object);

    /**
     * Invoked when an object is saved.
     *
     * @param object the saved object
     */
    void saved(IMObject object);

    /**
     * Invoked when an object is deleted
     *
     * @param object the deleted object
     */
    void deleted(IMObject object);
}
