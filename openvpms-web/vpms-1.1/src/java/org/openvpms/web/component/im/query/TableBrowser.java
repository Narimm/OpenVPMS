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

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;

import java.util.List;


/**
 * Implementation of {@link Browser} that renders results in a table.
 * .
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class TableBrowser<T> extends AbstractBrowser<T> {

    /**
     * The selected action command.
     */
    public static final String SELECTED = "selected";

    /**
     * The paged table.
     */
    private PagedIMTable<T> table;

    /**
     * The model to render results.
     */
    private IMTableModel<T> model;


    /**
     * Construct a new <code>TableBrowser</code> that queries objects using the
     * specified query, displaying them in the table.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     * @param model the table model
     */
    public TableBrowser(Query<T> query, SortConstraint[] sort,
                        IMTableModel<T> model) {
        super(query, sort);
        this.model = model;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public T getSelected() {
        return (table != null) ? table.getTable().getSelected() : null;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(T object) {
        getComponent();
        table.getTable().setSelected(object);
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query.
     */
    public List<T> getObjects() {
        return model.getObjects();
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        Component component = getComponent();

        ResultSet<T> set = doQuery();
        if (table == null) {
            doLayout(component);
        }

        if (set == null) {
            set = new EmptyResultSet<T>(getQuery().getMaxResults());
        }
        table.setResultSet(set);
        IMTable<T> imTable = table.getTable();
        if (!imTable.getObjects().isEmpty()
                && imTable.isFocusTraversalParticipant()) {
            ApplicationInstance.getActive().setFocusedComponent(imTable);
        }
    }

    /**
     * Adds the table to the browser component.
     *
     * @param component the browser component
     */
    protected void doLayout(Component component) {
        table = createTable(model);
        table.getTable().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onSelect();
            }
        });
        component.add(table);
        getFocusGroup().add(table);
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    protected PagedIMTable<T> createTable(IMTableModel<T> model) {
        return new PagedIMTable<T>(model);
    }

    /**
     * Returns the underlying table.
     *
     * @return the table
     */
    protected PagedIMTable<T> getTable() {
        return table;
    }

    /**
     * Returns the underlying table model.
     *
     * @return the table model
     */
    protected IMTableModel<T> getTableModel() {
        return model;
    }

    /**
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        T selected = getSelected();
        if (selected != null) {
            notifySelected(selected);
        }
    }

}
