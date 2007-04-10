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

package org.openvpms.web.app.patient.reminder;

import org.openvpms.archetype.rules.patient.reminder.AbstractReminderProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;

import java.util.ArrayList;
import java.util.List;


/**
 * Listener for {@link ReminderEvent.Action.PHONE} events.
 * Collects all of the relevant acts for later processing into a report.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class ReminderPhoneListener extends AbstractReminderProcessorListener {

    /**
     * Reminders that need to be listed to phone.
     */
    private final List<Act> phoneReminders = new ArrayList<Act>();


    /**
     * Invoked to process a reminder.
     *
     * @param event the event
     */
    public void process(ReminderEvent event) {
        phoneReminders.add(event.getReminder());
    }

    /**
     * Returns the phone reminders.
     *
     * @return the phone reminders
     */
    public List<Act> getPhoneReminders() {
        return phoneReminders;
    }

    /**
     * Updates all phone reminders.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void updateAll() {
        for (Act reminder : phoneReminders) {
            update(reminder);
        }
    }
}
