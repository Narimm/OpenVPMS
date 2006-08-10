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

import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.DefaultIMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTable;


/**
 * Browser of IMObject instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TableBrowser<T extends IMObject> extends AbstractBrowser {

    /**
     * The selected action command.
     */
    public static final String SELECTED = "selected";

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
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     */
    public TableBrowser(Query query) {
        this(query, null);
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     */
    public TableBrowser(Query query, SortConstraint[] sort) {
        this(query, sort, new DefaultIMObjectTableModel());
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects using the
     * specified query, displaying them in the table.
     *
     * @param query the query
     * @param sort  the sort criteria. May be <code>null</code>
     * @param model the table model
     */
    public TableBrowser(Query query, SortConstraint[] sort,
                        IMObjectTableModel model) {
        super(query, sort);
        _model = model;
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
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        Component component = getComponent();

        ResultSet set = doQuery();
        if (_table == null) {
            _table = new PagedIMObjectTable(_model);
            _table.getTable().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onSelect();
                }
            });
            component.add(_table);
        }

        _table.setResultSet(set);
    }

    /**
     * Updates the selected IMObject from the table, and notifies any
     * listeners.
     */
    private void onSelect() {
        _selected = _table.getTable().getSelected();
        if (_selected != null) {
            notifySelected(_selected);
        }
    }

}
