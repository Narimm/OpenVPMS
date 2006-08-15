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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
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
public abstract class AbstractBrowser<T extends IMObject>
        implements Browser<T> {

    /**
     * The query object.
     */
    private final Query<T> _query;

    /**
     * The sort criteria. May be <code>null</code>
     */
    private SortConstraint[] _sort;

    /**
     * The browser component.
     */
    private Component _component;

    /**
     * The event listener list.
     */
    private List<QueryBrowserListener> _listeners
            = new ArrayList<QueryBrowserListener>();

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
     * Construct a new <code>AbstractBrowser</code> that queries IMObjects using
     * the specified query.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     */
    public AbstractBrowser(Query<T> query, SortConstraint[] sort) {
        _query = query;
        _sort = sort;
        _query.addQueryListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
    }

    /**
     * Adds a listener to receive notification of selection and query actions.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryBrowserListener listener) {
        _listeners.add(listener);
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    /**
     * Lay out this component.
     */
    protected void doLayout() {
        // query component
        Component component = _query.getComponent();

        // query button
        Button query = ButtonFactory.create(QUERY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onQuery();
            }
        });

        Row row = RowFactory.create(CELLSPACING_STYLE, component, query);
        _component = ColumnFactory.create(STYLE, row);

        if (_query.isAuto()) {
            query();
        }
    }

    /**
     * Performs the query.
     *
     * @return the query result set
     */
    protected ResultSet<T> doQuery() {
        return _query.query(_sort);
    }

    /**
     * Notifies listeners when an object is selected.
     *
     * @param selected the selected object
     */
    protected void notifySelected(T selected) {
        QueryBrowserListener<T>[] listeners
                = (QueryBrowserListener<T>[]) _listeners.toArray(
                new QueryBrowserListener[0]);
        for (QueryBrowserListener<T> listener : listeners) {
            listener.selected(selected);
        }
    }

    /**
     * Invoked when the query button is pressed. Performs the query and notifies
     * any listeners.
     */
    private void onQuery() {
        query();
        QueryBrowserListener[] listeners
                = _listeners.toArray(new QueryBrowserListener[0]);
        for (QueryBrowserListener listener : listeners) {
            listener.query();
        }
    }

    /**
     * @return Returns the _query.
     */
    public Query<T> getQuery() {
        return _query;
    }

}
