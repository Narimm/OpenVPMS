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

import org.openvpms.archetype.rules.patient.reminder.ReminderItemQueryFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.ObjectSetResultSetAdapter;
import org.openvpms.web.component.im.query.QueryAdapter;
import org.openvpms.web.component.im.query.ResultSet;

/**
 * Adapts an {@link ReminderItemObjectSetQuery} to return <em>act.patientReminderItem*</em> acts.
 *
 * @author Tim Anderson
 */
class ReminderItemQuery extends QueryAdapter<ObjectSet, Act> {

    /**
     * Constructs a {@link QueryAdapter}.
     *
     * @param query the query to adapt from
     */
    public ReminderItemQuery(ReminderItemObjectSetQuery query) {
        super(query, Act.class);
    }

    /**
     * Returns the underlying query factory.
     *
     * @return the query factory
     */
    public ReminderItemQueryFactory getFactory() {
        return ((ReminderItemObjectSetQuery) getQuery()).getFactory();
    }

    /**
     * Converts a result set.
     *
     * @param set the set to convert
     * @return the converted set
     */
    @Override
    protected ResultSet<Act> convert(ResultSet<ObjectSet> set) {
        return new ObjectSetResultSetAdapter<>(set, "item", Act.class);
    }
}
