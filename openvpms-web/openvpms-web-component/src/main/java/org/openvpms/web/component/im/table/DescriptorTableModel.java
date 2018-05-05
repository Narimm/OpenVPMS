/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.TableComponentFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Table model created from an {@link ArchetypeDescriptor} or {@link NodeDescriptor}s.
 *
 * @author Tim Anderson
 */
public abstract class DescriptorTableModel<T extends IMObject> extends BaseIMObjectTableModel<T> {

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The archetypes used to create the column model. May be {@code null}
     */
    private List<ArchetypeDescriptor> archetypes;

    /**
     * Constructs a {@link DescriptorTableModel}.
     * <p>
     * The column model must be set using {@link #setTableColumnModel}.
     *
     * @param context the layout context
     */
    public DescriptorTableModel(LayoutContext context) {
        super(null);
        if (context.getComponentFactory() == null) {
            context = new DefaultLayoutContext(context);
            context.setComponentFactory(new TableComponentFactory(context));
        }
        this.context = context;
    }

    /**
     * Constructs a {@link DescriptorTableModel}.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     */
    public DescriptorTableModel(String[] shortNames, LayoutContext context) {
        this(context);
        setTableColumnModel(createColumnModel(shortNames, this.context));
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        TableColumn col = getColumn(column);
        if (col instanceof DescriptorTableColumn) {
            DescriptorTableColumn descCol = (DescriptorTableColumn) col;
            List<SortConstraint> list = getSortConstraints(descCol, ascending);
            result = (list != null) ? list.toArray(new SortConstraint[list.size()]) : null;
        } else {
            result = super.getSortConstraints(column, ascending);
        }
        return result;
    }

    /**
     * Returns a column, given its node name.
     *
     * @param name the node name
     * @return the descriptor column, or {@code null} if none exists
     */
    public DescriptorTableColumn getColumn(String name) {
        return getColumn(getColumnModel(), name);
    }

    /**
     * Returns the sort constraints, given a primary sort column.
     * <p>
     * If the column is not sortable, this implementation returns null.
     *
     * @param primary   the primary sort column
     * @param ascending whether to sort in ascending or descending order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    protected List<SortConstraint> getSortConstraints(DescriptorTableColumn primary, boolean ascending) {
        if (!primary.isSortable()) {
            return null;
        }
        if (primary.getName().equals("description")) {
            return getSortConstraints(primary, ascending, "name", "id");
        }
        return getSortConstraints(primary, ascending, "description", "id");
    }

    /**
     * Returns the sort constraints, given a primary sort column.
     *
     * @param primary   the primary sort column
     * @param ascending whether to sort in ascending or descending order
     * @param names     the secondary sort column names
     * @return the sort constraints
     */
    protected List<SortConstraint> getSortConstraints(DescriptorTableColumn primary, boolean ascending,
                                                      String... names) {
        List<SortConstraint> result = new ArrayList<>();
        result.add(primary.createSortConstraint(ascending));
        for (String name : names) {
            DescriptorTableColumn column = getColumn(name);
            if (column != null && column.isSortable()) {
                result.add(column.createSortConstraint(ascending));
            } else if (column == null && archetypes != null) {
                // node is not present in the table. If it exists in each of the archetypes, add it as a sort constraint
                boolean found = true;
                for (ArchetypeDescriptor archetype : archetypes) {
                    if (archetype.getNodeDescriptor(name) == null) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    result.add(new NodeSortConstraint(name, ascending));
                }
            }
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
    protected Object getValue(T object, TableColumn column, int row) {
        Object result;
        if (column instanceof DescriptorTableColumn) {
            result = getValue(object, (DescriptorTableColumn) column, row);
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
     * @param row    the row
     * @return the value for the column
     */
    protected Object getValue(T object, DescriptorTableColumn column, int row) {
        return column.getComponent(object, context);
    }

    /**
     * Returns the layout context.
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

    /**
     * Creates a column model for a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @param context    the layout context
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(String[] shortNames, LayoutContext context) {
        List<ArchetypeDescriptor> archetypes = DescriptorHelper.getArchetypeDescriptors(shortNames);
        if (archetypes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Argument 'shortNames' doesn't refer to a valid archetype: " + StringUtils.join(shortNames, ", "));
        }
        return createColumnModel(archetypes, context);
    }

    /**
     * Creates a column model for one or more archetypes.
     * If there are multiple archetypes, the intersection of the descriptors
     * will be used.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        this.archetypes = archetypes;
        List<String> names = getNodeNames(archetypes, context);
        TableColumnModel columns = new DefaultTableColumnModel();

        int idIndex = names.indexOf("id");
        if (idIndex != -1) {
            if (!(names instanceof ArrayList)) {
                names = new ArrayList<>(names);  // Arrays.asList() doesn't support remove
            }
            names.remove(idIndex);
            // use default formatting for ID columns.
            TableColumn column = createTableColumn(ID_INDEX, "table.imobject.id");
            columns.addColumn(column);
        }

        if (showArchetypeColumn(archetypes)) {
            addColumns(archetypes, names, columns);
            int index = getArchetypeColumnIndex(idIndex != -1);
            TableColumn column = createTableColumn(ARCHETYPE_INDEX, "table.imobject.archetype");
            columns.addColumn(column);
            columns.moveColumn(columns.getColumnCount() - 1, index);
        } else {
            addColumns(archetypes, names, columns);
        }
        return columns;
    }

    /**
     * Add columns to a column model.
     *
     * @param archetypes the archetypes
     * @param names      the node descriptor names
     * @param columns    the columns to add to
     */
    protected void addColumns(List<ArchetypeDescriptor> archetypes, List<String> names, TableColumnModel columns) {
        // determine a unique starting index for the columns
        int index = getNextModelIndex(columns);

        for (String name : names) {
            addColumn(archetypes, name, index, columns);
            ++index;
        }
    }

    /**
     * Adds a column for a node.
     *
     * @param archetype the archetypes
     * @param name      the node name
     * @param columns   the columns to add to
     * @return the new column, or {@code null} if the node is not found in
     * the archetypes
     */
    protected TableColumn addColumn(ArchetypeDescriptor archetype, String name, TableColumnModel columns) {
        return addColumn(Collections.singletonList(archetype), name, getNextModelIndex(columns), columns);
    }

    /**
     * Adds a column for a node.
     *
     * @param archetypes the archetypes
     * @param name       the node name
     * @param index      the index to assign the column
     * @param columns    the columns to add to
     * @return the new column
     */
    protected TableColumn addColumn(List<ArchetypeDescriptor> archetypes, String name, int index,
                                    TableColumnModel columns) {
        TableColumn column = createColumn(archetypes, name, index);
        columns.addColumn(column);
        return column;
    }

    /**
     * Creates a new column for a node.
     *
     * @param archetypes the archetypes
     * @param name       the node name
     * @param index      the index to assign the column
     * @return a new column
     */
    protected TableColumn createColumn(List<ArchetypeDescriptor> archetypes, String name, int index) {
        return new DescriptorTableColumn(index, name, archetypes);
    }

    /**
     * Returns a column offset given its node name.
     *
     * @param model the model
     * @param name  the node name
     * @return the column offset, or {@code -1} if a column with the specified name doesn't exist
     */
    protected int getColumnOffset(TableColumnModel model, String name) {
        int result = -1;
        int offset = 0;
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col instanceof DescriptorTableColumn) {
                DescriptorTableColumn descriptorCol = (DescriptorTableColumn) col;
                if (descriptorCol.getName().equals(name)) {
                    result = offset;
                    break;
                }
            }
            ++offset;
        }
        return result;
    }

    /**
     * Returns a column, given its node name.
     *
     * @param model the model
     * @param name  the node name
     * @return the descriptor column, or {@code null} if none exists
     */
    protected DescriptorTableColumn getColumn(TableColumnModel model, String name) {
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col instanceof DescriptorTableColumn) {
                DescriptorTableColumn descriptorCol = (DescriptorTableColumn) col;
                if (descriptorCol.getName().equals(name)) {
                    return descriptorCol;
                }
            }
        }
        return null;
    }

