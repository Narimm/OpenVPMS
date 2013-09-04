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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.TaskStatus;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Calendar;
import java.util.Date;


/**
 * Scheduling helper.
 *
 * @author Tim Anderson
 */
public class SchedulingHelper {

    /**
     * Returns the minutes from midnight for the specified time.
     *
     * @param time the time
     * @return the minutes from midnight for {@code time}
     */
    public static int getMinutes(Date time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int mins = calendar.get(Calendar.MINUTE);
        return (hour * 60) + mins;
    }

    /**
     * Returns the minutes from midnight for the specified time, rounded
     * up or down to the nearest slot.
     *
     * @param time     the time
     * @param slotSize the slot size
     * @param roundUp  if {@code true} round up to the nearest slot, otherwise
     *                 round down
     * @return the minutes from midnight for the specified time
     */
    public static int getSlotMinutes(Date time, int slotSize, boolean roundUp) {
        int mins = getMinutes(time);
        int result = (mins / slotSize) * slotSize;
        if (result != mins && roundUp) {
            result += slotSize;
        }
        return result;
    }

    /**
     * Evaluates an xpath expression against the supplied event.
     * <p/>
     * This adds a "waiting" time attribute to the event prior to evaluation as determined by {@link #getWaitingTime}.
     * <p/>
     * NOTE: any string sequence containing the characters '\\n' will be treated
     * as new lines.
     *
     * @param expression the expression
     * @param event      the event
     * @return the evaluate result. May be {@code null}
     */
    public static String evaluate(String expression, PropertySet event) {
        String text;
        String waiting = getWaitingTime(event);
        if (waiting != null) {
            waiting = Messages.format("scheduleview.expression.waiting", waiting);
        } else {
            waiting = ""; // makes it easier to use in expressions
        }
        event.set("waiting", waiting);

        JXPathContext context = JXPathHelper.newContext(event);

        // hack to replace all instances of '\\n' with new lines to
        // enable new lines to be included in the text
        // Can't use <br> as all xml is escaped
        expression = expression.replace("\'\\n\'", "\'\n\'");
        try {
            Object value = context.getValue(expression);
            text = (value != null) ? value.toString() : null;
        } catch (Throwable exception) {
            text = "Expression Error";
        }
        return text;
    }

    /**
     * Calculates a waiting time for an event.
     * <p/>
     * This is a formatted string indicating the amount of time spent waiting in an event.
     * <p/>
     * If the event is an appointment with:
     * <ul>
     * <li>an {@link ScheduleEvent#ARRIVAL_TIME} property and is CHECKED_IN,
     * this a formatted string indicating the difference between difference between the arrival time and the
     * current time.</li>
     * <li>any other status, the waiting time is {@code null}</li>
     * </ul>
     * <p/>
     * If the event is a task, the waiting time will be calculated as:
     * <ul>
     * <li>{@code consultStartTime - startTime} if the task is not PENDING</li>
     * <li>{@code now - startTime} if the task is PENDING</li>
     * </ul>
     * <p/>
     *
     * @param event the event
     * @return the waiting time. May be {@code null}
     */
    public static String getWaitingTime(PropertySet event) {
        String waiting = null;
        String status = event.getString(ScheduleEvent.ACT_STATUS);
        boolean appointment = TypeHelper.isA(event.getReference(ScheduleEvent.ACT_REFERENCE),
                                             ScheduleArchetypes.APPOINTMENT);
        if (appointment) {
            if (status.equals(AppointmentStatus.CHECKED_IN) && event.exists(ScheduleEvent.ARRIVAL_TIME)) {
                Date arrival = event.getDate(ScheduleEvent.ARRIVAL_TIME);
                if (arrival != null) {
                    waiting = DateFormatter.formatTimeDiff(arrival, new Date());
                }
            }
        } else {
            Date start = event.getDate(ScheduleEvent.ACT_START_TIME);
            Date end;
            if (status.equals(TaskStatus.PENDING) || !event.exists(ScheduleEvent.CONSULT_START_TIME)) {
                end = new Date();
            } else {
                end = event.getDate(ScheduleEvent.CONSULT_START_TIME);
            }
            waiting = DateFormatter.formatTimeDiff(start, end);
        }
        return waiting;
    }

}
