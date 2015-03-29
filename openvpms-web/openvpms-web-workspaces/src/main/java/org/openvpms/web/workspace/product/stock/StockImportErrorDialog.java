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

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.stock.io.StockData;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Dialog to report stock import errors.
 *
 * @author Tim Anderson
 */
public class StockImportErrorDialog extends PopupDialog {

    /**
     * Constructs a {@link StockImportErrorDialog}.
     *
     * @param errors the import errors
     * @param help   the help context
     */
    public StockImportErrorDialog(List<StockData> errors, HelpContext help) {
        super(Messages.get("product.stock.import.error.title"), "BrowserDialog", OK, help);
        setModal(true);

        ResultSet<StockData> resultSet = new ListResultSet<StockData>(errors, 20);
        PagedIMTableModel<StockData, StockData> model
                = new PagedIMTableModel<StockData, StockData>(new ErrorTableModel());
        PagedIMTable<StockData> table = new PagedIMTable<StockData>(model, resultSet);
        Label message = LabelFactory.create("product.stock.import.error.message");
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET,
                                             ColumnFactory.create(Styles.WIDE_CELL_SPACING, message, table)));
    }

    private static class ErrorTableModel extends StockDataTableModel {

        private static final int LOCATION_ID = QUANTITY_INDEX + 1;
        private static final int LOCATION_NAME = LOCATION_ID + 1;
        private static final int NEW_QUANTITY = LOCATION_NAME + 1;
        private static final int LINE = NEW_QUANTITY + 1;
        private static final int ERROR = LINE + 1;

        public ErrorTableModel() {
            DefaultTableColumnModel model = (DefaultTableColumnModel) getColumnModel();
            model.addColumn(createTableColumn(LOCATION_ID, "product.stock.import.locationId"));
            model.moveColumn(getColumnCount() - 1, 0);
            model.addColumn(createTableColumn(LOCATION_NAME, "product.stockLocation"));
            model.moveColumn(getColumnCount() - 1, 1);
            model.addColumn(createTableColumn(NEW_QUANTITY, "product.stock.import.newQuantity"));
            model.addColumn(createTableColumn(LINE, "product.import.line"));
            model.addColumn(createTableColumn(ERROR, "product.import.error"));
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
            Object result;
            switch (column.getModelIndex()) {
                case LOCATION_ID:
                    result = object.getStockLocationId();
                    break;
                case LOCATION_NAME:
                    result = object.getStockLocationName();
                    break;
                case NEW_QUANTITY:
                    result = object.getNewQuantity();
                    break;
                case LINE:
                    result = object.getLine();
                    break;
                case ERROR:
                    result = object.getError();
                    break;
                default:
                    result = super.getValue(object, column, row);
            }
            return result;
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
    }

}
