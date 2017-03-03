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
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRange;

/**
 * Queries <em>act.patientReminderItem*</em> archetypes.
 *
 * @author Tim Anderson
 */
public class ReminderItemDateRangeObjectSetQuery extends ReminderItemObjectSetQuery {

    /**
     * The date range.
     */
    private DateRange dateRange;

    /**
     * Constructs a {@link ReminderItemDateRangeObjectSetQuery}.
     */
    public ReminderItemDateRangeObjectSetQuery(String defaultStatus, Context context, String... statuses) {
        super(new ActStatuses(new StatusLookupQuery(defaultStatus, statuses)), context);
        dateRange = new DateRange(false);
        dateRange.getComponent();
        dateRange.setAllDates(false);
        dateRange.setFrom(DateRules.getToday());
        dateRange.setTo(DateRules.getTomorrow());
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
        addStatusSelector(container);
        container.add(dateRange.getComponent());
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
        factory.setFrom(dateRange.getFrom());
        factory.setTo(dateRange.getTo());
    }

    private static class StatusLookupQuery extends NodeLookupQuery {

        private final Lookup defaultLookup;

        /**
         * Constructs a {@link StatusLookupQuery}.
         */
        public StatusLookupQuery(String defaultCode, String... codes) {
            super(ReminderArchetypes.EMAIL_REMINDER, "status", codes);
            defaultLookup = (defaultCode != null) ? getLookup(defaultCode, getLookups()) : null;
        }

        /**
         * Returns the default lookup.
         *
         * @return the default lookup, or {@code null} if none is defined
         */
        @Override
        public Lookup getDefault() {
            return defaultLookup;
        }
    }

}
