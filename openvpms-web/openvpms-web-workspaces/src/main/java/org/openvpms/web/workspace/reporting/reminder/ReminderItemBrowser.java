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
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractQueryBrowser;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.ObjectSetResultSetAdapter;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryBrowserAdapter;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.view.TableComponentFactory;

/**
 * A browser for <em>act.patientReminderItem*</em> acts.
 *
 * @author Tim Anderson
 */
class ReminderItemBrowser extends QueryBrowserAdapter<ObjectSet, Act> {

    /**
     * The query.
     */
    private final ReminderItemQuery query;

    /**
     * Constructs a {@link ReminderItemBrowser}.
     *
     * @param query   the query
     * @param context the context
     */
    public ReminderItemBrowser(ReminderItemQuery query, LayoutContext context) {
        this.query = query;
        context.setComponentFactory(new TableComponentFactory(context));
        setBrowser(createBrowser(query, context));
    }

    /**
     * Converts an object.
     *
     * @param object the object to convert
     * @return the converted object
     */
    @Override
    protected Act convert(ObjectSet object) {
        return (Act) object.get("item");
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    @Override
    public ReminderItemQuery getQuery() {
        return query;
    }

    /**
     * Returns the underlying query factory.
     *
     * @return the query factory
     */
    public ReminderItemQueryFactory getFactory() {
        return query.getFactory();
    }

    /**
     * Returns the result set.
     * <p>
     * Note that this is a snapshot of the browser's result set. Iterating over it will not affect the browser.
     *
     * @return the result set, or {@code null} if the query hasn't been executed
     */
    @Override
    public ResultSet<Act> getResultSet() {
        return new ObjectSetResultSetAdapter<>(getBrowser().getResultSet(), "item", Act.class);
    }

    /**
     * Returns the underlying browser.
     *
     * @return the underlying browser
     */
    @Override
    public ReminderItemObjectSetBrowser getBrowser() {
        return (ReminderItemObjectSetBrowser) super.getBrowser();
    }

    /**
     * Creates a table browser that changes the model depending on what columns have been queried on.
     *
     * @param query   the query
     * @param context the layout context
     * @return a new browser
     */
    protected Browser<ObjectSet> createBrowser(ReminderItemQuery query, LayoutContext context) {
        return new ReminderItemObjectSetBrowser(query.getQuery(), context);
    }

    protected static class ReminderItemObjectSetBrowser extends AbstractQueryBrowser<ObjectSet> {
        public ReminderItemObjectSetBrowser(Query<ObjectSet> delegate, LayoutContext context) {
            super(delegate, delegate.getDefaultSortConstraint(), new ReminderItemTableModel(context), context);
        }
    }
}
