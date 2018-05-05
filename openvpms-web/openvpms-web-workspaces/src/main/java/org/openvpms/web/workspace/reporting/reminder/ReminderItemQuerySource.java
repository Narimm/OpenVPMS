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

import org.openvpms.archetype.rules.patient.reminder.GroupingReminderIterator;
import org.openvpms.archetype.rules.patient.reminder.PagedReminderItemIterator;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderGroupingPolicy;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.patient.reminder.Reminders;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.system.ServiceHelper;

import java.util.Iterator;

/**
 * A {@link ReminderItemSource} that uses an {@link ReminderItemQueryFactory}.
 *
 * @author Tim Anderson
 */
public class ReminderItemQuerySource implements ReminderItemSource {

    /**
     * The query factory.
     */
    private final ReminderItemQueryFactory factory;

    /**
     * The reminder types.
     */
    private final ReminderTypes reminderTypes;

    /**
     * Determines the policy when a reminder type indicates {@link ReminderType.GroupBy#CUSTOMER}.
     */
    private final ReminderGroupingPolicy groupByCustomer;

    /**
     * Determines the policy when a reminder type indicates {@link ReminderType.GroupBy#PATIENT}.
     */
    private final ReminderGroupingPolicy groupByPatient;

    /**
     * The maximum no. of reminders to query at once.
     */
    private static final int PAGE_SIZE = 100;

    /**
     * Constructs a {@link ReminderItemQuerySource}.
     *
     * @param factory       the query factory
     * @param reminderTypes the reminder types
     * @param configuration the reminder configuration
     */
    public ReminderItemQuerySource(ReminderItemQueryFactory factory, ReminderTypes reminderTypes,
                                   ReminderConfiguration configuration) {
        this.factory = factory;
        this.reminderTypes = reminderTypes;
        groupByCustomer = configuration.getGroupByCustomerPolicy();
        groupByPatient = configuration.getGroupByPatientPolicy();
    }

    /**
     * Returns the reminder item archetype short names.
     *
     * @return the reminder item archetype short names
     */
    public String[] getArchetypes() {
        return factory.getArchetypes();
    }

    /**
     * Returns all items that match the query.
     *
     * @return all items that match the query
     */
    @Override
    public Iterator<ReminderEvent> all() {
        return new ObjectSetToReminderEventIterator(new PagedReminderItemIterator(factory, PAGE_SIZE,
                                                                                  ServiceHelper.getArchetypeService()));
    }

    /**
     * Executes the query.
     *
     * @return the items matching the query
     */
    public Iterable<Reminders> query() {
        return new Iterable<Reminders>() {
            @Override
            public Iterator<Reminders> iterator() {
                return new GroupingReminderIterator(factory, reminderTypes, PAGE_SIZE, groupByCustomer, groupByPatient,
                                                    ServiceHelper.getArchetypeService());
            }
        };
    }

    /**
     * Counts the number of items matching the criteria.
     *
     * @return the number of items matching
     */
    public int count() {
        ArchetypeQuery query = factory.createQuery();
        query.setCountResults(true);
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        IPage<ObjectSet> results = service.getObjects(query);
        return results.getTotalResults();
    }

    private static class ObjectSetToReminderEventIterator implements Iterator<ReminderEvent> {
        private final Iterator<ObjectSet> iterator;

        public ObjectSetToReminderEventIterator(Iterator<ObjectSet> iterator) {
            this.iterator = iterator;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         */
        @Override
        public ReminderEvent next() {
            return new ReminderEvent(iterator.next());
        }

        /**
         * Removes from the underlying collection the last element returned by this iterator (optional operation).
         *
         * @throws UnsupportedOperationException if the {@code remove} operation is not supported by this iterator
         */
        @Override
        public void remove() {
            iterator.remove();
        }
    }
}
