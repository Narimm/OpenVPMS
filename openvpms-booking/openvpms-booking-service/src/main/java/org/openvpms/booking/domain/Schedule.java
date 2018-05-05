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
 * Appointment schedule.
 *
 * @author Tim Anderson
 */
public class Schedule extends Resource {

    /**
     * The slot size, in minutes.
     */
    private final int slotSize;

    /**
     * Constructs a {@link Schedule}.
     *
     * @param id       the schedule identifier
     * @param name     the schedule name
     * @param slotSize the slot size, in minutes
     */
    public Schedule(long id, String name, int slotSize) {
        super(id, name);
        this.slotSize = slotSize;
    }

    /**
     * Returns the slot size.
     *
     * @return the slot size, in minutes
     */
    public int getSlotSize() {
        return slotSize;
    }
}
