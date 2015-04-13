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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.stock.io.StockData;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMTableModel;

/**
 * Stock data table model.
 *
 * @author Tim Anderson
 */
class StockDataTableModel extends AbstractIMTableModel<StockData> {

    /**
     * The product id model index.
     */
    protected static final int ID_INDEX = 0;

    /**
     * The product name model index.
     */
    protected static final int NAME_INDEX = 1;

    /**
     * The product selling units model index.
     */
    protected static final int SELLING_UNITS_INDEX = 2;

    /**
     * The current quantity model index.
     */
    protected static final int QUANTITY_INDEX = 3;

    /**
     * Constructs an {@link StockDataTableModel}.
     */
    public StockDataTableModel() {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, "product.import.id"));
        model.addColumn(createTableColumn(NAME_INDEX, "product.import.name"));
        model.addColumn(createTableColumn(SELLING_UNITS_INDEX, "product.stock.export.sellingUnits"));
        model.addColumn(createTableColumn(QUANTITY_INDEX, "product.stock.export.quantity"));
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
    protected Object getValue(StockData object, TableColumn column, int row) {
        switch (column.getModelIndex()) {
            case ID_INDEX:
                return object.getProductId();
            case NAME_INDEX:
                return object.getProductName();
            case SELLING_UNITS_INDEX:
                return object.getSellingUnits();
            case QUANTITY_INDEX:
                return object.getQuantity();
        }
        return null;
    }
}
