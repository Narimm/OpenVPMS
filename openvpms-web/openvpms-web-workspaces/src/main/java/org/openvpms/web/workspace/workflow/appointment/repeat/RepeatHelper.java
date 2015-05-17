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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Appointment repeat helper methods.
 *
 * @author Tim Anderson
 */
class RepeatHelper {

    /**
     * Returns the repeat expression associated with a series.
     *
     * @param series the series bean
     * @return the expression, or {@code null} if there is no repeat expression
     */
    public static RepeatExpression getExpression(ActBean series) {
        RepeatExpression result = null;
        int interval = series.getInt("interval", -1);
        DateUnits units = DateUnits.fromString(series.getString("units"));
        if (interval != -1 && units != null) {
            result = new CalendarRepeatExpression(interval, units);
        } else {
            String expression = series.getString("expression");
            if (!StringUtils.isEmpty(expression)) {
                result = CronRepeatExpression.parse(expression);
            }
        }
        return result;
    }

    /**
     * Returns the repeat condition associated with a series.
     *
     * @param series the series bean
     * @param count  the no. of appointments in the series prior to the current
     * @return the condition, or {@code null} if there is no repeat condition
     */
    public static RepeatCondition getCondition(ActBean series, int count) {
        RepeatCondition result = null;
        int times = series.getInt("times", -1);
        Date endTime = series.getAct().getActivityEndTime();
        if (times > 0) {
            if (count > 0) {
                // if the series isn't being edited from the start, adjust the no. of repeats
                times -= count;
            }
            result = Repeats.times(times);
        } else if (endTime != null) {
            result = Repeats.until(endTime);
        }
        return result;
    }

    /**
     * Returns a string representation of a repeat expression type.
     *
     * @param type the type
     * @return a string representation of the type
     */
    public static String toString(RepeatExpression.Type type) {
        String result;
        switch (type) {
            case DAILY:
                result = Messages.get("workflow.scheduling.appointment.daily");
                break;
            case WEEKDAYS:
                result = Messages.get("workflow.scheduling.appointment.weekdays");
                break;
            case WEEKLY:
                result = Messages.get("workflow.scheduling.appointment.weekly");
                break;
            case MONTHLY:
                result = Messages.get("workflow.scheduling.appointment.monthly");
                break;
            case YEARLY:
                result = Messages.get("workflow.scheduling.appointment.yearly");
                break;
            default:
                result = WordUtils.capitalizeFully(type.name());
                break;
        }
        return result;
    }

    /**
     * Returns a string representation of date units.
     *
     * @param units the date units
     * @return a string representation of the units
     */
    public static String toString(DateUnits units) {
        String result;
        switch (units) {
            case DAYS:
                result = Messages.get("workflow.scheduling.appointment.days");
                break;
            case WEEKS:
                result = Messages.get("workflow.scheduling.appointment.weeks");
                break;
            case MONTHS:
                result = Messages.get("workflow.scheduling.appointment.months");
                break;
            case YEARS:
                result = Messages.get("workflow.scheduling.appointment.years");
                break;
            default:
                result = WordUtils.capitalizeFully(units.toString().toLowerCase());
                break;
        }
        return result;
    }

}
