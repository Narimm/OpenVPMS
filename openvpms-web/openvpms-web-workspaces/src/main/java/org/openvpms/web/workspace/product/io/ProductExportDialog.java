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
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.io.ProductWriter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Product export dialog.
 *
 * @author Tim Anderson
 */
public class ProductExportDialog extends BrowserDialog<Product> {

    /**
     * The price rules.
     */
    private final ProductPriceRules rules;

    /**
     * The export button identifier.
     */
    private static final String EXPORT_ID = "button.export";

    /**
     * The dialog buttons.
     */
    private static final String[] BUTTONS = {EXPORT_ID, CLOSE_ID};

    /**
     * Constructs a {@link ProductExportDialog}.
     *
     * @param context the layout context
     * @param help    the help context
     */
    public ProductExportDialog(LayoutContext context, HelpContext help) {
        super(Messages.get("product.io.export.title"), BUTTONS, false, help);
        this.rules = ServiceHelper.getBean(ProductPriceRules.class);
        ProductExportQuery query = new ProductExportQuery();
        PagedProductPricesTableModel model = new PagedProductPricesTableModel();
        Browser<Product> browser = new DefaultIMObjectTableBrowser<Product>(query, model, context);
        init(browser, null);
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (EXPORT_ID.equals(button)) {
            onExport();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Invoked when the "export" button is pressed.
     * <p/>
     * This runs the {@link ProductWriter} against the products returned by the {@link ProductExportQuery},
     * and starts a download of the resulting document.
     */
    private void onExport() {
        ProductExportQuery query = getQuery();
        ProductWriter exporter = ServiceHelper.getBean(ProductWriter.class);
        Iterator<Product> iterator = new ResultSetIterator<Product>(query.query());
        Document document;
        switch (query.getPrices()) {
            case CURRENT:
                document = exporter.write(iterator, true);
                break;
            case ALL:
                document = exporter.write(iterator, false);
                break;
            default:
                document = exporter.write(iterator, query.getFrom(), query.getTo());
        }
        DownloadServlet.startDownload(document);
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    private ProductExportQuery getQuery() {
        return (ProductExportQuery) ((QueryBrowser<Product>) getBrowser()).getQuery();
    }

    private final class PagedProductPricesTableModel extends PagedIMTableModel<Product, ProductPrices> {

        /**
         * Constructs a {@code PagedProductPricesTableModel}.
         */
        public PagedProductPricesTableModel() {
            super(new ProductPricesModel());
        }

        /**
         * Converts to the delegate type
         *
         * @param list the list to convert
         * @return the converted list
         */
        @Override
        protected List<ProductPrices> convertTo(List<Product> list) {
            List<ProductPrices> result = new ArrayList<ProductPrices>();
            for (Product product : list) {
                List<ProductPrice> fixedPrices = getPrices(product, ProductArchetypes.FIXED_PRICE);
                List<ProductPrice> unitPrices = getPrices(product, ProductArchetypes.UNIT_PRICE);
                int count = Math.max(fixedPrices.size(), unitPrices.size());
                if (count == 0) {
                    count = 1;
                }
                for (int i = 0; i < count; ++i) {
                    ProductPrice fixedPrice = i < fixedPrices.size() ? fixedPrices.get(i) : null;
                    ProductPrice unitPrice = i < unitPrices.size() ? unitPrices.get(i) : null;
                    ProductPrices prices = new ProductPrices(product, fixedPrice, unitPrice);
                    result.add(prices);
                }
            }
            return result;
        }

        /**
         * Returns prices matching some criteria.
         *
         * @param product   the product
         * @param shortName the price archetype short name
         * @return the matching prices
         */
        private List<ProductPrice> getPrices(Product product, String shortName) {
            List<ProductPrice> result = new ArrayList<ProductPrice>();
            ProductExportQuery query = getQuery();
            ProductExportQuery.Prices prices = query.getPrices();
            if (prices == ProductExportQuery.Prices.CURRENT) {
                List<ProductPrice> list = rules.getProductPrices(product, shortName);
                if (!list.isEmpty()) {
                    result.add(list.get(0));
                }
            } else if (prices == ProductExportQuery.Prices.ALL) {
                result.addAll(rules.getProductPrices(product, shortName));
            } else {
                result.addAll(rules.getProductPrices(product, shortName, query.getFrom(), query.getTo()));
            }
            return result;
        }

    }

    private static class ProductPricesModel extends AbstractIMTableModel<ProductPrices> {

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

        public ProductPricesModel() {
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
        protected Object getValue(ProductPrices object, TableColumn column, int row) {
            boolean first = row == 0 || getObjects().get(row - 1).getProduct().getId() != object.getProduct().getId();
            ProductPrice fixedPrice = object.getFixedPrice();
            ProductPrice unitPrice = object.getUnitPrice();
            Object result;
            switch (column.getModelIndex()) {
                case ID:
                    result = first ? object.getProduct().getId() : null;
                    break;
                case NAME:
                    result = first ? object.getProduct().getName() : null;
                    break;
                case PRINTED_NAME:
                    if (first) {
                        IMObjectBean bean = new IMObjectBean(object.getProduct());
                        result = bean.getString("printedName");
                    } else {
                        result = null;
                    }
                    break;
                case FIXED_PRICE:
                    result = (fixedPrice != null) ? fixedPrice.getPrice() : null;
                    break;
                case FIXED_COST:
                    result = getCost(fixedPrice);
                    break;
                case FIXED_START_DATE:
                    result = (fixedPrice != null) ? formatDate(fixedPrice.getFromDate()) : null;
                    break;
                case FIXED_END_DATE:
                    result = (fixedPrice != null) ? formatDate(fixedPrice.getToDate()) : null;
                    break;
                case UNIT_PRICE:
                    result = (unitPrice != null) ? unitPrice.getPrice() : null;
                    break;
                case UNIT_COST:
                    result = getCost(unitPrice);
                    break;
                case UNIT_START_DATE:
                    result = (unitPrice != null) ? formatDate(unitPrice.getFromDate()) : null;
                    break;
                case UNIT_END_DATE:
                    result = (unitPrice != null) ? formatDate(unitPrice.getToDate()) : null;
                    break;
                default:
                    result = null;
            }
            return result;
        }

        private Object getCost(ProductPrice price) {
            if (price != null) {
                IMObjectBean bean = new IMObjectBean(price);
                return bean.getString("cost");
            }
            return null;
        }

        private String formatDate(Date date) {
            return date != null ? DateFormatter.formatDate(date, false) : null;
        }

    }

}
