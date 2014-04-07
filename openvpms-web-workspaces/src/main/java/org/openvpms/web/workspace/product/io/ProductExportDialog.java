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
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.TableLayoutData;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.io.ProductCSVWriter;
import org.openvpms.archetype.rules.product.io.ProductWriter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.QueryBrowser;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.table.PagedIMTableModel;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collections;
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
     * The tax rules.
     */
    private final TaxRules taxRules;

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
        super(Messages.get("product.export.title"), BUTTONS, false, help);
        setStyleName("ProductImportExportDialog");
        rules = ServiceHelper.getBean(ProductPriceRules.class);
        taxRules = new TaxRules(context.getContext().getPractice());
        ProductExportQuery query = new ProductExportQuery(context);
        PagedProductPricesTableModel model = new PagedProductPricesTableModel();
        Browser<Product> browser = new DefaultIMObjectTableBrowser<Product>(query, model, context);
        init(browser, null);
        setCloseOnSelection(false);
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
        ProductWriter exporter = new ProductCSVWriter(ServiceHelper.getArchetypeService(),
                                                      rules, taxRules, ServiceHelper.getBean(DocumentHandlers.class));
        Iterator<Product> iterator = new ResultSetIterator<Product>(query.query());
        Document document;
        boolean includeLinkedPrices = query.includeLinkedPrices();
        PricingGroup pricingGroup = query.getPricingGroup();
        switch (query.getPrices()) {
            case CURRENT:
                document = exporter.write(iterator, true, includeLinkedPrices, pricingGroup);
                break;
            case ALL:
                document = exporter.write(iterator, false, includeLinkedPrices, pricingGroup);
                break;
            default:
                document = exporter.write(iterator, query.getFrom(), query.getTo(), includeLinkedPrices, pricingGroup);
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
            boolean includeLinkedPrices = getQuery().includeLinkedPrices();
            PricingGroup pricingGroup = getQuery().getPricingGroup();
            for (Product product : list) {
                List<ProductPrice> fixedPrices = getPrices(product, ProductArchetypes.FIXED_PRICE, includeLinkedPrices,
                                                           pricingGroup);
                List<ProductPrice> unitPrices = getPrices(product, ProductArchetypes.UNIT_PRICE, includeLinkedPrices,
                                                          pricingGroup);
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
         * @param product             the product
         * @param shortName           the price archetype short name
         * @param includeLinkedPrices if {@code true}, include prices linked from price template products
         * @param pricingGroup        the pricing group. May be {@code null}
         * @return the matching prices
         */
        private List<ProductPrice> getPrices(Product product, String shortName, boolean includeLinkedPrices,
                                             PricingGroup pricingGroup) {
            List<ProductPrice> result = new ArrayList<ProductPrice>();
            ProductExportQuery query = getQuery();
            ProductExportQuery.Prices prices = query.getPrices();
            if (prices == ProductExportQuery.Prices.CURRENT) {
                if (pricingGroup.isAll()) {
                    result.addAll(rules.getProductPrices(product, shortName, includeLinkedPrices, pricingGroup));
                } else {
                    List<ProductPrice> list = rules.getProductPrices(product, shortName, includeLinkedPrices,
                                                                     pricingGroup);
                    if (!list.isEmpty()) {
                        result.add(list.get(0));
                    }
                }
            } else if (prices == ProductExportQuery.Prices.ALL) {
                result.addAll(rules.getProductPrices(product, shortName, includeLinkedPrices, pricingGroup));
            } else {
                result.addAll(rules.getProductPrices(product, shortName, query.getFrom(), query.getTo(),
                                                     includeLinkedPrices, pricingGroup));
            }
            return result;
        }

    }

    private class ProductPricesModel extends ProductImportExportTableModel<ProductPrices> {

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
                    result = (fixedPrice != null) ? rightAlign(fixedPrice.getPrice()) : null;
                    break;
                case FIXED_COST:
                    result = getCost(fixedPrice);
                    break;
                case FIXED_MAX_DISCOUNT:
                    result = getMaxDiscount(fixedPrice);
                    break;
                case FIXED_START_DATE:
                    result = (fixedPrice != null) ? rightAlign(fixedPrice.getFromDate()) : null;
                    break;
                case FIXED_END_DATE:
                    result = (fixedPrice != null) ? rightAlign(fixedPrice.getToDate()) : null;
                    break;
                case FIXED_PRICING_GROUPS:
                    result = getPricingGroups(fixedPrice);
                    break;
                case UNIT_PRICE:
                    result = (unitPrice != null) ? rightAlign(unitPrice.getPrice()) : null;
                    break;
                case UNIT_COST:
                    result = getCost(unitPrice);
                    break;
                case UNIT_MAX_DISCOUNT:
                    result = getMaxDiscount(unitPrice);
                    break;
                case UNIT_START_DATE:
                    result = (unitPrice != null) ? rightAlign(unitPrice.getFromDate()) : null;
                    break;
                case UNIT_END_DATE:
                    result = (unitPrice != null) ? rightAlign(unitPrice.getToDate()) : null;
                    break;
                case UNIT_PRICING_GROUPS:
                    result = getPricingGroups(unitPrice);
                    break;
                default:
                    result = null;
            }
            if (result != null && !(result instanceof Component)) {
                Label label = LabelFactory.create();
                TableLayoutData layoutData = new TableLayoutData();
                layoutData.setAlignment(Alignment.ALIGN_TOP);
                label.setLayoutData(layoutData);
                label.setText(result.toString());
                result = label;
            }
            return result;
        }

        private Object getPricingGroups(ProductPrice price) {
            Object result = null;
            if (price != null) {
                List<Lookup> groups = rules.getPricingGroups(price);
                if (!groups.isEmpty()) {
                    Collections.sort(groups, IMObjectSorter.getNameComparator(true));
                    Column column = ColumnFactory.create(Styles.CELL_SPACING);
                    for (Lookup group : groups) {
                        Label label = LabelFactory.create();
                        label.setText(group.getName());
                        column.add(label);
                    }
                    result = column;
                }
            }
            return result;
        }

        private Label getCost(ProductPrice price) {
            return rightAlign(price, "cost");
        }

        private Label getMaxDiscount(ProductPrice price) {
            return rightAlign(price, "maxDiscount");
        }

        private Label rightAlign(ProductPrice price, String name) {
            Label result = null;
            if (price != null) {
                IMObjectBean bean = new IMObjectBean(price);
                result = rightAlign(bean.getBigDecimal(name));
            }
            return result;
        }

    }

}
