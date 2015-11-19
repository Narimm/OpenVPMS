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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.supplier;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

/**
 * Generates orders.
 *
 * @author Tim Anderson
 */
class OrderGenerator {

    /**
     * The tax rules.
     */
    private final TaxRules taxRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Column names returned by the getStockToOrderByStockLocationAndSupplier named query.
     */
    private static final List<String> NAMES = Arrays.asList("productId", "productShortName", "productLinkId",
                                                            "productSupplierId", "quantity", "idealQty",
                                                            "criticalQty", "packageSize", "packageUnits",
                                                            "reorderCode", "reorderDesc",
                                                            "nettPrice", "listPrice", "orderedQty", "receivedQty",
                                                            "cancelledQty");

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OrderGenerator.class);

    /**
     * Constructs an {@link OrderGenerator}.
     *
     * @param taxRules the tax rules
     * @param service  the service
     */
    public OrderGenerator(TaxRules taxRules, IArchetypeService service) {
        this.taxRules = taxRules;
        this.service = service;
    }

    /**
     * Returns the orderable stock for the specified supplier and stock location.
     *
     * @param supplier           the supplier
     * @param stockLocation      the stock location
     * @param belowIdealQuantity if {@code true}, return stock that is {@code <=} the ideal quantity, else return stock
     *                           that is {@code <=} the critical quantity
     * @return the orderable stock
     */
    public List<Stock> getOrderableStock(Party supplier, Party stockLocation, boolean belowIdealQuantity) {
        List<Stock> result = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("stockLocationId", stockLocation.getId());
        parameters.put("supplierId", supplier.getId());
        parameters.put("supplier", supplier.getObjectReference().toString());
        NamedQuery query = new NamedQuery("getStockToOrderByStockLocationAndSupplier", NAMES, parameters);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            Product product = (Product) getObject("product", set);
            if (product != null) {
                Stock stock = getStock(set, product, supplier, stockLocation, belowIdealQuantity);
                if (stock != null) {
                    result.add(stock);
                }
            }
        }
        return result;
    }

    /**
     * Creates an order for all products supplied by the supplier for the specified stock location.
     *
     * @param supplier           the supplier
     * @param stockLocation      the stock location
     * @param belowIdealQuantity if {@code true}, return stock that is {@code <=} the ideal quantity, else return stock
     *                           that is {@code <=} the critical quantity
     * @return the order and its items, or an empty list if there are no products to order
     */
    public List<FinancialAct> createOrder(Party supplier, Party stockLocation, boolean belowIdealQuantity) {
        ActCalculator calculator = new ActCalculator(service);
        List<FinancialAct> result = new ArrayList<>();
        List<Stock> toOrder = getOrderableStock(supplier, stockLocation, belowIdealQuantity);
        if (!toOrder.isEmpty()) {
            FinancialAct order = (FinancialAct) service.create(SupplierArchetypes.ORDER);
            result.add(order);
            ActBean bean = new ActBean(order, service);
            bean.addNodeParticipation("supplier", supplier);
            bean.addNodeParticipation("stockLocation", stockLocation);
            List<FinancialAct> items = new ArrayList<>();

            for (Stock stock : toOrder) {
                FinancialAct item = (FinancialAct) service.create(SupplierArchetypes.ORDER_ITEM);
                bean.addNodeRelationship("items", item);
                items.add(item);

                ActBean itemBean = new ActBean(item, service);
                itemBean.setValue("startTime", order.getActivityStartTime());
                itemBean.addNodeParticipation("product", stock.getProduct());
                itemBean.setValue("reorderCode", stock.getReorderCode());
                itemBean.setValue("reorderDescription", stock.getReorderDescription());
                itemBean.setValue("packageSize", stock.getPackageSize());
                itemBean.setValue("packageUnits", stock.getPackageUnits());
                itemBean.setValue("quantity", stock.getToOrder());
                itemBean.setValue("unitPrice", stock.getUnitPrice());
                itemBean.setValue("listPrice", stock.getListPrice());

                BigDecimal amount = stock.getToOrder().multiply(stock.getUnitPrice());
                BigDecimal tax = taxRules.calculateTax(amount, stock.getProduct(), false);
                tax = MathRules.round(tax, 2);  // TODO - should round according to currency conventions

                itemBean.setValue("tax", tax);
                service.deriveValues(item);
            }
            bean.setValue("amount", calculator.sum(order, items, "total"));
            bean.setValue("tax", calculator.sum(order, items, "tax"));
            service.deriveValues(order);
            result.addAll(items);
        }
        return result;
    }

    /**
     * Creates a {@link Stock} from {@code set}, if stock needs to be ordered.
     *
     * @param set                the object set
     * @param product            the product
     * @param supplier           the product supplier
     * @param stockLocation      the product stock location
     * @param belowIdealQuantity if {@code true}, create stock if the current quantity {@code <=} the ideal quantity,
     *                           create stock if the current quantity {@code <=} the critical quantity
     * @return the stock, or {@code null} if the requirements for ordering the stock aren't met
     */
    private Stock getStock(ObjectSet set, Product product, Party supplier, Party stockLocation,
                           boolean belowIdealQuantity) {
        Stock stock = null;
        long productSupplierId = set.getLong("productSupplierId");
        BigDecimal quantity = getDecimal("quantity", set);
        BigDecimal idealQty = getDecimal("idealQty", set);
        BigDecimal criticalQty = getDecimal("criticalQty", set);
        int packageSize = set.getInt("packageSize");
        String packageUnits = set.getString("packageUnits");
        String reorderCode = set.getString("reorderCode");
        String reorderDesc = set.getString("reorderDesc");
        BigDecimal nettPrice = getDecimal("nettPrice", set);
        BigDecimal listPrice = getDecimal("listPrice", set);
        BigDecimal orderedQty = getDecimal("orderedQty", set);
        BigDecimal receivedQty = getDecimal("receivedQty", set);
        BigDecimal cancelledQty = getDecimal("cancelledQty", set);
        if (packageSize != 0) {
            BigDecimal decSize = BigDecimal.valueOf(packageSize);
            BigDecimal onOrder;
            if (receivedQty.compareTo(orderedQty) > 0) {
                onOrder = BigDecimal.ZERO;
            } else {
                onOrder = orderedQty.subtract(receivedQty).subtract(cancelledQty);
            }
            BigDecimal current = quantity.add(onOrder); // the on-hand and on-order stock
            BigDecimal toOrder = ZERO;
            BigDecimal units = idealQty.subtract(current); // no. of units required to get to idealQty
            if (!MathRules.equals(ZERO, units)) {
                // Round up as the desired no. may be less than a packageSize, but must order a whole pack.
                toOrder = units.divide(decSize, 0, RoundingMode.UP);
            }

            if (toOrder.compareTo(BigDecimal.ZERO) > 0
                && ((belowIdealQuantity && current.compareTo(idealQty) <= 0) || current.compareTo(criticalQty) <= 0)) {
                stock = new Stock(product, stockLocation, supplier, productSupplierId, quantity, idealQty,
                                  onOrder, toOrder, reorderCode, reorderDesc, packageSize, packageUnits, nettPrice,
                                  listPrice);
            }
            if (log.isDebugEnabled()) {
                log.debug("Stock: product=" + product.getName() + " (" + product.getId()
                          + "), location=" + stockLocation.getName() + " (" + stockLocation.getId()
                          + "), supplier=" + supplier.getName() + " (" + supplier.getId()
                          + "), onHand=" + quantity + ", onOrder=" + onOrder + ", toOrder=" + toOrder
                          + ", current=" + current + ", idealQty=" + idealQty + ", criticalQty=" + criticalQty
                          + ", packageSize=" + packageSize + ", packageUnits=" + packageUnits
                          + ", order=" + (stock != null));
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("Cannot order product=" + product.getName() + " (" + product.getId()
                          + ") at location=" + stockLocation.getName() + " (" + stockLocation.getId()
                          + ") from supplier=" + supplier.getName() + " (" + supplier.getId()
                          + ") - no package size");
            }
        }
        return stock;
    }

    /**
     * Helper to return an object from a set.
     *
     * @param prefix the object reference prefix
     * @param set    the set
     * @return the corresponding object, or {@code null} if none is found
     */
    private IMObject getObject(String prefix, ObjectSet set) {
        String shortName = set.getString(prefix + "ShortName");
        if (shortName != null) {
            long id = set.getLong(prefix + "Id");
            return service.get(new IMObjectReference(new ArchetypeId(shortName), id));
        }
        return null;
    }

    /**
     * Helper to return a decimal from a set, constraining it to be {@code >= 0}.
     *
     * @param name the decimal name
     * @param set  the set
     * @return the decimal value
     */
    private BigDecimal getDecimal(String name, ObjectSet set) {
        BigDecimal result = set.getBigDecimal(name, ZERO);
        if (result.compareTo(ZERO) < 0) {
            result = ZERO;
        }
        return result;
    }

}
