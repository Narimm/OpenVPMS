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

import org.openvpms.web.echo.help.HelpContext;

/**
 * Previews print reminders.
 *
 * @author Tim Anderson
 */
public class ReminderPrintPreviewer extends AbstractPatientReminderPreviewer {

    /**
     * Constructs a {@link ReminderPrintPreviewer}.
     *
     * @param processor the processor to use to prepare reminders
     * @param help      the help context
     */
    public ReminderPrintPreviewer(ReminderPrintProcessor processor, HelpContext help) {
        super(processor, help);
    }

    /**
     * Previews reminders.
     *
     * @param reminders the reminders
     * @param processor the processor
     * @param help      the help context
     */
    @Override
    protected void preview(PatientReminders reminders, PatientReminderProcessor processor, HelpContext help) {
        processor.process(reminders);
    }
}
