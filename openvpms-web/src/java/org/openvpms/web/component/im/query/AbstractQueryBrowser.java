/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.ErrorHelper;


/**
 * Abstract implementation of the {@link QueryBrowser} interface where
 * the objects being browsed are provided by an {@link Query}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractQueryBrowser<T> extends AbstractBrowser<T> implements QueryBrowser<T> {

    /**
     * The query object.
     */
    private final Query<T> query;

    /**
     * The sort criteria. If <tt>null</tt>, the query's default sort criteria
     * is used.
     */
    private SortConstraint[] sort;

    /**
     * The browser component.
     */
    private Component component;

    /**
     * Query button id.
     */
    private static final String QUERY_ID = "query";

    /**
     * Style name for this.
     */
    private static final String STYLE = "Browser";


    /**
     * Construct a new <tt>AbstractBrowser</tt> that queries IMObjects using
     * the specified query.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <tt>null</tt>
     */
    public AbstractQueryBrowser(Query<T> query, SortConstraint[] sort) {
        this.query = query;
        this.sort = sort;
        this.query.addQueryListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
        getFocusGroup().add(query.getFocusGroup());
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            doLayout();
            if (query.isAuto()) {
                query();
            }
        }
        return component;
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
     * Sets the sort criteria.
     *
     * @param sort  the sort criteria. May be <tt>null</tt>
     */
    protected void setSortConstraint(SortConstraint[] sort) {
        this.sort = sort;
    }

    /**
     * Lay out this component.
     */
    protected void doLayout() {
        Column container = ColumnFactory.create(STYLE);
        doLayout(container);
        setComponent(container);
    }

    /**
     * Lays out this component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Component row = doQueryLayout();
        container.add(row);
    }

    /**
     * Lays out the query component.
     *
     * @return the query component
     */
    protected Component doQueryLayout() {
        // query component
        Component component = query.getComponent();

        ButtonRow row = new ButtonRow(getFocusGroup());
        row.add(component);
        row.addButton(QUERY_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return row;
    }

    /**
     * Performs the query.
     *
     * @return the query result set
     */
    protected ResultSet<T> doQuery() {
        ResultSet<T> result = null;
        try {
            result = (sort != null) ? query.query(sort) : query.query();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        if (result == null) {
            result = new EmptyResultSet<T>(query.getMaxResults());
        }
        return result;
    }

    /**
     * Registers the browser component.
     *
     * @param component the component
     */
    protected void setComponent(Component component) {
        this.component = component;
    }

    /**
     * Invoked when the query button is pressed. Performs the query and notifies
     * any listeners.
     */
    private void onQuery() {
        query();
        notifyBrowserListeners();
    }

}
