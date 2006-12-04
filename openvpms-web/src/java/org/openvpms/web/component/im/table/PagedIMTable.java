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
import org.openvpms.web.component.im.query.ResultSet;
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
     * Constructs a new <code>PagedIMTable</code>.
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
        add(table);

    }

    /**
     * Sets the result set.
     *
     * @param set the set
     */
    public void setResultSet(ResultSet<T> set) {
        PagedIMTableModel<T> model = getPagedIMTableModel();
        model.setResultSet(set);
        if (set.hasNext() && set.getPages() > 1) {
            if (navigator == null) {
                navigator = new TableNavigator(table);
                navigator.setFocusTraversalIndex(
                        table.getFocusTraversalIndex());
            }
            if (indexOf(navigator) == -1) {
                add(navigator, 0);
            }
        } else {
            if (navigator != null) {
                remove(navigator);
            }
        }
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
     * Returns the underlying table model.
     *
     * @return the underlying model
     */
    @SuppressWarnings("unchecked")
    private PagedIMTableModel<T> getPagedIMTableModel() {
        return (PagedIMTableModel<T>) table.getModel();
    }
}
