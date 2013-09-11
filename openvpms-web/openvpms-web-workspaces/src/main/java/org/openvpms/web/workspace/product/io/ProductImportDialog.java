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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.product.io.PriceData;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Product import dialog.
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
         * Constructs a {@code PagedProductDataTableModel}.
         */
        public PagedProductDataTableModel() {
            super(new ProductPriceDataModel());
        }

        /**
         * Converts to the delegate type.
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
            boolean first = row == 0 || getObjects().get(row - 1).getProductData().getId() != object.getProductData().getId();
            PriceData fixedPrice = object.getFixedPrice();
            PriceData unitPrice = object.getUnitPrice();
            Object result;
            switch (column.getModelIndex()) {
                case ID:
                    result = first ? object.getProductData().getId() : null;
                    break;
                case NAME:
                    result = first ? object.getProductData().getName() : null;
                    break;
                case PRINTED_NAME:
                    result = first ? getPrintedName(object) : null;
                    break;
                case FIXED_PRICE:
                    result = (fixedPrice != null) ? getPrice(object, fixedPrice) : null;
                    break;
                case FIXED_COST:
                    result = (fixedPrice != null) ? getCost(object, fixedPrice) : null;
                    break;
                case FIXED_START_DATE:
                    result = (fixedPrice != null) ? getFromDate(object, fixedPrice) : null;
                    break;
                case FIXED_END_DATE:
                    result = (fixedPrice != null) ? getToDate(object, fixedPrice) : null;
                    break;
                case UNIT_PRICE:
                    result = (unitPrice != null) ? getPrice(object, unitPrice) : null;
                    break;
                case UNIT_COST:
                    result = (unitPrice != null) ? getCost(object, unitPrice) : null;
                    break;
                case UNIT_START_DATE:
                    result = (unitPrice != null) ? getFromDate(object, unitPrice) : null;
                    break;
                case UNIT_END_DATE:
                    result = (unitPrice != null) ? getToDate(object, unitPrice) : null;
                    break;
                default:
                    result = null;
            }
            return result;
        }

        private Object getPrice(ProductPriceData object, PriceData price) {
            ProductPrice current = getProductPrice(object, price);
            if (current != null) {
                return getValue(current.getPrice(), price.getPrice());
            }
            return getValue(price.getPrice(), price.getPrice());
        }

        private Object getCost(ProductPriceData object, PriceData price) {
            ProductPrice current = getProductPrice(object, price);
            if (current != null) {
                IMObjectBean bean = new IMObjectBean(current);
                BigDecimal oldValue = bean.getBigDecimal("cost");
                return getValue(oldValue, price.getCost());
            }
            return getValue(price.getCost(), price.getCost());
        }

        private Object getFromDate(ProductPriceData object, PriceData price) {
            ProductPrice current = getProductPrice(object, price);
            if (current != null) {
                Date oldValue = current.getFromDate();
                return getValue(formatDate(oldValue), formatDate(price.getFrom()));
            }
            return getValue(formatDate(price.getFrom()), formatDate(price.getFrom()));
        }

        private Object getToDate(ProductPriceData object, PriceData price) {
            ProductPrice current = getProductPrice(object, price);
            if (current != null) {
                Date oldValue = current.getToDate();
                return getValue(formatDate(oldValue), formatDate(price.getTo()));
            }
            return getValue(formatDate(price.getTo()), formatDate(price.getTo()));
        }

        private ProductPrice getProductPrice(ProductPriceData object, PriceData price) {
            ProductPrice result = null;
            if (price.getId() != -1) {
                Product product = object.getProduct();
                if (product != null) {
                    for (ProductPrice productPrice : product.getProductPrices()) {
                        if (productPrice.getId() == price.getId()) {
                            result = productPrice;
                            break;
                        }
                    }
                }
            }
            return result;
        }

        private Object getPrintedName(ProductPriceData object) {
            Object oldValue = null;
            Object newValue = object.getProductData().getPrintedName();
            Product product = object.getProduct();
            if (product != null) {
                IMObjectBean bean = new IMObjectBean(product);
                oldValue = bean.getString("printedName");
            }
            if (!ObjectUtils.equals(oldValue, newValue)) {
                Label oldLabel = createOldValueLabel(oldValue);
                Label newLabel = createNewValueLabel(newValue);
                return ColumnFactory.create(oldLabel, newLabel);
            }
            return newValue;
        }

        private Object getValue(Object oldValue, Object newValue) {
            Object result;
            if (!ObjectUtils.equals(oldValue, newValue)) {
                GridLayoutData oldLayout = new GridLayoutData();
                oldLayout.setAlignment(Alignment.ALIGN_RIGHT);
                oldLayout.setInsets(new Insets(0, 0, 5, 0));
                Label oldLabel = createOldValueLabel(oldValue);
                oldLabel.setLayoutData(oldLayout);

                GridLayoutData newLayout = new GridLayoutData();
                newLayout.setAlignment(Alignment.ALIGN_RIGHT);
                Label newLabel = createNewValueLabel(newValue);
                newLabel.setLayoutData(newLayout);

                Grid grid = new Grid(2);
                grid.setWidth(Styles.FULL_WIDTH);
                grid.setColumnWidth(0, new Extent(50, Extent.PERCENT));
                grid.setColumnWidth(1, new Extent(50, Extent.PERCENT));
                grid.add(oldLabel);
                grid.add(newLabel);
                TableLayoutData layoutData = new TableLayoutData();
                layoutData.setAlignment(Alignment.ALIGN_RIGHT);
                grid.setLayoutData(layoutData);
                result = grid;
            } else {
                if (newValue != null) {
                    Label label = new Label();
                    label.setText(newValue.toString());
                    TableLayoutData layoutData = new TableLayoutData();
                    layoutData.setAlignment(Alignment.ALIGN_RIGHT);
                    label.setLayoutData(layoutData);
                    result = label;
                } else {
                    result = null;
                }
            }
            return result;
        }

        private Label createNewValueLabel(Object newValue) {
            Label newLabel = new Label();
            newLabel.setText(newValue != null ? newValue.toString() : "No Value");
            return newLabel;
        }

        private Label createOldValueLabel(Object oldValue) {
            Label oldLabel = new Label();
            oldLabel.setText(oldValue != null ? oldValue.toString() : "No Value");
            oldLabel.setStyleName("italicLineThrough");
            return oldLabel;
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

        private String formatDate(Date date) {
            return date != null ? DateFormatter.formatDate(date, false) : null;
        }

    }

}
