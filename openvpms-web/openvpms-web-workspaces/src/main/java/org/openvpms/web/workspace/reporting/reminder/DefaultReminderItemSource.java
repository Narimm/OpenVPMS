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
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypeCache;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.system.ServiceHelper;

import java.util.Iterator;
import java.util.List;

/**
 * Default implementation of the {@link ReminderItemSource}.
 *
 * @author Tim Anderson
 */
public class DefaultReminderItemSource implements ReminderItemSource {

    private final ReminderItemQueryFactory factory;

    private final ReminderTypeCache reminderTypes;

    public DefaultReminderItemSource(ReminderItemQueryFactory factory, ReminderTypeCache reminderTypes) {
        this.factory = factory;
        this.reminderTypes = reminderTypes;
    }

    /**
     * Returns the reminder item archetype short names.
     *
     * @return the reminder item archetype short names
     */
    public String[] getShortNames() {
        return factory.getShortNames();
    }

    /**
     * Returns all items that match the query.
     *
     * @return all items that match the query
     */
    @Override
    public List<ObjectSet> all() {
        ArchetypeQuery query = factory.createQuery();
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        return ServiceHelper.getArchetypeService().getObjects(query).getResults();
    }

    /**
     * Executes the query.
     *
     * @return the items matching the query
     */
    public Iterable<List<ObjectSet>> query() {
        return new Iterable<List<ObjectSet>>() {
            @Override
            public Iterator<List<ObjectSet>> iterator() {
                return new GroupingReminderIterator(factory, reminderTypes, 1000, ServiceHelper.getArchetypeService());
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
}
