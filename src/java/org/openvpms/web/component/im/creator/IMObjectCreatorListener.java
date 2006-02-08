package org.openvpms.web.component.im.creator;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectCreatorListener {

    /**
     * Notifies that a new object has been created.
     *
     * @param object the new object
     */
    void created(IMObject object);
}
