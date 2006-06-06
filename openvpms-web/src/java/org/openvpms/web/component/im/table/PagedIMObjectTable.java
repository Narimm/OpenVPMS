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

import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.table.TableNavigator;

import nextapp.echo2.app.Column;


/**
 * Paged IMObject table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PagedIMObjectTable extends Column {

    /**
     * The underlying table.
     */
    private final IMObjectTable _table;

    /**
     * The navigator.
     */
    private TableNavigator _navigator;


    /**
     * Construct a new <code>PagedIMObjectTable</code>.
     *
     * @param model the model to render results
     */
    public PagedIMObjectTable(IMObjectTableModel model) {
        setStyleName("CellSpacing");
        IMObjectTableModel paged = new PagedIMObjectTableModel(model);
        _table = new IMObjectTable(paged);
        add(_table);
    }

    /**
     * Construct a new <code>PagedIMObjectTable</code>.
     *
     * @param model the model to render results
     * @param set   the result set
     */
    public PagedIMObjectTable(IMObjectTableModel model, ResultSet set) {
        this(model);
        setResultSet(set);
    }

    /**
     * Sets the result set.
     *
     * @param set the set
     */
    public void setResultSet(ResultSet set) {
        PagedIMObjectTableModel model
                = (PagedIMObjectTableModel) _table.getModel();
        model.setResultSet(set);
        if (set.hasNext() && set.getPages() > 1) {
            if (_navigator == null) {
                _navigator = new TableNavigator(_table);
                _navigator.setFocusTraversalIndex(
                        _table.getFocusTraversalIndex());
            }
            if (indexOf(_navigator) == -1) {
                add(_navigator, 0);
            }
        } else {
            if (_navigator != null) {
                remove(_navigator);
            }
        }
    }

    /**
     * Returns the underlying table.
     *
     * @return the underlying table
     */
    public IMObjectTable getTable() {
        return _table;
    }

    /**
     * Sets the focus traversal (tab) index of the component.
     *
     * @param newValue the new focus traversal index
     * @see #getFocusTraversalIndex()
     */
    @Override
    public void setFocusTraversalIndex(int newValue) {
        if (_navigator != null) {
            _navigator.setFocusTraversalIndex(newValue);
        }
        _table.setFocusTraversalIndex(newValue);
    }


}
