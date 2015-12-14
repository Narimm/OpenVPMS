package org.openvpms.web.webdav.milton;

import io.milton.cache.LocalCacheManager;
import io.milton.http.Auth;
import io.milton.http.HttpManager;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Request;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.fs.SimpleLockManager;
import io.milton.resource.LockableResource;
import org.openvpms.web.webdav.resource.ResourceLock;
import org.openvpms.web.webdav.resource.ResourceLockManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the {@link ResourceLockManager} interface.
 *
 * @author Tim Anderson
 */
public class ResourceLockManagerImpl extends SimpleLockManager implements ResourceLockManager {

    /**
     * The resource locks, keyed on unique id.
     */
    private Map<String, ResourceLock> locks = Collections.synchronizedMap(new HashMap<String, ResourceLock>());

    /**
     * Constructs a {@link ResourceLockManagerImpl}.
     */
    public ResourceLockManagerImpl() {
        super(new LocalCacheManager());
    }

    /**
     * Returns the list of resources that are currently locked.
     *
     * @return the locked resources
     */
    @Override
    public synchronized List<ResourceLock> getLocked() {
        List<ResourceLock> result = new ArrayList<>();
        for (Map.Entry<String, ResourceLock> entry : new ArrayList<>(locks.entrySet())) {
            ResourceLock lock = entry.getValue();
            if (lock.getToken().isExpired()) {
                locks.remove(entry.getKey());
            } else {
                result.add(lock);
            }
        }
        return result;
    }

    /**
     * Administratively removes a lock.
     *
     * @param lock the lock to remove
     */
    @Override
    public boolean remove(ResourceLock lock) {
        boolean result = false;
        LockableResourceProxy proxy = new LockableResourceProxy(lock);
        try {
            unlock(lock.getToken().tokenId, proxy);
            result = true;
        } catch (NotAuthorizedException exception) {
            // do nothing
        }
        return result;
    }

    /**
     * Locks a resource.
     *
     * @param timeout  the lock timeout
     * @param lockInfo the lock information
     * @param resource the resource to lock
     * @return the result of the lock
     */
    @Override
    public synchronized LockResult lock(LockTimeout timeout, LockInfo lockInfo, LockableResource resource) {
        LockResult result = super.lock(timeout, lockInfo, resource);
        register(resource, result);
        return result;
    }

    /**
     * Refreshes a lock.
     *
     * @param tokenId  the lock token identifier
     * @param resource the resource to refresh to lock on
     * @return the result of the lock
     */
    @Override
    public synchronized LockResult refresh(String tokenId, LockableResource resource) {
        LockResult result = super.refresh(tokenId, resource);
        register(resource, result);
        return result;
    }

    /**
     * Unlocks a resource.
     *
     * @param tokenId  the lock token identifier
     * @param resource the resource to unlock
     * @throws NotAuthorizedException if the tokenId doesn't correspond to an existing token
     */
    @Override
    public synchronized void unlock(String tokenId, LockableResource resource) throws NotAuthorizedException {
        super.unlock(tokenId, resource);
        locks.remove(resource.getUniqueId());
    }

    /**
     * Returns the current lock for a resource.
     *
     * @param resource the resource
     * @return the current lock, or {@code null} if the resource is not locked
     */
    @Override
    public LockToken getCurrentToken(LockableResource resource) {
        LockToken result = super.getCurrentToken(resource);
        if (result != null && result.isExpired()) {
            result = null;
        }
        return result;
    }

    /**
     * Registers the lock on a resource.
     *
     * @param resource the resource
     * @param result   the lock result
     */
    private void register(LockableResource resource, LockResult result) {
        LockToken lock = result.getLockToken();
        if (lock != null) {
            String user;
            Auth auth = HttpManager.request().getAuthorization();
            user = (auth != null) ? auth.getUser() : lock.info.lockedByUser;
            locks.put(resource.getUniqueId(), new ResourceLock(resource, lock, user));
        }
    }

    /**
     * Helper used to unlock a resource without retrieving it.
     */
    private static class LockableResourceProxy implements LockableResource {

        private final String uniqueId;

        public LockableResourceProxy(ResourceLock lock) {
            uniqueId = lock.getUniqueId();
        }

        @Override
        public LockResult lock(LockTimeout timeout, LockInfo lockInfo) {
            return null;
        }

        @Override
        public LockResult refreshLock(String token) {
            return null;
        }

        @Override
        public void unlock(String tokenId) {

        }

        @Override
        public LockToken getCurrentLock() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return uniqueId;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Object authenticate(String user, String password) {
            return null;
        }

        @Override
        public boolean authorise(Request request, Request.Method method, Auth auth) {
            return false;
        }

        @Override
        public String getRealm() {
            return null;
        }

        @Override
        public Date getModifiedDate() {
            return null;
        }

        @Override
        public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {
            return null;
        }
    }
}
