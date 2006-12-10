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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.focus.FocusTree;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link Browser} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractBrowser<T> implements Browser<T> {

    /**
     * The query object.
     */
    private final Query<T> query;

    /**
     * The sort criteria. May be <code>null</code>
     */
    private SortConstraint[] sort;

    /**
     * The browser component.
     */
    private Component component;

    /**
     * The event listener list.
     */
    private List<QueryBrowserListener<T>> listeners
            = new ArrayList<QueryBrowserListener<T>>();

    /**
     * Query button id.
     */
    private static final String QUERY_ID = "query";

    /**
     * Style name for this.
     */
    private static final String STYLE = "Browser";

    /**
     * Cell spacing row style.
     */
    private static final String CELLSPACING_STYLE = "CellSpacing";

    /**
     * The focus tree.
     */
    private FocusTree focusTree = new FocusTree(getClass().getName());

    /**
     * The focus set.
     */
    private FocusSet focusSet = new FocusSet(getClass().getName() + "-"
            + QUERY_ID);


    /**
     * Construct a new <code>AbstractBrowser</code> that queries IMObjects using
     * the specified query.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     */
    public AbstractBrowser(Query<T> query, SortConstraint[] sort) {
        this.query = query;
        this.sort = sort;
        this.query.addQueryListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
        focusTree.add(query.getFocusGroup());
        focusTree.add(focusSet);
    }

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryBrowserListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            doLayout();
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
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusTree;
    }

    /**
     * Lay out this component.
     */
    protected void doLayout() {
        // query component
        Component component = query.getComponent();

        // query button
        Button query = ButtonFactory.create(QUERY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onQuery();
            }
        });

        Row row = RowFactory.create(CELLSPACING_STYLE, component, query);
        this.component = ColumnFactory.create(STYLE, row);
        focusSet.add(query);

        if (this.query.isAuto()) {
            query();
        }
    }

    /**
     * Performs the query.
     *
     * @return the query result set. May be <code>null</code>
     */
    protected ResultSet<T> doQuery() {
        try {
            return query.query(sort);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
        return null;
    }

    /**
     * Notifies listeners when an object is selected.
     *
     * @param selected the selected object
     */
    @SuppressWarnings("unchecked")
    protected void notifySelected(T selected) {
        QueryBrowserListener<T>[] listeners
                = (QueryBrowserListener<T>[]) this.listeners.toArray(
                new QueryBrowserListener[0]);
        for (QueryBrowserListener<T> listener : listeners) {
            listener.selected(selected);
        }
    }

    /**
     * Returns the focus set.
     *
     * @return the focus set
     */
    protected FocusSet getFocusSet() {
        return focusSet;
    }

    /**
     * Invoked when the query button is pressed. Performs the query and notifies
     * any listeners.
     */
    private void onQuery() {
        query();
        QueryBrowserListener[] listeners
                = this.listeners.toArray(new QueryBrowserListener[0]);
        for (QueryBrowserListener listener : listeners) {
            listener.query();
        }
    }

}
