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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.patient.reminder;

/**
 * Determines the policy for grouping reminders.
 *
 * @author Tim Anderson
 */
public class ReminderGroupingPolicy {

    /**
     * Policy indicating that all reminders are grouped.
     */
    public static final ReminderGroupingPolicy ALL = new ReminderGroupingPolicy(true, true, true);

    /**
     * Policy indicating that no reminders are grouped.
     */
    public static final ReminderGroupingPolicy NONE = new ReminderGroupingPolicy(false, false, false);

    /**
     * Determines if print reminders are grouped.
     */
    private final boolean print;

    /**
     * Determines if email reminders are grouped.
     */
    private final boolean email;

    /**
     * Determines if SMS reminders are grouped.
     */
    private final boolean sms;

    /**
     * Constructs a {@link ReminderGroupingPolicy}.
     *
     * @param print if {@code true}, group print reminders
     * @param email if {@code true}, group email reminders
     * @param sms   if {@code true}, group SMS reminders
     */
    private ReminderGroupingPolicy(boolean print, boolean email, boolean sms) {
        this.print = print;
        this.email = email;
        this.sms = sms;
    }

    /**
     * Returns a reminder grouping policy for the specified criteria.
     *
     * @param print if {@code true}, group print reminders
     * @param email if {@code true}, group email reminders
     * @param sms   if {@code true}, group SMS reminders
     * @return the reminder grouping policy
     */
    public static ReminderGroupingPolicy getPolicy(boolean print, boolean email, boolean sms) {
        if (print && email && sms) {
            return ReminderGroupingPolicy.ALL;
        } else if (!print && !email && !sms) {
            return ReminderGroupingPolicy.NONE;
        }
        return new ReminderGroupingPolicy(print, email, sms);
    }

    /**
     * Determines if reminder items of the specified archetype are grouped.
     *
     * @param archetype the reminder item archetype
     * @return {@code true} if they are grouped, otherwise {@code false}
     */
    public boolean group(String archetype) {
        if (print && ReminderArchetypes.PRINT_REMINDER.equals(archetype)) {
            return true;
        } else if (email && ReminderArchetypes.EMAIL_REMINDER.equals(archetype)) {
            return true;
        } else if (sms && ReminderArchetypes.SMS_REMINDER.equals(archetype)) {
            return true;
        }
        return false;
    }
}
