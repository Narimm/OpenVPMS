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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import java.util.List;

/**
 * Calendar blocks that overlap other events.
 *
 * @author Tim Anderson
 */
public class CalendarBlocks {

    /**
     * The first reserved block encountered.
     */
    private final CalendarBlock reserved;

    /**
     * The unreserved blocks.
     */
    private List<CalendarBlock> unreserved;

    /**
     * Constructs a {@link CalendarBlocks}.
     *
     * @param reserved   the first reserved block encountered. May be {@code null}
     * @param unreserved the unreserved blocks
     */
    public CalendarBlocks(CalendarBlock reserved, List<CalendarBlock> unreserved) {
        this.reserved = reserved;
        this.unreserved = unreserved;
    }

    /**
     * Returns the event that prevents scheduling of an appointment.
     *
     * @return the event, or {@code null} if there is no such event
     */
    public CalendarBlock getReserved() {
        return reserved;
    }

    /**
     * Returns the calendar blocking events that overlap one or more events.
     *
     * @return the events
     */
    public List<CalendarBlock> getUnreserved() {
        return unreserved;
    }
}
