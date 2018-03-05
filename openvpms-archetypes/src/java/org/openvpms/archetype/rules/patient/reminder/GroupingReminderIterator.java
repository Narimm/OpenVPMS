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

package org.openvpms.archetype.rules.patient.reminder;

import org.apache.commons.collections4.iterators.PushbackIterator;
import org.openvpms.archetype.rules.patient.reminder.ReminderType.GroupBy;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator that groups reminders according to their reminder type {@code groupBy} node.
 *
 * @author Tim Anderson
 * @see ReminderItemQueryFactory
 */
public class GroupingReminderIterator implements Iterator<Reminders> {

    /**
     * The cache of reminder types.
     */
    private final ReminderTypes reminderTypes;

    /**
     * Determines the policy to use when a reminder type indicates to group by customer.
     */
    private final ReminderGroupingPolicy groupByCustomer;

    /**
     * Determines the policy to use when a reminder type indicates to group by patient.
     */
    private final ReminderGroupingPolicy groupByPatient;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The underlying iterator over the reminders.
     */
    private PagedReminderItemIterator pagedIterator;

    /**
     * Wraps the pagedIterator.
     */
    private PushbackIterator<ObjectSet> pushbackIterator;

    /**
     * Reminders grouped by customer.
     */
    private List<ReminderEvent> remindersByCustomer;

    /**
     * Reminders grouped by patient.
     */
    private List<ReminderEvent> remindersByPatient;

    /**
     * Reminders with no group.
     */
    private List<ReminderEvent> ungroupedReminders;

    /**
     * The next reminders to process.
     */
    private Reminders next;

    /**
     * Constructs a {@link GroupingReminderIterator}.
     *
     * @param factory         the reminder item query factory
     * @param reminderTypes   the reminder type cache
     * @param pageSize        the query page size
     * @param groupByCustomer determines the policy to use when a reminder type indicates to group by customer
     * @param groupByPatient  determines the policy to use when a reminder type indicates to group by patient
     * @param service         the archetype service
     */
    public GroupingReminderIterator(ReminderItemQueryFactory factory, ReminderTypes reminderTypes,
                                    int pageSize, ReminderGroupingPolicy groupByCustomer,
                                    ReminderGroupingPolicy groupByPatient, IArchetypeService service) {
        this.service = service;
        this.reminderTypes = reminderTypes;
        this.groupByCustomer = groupByCustomer;
        this.groupByPatient = groupByPatient;
        pagedIterator = new PagedReminderItemIterator(factory, pageSize, service);
        pushbackIterator = new PushbackIterator<>(pagedIterator);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    public boolean hasNext() {
        if (next == null) {
            next = getNext();
        }
        return next != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public Reminders next() {
        if (next == null) {
            next = getNext();
            if (next == null) {
                throw new NoSuchElementException();
            }
        }
        Reminders result = next;
        next = null;
        return result;
    }

    /**
     * Flags the set as updated. This forces the query to be re-issued from the start.
     */
    public void updated() {
        pagedIterator.updated();
    }

    /**
     * Returns the next set of reminders.
     *
     * @return the reminders
     */
    private Reminders getNext() {
        Reminders result = getNextGroup();
        if (result == null) {
            String lastArchetype = null;
            Party lastCustomer = null;
            Party lastPatient = null;
            while (pushbackIterator.hasNext()) {
                ObjectSet set = pushbackIterator.next();
                Act item = (Act) set.get("item");
                boolean processed = false;
                String archetype = item.getArchetypeId().getShortName();
                if (lastArchetype == null || lastArchetype.equals(archetype)) {
                    lastArchetype = archetype;
                    Party customer = (Party) set.get("customer");
                    if (lastCustomer == null || customer.equals(lastCustomer)) {
                        lastCustomer = customer;
                        Act reminder = (Act) set.get("reminder");
                        ActBean bean = new ActBean(reminder, service);
                        GroupBy groupBy = getGroupBy(bean);
                        if (groupBy == GroupBy.NONE
                            || (groupBy == GroupBy.CUSTOMER && !groupByCustomer.group(archetype))
                            || (groupBy == GroupBy.PATIENT && !groupByPatient.group(archetype))) {
                            // reminder not grouped
                            processed = true;
                            ungroupedReminders = add(ungroupedReminders, set);
                        } else if (groupBy == GroupBy.CUSTOMER) {
                            processed = true;
                            remindersByCustomer = add(remindersByCustomer, set);
                        } else {
                            // reminder type grouped by patient
                            Party patient = (Party) set.get("patient");
                            if (lastPatient == null || lastPatient.equals(patient)) {
                                processed = true;
                                lastPatient = patient;
                                remindersByPatient = add(remindersByPatient, set);
                            }
                        }
                    }
                }
                if (!processed) {
                    // put it back in the list
                    pushbackIterator.pushback(set);
                    break;
                }
            }
            result = getNextGroup();
        }
        return result;
    }

    /**
     * Returns the next group of reminders.
     *
     * @return the next group of reminders
     */
    private Reminders getNextGroup() {
        Reminders result = null;
        if (remindersByPatient != null) {
            result = new Reminders(remindersByPatient, GroupBy.PATIENT);
            remindersByPatient = null;
        } else if (remindersByCustomer != null) {
            result = new Reminders(remindersByCustomer, GroupBy.CUSTOMER);
            remindersByCustomer = null;
        } else if (ungroupedReminders != null) {
            // ungrouped reminder items are returned one at a time
            if (ungroupedReminders.size() > 1) {
                result = new Reminders(ungroupedReminders.remove(0));
            } else {
                result = new Reminders(ungroupedReminders, GroupBy.NONE);
                ungroupedReminders = null;
            }
        }
        return result;
    }

    /**
     * Adds a reminder set to a list.
     *
     * @param list the list to add to. If {@code null}, it will be created
     * @param set  the reminder set
     * @return the list
     */
    private List<ReminderEvent> add(List<ReminderEvent> list, ObjectSet set) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(new ReminderEvent((Act) set.get("reminder"), (Act) set.get("item"), (Party) set.get("patient"),
                                   (Party) set.get("customer")));
        return list;
    }

    /**
     * Returns the group-by policy of the reminder type.
     *
     * @param bean the reminder bean
     * @return the group-by policy
     */
    private GroupBy getGroupBy(ActBean bean) {
        ReminderType type = reminderTypes.get(bean.getNodeParticipantRef("reminderType"));
        return type != null ? type.getGroupBy() : GroupBy.NONE;
    }

}
