package org.openvpms.web.app.subsystem;

import java.util.EventListener;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Event listener for {@link CRUDWindow} events.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface CRUDWindowListener extends EventListener {

    /**
     * Invoked when an object is saved.
     *
     * @param object the saved object
     * @param isNew determines if the object is a new instance
     */
    void saved(IMObject object, boolean isNew);

    /**
     * Invoked when an object is deleted.
     *
     * @param object the deleted object
     */
    void deleted(IMObject object);
}
