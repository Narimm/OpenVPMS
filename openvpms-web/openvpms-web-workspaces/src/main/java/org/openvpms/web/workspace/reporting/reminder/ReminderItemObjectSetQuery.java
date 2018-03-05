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
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.location.LocationSelectField;
import org.openvpms.web.component.im.query.AbstractArchetypeServiceResultSet;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.ObjectSetQueryExecutor;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;

import java.util.List;

/**
 * Queries <em>act.patientReminderItem*</em> archetypes.
 *
 * @author Tim Anderson
 */
public abstract class ReminderItemObjectSetQuery extends ActQuery<ObjectSet> {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The query factory.
     */
    private final ReminderItemQueryFactory factory;

    /**
     * The location filter.
     */
    private LocationSelectField location;

    /**
     * Constructs a {@link ReminderItemObjectSetQuery}.
     *
     * @param status  the status to query
     * @param context the context
     */
    public ReminderItemObjectSetQuery(String status, Context context) {
        this((ActStatuses) null, context);
        setStatus(status);
    }

    /**
     * Constructs a {@link ReminderItemObjectSetQuery}.
     *
     * @param statuses the statuses to query
     * @param context  the context
     */
    public ReminderItemObjectSetQuery(ActStatuses statuses, Context context) {
        super(null, null, null, new String[]{ReminderArchetypes.REMINDER_ITEMS}, statuses, ObjectSet.class);
        this.context = context;
        factory = new ReminderItemQueryFactory();
    }

    /**
     * Returns the location.
     *
     * @return the location
     */
    public Location getLocation() {
        return location != null ? location.getSelected() : Location.ALL;
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
     * Adds the location selector to a container.
     *
     * @param container the container
     */
    protected void addLocationSelector(Component container) {
        if (location == null) {
            location = new LocationSelectField(context.getUser(), context.getPractice(), true);
            location.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
        }

        Label label = LabelFactory.create("reporting.reminder.location");
        container.add(label);
        container.add(location);
        getFocusGroup().add(location);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        populate(factory);
        final ArchetypeQuery query = factory.createQuery();
        return new AbstractArchetypeServiceResultSet<ObjectSet>(getMaxResults(), null, new ObjectSetQueryExecutor()) {
            @Override
            protected ArchetypeQuery createQuery() {
                return query;
            }
        };
    }

    /**
     * Populates the query factory.
     *
     * @param factory the factory
     */
    protected void populate(ReminderItemQueryFactory factory) {
        String shortName = getShortName();
        if (shortName != null) {
            factory.setArchetype(shortName);
        } else {
            factory.setArchetypes(getShortNames());
        }
        String[] statuses = getStatuses();
        if (statuses.length == 0) {
            List<String> codes = getStatusLookups().getCodes();
            statuses = codes.toArray(new String[codes.size()]);
        }
        factory.setStatuses(statuses);
        factory.setLocation(getLocation());
    }

}
