package org.openvpms.web.webdav.resource;

import io.milton.http.LockManager;

import java.util.List;

/**
 * An {@link LockManager} for {@link IMObjectResource} instances that allows the list of locked resources to be
 * retrieved.
 *
 * @author Tim Anderson
 */
public interface ResourceLockManager extends LockManager {

    /**
     * Returns the list of resources that are currently locked.
     *
     * @return the locked resources
     */
    List<ResourceLock> getLocked();

    /**
     * Administratively removes a lock.
     *
     * @param lock the lock to remove
     * @return {@code true} if the removal was successful
     */
    boolean remove(ResourceLock lock);
}
