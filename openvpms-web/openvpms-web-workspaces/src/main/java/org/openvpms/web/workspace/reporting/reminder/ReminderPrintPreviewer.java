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

import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;
import java.util.List;

/**
 * Previews print reminders.
 *
 * @author Tim Anderson
 */
public class ReminderPrintPreviewer implements PatientReminderPreviewer {

    /**
     * The reminder processor.
     */
    private final ReminderPrintProcessor processor;

    /**
     * Constructs a {@link ReminderPrintPreviewer}.
     *
     * @param processor the processor to use to prepare reminders
     */
    public ReminderPrintPreviewer(ReminderPrintProcessor processor) {
        this.processor = processor;
    }

    /**
     * Previews reminders.
     *
     * @param reminders  the reminders
     * @param groupBy    the reminder grouping policy. This determines which document template is selected
     * @param cancelDate the date to use when determining if a reminder item should be cancelled
     * @param sent       if {@code true}, the reminder items have been sent previously
     */
    @Override
    public void preview(List<ObjectSet> reminders, ReminderType.GroupBy groupBy, Date cancelDate, boolean sent) {
        PatientReminders state = processor.prepare(reminders, groupBy, cancelDate, sent);
        processor.process(state);
    }
}
