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

import java.util.List;

import nextapp.echo2.app.Table;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.table.EvenOddTableCellRenderer;
import org.openvpms.web.component.table.SortableTableHeaderRenderer;


/**
 * Paged, sortable table of {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectTable extends Table {

    /**
     * IMObject table model.
     */
    private final IMObjectTableModel _model;

    /**
     * Construct a new <code>IMObjectTable</code>.
     */
    public IMObjectTable() {
        this(new DefaultIMObjectTableModel());
    }

    /**
     * Construct a new <code>IMObjectTable</code>.
     *
     * @param model the table model
     */
    public IMObjectTable(IMObjectTableModel model) {
        _model = model;
        setStyleName("default");
        setAutoCreateColumnsFromModel(false);
        setSelectionEnabled(model.getEnableSelection());
        setRolloverEnabled(model.getEnableSelection());
        setModel(model);
        setColumnModel(model.getColumnModel());
        setDefaultRenderer(Object.class, new EvenOddTableCellRenderer());
        setDefaultHeaderRenderer(new SortableTableHeaderRenderer());
    }

    /**
     * Sets the objects to display in the table.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        _model.setObjects(objects);
    }

    /**
     * Returns the objects displayed in the table.
     *
     * @return the object being displayed.
     */
    public List<IMObject> getObjects() {
        return _model.getObjects();
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if no object is
     *         selected
     */
    public IMObject getSelected() {
        IMObject result = null;
        int index = getSelectionModel().getMinSelectedIndex();
        if (index != -1) {
            List<IMObject> objects = _model.getObjects();
            if (index < objects.size()) {
                result = objects.get(index);
            }
        }
        return result;
    }

    /**
     * Sets the selected object.
     *
     * @param object the object to select
     */
    public void setSelected(IMObject object) {
        int index = getObjects().indexOf(object);
        if (index != -1) {
            getSelectionModel().setSelectedIndex(index, true);
        }
    }

}
