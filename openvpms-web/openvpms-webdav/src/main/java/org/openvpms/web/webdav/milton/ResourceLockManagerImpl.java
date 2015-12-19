/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.webdav.milton;

import io.milton.http.HttpManager;
import io.milton.http.LockInfo;
import io.milton.http.LockResult;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import io.milton.http.Request;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.LockableResource;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.webdav.resource.ResourceLock;
import org.openvpms.web.webdav.resource.ResourceLockManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of the {@link ResourceLockManager} interface.
 * <p/>
 * This is heavily based on Milton's {@code SimpleLockManager}.
 *
 * @author Tim Anderson
 */
public class ResourceLockManagerImpl implements ResourceLockManager {

    /**
     * Locks keyed on their unique identifier.
     */
    private final Map<String, ResourceLock> locksByUniqueId;

    /**
     * Locks keyed on their token.
     */
    private final Map<String, ResourceLock> locksByToken;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ResourceLockManagerImpl.class);

    /**
     * Constructs a {@link ResourceLockManagerImpl}.
     */
    public ResourceLockManagerImpl() {
        PassiveExpiringMap.ExpirationPolicy<String, ResourceLock> policy
                = new PassiveExpiringMap.ExpirationPolicy<String, ResourceLock>() {
            @Override
            public long expirationTime(String key, ResourceLock value) {
                return value.getExpirationTime();
            }
        };
        locksByUniqueId = new PassiveExpiringMap<>(policy);
        locksByToken = new PassiveExpiringMap<>(policy);
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
        String token = UUID.randomUUID().toString();
        return lock(timeout, lockInfo, resource, token);
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
        LockResult result;
        ResourceLock lock = locksByToken.get(tokenId);

        // Some clients (yes thats you cadaver) send etags instead of lock tokens in the If header
        // So if the resource is locked by the current user just do a normal refresh
        String uniqueId = resource.getUniqueId();
        if (lock == null) {
            lock = locksByUniqueId.get(uniqueId);
        }

        if (lock == null) {
            log.warn("attempt to refresh missing token/etaq: " + tokenId + " on resource: "
                     + resource.getName() + " will create a new lock");
            LockTimeout timeout = new LockTimeout(60 * 60l);
            String lockedByUser = getCurrentUser();
            if (lockedByUser == null) {
                log.warn("No user in context, lock wont be very effective");
            }
            LockInfo lockInfo = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lockedByUser,
                                             LockInfo.LockDepth.ZERO);
            result = lock(timeout, lockInfo, resource, UUID.randomUUID().toString());
        } else {
            LockToken token = lock.getToken();
            token.setFrom(new Date());
            addLock(tokenId, uniqueId, lock);
            result = LockResult.success(token);
        }
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
        LockToken lockToken = getCurrentLock(resource.getUniqueId());
        if (lockToken == null) {
            log.debug("not locked");
        } else if (lockToken.tokenId.equals(tokenId)) {
            removeLock(lockToken);
        } else {
            throw new NotAuthorizedException("Non-matching tokens: " + tokenId, resource);
        }
    }

    /**
     * Returns the current lock for a resource.
     *
     * @param resource the resource
     * @return the current lock, or {@code null} if the resource is not locked
     */
    @Override
    public synchronized LockToken getCurrentToken(LockableResource resource) {
        LockToken result = null;
        if (resource.getUniqueId() == null) {
            log.warn("No uniqueID for resource: " + resource.getName() + " :: " + resource.getClass());
        } else {
            ResourceLock lock = locksByUniqueId.get(resource.getUniqueId());
            if (lock != null && !lock.isExpired()) {
                LockInfo info = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, lock.getUser(),
                                             LockInfo.LockDepth.ZERO);
                LockToken token = lock.getToken();
                result = new LockToken(token.tokenId, info, token.timeout);
            }
        }
        return result;
    }

    /**
     * Returns the list of resources that are currently locked.
     *
     * @return the locked resources
     */
    @Override
    public synchronized List<ResourceLock> getLocked() {
        return new ArrayList<>(locksByUniqueId.values());
    }

    /**
     * Administratively removes a lock.
     *
     * @param lock the lock to remove
     */
    @Override
    public synchronized boolean remove(ResourceLock lock) {
        locksByUniqueId.remove(lock.getUniqueId());
        return locksByToken.remove(lock.getToken().tokenId) != null;
    }

    /**
     * Locks a resource.
     *
     * @param timeout  the lock timeout
     * @param lockInfo the lock information
     * @param resource the resource to lock
     * @param token    the lock token
     * @return the result of the lock
     */
    private LockResult lock(LockTimeout timeout, LockInfo lockInfo, LockableResource resource, String token) {
        LockResult result;
        String uniqueId = resource.getUniqueId();
        if (uniqueId == null) {
            result = LockResult.failed(LockResult.FailureReason.PRECONDITION_FAILED);
        } else {
            LockToken currentLock = getCurrentLock(uniqueId);
            if (currentLock != null) {
                result = LockResult.failed(LockResult.FailureReason.ALREADY_LOCKED);
            } else {
                LockToken newToken = new LockToken(token, lockInfo, timeout);
                String lockedByUser = lockInfo.lockedByUser;
                // Use this by default, but will normally overwrite with current user
                Request req = HttpManager.request();
                if (req != null) {
                    String currentUser = getCurrentUser();
                    if (currentUser != null) {
                        lockedByUser = currentUser;
                    }
                }
                ResourceLock lock = new ResourceLock(resource, newToken, lockedByUser);
                addLock(token, uniqueId, lock);
                result = LockResult.success(newToken);
            }
        }
        return result;
    }

    /**
     * Adds a lock.
     * <p/>
     * If the lock already exists, this refreshes its expiry time.
     *
     * @param token    the lock token
     * @param uniqueId the resource unique identifier
     * @param lock     the lock
     */
    private void addLock(String token, String uniqueId, ResourceLock lock) {
        locksByUniqueId.put(uniqueId, lock);
        locksByToken.put(token, lock);
    }

    /**
     * Returns the current lock for a resource.
     *
     * @param uniqueId the resource's unique id
     * @return the lock token, or {@code null} if there is no unexpired lock
     */
    private LockToken getCurrentLock(String uniqueId) {
        LockToken result = null;
        ResourceLock lock = locksByUniqueId.get(uniqueId);
        if (lock != null) {
            LockToken token = lock.getToken();
            if (token.isExpired()) {
                removeLock(token);
            } else {
                result = token;
            }
        }
        return result;
    }

    /**
     * Removes a lock.
     *
     * @param token the lock token
     */
    private synchronized void removeLock(LockToken token) {
        log.debug("removeLock: " + token.tokenId);
        ResourceLock lock = locksByToken.remove(token.tokenId);
        if (lock != null) {
            locksByUniqueId.remove(lock.getUniqueId());
        } else {
            log.warn("Couldn't find lock: " + token.tokenId);
        }
    }

    /**
     * Returns the logged in user name.
     *
     * @return the logged in user, or {@code null} if there is no current user.
     */
    private String getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null) ? authentication.getName() : null;
    }

}
