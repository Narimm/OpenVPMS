package org.openvpms.web.component.im.create;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Listener for {@link IMObjectCreator} events.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public interface IMObjectCreatorListener {

    /**
     * Notifies that a new object has been created.
     *
     * @param object the new object
     */
    void created(IMObject object);
}
