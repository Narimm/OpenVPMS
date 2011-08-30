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

import org.openvpms.archetype.rules.patient.reminder.AbstractReminderProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper to collect reminders produced by the {@link ReminderProcessor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderCollector extends AbstractReminderProcessorListener {

    /**
     * The reminders.
     */
    private List<ReminderEvent> reminders = new ArrayList<ReminderEvent>();

    /**
     * Process an event.
     *
     * @param event the event to process
     * @throws OpenVPMSException for any error
     */
    public void process(ReminderEvent event) {
        reminders.add(event);
    }

    /**
     * Returns the reminders.
     *
     * @return the reminders
     */
    public List<ReminderEvent> getReminders() {
        return reminders;
    }
}
