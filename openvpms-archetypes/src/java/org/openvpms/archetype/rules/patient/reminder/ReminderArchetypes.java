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
 * Reminder archetypes.
 *
 * @author Tim Anderson
 */
public final class ReminderArchetypes {

    /**
     * Patient act reminder short name.
     */
    public static String REMINDER = "act.patientReminder";

    /**
     * Reminder type short name.
     */
    public static final String REMINDER_TYPE = "entity.reminderType";

    /**
     * Reminder count short name.
     */
    public static final String REMINDER_COUNT = "entity.reminderCount";

    /**
     * Reminder rule short name.
     */
    public static final String REMINDER_RULE = "entity.reminderRule";

    /**
     * Reminder type participation short name.
     */
    public static final String REMINDER_TYPE_PARTICIPATION = "participation.reminderType";

    /**
     * Email reminder item short name.
     */
    public static final String EMAIL_REMINDER = "act.patientReminderItemEmail";

    /**
     * SMS reminder item short name.
     */
    public static final String SMS_REMINDER = "act.patientReminderItemSMS";

    /**
     * Print reminder item short name.
     */
    public static final String PRINT_REMINDER = "act.patientReminderItemPrint";

    /**
     * Export reminder item short name.
     */
    public static final String EXPORT_REMINDER = "act.patientReminderItemExport";

    /**
     * List reminder item short name.
     */
    public static final String LIST_REMINDER = "act.patientReminderItemList";

    /**
     * All patient reminder items.
     */
    public static final String REMINDER_ITEMS = "act.patientReminderItem*";

    /**
     * Reminder configuration short name.
     */
    public static final String CONFIGURATION = "entity.reminderConfigurationType";

}
