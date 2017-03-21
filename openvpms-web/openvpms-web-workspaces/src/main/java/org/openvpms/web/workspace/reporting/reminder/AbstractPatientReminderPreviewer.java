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
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;
import java.util.List;

/**
 * Abstract implementation of {@link PatientReminderPreviewer}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPatientReminderPreviewer implements PatientReminderPreviewer {

    /**
     * The reminder processor.
     */
    private final PatientReminderProcessor processor;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs an {@link AbstractPatientReminderPreviewer}.
     *
     * @param processor the processor
     * @param help      the help context
     */
    public AbstractPatientReminderPreviewer(PatientReminderProcessor processor, HelpContext help) {
        this.processor = processor;
        this.help = help;
    }

    /**
     * Previews reminders.
     *
     * @param item       the selected item
     * @param reminders  the reminders
     * @param groupBy    the reminder grouping policy. This determines which document template is selected
     * @param cancelDate the date to use when determining if a reminder item should be cancelled
     * @param sent       if {@code true}, the reminder items have been sent previously
     */
    @Override
    public void preview(Act item, List<ReminderEvent> reminders, ReminderType.GroupBy groupBy, Date cancelDate,
                        boolean sent) {
        PatientReminders state = processor.prepare(reminders, groupBy, cancelDate, sent);
        if (!Reminders.contains(item, state.getReminders())) {
            if (Reminders.contains(item, state.getCancelled())) {
                InformationDialog.show(Messages.get("reporting.reminder.send.cancelled"));
            } else {
                item = Reminders.findItem(item, state.getErrors());
                String error = null;
                if (item != null) {
                    ActBean bean = new ActBean(item);
                    error = bean.getString("error");
                }
                InformationDialog.show(Messages.format("reporting.reminder.preview.error", error));
            }
        } else {
            preview(state, processor, help);
        }
    }

    /**
     * Previews reminders.
     *
     * @param reminders the reminders
     * @param processor the processor
     * @param help      the help context
     */
    protected abstract void preview(PatientReminders reminders, PatientReminderProcessor processor, HelpContext help);
}
