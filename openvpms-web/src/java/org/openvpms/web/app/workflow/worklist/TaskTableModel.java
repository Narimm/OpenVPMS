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

package org.openvpms.web.app.workflow.worklist;

import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.property.IMObjectProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.DateHelper;

import java.util.Date;


/**
 * Table model for display <em>act.customerTask<em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskTableModel extends AbstractActTableModel {

    /**
     * The index of the time column in the model.
     */
    private int timeIndex;


    /**
     * Creates a new <code>TaskTableModel</code>.
     */
    public TaskTableModel() {
        String[] shortNames = new String[]{"act.customerTask"};
        TableColumnModel model
                = createColumnModel(shortNames, getLayoutContext());
        timeIndex = getNextModelIndex(model);
        model.addColumn(createTableColumn(timeIndex, "tasktablemodel.time"));
        setTableColumnModel(model);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getDescriptorNames() {
        return new String[]{"status", "taskType", "customer", "patient",
                            "description"};
    }

    /**
     * Returns the value found at the specified descriptor table column.
     *
     * @param object the object
     * @param column the descriptor table column
     * @return the value at the specified column
     */
    @Override
    protected Object getValue(Act object, DescriptorTableColumn column) {
        NodeDescriptor descriptor = column.getDescriptor();
        if (descriptor.getName().equals("taskType")) {
            LayoutContext context = new DefaultLayoutContext();
            TableComponentFactory factory = new TableComponentFactory(context);
            context.setComponentFactory(factory);
            context.setEdit(true);
            Property property = new IMObjectProperty(object, descriptor);
            return factory.create(property, object).getComponent();
        } else {
            return super.getValue(object, column);
        }
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param act    the object
     * @param column the table column
     * @param row    the table row
     * @return the value at the specified coordinate
     */
    @Override
    protected Object getValue(Act act, TableColumn column, int row) {
        Object result = null;
        if (column.getModelIndex() == timeIndex) {
            Date start = act.getActivityStartTime();
            Date end = act.getActivityEndTime();
            if (start != null) {
                if (end == null) {
                    end = new Date();
                }
                result = DateHelper.formatTimeDiff(start, end);
            }
        } else {
            result = super.getValue(act, column, row);
        }
        return result;
    }
}
