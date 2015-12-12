package org.openvpms.web.webdav.milton;

import io.milton.http.webdav.PropertyMap;
import io.milton.http.webdav.SupportedLocks;
import io.milton.resource.LockableResource;
import io.milton.resource.PropFindableResource;

/**
 * Represents the supportedlock property used in PROPFIND requests.
 *
 * @author Tim Anderson
 */
class SupportedLockProperty implements PropertyMap.StandardProperty<SupportedLocks> {

    /**
     * Returns the field name.
     *
     * @return the field name
     */
    public String fieldName() {
        return "supportedlock";
    }

    /**
     * Returns the supported locks for a resource.
     *
     * @param resource the resource
     * @return the supported locks. May be {@code null}
     */
    public SupportedLocks getValue(PropFindableResource resource) {
        SupportedLocks result = null;
        if (resource instanceof LockableResource) {
            result = new SupportedLocks(resource);
        }
        return result;
    }

    /**
     * Returns the class of this property.
     *
     * @return the value class
     */
    public Class getValueClass() {
        return SupportedLocks.class;
    }
}
