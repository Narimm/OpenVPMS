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

package org.openvpms.web.webdav.resource;

import io.milton.http.LockToken;
import io.milton.resource.LockableResource;
import org.joda.time.DateTime;

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

    /**
     * Determines if the lock has expired.
     *
     * @return {@code true} if the lock has expired
     */
    public boolean isExpired() {
        return token.isExpired();
    }

    /**
     * Returns the time when a lock expires.
     *
     * @return the time, in milliseconds
     */
    public long getExpirationTime() {
        long result;
        Long seconds = token.timeout.getSeconds();
        if (seconds != null) {
            result = new DateTime(token.getFrom()).plusSeconds(seconds.intValue()).getMillis();
        } else {
            result = Long.MAX_VALUE;
        }
        return result;
    }
}
