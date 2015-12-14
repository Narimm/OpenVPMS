package org.openvpms.web.webdav.resource;

import io.milton.resource.Resource;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * An {@link IMObject} WebDAV resource.
 *
 * @author Tim Anderson
 */
public interface IMObjectResource extends Resource {

    /**
     * Returns the object reference.
     *
     * @return the object reference
     */
    IMObjectReference getReference();

    /**
     * Returns the object version.
     *
     * @return the version
     */
    long getVersion();
}
