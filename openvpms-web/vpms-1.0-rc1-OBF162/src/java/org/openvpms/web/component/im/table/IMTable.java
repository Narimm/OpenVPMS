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

import nextapp.echo2.app.event.TableModelEvent;
import nextapp.echo2.app.event.TableModelListener;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.web.component.table.EvenOddTableCellRenderer;
import org.openvpms.web.component.table.KeyTable;

import java.util.List;


/**
 * Table for domain objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMTable<T> extends KeyTable {

    /**
     * The table model.
     */
    private final IMTableModel<T> model;


    /**
     * Constructs a new <code>IMTable</code>.
     *
     * @param model the table model
     */
    public IMTable(IMTableModel<T> model) {
        this.model = model;
        setStyleName("default");
        setAutoCreateColumnsFromModel(false);
        initialise(model);
    }

    /**
     * Sets the objects to display in the table.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<T> objects) {
        model.setObjects(objects);
    }

    /**
     * Returns the objects displayed in the table.
     *
     * @return the object being displayed.
     */
    public List<T> getObjects() {
        return model.getObjects();
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if no object is
     *         selected
     */
    public T getSelected() {
        T result = null;
        int index = getSelectionModel().getMinSelectedIndex();
        if (index != -1) {
            List<T> objects = model.getObjects();
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
    public void setSelected(T object) {
        int index = getObjects().indexOf(object);
        if (index != -1) {
            getSelectionModel().setSelectedIndex(index, true);
        }
    }

    /**
     * Initialises this.
     *
     * @param model the table model
     */
    @SuppressWarnings("unchecked")
    private void initialise(IMTableModel<T> model) {
        setSelectionEnabled(model.getEnableSelection());
        setRolloverEnabled(model.getEnableSelection());
        TableModel current = getModel();
        setModel(model);
        setColumnModel(model.getColumnModel());
        if (getDefaultRenderer(Object.class) == null) {
            setDefaultRenderer(Object.class, new EvenOddTableCellRenderer());
        }
        // need to add a listener to the model to be notified of column changes
        if (current != model) {
            model.addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent event) {
                    if (event.getType() == TableModelEvent.STRUCTURE_CHANGED) {
                        initialise(((IMTableModel<T>) getModel()));
                    }
                }
            });
        }
    }

}
