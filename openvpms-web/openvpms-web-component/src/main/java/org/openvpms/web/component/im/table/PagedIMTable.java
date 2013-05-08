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

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.table.PageListener;
import org.openvpms.web.component.table.SortableTableHeaderRenderer;
import org.openvpms.web.component.table.TableNavigator;


/**
 * Paged table for domain objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PagedIMTable<T> extends Column {

    /**
     * The underlying table.
     */
    private final IMTable<T> table;

    /**
     * The navigator.
     */
    private TableNavigator navigator;

    /**
     * Constructs a <tt>PagedIMTable</tt>.
     *
     * @param model the table model
     */
    public PagedIMTable(IMTableModel<T> model) {
        setStyleName("CellSpacing");
        IMTableModel<T> paged;
        if (!(model instanceof PagedIMTableModel)) {
            paged = new PagedIMTableModel<T>(model);
        } else {
            paged = model;
        }
        table = new IMTable<T>(paged);
        table.setDefaultHeaderRenderer(new SortableTableHeaderRenderer());
        table.setRolloverEnabled(false);
        add(table);
        table.addPageListener(new PageListener() {
            public void onAction(ActionEvent event) {
                doPage(event);
            }
        });
    }

    /**
     * Constructs a <tt>PagedIMTable</tt>.
     *
     * @param model the model to render results
     * @param set   the result set
     */
    public PagedIMTable(IMTableModel<T> model, ResultSet<T> set) {
        this(model);
        setResultSet(set);
    }

    /**
     * Sets the result set.
     *
     * @param set the set
     */
    public void setResultSet(ResultSet<T> set) {
        PagedIMTableModel<T> model = getModel();
        model.setResultSet(set);
        int pages = set.getEstimatedPages();
        boolean actual = set.isEstimatedActual();

        // only display the table navigator if:
        // . the no. of pages != 0 and is an estimation
        // . the no. of pages are known and > 1
        if (navigator == null) {
            navigator = new TableNavigator(table);
            add(navigator, 0);
        }
        if ((!actual && pages > 0) || pages > 1) {
            navigator.setVisible(true);
        } else {
            navigator.setVisible(false);
        }
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <tt>null</tt> if no object is selected
     */
    public T getSelected() {
        return table.getSelected();
    }

    /**
     * Sets the selected object.
     *
     * @param object the object to select
     */
    public void setSelected(T object) {
        table.setSelected(object);
    }

    /**
     * Returns the result set.
     *
     * @return the result set, or <tt>null</tt> if none has been set
     */
    public ResultSet<T> getResultSet() {
        return getModel().getResultSet();
    }

    /**
     * Returns the underlying table.
     *
     * @return the underlying table
     */
    public IMTable<T> getTable() {
        return table;
    }

    /**
     * Returns the underlying table model.
     *
     * @return the underlying model
     */
    @SuppressWarnings("unchecked")
    public PagedIMTableModel<T> getModel() {
        return (PagedIMTableModel<T>) table.getModel();
    }

    /**
     * Sets the focus traversal (tab) index of the component.
     *
     * @param newValue the new focus traversal index
     * @see #getFocusTraversalIndex()
     */
    @Override
    public void setFocusTraversalIndex(int newValue) {
        if (navigator != null) {
            navigator.setFocusTraversalIndex(newValue);
        }
        table.setFocusTraversalIndex(newValue);
    }

    /**
     * Returns the table navigator.
     *
     * @return the table navigator, or <tt>null</tt> if there is no result set
     */
    public TableNavigator getNavigator() {
        return navigator;
    }

    /**
     * Invoked when the table navigator changes pages.
     *
     * @param event the page event
     */
    private void doPage(ActionEvent event) {
        if (navigator != null) {
            String key = event.getActionCommand();
            if (PageListener.PAGE_PREVIOUS.equals(key)) {
                navigator.previous();
            } else if (PageListener.PAGE_NEXT.equals(key) || PageListener.PAGE_NEXT_TOP.equals(key)) {
                navigator.next();
            } else if (PageListener.PAGE_FIRST.equals(key)) {
                navigator.first();
            } else if (PageListener.PAGE_LAST.equals(key)) {
                navigator.last();
            } else if (PageListener.PAGE_PREVIOUS_BOTTOM.equals(key)) {
                if (navigator.previous()) {
                    int rows = table.getModel().getRowCount();
                    if (rows > 0) {
                        table.getSelectionModel().setSelectedIndex(rows - 1, true);
                    }
                }
            }

            // refocus on the table
            FocusHelper.setFocus(table);
        }
    }

}
