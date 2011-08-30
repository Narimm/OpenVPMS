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

package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Cancels reminders.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderCancelProcessor
        extends ProgressBarProcessor<ReminderEvent> {

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;


    /**
     * Constructs a new <tt>ReminderCancelProcessor</tt>.
     *
     * @param reminders the reminders to cancel
     */
    public ReminderCancelProcessor(List<ReminderEvent> reminders) {
        super(reminders, Messages.get("reporting.reminder.run.cancel"));
        rules = new ReminderRules();
    }

    /**
     * Invoked to process a reminder.
     *
     * @param event the event
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected void process(ReminderEvent event) {
        rules.cancelReminder(event.getReminder());
        processCompleted(event);
    }
}
