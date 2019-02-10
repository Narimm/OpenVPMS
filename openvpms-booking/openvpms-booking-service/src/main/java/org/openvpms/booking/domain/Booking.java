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

/**
 * Appointment booking request.
 *
 * @author Tim Anderson
 */
public class Booking extends org.openvpms.booking.domain.v1.Booking {

    /**
     * The user identifier.
     */
    private long user;

    /**
     * Default constructor.
     */
    public Booking() {
    }

    /**
     * Returns the user to make the booking with.
     *
     * @return the user, or {@code <= 0} if no specific user is required
     */
    public long getUser() {
        return user;
    }

    /**
     * Sets the user to make the booking with.
     *
     * @param user the user, or {@code <= 0} if no specific user is required
     */
    public void setUser(long user) {
        this.user = user;
    }
}
