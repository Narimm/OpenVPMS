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

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;

import java.util.Date;

/**
 * Appointment start and end times.
 *
 * @author Tim Anderson
 */
public class Times implements Comparable<Times> {

    /**
     * The appointment id, or {@code -1} to indicate the appointment is unsaved.
     */
    private final long id;

    /**
     * The start time.
     */
    private final Date startTime;

    /**
     * The end time.
     */
    private final Date endTime;

    /**
     * Constructs an {@link Times} not associated with an existing appointment.
     *
     * @param startTime the start time
     * @param endTime   the end time
     */
    public Times(Date startTime, Date endTime) {
        this(-1, startTime, endTime);
    }

    /**
     * Constructs an {@link Times} not associated with an existing appointment.
     *
     * @param id        the appointment identifier, or {@code -1} if the appointment hasn't been saved
     * @param startTime the start time
     * @param endTime   the end time
     */
    public Times(long id, Date startTime, Date endTime) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns the appointment identifier.
     *
     * @return the appointment identifier, or {@code -1} if the appointment hasn't been saved
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the appointment start time.
     *
     * @return the appointment start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Returns the appointment end time.
     *
     * @return the appointment end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Creates a new instance from an appointment act.
     *
     * @param act the appointment act
     * @return a new {@link Times}, or {@code null} if the act has no start or end time
     */
    public static Times create(Act act) {
        Date startTime = act.getActivityStartTime();
        Date endTime = act.getActivityEndTime();
        return startTime != null && endTime != null ? new Times(act.getId(), startTime, endTime) : null;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Times) {
            return compareTo((Times) obj) == 0;
        }
        return false;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param object the object to be compared.
     */
    @Override
    public int compareTo(Times object) {
        Date startTime2 = object.getStartTime();
        Date endTime2 = object.getEndTime();
        if (DateRules.compareTo(startTime, startTime2) < 0 && DateRules.compareTo(endTime, startTime2) <= 0) {
            return -1;
        }
        if (DateRules.compareTo(startTime, endTime2) >= 0 && DateRules.compareTo(endTime, endTime2) > 0) {
            return 1;
        }
        return (id < object.id) ? -1 : ((id == object.id) ? 0 : 1);  // TODO replace with Integer.compare()
    }

    /**
     * Determines if the times intersect with a date/time range.
     *
     * @param from the start of the date/time range. May be {@code null}
     * @param to   the end of the date/time range. May be {@code null}
     * @return {@code true} if the ranges intersect
     */
    public boolean intersects(Date from, Date to) {
        return DateRules.intersects(startTime, endTime, from, to);
    }
}
