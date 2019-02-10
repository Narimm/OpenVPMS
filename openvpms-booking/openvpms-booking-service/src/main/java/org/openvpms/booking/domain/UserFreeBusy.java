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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.booking.domain;

import java.util.List;

/**
 * User free and busy time ranges.
 *
 * @author Tim Anderson
 */
public class UserFreeBusy {

    /**
     * The user identifier.
     */
    private final long user;

    /**
     * The free ranges.
     */
    private final List<ScheduleRange> free;

    /**
     * The busy ranges.
     */
    private final List<ScheduleRange> busy;

    /**
     * Constructs a {@link UserFreeBusy}.
     *
     * @param free the free ranges
     * @param busy the busy ranges
     * @parma user the user identifier
     */
    public UserFreeBusy(long user, List<ScheduleRange> free, List<ScheduleRange> busy) {
        this.user = user;
        this.free = free;
        this.busy = busy;
    }

    /**
     * Returns the user identifier.
     *
     * @return the user identifier
     */
    public long getUser() {
        return user;
    }

    /**
     * Returns the free ranges.
     *
     * @return the free ranges
     */
    public List<ScheduleRange> getFree() {
        return free;
    }

    /**
     * Returns the busy ranges.
     *
     * @return the busy ranges
     */
    public List<ScheduleRange> getBusy() {
        return busy;
    }
}
