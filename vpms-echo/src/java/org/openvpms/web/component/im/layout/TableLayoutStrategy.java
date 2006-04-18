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

package org.openvpms.web.component.im.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import echopointng.table.DefaultPageableSortableTableModel;
import echopointng.table.PageableSortableTable;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableColumn;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.edit.Property;


/**
 * Layout strategy that renders objects in a table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TableLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The collection descriptor.
     */
    private final NodeDescriptor _descriptor;


    /**
     * Construct a new <code>TableLayoutStrategy</code>.
     *
     * @param descriptor the collection descriptor
     */
    public TableLayoutStrategy(NodeDescriptor descriptor) {
        _descriptor = descriptor;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    @Override
    public Component apply(IMObject object, PropertySet properties,
                           LayoutContext context) {
        String[] range = _descriptor.getArchetypeRange();
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(range[0]);

        List<NodeDescriptor> simple;
        List<NodeDescriptor> complex;
        List<NodeDescriptor> filtered;

        NodeFilter filter = getNodeFilter(context);
        simple = filter(object, archetype.getSimpleNodeDescriptors(), filter);
        complex = filter(object, archetype.getComplexNodeDescriptors(), filter);

        filtered = new ArrayList<NodeDescriptor>(simple);
        filtered.addAll(complex);

        TableColumnModel columns = new DefaultTableColumnModel();
        for (int i = 0; i < filtered.size(); ++i) {
            columns.addColumn(new TableColumn(i));
        }
        DefaultPageableSortableTableModel model
                = new DefaultPageableSortableTableModel(columns);
        for (int i = 0; i < filtered.size(); ++i) {
            NodeDescriptor node = filtered.get(i);
            model.setColumnName(i, node.getDisplayName());
        }
        Collection values = (Collection) _descriptor.getValue(object);
        PageableSortableTable table = new PageableSortableTable(model, columns);
        populate(model, values, filtered, properties, context);
        return table;
    }

    protected void populate(DefaultPageableSortableTableModel table,
                            Collection values,
                            List<NodeDescriptor> descriptors,
                            PropertySet properties,
                            LayoutContext context) {
        IMObjectComponentFactory factory = context.getComponentFactory();
        int row = 0;
        for (Object value : values) {
            IMObject object = (IMObject) value;
            int column = 0;
            for (NodeDescriptor descriptor : descriptors) {
                Property property = properties.get(descriptor);
                Component c = factory.create(property, object);
                table.setValueAt(c, column, row);
                ++column;
            }
            ++row;
        }
    }
}
