/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;


/**
 * Cancels reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderCancelProcessor extends ReminderProgressBarProcessor {

    /**
     * Creates a new <tt>ReminderCancelProcessor</tt>.
     *
     * @param reminders  the reminders to cancel
     * @param statistics the statistics
     */
    public ReminderCancelProcessor(List<List<ReminderEvent>> reminders,
                                   Statistics statistics) {
        super(reminders, statistics, Messages.get("reporting.reminder.run.cancel"));
    }

    /**
     * Invoked to process a reminder.
     *
     * @param events the event
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    protected void process(List<ReminderEvent> events) {
        ReminderRules rules = getRules();
        try {
            for (ReminderEvent event : events) {
                rules.cancelReminder(event.getReminder());
            }
            processCompleted(events);
        } catch (Throwable exception) {
            processError(exception, events);
        }
    }

}
