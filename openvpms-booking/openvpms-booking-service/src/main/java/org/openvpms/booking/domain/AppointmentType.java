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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.booking.domain;

/**
 * Appointment type.
 *
 * @author Tim Anderson
 */
public class AppointmentType extends Resource {

    /**
     * The no. of slots the appointment type occupies.
     */
    private final int slots;

    /**
     * Constructs a {@link AppointmentType}.
     *
     * @param id    the appointment type identifier
     * @param name  the appointment type name
     * @param slots the no. of slots
     */
    public AppointmentType(long id, String name, int slots) {
        super(id, name);
        this.slots = slots;
    }

    /**
     * Returns the no. of slots the appointment type occupies.
     *
     * @return the no. of slots
     */
    public int getSlots() {
        return slots;
    }
}
