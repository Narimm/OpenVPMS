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
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.AbstractArchetypeServiceResultSet;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.query.ObjectSetQueryExecutor;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Queries <em>act.patientReminderItem*</em> archetypes.
 *
 * @author Tim Anderson
 */
public class ReminderItemObjectSetQuery extends ActQuery<ObjectSet> {

    /**
     * The query factory.
     */
    private final ReminderItemQueryFactory factory;

    /**
     * The date range.
     */
    private final DateRange dateRange;

    /**
     * Determines if only items with error status should be displayed.
     */
    private final boolean errorOnly;

    /**
     * The statuses to query.
     */
    private final static ActStatuses statuses = new ActStatuses(new StatusLookupQuery(), null);

    /**
     * Dummy incomplete status. Finds all items with PENDING or ERROR status.
     */
    private static final String INCOMPLETE = "INCOMPLETE";

    /**
     * Dummy incomplete status, used in the status selector.
     */
    private static Lookup INCOMPLETE_STATUS = new Lookup(new ArchetypeId("lookup.local"), INCOMPLETE,
                                                         Messages.get("reporting.reminder.incomplete"));

    /**
     * Constructs a {@link ReminderItemObjectSetQuery}.
     */
    public ReminderItemObjectSetQuery() {
        this(false);
    }

    /**
     * Constructs a {@link ReminderItemObjectSetQuery}.
     *
     * @param errorOnly if {@code true}, only include items with error status
     */
    public ReminderItemObjectSetQuery(boolean errorOnly) {
        super(null, null, null, new String[]{ReminderArchetypes.REMINDER_ITEMS}, statuses, ObjectSet.class);
        factory = new ReminderItemQueryFactory();
        dateRange = new DateRange(true);
        this.errorOnly = errorOnly;
        if (errorOnly) {
            setStatus(ReminderItemStatus.ERROR);
        }
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
        if (!errorOnly) {
            addStatusSelector(container);
        }
        container.add(dateRange.getComponent());
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        factory.setShortNames(getShortNames());
        if (errorOnly) {
            factory.setStatus(ReminderItemStatus.ERROR);
        } else if (getStatusSelector().getSelected() == INCOMPLETE_STATUS) {
            factory.setStatuses(new String[]{ReminderItemStatus.PENDING, ReminderItemStatus.ERROR});
        } else {
            factory.setStatuses(getStatuses());
        }
        factory.setFrom(dateRange.getFrom());
        factory.setTo(dateRange.getTo());
        return new AbstractArchetypeServiceResultSet<ObjectSet>(getMaxResults(), null, new ObjectSetQueryExecutor()) {
            @Override
            protected ArchetypeQuery createQuery() {
                return factory.createQuery();
            }
        };
    }

    private static class StatusLookupQuery extends NodeLookupQuery {

        /**
         * Constructs a {@link StatusLookupQuery}.
         */
        public StatusLookupQuery() {
            super(ReminderArchetypes.EMAIL_REMINDER, "status");
        }

        /**
         * Returns the default lookup.
         *
         * @return {@link #INCOMPLETE_STATUS}
         */
        @Override
        public Lookup getDefault() {
            return INCOMPLETE_STATUS;
        }

        /**
         * Returns the lookups.
         *
         * @return the lookups
         */
        @Override
        public List<Lookup> getLookups() {
            List<Lookup> lookups = super.getLookups();
            lookups.add(0, INCOMPLETE_STATUS);
            return lookups;
        }
    }

}
