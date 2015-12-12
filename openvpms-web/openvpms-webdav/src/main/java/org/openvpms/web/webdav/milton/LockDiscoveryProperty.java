package org.openvpms.web.webdav.milton;

import io.milton.http.LockToken;
import io.milton.http.webdav.PropertyMap;
import io.milton.resource.LockableResource;
import io.milton.resource.PropFindableResource;

/**
 * Represents the lockdiscovery property used in PROPFIND requests.
 *
 * @author Tim Anderson
 */
class LockDiscoveryProperty implements PropertyMap.StandardProperty<LockToken> {

    /**
     * Returns the field name.
     *
     * @return the field name
     */
    public String fieldName() {
        return "lockdiscovery";
    }

    /**
     * Returns the lock token for a resource.
     *
     * @param resource the resource
     * @return the lock token. May be {@code null}
     */
    public LockToken getValue(PropFindableResource resource) {
        LockToken result = null;
        if (resource instanceof LockableResource) {
            result = ((LockableResource) resource).getCurrentLock();
        }
        return result;
    }

    /**
     * Returns the class of this property.
     *
     * @return the value class
     */
    public Class getValueClass() {
        return LockToken.class;
    }

}
