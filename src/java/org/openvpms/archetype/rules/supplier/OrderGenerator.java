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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                                            "quantity", "idealQty",
                                                            "criticalQty", "packageSize", "packageUnits",
                                                            "reorderCode", "reorderDesc",
                                                            "nettPrice", "listPrice", "orderedQty", "receivedQty",
                                                            "cancelledQty", "orderPackageSize");

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OrderGenerator.class);

    /**
     * Constructs a {@code OrderGenerator}.
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
        List<Stock> result = new ArrayList<Stock>();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("stockLocationId", stockLocation.getId());
        parameters.put("supplierId", supplier.getId());
        NamedQuery query = new NamedQuery("getStockToOrderByStockLocationAndSupplier", NAMES, parameters);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            Product product = (Product) getObject("product", set);
            if (product != null) {
                BigDecimal quantity = set.getBigDecimal("quantity", BigDecimal.ZERO);
                BigDecimal idealQty = set.getBigDecimal("idealQty", BigDecimal.ZERO);
                BigDecimal criticalQty = set.getBigDecimal("criticalQty", BigDecimal.ZERO);
                int packageSize = set.getInt("packageSize");
                String packageUnits = set.getString("packageUnits");
                String reorderCode = set.getString("reorderCode");
                String reorderDesc = set.getString("reorderDesc");
                BigDecimal nettPrice = set.getBigDecimal("nettPrice", BigDecimal.ZERO);
                BigDecimal listPrice = set.getBigDecimal("listPrice", BigDecimal.ZERO);
                BigDecimal orderedQty = set.getBigDecimal("orderedQty", BigDecimal.ZERO);
                BigDecimal receivedQty = set.getBigDecimal("receivedQty", BigDecimal.ZERO);
                BigDecimal cancelledQty = set.getBigDecimal("cancelledQty", BigDecimal.ZERO);
                int orderPackageSize = set.getInt("orderPackageSize");
                int size = (packageSize != 0) ? packageSize : orderPackageSize;
                if (size != 0) {
                    BigDecimal onOrder = orderedQty.subtract(receivedQty).subtract(cancelledQty).multiply(
                            BigDecimal.valueOf(size));

                    BigDecimal current = quantity.add(onOrder); // the on-hand and on-order stock
                    BigDecimal toOrder = MathRules.divide(idealQty.subtract(current), size, 0);

                    if (log.isDebugEnabled()) {
                        log.debug("Stock: product=" + product.getName() + " (" + product.getId()
                                  + "), location=" + stockLocation.getName() + " (" + stockLocation.getId()
                                  + "), supplier=" + supplier.getName() + " (" + supplier.getId()
                                  + "), onHand=" + quantity + ", onOrder=" + onOrder + ", toOrder=" + toOrder
                                  + ", idealQty=" + idealQty + ", criticalQty=" + criticalQty);
                    }
                    if ((belowIdealQuantity && current.compareTo(idealQty) <= 0
                         || (current.compareTo(criticalQty) <= 0)) && !MathRules.equals(BigDecimal.ZERO, toOrder)) {
                        result.add(new Stock(product, stockLocation, supplier, quantity, idealQty, onOrder, toOrder,
                                             reorderCode, reorderDesc, size, packageUnits, nettPrice, listPrice));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Cannot order product=" + product.getName() + " (" + product.getId()
                                  + ") at location=" + stockLocation.getName() + " (" + stockLocation.getId()
                                  + ") from supplier=" + supplier.getName() + " (" + supplier.getId()
                                  + ") - no package size");
                    }
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
        List<FinancialAct> result = new ArrayList<FinancialAct>();
        List<Stock> toOrder = getOrderableStock(supplier, stockLocation, belowIdealQuantity);
        if (!toOrder.isEmpty()) {
            FinancialAct order = (FinancialAct) service.create(SupplierArchetypes.ORDER);
            result.add(order);
            ActBean bean = new ActBean(order, service);
            bean.addNodeParticipation("supplier", supplier);
            bean.addNodeParticipation("stockLocation", stockLocation);
            List<FinancialAct> items = new ArrayList<FinancialAct>();

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

}
