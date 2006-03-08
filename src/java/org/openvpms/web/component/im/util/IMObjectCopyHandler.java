package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Handler to determine how {@link IMObjectCopier} should copy objects.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectCopyHandler {

    /**
     * Determines if an object should be copied
     *
     * @param object the object to check
     * @param parent the parent of <code>object</code>. May be <code>null</code>
     * @return <code>true</code> if the object should be copied; otherwise
     *         <code>false</code>
     */
    boolean copy(IMObject object, IMObject parent);
}
