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

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An {@link ReminderItemSource} that returns a single item.
 *
 * @author Tim Anderson
 */
public class SingleReminderItemSource implements ReminderItemSource {

    /**
     * The reminder event, or {@code null} if the patient or customer cannot be determined.
     */
    private final ReminderEvent event;

    /**
     * The reminder item short name.
     */
    private final String[] shortNames;

    /**
     * Constructs a {@link SingleReminderItemSource}.
     *
     * @param item     the reminder item
     * @param reminder the reminder
     * @param contact  the contact to send to. May be {@code null}
     */
    public SingleReminderItemSource(Act item, Act reminder, Contact contact) {
        this.shortNames = new String[]{item.getArchetypeId().getShortName()};
        ActBean bean = new ActBean(reminder);
        Party patient = (Party) bean.getNodeParticipant("patient");
        ReminderEvent event = null;
        if (patient != null) {
            Party customer = ServiceHelper.getBean(PatientRules.class).getOwner(patient);
            if (customer != null) {
                event = new ReminderEvent(reminder, item, patient, customer, contact);
            }
        }
        this.event = event;
    }

    /**
     * Returns the reminder item archetype short names.
     *
     * @return the reminder item archetype short names
     */
    public String[] getArchetypes() {
        return shortNames;
    }

    /**
     * Returns all items that match the query.
     *
     * @return all items that match the query
     */
    @Override
    public Iterator<ReminderEvent> all() {
        return event == null ? Collections.<ReminderEvent>emptyList().iterator() : Collections.singletonList(event).iterator();
    }

    /**
     * Executes the query.
     *
     * @return the items matching the query
     */
    @Override
    public Iterable<Reminders> query() {
        List<ReminderEvent> reminders = new ArrayList<>();
        if (event != null) {
            reminders.add(event);
        }
        return Collections.singletonList(new Reminders(reminders, ReminderType.GroupBy.NONE));
    }

    /**
     * Counts the number of items matching the criteria.
     *
     * @return the number of items matching
     */
    public int count() {
        return event == null ? 0 : 1;
    }

}
