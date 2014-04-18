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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import java.util.Date;

/**
 * Represents an appointment slot.
 *
 * @author Tim Anderson
 */
public class Slot {

    /**
     * The schedule Is.
     */
    private final long schedule;

    /**
     * The slot start time.
     */
    private Date startTime;

    /**
     * The slot end time.
     */
    private Date endTime;


    /**
     * Constructs a {@link Slot}.
     *
     * @param schedule  the schedule id
     * @param startTime the slot start time
     * @param endTime   the slot end time
     */
    public Slot(long schedule, Date startTime, Date endTime) {
        this.schedule = schedule;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns the schedule Id.
     *
     * @return the schedule Id
     */
    public long getSchedule() {
        return schedule;
    }

    /**
     * Returns the slot start time.
     *
     * @return the slot start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the slot start time.
     *
     * @param startTime the slot start time
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the slot end time.
     *
     * @return the slot end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the slot end time.
     *
     * @param endTime the slot end time
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

}
