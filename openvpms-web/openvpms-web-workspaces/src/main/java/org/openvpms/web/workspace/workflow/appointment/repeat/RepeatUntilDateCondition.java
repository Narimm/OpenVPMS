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
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;

/**
 * A {@link RepeatCondition} that repeats until the specified date.
 *
 * @author Tim Anderson
 */
class RepeatUntilDateCondition implements RepeatCondition {

    private final Date date;

    public RepeatUntilDateCondition(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
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
        } else if (obj instanceof RepeatUntilDateCondition) {
            return DateRules.dateEquals(date, ((RepeatUntilDateCondition) obj).getDate());
        }
        return false;
    }

    /**
     * Creates a predicate for this condition.
     *
     * @return a new predicate
     */
    @Override
    public Predicate<Date> create() {
        return new Predicate<Date>() {
            @Override
            public boolean evaluate(Date object) {
                return DateRules.compareDates(object, date) <= 0;
            }
        };
    }

    /**
     * Returns a string representation of the condition.
     *
     * @return a formatted string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(Messages.get("workflow.scheduling.appointment.until"));
        result.append(" ");
        result.append(DateFormatter.getFullDateFormat().format(date));
        return result.toString();
    }

}
