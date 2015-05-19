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

import org.apache.commons.collections4.Predicate;

import java.util.Date;

/**
 * Appointment repeat expression.
 *
 * @author Tim Anderson
 */
public interface RepeatExpression {

    public enum Type {
        DAILY, WEEKDAYS, WEEKLY, MONTHLY, YEARLY, CUSTOM
    }

    /**
     * Returns the type of the expression.
     *
     * @return the type
     */
    Type getType();

    /**
     * Returns the next repeat time after the specified time.
     *
     * @param time      the time
     * @param condition the condition to evaluate for each date
     * @return the next repeat time, or {@code null} if there are no more repeats, or the predicate returns
     *         {@code false}
     */
    Date getRepeatAfter(Date time, Predicate<Date> condition);

}