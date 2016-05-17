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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
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
     * Reminder type participation short name.
     */
    public static final String REMINDER_TYPE_PARTICIPATION = "participation.reminderType";

    /**
     * Reminder type template relationship short name.
     */
    public static final String REMINDER_TYPE_TEMPLATE = "entityRelationship.reminderTypeTemplate";
}
