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

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.DefaultIMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Browser of IMObject instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Browser {

    /**
     * The selected action command.
     */
    public static final String SELECTED = "selected";

    /**
     * The query object.
     */
    private final Query _query;

    /**
     * The node to sort on. May be <code>null</code>
     */
    private String _node;

    /**
     * The browser component.
     */
    private Component _component;

    /**
     * The paged table.
     */
    private PagedIMObjectTable _table;

    /**
     * The model to render results.
     */
    private IMObjectTableModel _model;

    /**
     * The selected object.
     */
    private IMObject _selected;

    /**
     * The event listener list.
     */
    private List<QueryBrowserListener> _listeners
            = new ArrayList<QueryBrowserListener>();

    /**
     * Style name for this.
     */
    private static final String STYLE = "Browser";

    /**
     * Cell spacing row style.
     */
    private static final String CELLSPACING_STYLE = "CellSpacing";

    /**
     * Query button id.
     */
    private static final String QUERY_ID = "query";

    /**
     * Maximum no. of rows to display.
     */
    private static final int ROWS = 15;


    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     */
    public Browser(Query query) {
        this(query, null);
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     * @param node  the node to sort on. May be <code>null</code>
     */
    public Browser(Query query, String node) {
        this(query, node, new DefaultIMObjectTableModel());
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query, displaying them in the table.
     *
     * @param query the query
     * @param node  the node to sort on. May be <code>null</code>
     * @param model the table model
     */
    public Browser(Query query, String node, IMObjectTableModel model) {
        _query = query;
        _node = node;
        _query.addQueryListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
        _model = model;
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
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public IMObject getSelected() {
        return _selected;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(IMObject object) {
        _table.getTable().setSelected(object);
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<IMObject> getObjects() {
        return _model.getObjects();
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
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        getComponent();  // ensure the component is rendered.

        ResultSet set = _query.query(ROWS, _node, true);
        if (_table == null) {
            _table = new PagedIMObjectTable(_model);
            _table.getTable().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onSelect();
                }
            });
            _component.add(_table);
        }

        _table.setResultSet(set);
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
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        _selected = _table.getTable().getSelected();
        if (_selected != null) {
            QueryBrowserListener[] listeners
                    = _listeners.toArray(new QueryBrowserListener[0]);
            for (QueryBrowserListener listener : listeners) {
                listener.selected(_selected);
            }
        }
    }

}
