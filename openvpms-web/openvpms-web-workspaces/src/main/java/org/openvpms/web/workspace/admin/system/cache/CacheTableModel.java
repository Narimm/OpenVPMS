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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system.cache;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumn;
import org.apache.commons.collections.Transformer;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;
import org.openvpms.web.echo.table.EvenOddTableCellRenderer;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

/**
 * Table model for {@link CacheState}.
 *
 * @author Tim Anderson
 */
class CacheTableModel extends AbstractIMTableModel<CacheState> {

    /**
     * The name column index.
     */
    private static final int NAME_INDEX = 0;

    /**
     * The current element count column index.
     */
    private static final int COUNT_INDEX = NAME_INDEX + 1;

    /**
     * The max element count column index.
     */
    private static final int MAX_COUNT_INDEX = COUNT_INDEX + 1;

    /**
     * The use column index.
     */
    private static final int USE_INDEX = MAX_COUNT_INDEX + 1;

    /**
     * The hits column index.
     */
    private static final int HITS_INDEX = USE_INDEX + 1;

    /**
     * The misses column index.
     */
    private static final int MISSES_INDEX = HITS_INDEX + 1;

    /**
     * The size column index.
     */
    private static final int SIZE_INDEX = MISSES_INDEX + 1;

    /**
     * Constructs a {@link CacheTableModel}.
     */
    public CacheTableModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        TableCellRenderer numericRenderer = new EvenOddTableCellRenderer() {
            @Override
            protected Component getComponent(Table table, Object value, int column, int row) {
                if (value instanceof Number) {
                    String number = NumberFormatter.format((Number) value);
                    return TableHelper.rightAlign(number);
                } else {
                    return super.getComponent(table, value, column, row);
                }
            }
        };
        TableCellRenderer percentRenderer = new EvenOddTableCellRenderer() {
            @Override
            protected Component getComponent(Table table, Object value, int column, int row) {
                if (value instanceof Integer) {
                    String number = NumberFormatter.format((Integer) value) + "%";
                    return TableHelper.rightAlign(number);
                } else {
                    return super.getComponent(table, value, column, row);
                }
            }
        };

        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(COUNT_INDEX, "admin.system.cache.count", numericRenderer));
        model.addColumn(createTableColumn(MAX_COUNT_INDEX, "admin.system.cache.maxcount", numericRenderer));
        model.addColumn(createTableColumn(USE_INDEX, "admin.system.cache.use", percentRenderer));
        model.addColumn(createTableColumn(HITS_INDEX, "admin.system.cache.hits", numericRenderer));
        model.addColumn(createTableColumn(MISSES_INDEX, "admin.system.cache.misses", numericRenderer));
        model.addColumn(createTableColumn(SIZE_INDEX, "admin.system.cache.size", numericRenderer));
        setTableColumnModel(model);
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
        if (column == NAME_INDEX) {
            return createSortConstraint("name", ascending, input -> ((CacheState) input).getDisplayName());
        } else if (column == COUNT_INDEX) {
            return createSortConstraint("count", ascending, input -> new Long(((CacheState) input).getCount()));
        } else if (column == MAX_COUNT_INDEX) {
            return createSortConstraint("maxcount", ascending, input -> new Long(((CacheState) input).getMaxCount()));
        } else if (column == USE_INDEX) {
            return createSortConstraint("use", ascending, input -> new Integer(((CacheState) input).getUse()));
        } else if (column == HITS_INDEX) {
            return createSortConstraint("hits", ascending, input -> new Long(((CacheState) input).getHits()));
        } else if (column == MISSES_INDEX) {
            return createSortConstraint("misses", ascending, input -> new Long(((CacheState) input).getMisses()));
        } else if (column == SIZE_INDEX) {
            return createSortConstraint("size", ascending, input -> new Long(((CacheState) input).getSize()));
        }
        return null;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    @Override
    protected Object getValue(CacheState object, TableColumn column, int row) {
        Object result = null;
        switch (column.getModelIndex()) {
            case NAME_INDEX:
                result = object.getDisplayName();
                break;
            case COUNT_INDEX:
                result = object.getCount();
                break;
            case MAX_COUNT_INDEX:
                result = object.getMaxCount();
                break;
            case USE_INDEX:
                result = object.getUse();
                break;
            case HITS_INDEX:
                result = object.getHits();
                break;
            case MISSES_INDEX:
                result = object.getMisses();
                break;
            case SIZE_INDEX:
                result = NumberFormatter.getSize(object.getSize());
                break;
        }
        return result;
    }

    /**
     * Helper to create a sort constraint.
     *
     * @param name        the constraint name
     * @param ascending   determines whether to sort in ascending or descending order
     * @param transformer a transformer to get the  node value
     * @return an array containing the sort constraint
     */
    private SortConstraint[] createSortConstraint(String name, boolean ascending, Transformer transformer) {
        return new SortConstraint[]{new VirtualNodeSortConstraint(name, ascending, transformer)};
    }
}
