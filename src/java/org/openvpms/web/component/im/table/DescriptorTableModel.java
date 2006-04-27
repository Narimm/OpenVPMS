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

import java.util.Iterator;
import java.util.List;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.web.component.edit.IMObjectProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;


/**
 * Table model created from an {@link ArchetypeDescriptor} or {@link
 * NodeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DescriptorTableModel extends DefaultIMObjectTableModel {

    /**
     * The layout context.
     */
    private final LayoutContext _context;


    /**
     * Construct a <code>DescriptorTableModel</code>.
     *
     * @param model   the table columne model
     * @param context the layout context
     */
    protected DescriptorTableModel(TableColumnModel model,
                                   LayoutContext context) {
        super(model);
        _context = context;
    }

    /**
     * Create a model.
     *
     * @param descriptors the column descriptors
     * @param context     the layout context
     * @return a new model
     */
    public static DefaultIMObjectTableModel create(List<NodeDescriptor> descriptors,
                                                   LayoutContext context) {
        return new DescriptorTableModel(create(descriptors), context);
    }

    /**
     * Create a column model.
     *
     * @param descriptors the column descriptors
     * @return a new column model
     */
    public static TableColumnModel create(List<NodeDescriptor> descriptors) {
        TableColumnModel columns = new DefaultTableColumnModel();
        create(descriptors, columns);
        return columns;
    }

    /**
     * Create columns, adding to an existing column model.
     *
     * @param descriptors the column descriptors
     * @param columns     the columns to add to
     */
    public static void create(List<NodeDescriptor> descriptors,
                              TableColumnModel columns) {
        // determine a unique starting index for the columns
        int index = DefaultIMObjectTableModel.NEXT_INDEX;
        Iterator iterator = columns.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() >= index) {
                index = col.getModelIndex() + 1;
            }
        }

        for (NodeDescriptor descriptor : descriptors) {
            TableColumn column = new DescriptorTableColumn(index, descriptor);
            columns.addColumn(column);
            ++index;
        }
    }

    /**
     * @see TableModel#getColumnName
     */
    @Override
    public String getColumnName(int column) {
        String result;
        TableColumn col = getColumn(column);
        if (col instanceof DescriptorTableColumn) {
            NodeDescriptor descriptor
                    = ((DescriptorTableColumn) col).getDescriptor();
            result = descriptor.getDisplayName();
        } else {
            result = super.getColumnName(column);
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        TableColumn col = getColumn(column);
        if (col instanceof DescriptorTableColumn) {
            NodeDescriptor descriptor
                    = ((DescriptorTableColumn) col).getDescriptor();
            result = new SortConstraint[] {
                new NodeSortConstraint(descriptor.getName(), ascending)
            };
        } else {
            result = super.getSortConstraints(column, ascending);
        }
        return result;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        TableColumn c = getColumn(column);
        IMObjectComponentFactory factory = _context.getComponentFactory();
        Object result = null;
        if (c instanceof DescriptorTableColumn) {
            DescriptorTableColumn col = (DescriptorTableColumn) c;
            NodeDescriptor descriptor = col.getDescriptor();
            Property property = new IMObjectProperty(object, descriptor);
            result = factory.create(property, object);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return _context;
    }

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    protected IMObjectComponentFactory getFactory() {
        return _context.getComponentFactory();
    }
}
