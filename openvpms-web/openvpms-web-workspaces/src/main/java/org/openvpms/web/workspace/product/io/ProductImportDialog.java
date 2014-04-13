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

package org.openvpms.web.workspace.product.io;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.TableColumn;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.io.PriceData;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Product import dialog.
 *
 * @author Tim Anderson
 */
public class ProductImportDialog extends PopupDialog {

    /**
     * Product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * Constructs a {@link ProductImportDialog}.
     *
     * @param data the data to import
     * @param help the help context
     */
    public ProductImportDialog(List<ProductData> data, HelpContext help) {
        super(Messages.get("product.import.title"), "ProductImportExportDialog", OK_CANCEL, help);
        setModal(true);
        rules = ServiceHelper.getBean(ProductPriceRules.class);

        ResultSet<ProductData> resultSet = new ListResultSet<ProductData>(data, 20);
        PagedProductDataTableModel model = new PagedProductDataTableModel();
        PagedIMTable<ProductData> table = new PagedIMTable<ProductData>(model, resultSet);
        getLayout().add(ColumnFactory.create(Styles.INSET, table));
    }

    /**
     * Invoked when the 'OK' button is pressed.
     */
    @Override
    protected void onOK() {
        PopupDialog dialog = new ConfirmationDialog(Messages.get("product.import.title"),
                                                    Messages.get("product.import.save"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                ProductImportDialog.super.onOK();
            }
        });
        dialog.show();
    }

    private class PagedProductDataTableModel extends PagedIMTableModel<ProductData, ProductPriceData> {

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

    private class ProductPriceDataModel extends ProductImportExportTableModel<ProductPriceData> {


        /**
         * Style used to display prior values.
         */
        private static final String LINE_THROUGH = "italicLineThrough";

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
            boolean first = row == 0 || getObjects().get(row - 1).getProductData().getId()
                                        != object.getProductData().getId();
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
                case FIXED_MAX_DISCOUNT:
                    result = (fixedPrice != null) ? getMaxDiscount(object, fixedPrice) : null;
                    break;
                case FIXED_START_DATE:
                    result = (fixedPrice != null) ? getFromDate(object, fixedPrice) : null;
                    break;
                case FIXED_END_DATE:
                    result = (fixedPrice != null) ? getToDate(object, fixedPrice) : null;
                    break;
                case FIXED_PRICING_GROUPS:
                    result = (fixedPrice != null) ? getPricingGroups(object, fixedPrice) : null;
                    break;
                case UNIT_PRICE:
                    result = (unitPrice != null) ? getPrice(object, unitPrice) : null;
                    break;
                case UNIT_COST:
                    result = (unitPrice != null) ? getCost(object, unitPrice) : null;
                    break;
                case UNIT_MAX_DISCOUNT:
                    result = (unitPrice != null) ? getMaxDiscount(object, unitPrice) : null;
                    break;
                case UNIT_START_DATE:
                    result = (unitPrice != null) ? getFromDate(object, unitPrice) : null;
                    break;
                case UNIT_END_DATE:
                    result = (unitPrice != null) ? getToDate(object, unitPrice) : null;
                    break;
                case UNIT_PRICING_GROUPS:
                    result = (unitPrice != null) ? getPricingGroups(object, unitPrice) : null;
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

        private Object getMaxDiscount(ProductPriceData object, PriceData price) {
            ProductPrice current = getProductPrice(object, price);
            if (current != null) {
                IMObjectBean bean = new IMObjectBean(current);
                BigDecimal oldValue = bean.getBigDecimal("maxDiscount");
                return getValue(oldValue, price.getMaxDiscount());
            }
            return getValue(price.getMaxDiscount(), price.getMaxDiscount());
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

        private Component getPricingGroups(ProductPriceData object, PriceData price) {
            Component result;
            ProductPrice current = getProductPrice(object, price);
            Set<Lookup> newValue = price.getPricingGroups();
            if (current != null) {
                Set<Lookup> oldValue = new HashSet<Lookup>(rules.getPricingGroups(current));
                if (!oldValue.equals(newValue)) {
                    result = getOldAndNewValueGrid(getPricingGroups(oldValue, LINE_THROUGH),
                                                   getPricingGroups(newValue, Styles.DEFAULT));
                } else {
                    result = getPricingGroups(newValue, Styles.DEFAULT);
                }
            } else {
                result = getPricingGroups(newValue, Styles.DEFAULT);
            }
            return result;
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
            boolean equals;
            if (oldValue instanceof BigDecimal && newValue instanceof BigDecimal) {
                equals = ((BigDecimal) oldValue).compareTo((BigDecimal) newValue) == 0;
            } else {
                equals = ObjectUtils.equals(oldValue, newValue);
            }
            if (!equals) {
                GridLayoutData oldLayout = new GridLayoutData();
                oldLayout.setAlignment(Alignment.ALIGN_RIGHT);
                oldLayout.setInsets(new Insets(0, 0, 5, 0));
                Label oldLabel = createOldValueLabel(oldValue);
                oldLabel.setLayoutData(oldLayout);

                GridLayoutData newLayout = new GridLayoutData();
                newLayout.setAlignment(Alignment.ALIGN_RIGHT);
                Label newLabel = createNewValueLabel(newValue);
                newLabel.setLayoutData(newLayout);

                result = getOldAndNewValueGrid(oldLabel, newLabel);
            } else {
                if (newValue instanceof BigDecimal) {
                    result = rightAlign((BigDecimal) newValue);
                } else {
                    result = rightAlign(newValue);
                }
            }
            return result;
        }

        private Grid getOldAndNewValueGrid(Component oldValue, Component newValue) {
            Grid grid = new Grid(2);
            grid.setWidth(Styles.FULL_WIDTH);
            grid.setColumnWidth(0, new Extent(50, Extent.PERCENT));
            grid.setColumnWidth(1, new Extent(50, Extent.PERCENT));
            grid.add(oldValue);
            grid.add(newValue);
            TableLayoutData layoutData = new TableLayoutData();
            layoutData.setAlignment(Alignment.ALIGN_RIGHT);
            grid.setLayoutData(layoutData);
            return grid;
        }

        private Label createNewValueLabel(Object newValue) {
            return createLabel(newValue);
        }

        private Label createOldValueLabel(Object oldValue) {
            Label oldLabel = createLabel(oldValue);
            oldLabel.setStyleName(LINE_THROUGH);
            return oldLabel;
        }

        private Label createLabel(Object newValue) {
            Label newLabel = new Label();
            newLabel.setText(newValue != null ? newValue.toString() : Messages.get("product.import.novalue"));
            return newLabel;
        }

    }

}
