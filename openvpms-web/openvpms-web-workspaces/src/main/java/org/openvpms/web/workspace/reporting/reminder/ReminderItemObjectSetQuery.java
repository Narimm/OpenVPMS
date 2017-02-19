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

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractArchetypeServiceResultSet;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.ObjectSetQueryExecutor;
import org.openvpms.web.component.im.query.ResultSet;

/**
 * Queries <em>act.patientReminderItem*</em> archetypes.
 *
 * @author Tim Anderson
 */
public abstract class ReminderItemObjectSetQuery extends ActQuery<ObjectSet> {

    /**
     * The query factory.
     */
    private final ReminderItemQueryFactory factory;

    /**
     * Constructs a {@link ReminderItemObjectSetQuery}.
     *
     * @param status the status to query
     */
    public ReminderItemObjectSetQuery(String status) {
        this((ActStatuses) null);
        setStatus(status);
    }

    /**
     * Constructs a {@link ReminderItemObjectSetQuery}.
     *
     * @param statuses the statuses to query
     */
    public ReminderItemObjectSetQuery(ActStatuses statuses) {
        super(null, null, null, new String[]{ReminderArchetypes.REMINDER_ITEMS}, statuses, ObjectSet.class);
        factory = new ReminderItemQueryFactory();
    }

    /**
     * Returns the factory used to create queries.
     *
     * @return the factory
     */
    public ReminderItemQueryFactory getFactory() {
        return factory;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addShortNameSelector(container);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        final ArchetypeQuery query = createQuery(factory);
        return new AbstractArchetypeServiceResultSet<ObjectSet>(getMaxResults(), null, new ObjectSetQueryExecutor()) {
            @Override
            protected ArchetypeQuery createQuery() {
                return query;
            }
        };
    }

    /**
     * Creates a new query.
     *
     * @param factory the query factory
     * @return a new query
     */
    protected abstract ArchetypeQuery createQuery(ReminderItemQueryFactory factory);

}
