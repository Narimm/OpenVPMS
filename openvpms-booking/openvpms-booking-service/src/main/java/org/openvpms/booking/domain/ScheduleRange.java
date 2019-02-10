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

import java.util.Date;

/**
 * Date/time range for a schedule.
 *
 * @author Tim Anderson
 */
public class ScheduleRange extends Range {

    /**
     * The schedule identifier.
     */
    private final long schedule;

    /**
     * Constructs a {@link ScheduleRange}.
     *
     * @param schedule the schedule identifier
     * @param start    the start of the range
     * @param end      the end of the range
     */
    public ScheduleRange(long schedule, Date start, Date end) {
        super(start, end);
        this.schedule = schedule;
    }

    /**
     * Returns the schedule.
     *
     * @return the schedule
     */
    public long getSchedule() {
        return schedule;
    }
}