    /**
     * Returns a column model index given its node name.
     *
     * @param model the model
     * @param name  the node name
     * @return the column index, or {@code -1} if a column with the specified name doesn't exist
     */
    protected int getModelIndex(TableColumnModel model, String name) {
        DescriptorTableColumn column = getColumn(model, name);
        return (column != null) ? column.getModelIndex() : -1;
    }

    /**
     * Returns the node names for a set of archetypes.
     * <p>
     * If {@link #getNodeNames()} returns a non-empty list, then
     * these names will be used, otherwise the {@link #getArchetypeNodes()} will be used to determine which nodes
     * are returned.
     *
     * @param archetypes the archetype descriptors
     * @param context    the layout context
     * @return the node names for the archetypes
     */
    protected List<String> getNodeNames(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        List<String> result;
        String[] names = getNodeNames();
        if (names != null && names.length != 0) {
            result = Arrays.asList(names);
        } else {
            ArchetypeNodes nodes = getArchetypeNodes();
            result = nodes.getNodeNames(archetypes);
        }
        return result;
    }

    /**
     * Returns an {@link ArchetypeNodes} that determines what nodes appear in the table.
     * This is only used when {@link #getNodeNames()} returns null or empty.
     *
     * @return the nodes to include
     */
    protected ArchetypeNodes getArchetypeNodes() {
        return allSimpleNodesMinusIdAndLongText();
    }

    /**
     * Helper to create an {@link ArchetypeNodes} that includes all simple nodes except for strings > 255 characters,
     * and any id or password node.
     *
     * @return a new instance
     */
    protected ArchetypeNodes allSimpleNodesMinusIdAndLongText() {
        return ArchetypeNodes.allSimple()
                .excludeStringLongerThan(NodeDescriptor.DEFAULT_MAX_LENGTH)
                .exclude("id")
                .excludePassword(true);
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     * This implementation returns {@code null} to indicate that the
     * intersection should be calculated from all descriptors.
     *
     * @return the list of node descriptor names to include in the table
     */
    protected String[] getNodeNames() {
        return null;
    }

    /**
     * Determines if the archetype column should be displayed.
     * <p>
     * This implementation returns true if there is more than one archetype.
     *
     * @param archetypes the archetypes
     * @return {@code true} if the archetype column should be displayed
     */
    protected boolean showArchetypeColumn(List<ArchetypeDescriptor> archetypes) {
        return archetypes.size() > 1;
    }

    /**
     * Returns the index to insert the archetype column.
     *
     * @param showId determines if the Id column is being displayed
     * @return the index to insert the archetype column
     */
    protected int getArchetypeColumnIndex(boolean showId) {
        return showId ? 1 : 0;
    }

}
