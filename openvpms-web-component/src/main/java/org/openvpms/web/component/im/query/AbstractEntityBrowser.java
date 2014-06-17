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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.NameDescObjectSetTableModel;
import org.openvpms.web.component.im.util.IMObjectHelper;

/**
 * Entity browser that displays the archetype, name and description of
 * the entities being queried.
 * <p/>
 * The archetype column is only displayed if more than one archetype is being queried.
 * <p/>
 * The active column is displayed if both active and inactive instances are being queried.
 *
 * @author Tim Anderson
 */
public class AbstractEntityBrowser<T extends Entity> extends QueryBrowserAdapter<ObjectSet, T> {

    /**
     * The query.
     */
    private final EntityQuery<T> query;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The table model.
     */
    private NameDescObjectSetTableModel model;


    /**
     * Constructs an {@link AbstractEntityBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public AbstractEntityBrowser(EntityQuery<T> query, LayoutContext context) {
        this(query, context, new NameDescObjectSetTableModel("entity", true, false));
    }

    /**
     * Constructs an {@link AbstractEntityBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public AbstractEntityBrowser(EntityQuery<T> query, LayoutContext context, NameDescObjectSetTableModel model) {
        this.query = query;
        this.context = context.getContext();
        this.model = model;
        setBrowser(createBrowser(query, context));
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    public Query<T> getQuery() {
        return query;
    }

    /**
     * Returns the result set.
     * <p/>
     * Note that this is a snapshot of the browser's result set. Iterating over it will not affect the browser.
     *
     * @return the result set
     */
    public ResultSet<T> getResultSet() {
        return new EntityResultSetAdapter<T>((EntityObjectSetResultSet) getBrowser().getResultSet(), context);
    }

    /**
     * Converts an object.
     *
     * @param set the object to convert
     * @return the converted object
     */
    @SuppressWarnings("unchecked")
    protected T convert(ObjectSet set) {
        IMObjectReference ref = set.getReference("entity.reference");
        return (T) IMObjectHelper.getObject(ref, context);
    }

    /**
     * Creates a table browser that changes the model depending on how many archetypes are being queried
     *
     * @param query   the query
     * @param context the layout context
     * @return a new browser
     */
    protected Browser<ObjectSet> createBrowser(final EntityQuery<T> query, LayoutContext context) {
        Query<ObjectSet> delegate = query.getQuery();
        return new AbstractQueryBrowser<ObjectSet>(delegate, delegate.getDefaultSortConstraint(), model, context) {
            /**
             * Performs the query.
             *
             * @return the query result set
             */
            @Override
            protected ResultSet<ObjectSet> doQuery() {
                ResultSet<ObjectSet> result = super.doQuery();
                boolean showArchetype = query.getShortName() == null && query.getShortNames().length > 1;
                model.showArchetype(showArchetype);
                model.setShowActive(query.getActive() == BaseArchetypeConstraint.State.BOTH);
                return result;
            }
        };
    }
}
