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

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.edit.IMObjectProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Table model created from an {@link ArchetypeDescriptor} or {@link
 * NodeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class DescriptorTableModel<T extends IMObject>
        extends BaseIMObjectTableModel<T> {

    /**
     * The layout context.
     */
    private final LayoutContext _context;


    /**
     * Creates a new <code>DescriptorTableModel</code>.
     * The column model must be set using {@link #setTableColumnModel}.
     */
    public DescriptorTableModel() {
        _context = new DefaultLayoutContext();
        TableComponentFactory factory = new TableComponentFactory(_context);
        _context.setComponentFactory(factory);
    }

    /**
     * Creates a new <code>DescriptorTableModel</code>.
     *
     * @param shortNames the archetype short names
     */
    public DescriptorTableModel(String[] shortNames) {
        _context = new DefaultLayoutContext();
        TableComponentFactory factory = new TableComponentFactory(_context);
        _context.setComponentFactory(factory);
        setTableColumnModel(createColumnModel(shortNames, _context));
    }

    /**
     * Creates a new <code>DescriptorTableModel</code>.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public DescriptorTableModel(String[] shortNames, LayoutContext context) {
        _context = context;
        setTableColumnModel(createColumnModel(shortNames, context));
    }

    /**
     * Construct a <code>DescriptorTableModel</code>.
     *
     * @param model   the table column model. May be <code>null</code>
     * @param context the layout context
     */
    public DescriptorTableModel(TableColumnModel model,
                                LayoutContext context) {
        super(model);
        _context = context;
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
     * @return the sort criteria, or <code>null</code> if the column isn't
     *         sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        TableColumn col = getColumn(column);
        if (col instanceof DescriptorTableColumn) {
            NodeDescriptor descriptor
                    = ((DescriptorTableColumn) col).getDescriptor();
            if (descriptor.getPath().lastIndexOf("/") <= 0) {
                // can only sort on top level nodes
                result = new SortConstraint[]{
                        new NodeSortConstraint(descriptor.getName(), ascending)
                };
            } else {
                result = null;
            }
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
        Object result;
        if (c instanceof DescriptorTableColumn) {
            result = getValue(object, (DescriptorTableColumn) c);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns a value for a given column.
     *
     * @param object the object to operate on
     * @param column the column
     * @return the value for the column
     */
    protected Object getValue(IMObject object, DescriptorTableColumn column) {
        Object result;
        IMObjectComponentFactory factory = _context.getComponentFactory();
        NodeDescriptor descriptor = column.getDescriptor();
        Property property = new IMObjectProperty(object, descriptor);
        result = factory.create(property, object);
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

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(String[] shortNames,
                                                 LayoutContext context) {
        List<ArchetypeDescriptor> archetypes
                = DescriptorHelper.getArchetypeDescriptors(shortNames);
        if (archetypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Argument 'shortNames' doesn't refer to a valid archetype");
        }
        return createColumnModel(archetypes, context);
    }

    /**
     * Creates a column model for one or more archetypes.
     * If there are multiple archetypes, the intersection of the descriptors
     * will be used, and the archetype will be inserted at the column indicated
     * by {@link #getArchetypeColumnIndex}.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(
            List<ArchetypeDescriptor> archetypes,
            LayoutContext context) {
        List<NodeDescriptor> descriptors = getDescriptors(archetypes, context);
        TableColumnModel columns = new DefaultTableColumnModel();

        if (archetypes.size() > 1) {
            addColumns(descriptors, columns);
            int index = getArchetypeColumnIndex();
            if (index != -1) {
                columns.addColumn(new TableColumn(ARCHETYPE_INDEX));
                columns.moveColumn(columns.getColumnCount() - 1, index);
            }
        } else {
            addColumns(descriptors, columns);
        }
        return columns;
    }

    /**
     * Add columns to a column model.
     *
     * @param descriptors the column descriptors
     * @param columns     the columns to add to
     */
    protected void addColumns(List<NodeDescriptor> descriptors,
                              TableColumnModel columns) {
        // determine a unique starting index for the columns
        int index = getNextModelIndex(columns);

        for (NodeDescriptor descriptor : descriptors) {
            TableColumn column = new DescriptorTableColumn(index, descriptor);
            columns.addColumn(column);
            ++index;
        }
    }

    /**
     * Helper to determine the next available model index.
     *
     * @param columns the columns
     * @return the next available model index.
     */
    protected int getNextModelIndex(TableColumnModel columns) {
        int index = NEXT_INDEX;
        Iterator iterator = columns.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() >= index) {
                index = col.getModelIndex() + 1;
            }
        }
        return index;
    }

    /**
     * Returns the intersection of descriptors for a set of archetypes.
     *
     * @param archetypes the archetype descriptors
     * @param context    the layout context
     * @return thhe intersection of descriptors for a set of archetypes
     */
    protected List<NodeDescriptor> getDescriptors(
            List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        List<NodeDescriptor> common = null;
        for (ArchetypeDescriptor archetype : archetypes) {
            List<NodeDescriptor> nodes = getDescriptors(archetype, context);
            if (common == null) {
                common = nodes;
            } else {
                common = getIntersection(common, nodes);
            }
        }
        return common;
    }

    /**
     * Returns a filtered list of descriptors for an archetype.
     *
     * @param archetype the archetype
     * @param context   the layout context
     * @return a filtered list of descriptors for the archetype
     */
    protected List<NodeDescriptor> getDescriptors(ArchetypeDescriptor archetype,
                                                  LayoutContext context) {
        return filter(archetype.getAllNodeDescriptors(), context);
    }

    /**
     * Returns the index to insert the archetype column.
     *
     * @return the index to insert the archetype column, or <code>-1<code>
     *         if it should not be inserted
     */
    protected int getArchetypeColumnIndex() {
        return 0;
    }

    /**
     * Filters descriptors using the context's default node filter.
     *
     * @param descriptors the column descriptors
     * @param context     the layout context
     * @return the filtered descriptors
     */
    protected List<NodeDescriptor> filter(List<NodeDescriptor> descriptors,
                                          LayoutContext context) {
        return FilterHelper.filter(null, context.getDefaultNodeFilter(),
                                   descriptors);
    }

    /**
     * Helper to return the intersection of two lists of node descriptors.
     *
     * @param first  the first list of nodes
     * @param second the second list of nodes
     * @return the intersection of the two lists
     */
    private List<NodeDescriptor> getIntersection(List<NodeDescriptor> first,
                                                 List<NodeDescriptor> second) {
        List<NodeDescriptor> result = new ArrayList<NodeDescriptor>();
        for (NodeDescriptor a : first) {
            for (NodeDescriptor b : second) {
                if (a.getName().equals(b.getName())) {
                    result.add(a);
                    break;
                }
            }
        }
        return result;
    }
}
