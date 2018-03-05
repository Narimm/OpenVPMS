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

import echopointng.DateField;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundCheckBox;
import org.openvpms.web.component.bound.BoundDateFieldFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ComponentHelper;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Queries <em>act.patientReminderItem*</em> archetypes.
 *
 * @author Tim Anderson
 */
public class ReminderItemDateObjectSetQuery extends ReminderItemObjectSetQuery {

    /**
     * The 'all dates' checkbox.
     */
    private final SimpleProperty all;

    /**
     * The date.
     */
    private final SimpleProperty date = new SimpleProperty(
            "date", null, Date.class, DescriptorHelper.getDisplayName(ReminderArchetypes.PRINT_REMINDER, "startTime"));

    /**
     * The date label.
     */
    private Label dateLabel;

    /**
     * The date field.
     */
    private DateField dateField;

    /**
     * Date change listener.
     */
    private final ModifiableListener listener;


    /**
     * Constructs a {@link ReminderItemDateObjectSetQuery}.
     *
     * @param status  the reminder item status to query
     * @param context the context
     */
    public ReminderItemDateObjectSetQuery(String status, Context context) {
        this(status, false, context);
    }

    /**
     * Constructs a {@link ReminderItemDateObjectSetQuery}.
     *
     * @param status  the reminder item status to query
     * @param all     if {@code true}, query all dates
     * @param context the context
     */
    public ReminderItemDateObjectSetQuery(String status, boolean all, Context context) {
        super(status, context);
        date.setValue(DateRules.getToday());
        dateLabel = LabelFactory.create();
        dateLabel.setText(date.getDisplayName());
        dateField = BoundDateFieldFactory.create(date);
        if (all) {
            this.all = new SimpleProperty("all", true, Date.class, Messages.get("daterange.all"));
            this.all.addModifiableListener(new ModifiableListener() {
                @Override
                public void modified(Modifiable modifiable) {
                    onAllChanged();
                }
            });
            enableDate();
        } else {
            this.all = null;
        }
        listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onQuery();
            }
        };
        date.addModifiableListener(listener);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        if (all == null) {
            Date to = date.getDate();
            if (to == null) {
                to = DateRules.getToday();
                date.removeModifiableListener(listener);
                date.setValue(to);
                date.addModifiableListener(listener);
            }
        }
        return super.createResultSet(sort);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        if (all != null) {
            Label allLabel = LabelFactory.create();
            allLabel.setText(all.getDisplayName());
            container.add(allLabel);
            container.add(new BoundCheckBox(all));
        }
        container.add(dateLabel);
        container.add(dateField);
        addLocationSelector(container);
    }

    /**
     * Populates the query factory.
     *
     * @param factory the factory
     */
    @Override
    protected void populate(ReminderItemQueryFactory factory) {
        super.populate(factory);
        factory.setFrom(null);
        if (all != null && all.getBoolean()) {
            factory.setTo(null);
        } else {
            Date to = date.getDate();
            factory.setTo(DateRules.getNextDate(to));
        }
    }

    /**
     * Invoked when the All checkbox changes.
     */
    private void onAllChanged() {
        enableDate();
        onQuery();
    }

    /**
     * Enables the date field if All is unticked, otherwise disables it.
     */
    private void enableDate() {
        boolean enable = !all.getBoolean();
        ComponentHelper.enable(dateLabel, enable);
        ComponentHelper.enable(dateField, enable);
    }
}
