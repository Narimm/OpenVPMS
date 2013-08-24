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

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.product.io.PriceData;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class ProductImportDialog extends PopupDialog {

    /**
     * Constructs a {@link ProductImportDialog}.
     */
    public ProductImportDialog(List<ProductData> data) {
        super(Messages.get("product.io.import.title"), "BrowserDialog", OK_CANCEL);

        ResultSet<ProductData> resultSet = new ListResultSet<ProductData>(data, 20);
        PagedProductDataTableModel model = new PagedProductDataTableModel();
        PagedIMTable<ProductData> table = new PagedIMTable<ProductData>(model, resultSet);
        getLayout().add(ColumnFactory.create(Styles.INSET, table));
    }


    private static final class PagedProductDataTableModel extends PagedIMTableModel<ProductData, ProductPriceData> {

        /**
         * Constructs a <tt>PagedProductDataTableModel</tt>.
         */
        public PagedProductDataTableModel() {
            super(new ProductPriceDataModel());
        }

        /**
         * Converts to the delegate type. This implementation does a simple cast.
         *
         * @param list the list to convert
         * @return the converted list
         */
        @Override
        protected List<ProductPriceData> convertTo(List<ProductData> list) {
            List<ProductPriceData> result = new ArrayList<ProductPriceData>();
            for (ProductData product : list) {
                List<PriceData> fixedPrices = product.getFixedPrices();
                List<PriceData> unitPrices = product.getUnitPrices();
                int count = Math.max(fixedPrices.size(), unitPrices.size());
                if (count == 0) {
                    count = 1;
                }
                for (int i = 0; i < count; ++i) {
                    PriceData fixedPrice = i < fixedPrices.size() ? fixedPrices.get(i) : null;
                    PriceData unitPrice = i < unitPrices.size() ? unitPrices.get(i) : null;
                    ProductPriceData data = new ProductPriceData(product, fixedPrice, unitPrice);
                    result.add(data);
                }
            }
            return result;
        }
    }

    private static class ProductPriceDataModel extends AbstractIMTableModel<ProductPriceData> {

        private static final int ID = 0;
        private static final int NAME = 1;
        private static final int PRINTED_NAME = 2;
        private static final int FIXED_PRICE = 3;
        private static final int FIXED_COST = 4;
        private static final int FIXED_START_DATE = 5;
        private static final int FIXED_END_DATE = 6;
        private static final int UNIT_PRICE = 7;
        private static final int UNIT_COST = 8;
        private static final int UNIT_START_DATE = 9;
        private static final int UNIT_END_DATE = 10;

        public ProductPriceDataModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(ID, "product.io.import.id"));
            model.addColumn(createTableColumn(NAME, "product.io.import.name"));
            model.addColumn(createTableColumn(PRINTED_NAME, "product.io.import.printedName"));
            model.addColumn(createTableColumn(FIXED_PRICE, "product.io.import.fixedPrice"));
            model.addColumn(createTableColumn(FIXED_COST, "product.io.import.fixedCost"));
            model.addColumn(createTableColumn(FIXED_START_DATE, "product.io.import.fixedPriceStartDate"));
            model.addColumn(createTableColumn(FIXED_END_DATE, "product.io.import.fixedPriceEndDate"));
            model.addColumn(createTableColumn(UNIT_PRICE, "product.io.import.unitPrice"));
            model.addColumn(createTableColumn(UNIT_COST, "product.io.import.unitCost"));
            model.addColumn(createTableColumn(UNIT_START_DATE, "product.io.import.unitPriceStartDate"));
            model.addColumn(createTableColumn(UNIT_END_DATE, "product.io.import.unitPriceEndDate"));
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
        protected Object getValue(ProductPriceData object, TableColumn column, int row) {
            boolean first = row == 0 || getObjects().get(row - 1).getProduct().getId() != object.getProduct().getId();
            PriceData fixedPrice = object.getFixedPrice();
            PriceData unitPrice = object.getUnitPrice();
            Object result;
            switch (column.getModelIndex()) {
                case ID:
                    result = first ? object.getProduct().getId() : null;
                    break;
                case NAME:
                    result = first ? object.getProduct().getName() : null;
                    break;
                case PRINTED_NAME:
                    result = first ? object.getProduct().getPrintedName() : null;
                    break;
                case FIXED_PRICE:
                    result = (fixedPrice != null) ? fixedPrice.getPrice() : null;
                    break;
                case FIXED_COST:
                    result = (fixedPrice != null) ? fixedPrice.getCost() : null;
                    break;
                case FIXED_START_DATE:
                    result = (fixedPrice != null) ? formatDate(fixedPrice.getFrom()) : null;
                    break;
                case FIXED_END_DATE:
                    result = (fixedPrice != null) ? formatDate(fixedPrice.getTo()) : null;
                    break;
                case UNIT_PRICE:
                    result = (unitPrice != null) ? unitPrice.getPrice() : null;
                    break;
                case UNIT_COST:
                    result = (unitPrice != null) ? unitPrice.getCost() : null;
                    break;
                case UNIT_START_DATE:
                    result = (unitPrice != null) ? formatDate(unitPrice.getFrom()) : null;
                    break;
                case UNIT_END_DATE:
                    result = (unitPrice != null) ? formatDate(unitPrice.getTo()) : null;
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
         * @param ascending if <tt>true</tt> sort in ascending order; otherwise
         *                  sort in <tt>descending</tt> order
         * @return the sort criteria, or <tt>null</tt> if the column isn't
         *         sortable
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            return null;
        }

        private String formatDate(Date date) {
            return date != null ? DateFormatter.formatDate(date, false) : null;
        }

    }

}
