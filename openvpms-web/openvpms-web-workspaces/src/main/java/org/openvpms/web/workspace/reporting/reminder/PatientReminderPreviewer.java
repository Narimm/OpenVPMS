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

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.act.Act;

import java.util.Date;
import java.util.List;

/**
 * Previews reminders.
 *
 * @author Tim Anderson
 */
public interface PatientReminderPreviewer {

    /**
     * Previews reminders.
     *
     * @param item       the selected item
     * @param reminders  the reminders
     * @param groupBy    the reminder grouping policy. This determines which document template is selected
     * @param cancelDate the date to use when determining if a reminder item should be cancelled
     * @param sent       if {@code true}, the reminder items have been sent previously
     */
    void preview(Act item, List<ReminderEvent> reminders, ReminderType.GroupBy groupBy, Date cancelDate, boolean sent);
}
