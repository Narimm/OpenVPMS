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
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An {@link ReminderItemSource} that returns a single item.
 *
 * @author Tim Anderson
 */
public class SingleReminderItemSource implements ReminderItemSource {

    /**
     * The reminder set, or {@code null} if the patient or customer cannot be determined.
     */
    private final ObjectSet set;

    /**
     * The reminder item short name.
     */
    private final String[] shortNames;

    /**
     * Constructs a {@link SingleReminderItemSource}.
     *
     * @param item     the reminder item
     * @param reminder the reminder
     */
    public SingleReminderItemSource(Act item, Act reminder) {
        this.shortNames = new String[]{item.getArchetypeId().getShortName()};
        ActBean bean = new ActBean(reminder);
        Party patient = (Party) bean.getNodeParticipant("patient");
        ObjectSet set = null;
        if (patient != null) {
            Party customer = ServiceHelper.getBean(PatientRules.class).getOwner(patient);
            if (customer != null) {
                set = new ObjectSet();
                set.set("reminder", reminder);
                set.set("item", item);
                set.set("customer", customer);
                set.set("patient", patient);
            }
        }
        this.set = set;
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
    public List<ObjectSet> all() {
        return set == null ? Collections.<ObjectSet>emptyList() : Collections.singletonList(set);
    }

    /**
     * Executes the query.
     *
     * @return the items matching the query
     */
    public Iterable<Reminders> query() {
        List<ObjectSet> reminders = new ArrayList<>();
        if (set != null) {
            reminders.add(set);
        }
        return Collections.singletonList(new Reminders(reminders, ReminderType.GroupBy.NONE));
    }

    /**
     * Counts the number of items matching the criteria.
     *
     * @return the number of items matching
     */
    public int count() {
        return set == null ? 0 : 1;
    }

}
