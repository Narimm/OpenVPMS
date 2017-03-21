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

import org.openvpms.archetype.rules.workflow.WorkflowStatus;

/**
 * Statuses for <em>act.patientReminder*</em> acts.
 *
 * @author Tim Anderson
 */
public class ReminderItemStatus {

    /**
     * Reminder item pending status.
     */
    public static final String PENDING = WorkflowStatus.PENDING;

    /**
     * Reminder item completed status.
     */
    public static final String COMPLETED = WorkflowStatus.COMPLETED;

    /**
     * Reminder item cancelled status.
     */
    public static final String CANCELLED = ReminderStatus.CANCELLED;

    /**
     * Reminder item error status.
     */
    public static final String ERROR = "ERROR";

}
