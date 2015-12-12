package org.openvpms.web.webdav.resource;

import io.milton.http.LockToken;
import io.milton.resource.LockableResource;

/**
 * Represents a lock on a resource.
 *
 * @author Tim Anderson
 */
public class ResourceLock {

    /**
     * The resource's unique identifier.
     */
    private final String uniqueId;

    /**
     * The resource name.
     */
    private final String name;

    /**
     * The lock token.
     */
    private final LockToken token;

    /**
     * The owner of the lock.
     */
    private final String user;

    /**
     * Constructs a {@link ResourceLock}.
     *
     * @param resource the resource
     * @param token    the lock token
     * @param user     the owner of the lock
     */
    public ResourceLock(LockableResource resource, LockToken token, String user) {
        this.uniqueId = resource.getUniqueId();
        this.name = resource.getName();
        this.token = token;
        this.user = user;
    }

    /**
     * Returns the resource's unique identifier.
     *
     * @return the unique identifier
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Returns the resource name.
     *
     * @return the resource name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the lock token.
     *
     * @return the lock token
     */
    public LockToken getToken() {
        return token;
    }

    /**
     * Returns the lock owner.
     *
     * @return the login name of the lock owner
     */
    public String getUser() {
        return user;
    }
}
