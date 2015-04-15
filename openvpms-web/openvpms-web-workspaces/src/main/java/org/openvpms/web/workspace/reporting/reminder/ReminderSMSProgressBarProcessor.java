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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.sms.Connection;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;


/**
 * Sends reminder SMSes, updating a progress bar as it goes.
 *
 * @author Tim Anderson
 */
class ReminderSMSProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * The SMS processor.
     */
    private final ReminderSMSProcessor processor;


    /**
     * Constructs a {@link ReminderSMSProgressBarProcessor}.
     *
     * @param reminders     the reminders
     * @param groupTemplate the grouped reminder document template
     * @param statistics    the statistics
     * @param context       the context
     */
    public ReminderSMSProgressBarProcessor(List<List<ReminderEvent>> reminders, Connection connection,
                                           DocumentTemplate groupTemplate, Statistics statistics, Context context) {
        super(reminders, statistics, Messages.get("reporting.reminder.run.sms"));
        processor = new ReminderSMSProcessor(connection, groupTemplate, context);
    }

    /**
     * Processes an object.
     *
     * @param events the reminder events to process
     */
    protected void process(List<ReminderEvent> events) {
        super.process(events);
        try {
            processor.process(events);
            processCompleted(events);
        } catch (Throwable exception) {
            processError(exception, events);
        }
    }

}
