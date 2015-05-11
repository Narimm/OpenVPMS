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
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * An {@link RepeatCondition} that repeats N times.
 *
 * @author Tim Anderson
 */
class RepeatNTimesCondition implements RepeatCondition {

    /**
     * The no. of times to repeat.
     */
    private final int times;

    /**
     * Constructs an {@link RepeatNTimesCondition}.
     *
     * @param times the no. of times to repeat
     */
    public RepeatNTimesCondition(int times) {
        this.times = times;
    }

    /**
     * Returns the number of times to repeat.
     *
     * @return the no. of times
     */
    public int getTimes() {
        return times;
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
        } else if (obj instanceof RepeatNTimesCondition) {
            return times == ((RepeatNTimesCondition) obj).getTimes();
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
        return new TimesPredicate<Date>(times);
    }

    /**
     * Returns a string representation of the condition.
     *
     * @return a formatted string
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        switch (times) {
            case 1:
                result.append(Messages.get("workflow.scheduling.appointment.once"));
                break;
            case 2:
                result.append(Messages.get("workflow.scheduling.appointment.twice"));
                break;
            default:
                result.append(times);
                result.append(" ");
                result.append(Messages.get("workflow.scheduling.appointment.times"));
        }
        return result.toString();
    }
}
