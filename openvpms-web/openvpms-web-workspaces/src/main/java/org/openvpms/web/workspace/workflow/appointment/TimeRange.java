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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Encapsulates a time range for displaying schedules.
 *
 * @author Tim Anderson
 */
public enum TimeRange {

    ALL(0, 24), MORNING(8, 12), AFTERNOON(12, 17), EVENING(17, 24),
    AM(0, 12), PM(12, 24);

    /**
     * The start time, in minutes.
     */
    private final int startMins;

    /**
     * The end time, in minutes.
     */
    private final int endMins;

    /**
     * Constructs a {@link TimeRange}.
     *
     * @param startHour the start hour
     * @param endHour   the end hour
     */
    TimeRange(int startHour, int endHour) {
        this.startMins = startHour * 60;
        this.endMins = endHour * 60;
    }

    /**
     * Returns the start of the range.
     *
     * @return the start of the range, in minutes
     */
    public int getStartMins() {
        return startMins;
    }

    /**
     * Returns the end of the range.
     *
     * @return the end of the range, in minutes
     */
    public int getEndMins() {
        return endMins;
    }

    /**
     * Returns the time range that the specified time falls into.
     *
     * @param time the time
     * @return the corresponding time range
     */
    public static TimeRange getRange(Date time) {
        DateTime dateTime = new DateTime(time);
        int hour = dateTime.getHourOfDay();
        if (hour < 8) {
            return AM;
        } else if (hour >= 8 && hour < 12) {
            return MORNING;
        } else if (hour >= 12 && hour < 17) {
            return AFTERNOON;
        } else if (hour >= 17) {
            return EVENING;
        }
        return ALL;
    }

    /**
     * Returns one of {@link #AM}, or {@link #PM} based on the supplied time.
     *
     * @param time the time
     * @return the corresponding time range
     */
    public static TimeRange getAMorPM(DateTime time) {
        return time.getHourOfDay() < 12 ? AM : PM;
    }

    /**
     * Returns one of {@link #MORNING}, {@link #AFTERNOON} or {@link #EVENING} based on the supplied time.
     *
     * @param time the time
     * @return the corresponding time range
     */
    public static TimeRange getMorningOrAfternoonOrEvening(DateTime time) {
        int hour = time.getHourOfDay();
        if (hour < 12) {
            return MORNING;
        } else if (hour < 15) {
            return AFTERNOON;
        }
        return EVENING;
    }
}
