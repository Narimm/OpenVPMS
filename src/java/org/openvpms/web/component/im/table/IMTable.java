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
     * Constructs a new <tt>IMTable</tt>.
     *
     * @param model the table model
     */
    public IMTable(IMTableModel<T> model) {
        setStyleName("default");
        setAutoCreateColumnsFromModel(false);
        setModel(model);
        addListener(model);
        initialise(model);
    }

    /**
     * Sets the objects to display in the table.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<T> objects) {
        getModel().setObjects(objects);
    }

    /**
     * Returns the objects displayed in the table.
     *
     * @return the object being displayed.
     */
    public List<T> getObjects() {
        return getModel().getObjects();
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <tt>null</tt> if no object is selected
     */
    public T getSelected() {
        T result = null;
        int index = getSelectionModel().getMinSelectedIndex();
        if (index != -1) {
            List<T> objects = getModel().getObjects();
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
     * Returns the model.
     *
     * @return the model
     */
    @SuppressWarnings("unchecked")
    public IMTableModel<T> getModel() {
        return (IMTableModel<T>) super.getModel();
    }

    /**
     * Sets the <tt>TableModel</tt> being visualized.
     *
     * @param model the new model (may not be null)
     */
    public void setModel(IMTableModel<T> model) {
        super.setModel(model);
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
        if (current != model) {
            // need to add a listener to the model to be notified of column
            // changes
            addListener(model);
        }
    }

    /**
     * Adds a listener to the table model to re-initialise this on column
     * changes.
     *
     * @param model the model to register the listener for
     */
    private void addListener(final IMTableModel<T> model) {
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                if (event.getType() == TableModelEvent.STRUCTURE_CHANGED) {
                    initialise(model);
                }
            }
        });
    }

}
