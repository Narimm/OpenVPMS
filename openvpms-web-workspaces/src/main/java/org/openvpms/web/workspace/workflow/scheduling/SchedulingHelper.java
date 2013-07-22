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
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Calendar;
import java.util.Date;


/**
 * Scheduling helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SchedulingHelper {

    /**
     * Returns the minutes from midnight for the specified time.
     *
     * @param time the time
     * @return the minutes from midnight for <tt>time</tt>
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
     * @param roundUp  if <tt>true</tt> round up to the nearest slot, otherwise
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
     * If the event has an {@link ScheduleEvent#ARRIVAL_TIME} property,
     * a formatted string named <em>waiting</em> will be added to the set prior
     * to evaluation of the expression. This indicates the waiting time, and
     * is the difference between the arrival time and the current time.
     * <p/>
     * NOTE: any string sequence containing the characters '\\n' will be treated
     * as new lines.
     *
     * @param expression the expression
     * @param event      the event
     * @return the evaluate result. May be <tt>null</tt>
     */
    public static String evaluate(String expression, PropertySet event) {
        String text;
        String waiting = "";
        if (event.exists(ScheduleEvent.ARRIVAL_TIME)) {
            Date arrival = event.getDate(ScheduleEvent.ARRIVAL_TIME);
            if (arrival != null) {
                waiting = DateFormatter.formatTimeDiff(arrival, new Date());
                waiting = Messages.format("scheduleview.expression.waiting",
                                          waiting);
            }
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

}
