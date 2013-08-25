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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.io;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Dialog to report product import errors.
 *
 * @author Tim Anderson
 */
public class ProductImportErrorDialog extends PopupDialog {

    /**
     * Constructs a {@link ProductImportErrorDialog}.
     */
    public ProductImportErrorDialog(List<ProductData> errors) {
        super(Messages.get("product.io.import.error.title"), "BrowserDialog", OK);

        ResultSet<ProductData> resultSet = new ListResultSet<ProductData>(errors, 20);
        PagedIMTableModel<ProductData, ProductData> model
                = new PagedIMTableModel<ProductData, ProductData>(new ErrorTableModel());
        PagedIMTable<ProductData> table = new PagedIMTable<ProductData>(model, resultSet);
        Label message = LabelFactory.create("product.io.import.error.message");
        getLayout().add(ColumnFactory.create(Styles.INSET,
                                             ColumnFactory.create(Styles.WIDE_CELL_SPACING, message, table)));
    }

    private static class ErrorTableModel extends AbstractIMTableModel<ProductData> {

        private static final int ID = 0;
        private static final int NAME = 1;
        private static final int PRINTED_NAME = 2;
        private static final int ERROR = 3;
        private static final int LINE = 4;

        public ErrorTableModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(ID, "product.io.import.id"));
            model.addColumn(createTableColumn(NAME, "product.io.import.name"));
            model.addColumn(createTableColumn(PRINTED_NAME, "product.io.import.printedName"));
            model.addColumn(createTableColumn(ERROR, "product.io.import.error"));
            model.addColumn(createTableColumn(LINE, "product.io.import.line"));
            setTableColumnModel(model);
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
        protected Object getValue(ProductData object, TableColumn column, int row) {
            Object result;
            switch (column.getModelIndex()) {
                case ID:
                    result = object.getId();
                    break;
                case NAME:
                    result = object.getName();
                    break;
                case PRINTED_NAME:
                    result = object.getPrintedName();
                    break;
                case ERROR:
                    result = object.getError();
                    break;
                case LINE:
                    result = object.getLine();
                    break;
                default:
                    result = null;
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
