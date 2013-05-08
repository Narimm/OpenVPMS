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
 */

package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.AbstractReminderProcessorListener;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderProcessor;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Helper to collect reminders produced by the {@link ReminderProcessor}.
 * Each reminder is grouped according to:
 * <ul>
 * <li>contact</li>
 * <li>whether or not the associated reminder type supports grouping</li>
 * </ul>
 *
 * @author Tim Anderson
 */
class ReminderCollector extends AbstractReminderProcessorListener {

    /**
     * The reminders.
     */
    private HashMap<Key, List<ReminderEvent>> reminders = new LinkedHashMap<Key, List<ReminderEvent>>();

    /**
     * Constructs a {@code ReminderCollector}.
     */
    public ReminderCollector() {
        super(ServiceHelper.getArchetypeService(), new PatientRules(ServiceHelper.getArchetypeService(),
                                                                    ServiceHelper.getLookupService()));
    }

    /**
     * Process an event.
     *
     * @param event the event to process
     * @throws OpenVPMSException for any error
     */
    public void process(ReminderEvent event) {
        Key key = new Key(event);
        List<ReminderEvent> events = reminders.get(key);
        if (events == null) {
            events = new ArrayList<ReminderEvent>();
            reminders.put(key, events);
        }
        events.add(event);
    }

    /**
     * Returns the reminders.
     *
     * @return the reminders
     */
    public List<List<ReminderEvent>> getReminders() {
        return new ArrayList<List<ReminderEvent>>(reminders.values());
    }

    private static class Key {

        private final long contactId;

        private boolean group;

        private int hashCode;

        public Key(ReminderEvent event) {
            Contact contact = event.getContact();
            contactId = (contact != null) ? contact.getId() : -1;
            group = event.getReminderType().canGroup();
            hashCode = (contact != null) ? contact.hashCode() : event.getReminder().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean equal = false;
            if (obj instanceof Key) {
                Key key = (Key) obj;
                if (contactId != -1 && contactId == key.contactId && group && key.group) {
                    equal = true;
                }
            }
            return equal;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
