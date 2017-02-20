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
public class GroupingReminderIterator implements Iterator<List<ObjectSet>> {

    /**
     * The cache of reminder types.
     */
    private final ReminderTypes reminderTypes;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The underlying iterator over the reminders.
     */
    private PagedReminderIterator pagedIterator;

    /**
     * Wraps the pagedIterator.
     */
    private PushbackIterator<ObjectSet> pushbackIterator;

    /**
     * Reminders grouped by customer.
     */
    private List<ObjectSet> groupByCustomer;

    /**
     * Reminders grouped by patient.
     */
    private List<ObjectSet> groupByPatient;

    /**
     * Reminders with no group.
     */
    private List<ObjectSet> noGroup;

    /**
     * The next reminders to process.
     */
    private List<ObjectSet> next;

    /**
     * Constructs a {@link GroupingReminderIterator}.
     *
     * @param factory       the reminder item query factory
     * @param reminderTypes the reminder type cache
     * @param pageSize      the query page size
     * @param service       the archetype service
     */
    public GroupingReminderIterator(ReminderItemQueryFactory factory, ReminderTypes reminderTypes,
                                    int pageSize, IArchetypeService service) {
        this.service = service;
        this.reminderTypes = reminderTypes;
        pagedIterator = new PagedReminderIterator(factory, pageSize, service);
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
    public List<ObjectSet> next() {
        if (next == null) {
            next = getNext();
            if (next == null) {
                throw new NoSuchElementException();
            }
        }
        List<ObjectSet> result = next;
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
    private List<ObjectSet> getNext() {
        List<ObjectSet> result;
        result = getNextGroup();
        if (result == null) {
            String lastArchetype = null;
            Party lastCustomer = null;
            Party lastPatient = null;
            while (pushbackIterator.hasNext()) {
                ObjectSet set = pushbackIterator.next();
                Act item = (Act) set.get("item");
                boolean grouped = false;
                String archetype = item.getArchetypeId().getShortName();
                if (lastArchetype == null || lastArchetype.equals(archetype)) {
                    lastArchetype = archetype;
                    Party customer = (Party) set.get("customer");
                    if (lastCustomer == null || customer.equals(lastCustomer)) {
                        lastCustomer = customer;
                        Act reminder = (Act) set.get("reminder");
                        ActBean bean = new ActBean(reminder, service);
                        ReminderType type = reminderTypes.get(bean.getNodeParticipantRef("reminderType"));
                        if (type == null || type.getGroupBy() == ReminderType.GroupBy.NONE) {
                            // reminder not grouped
                            grouped = true;
                            noGroup = add(noGroup, set);
                        } else if (type.getGroupBy() == ReminderType.GroupBy.CUSTOMER) {
                            grouped = true;
                            groupByCustomer = add(groupByCustomer, set);
                        } else {
                            // reminder type grouped by patient
                            Party patient = (Party) set.get("patient");
                            if (lastPatient == null || lastPatient.equals(patient)) {
                                grouped = true;
                                lastPatient = patient;
                                groupByPatient = add(groupByPatient, set);
                            }
                        }
                    }
                }
                if (!grouped) {
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
    private List<ObjectSet> getNextGroup() {
        List<ObjectSet> result = null;
        if (groupByPatient != null) {
            result = groupByPatient;
            groupByPatient = null;
        } else if (groupByCustomer != null) {
            result = groupByCustomer;
            groupByCustomer = null;
        } else if (noGroup != null) {
            // ungrouped reminder items are returned one at a time
            if (noGroup.size() > 1) {
                result = new ArrayList<>();
                result.add(noGroup.remove(0));
            } else {
                result = noGroup;
                noGroup = null;
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
    private List<ObjectSet> add(List<ObjectSet> list, ObjectSet set) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(set);
        return list;
    }

}
