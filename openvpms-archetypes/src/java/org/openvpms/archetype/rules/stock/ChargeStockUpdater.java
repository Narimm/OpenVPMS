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

package org.openvpms.archetype.rules.stock;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.openvpms.archetype.rules.stock.StockArchetypes.STOCK_LOCATION_PARTICIPATION;


/**
 * Updates stock levels associated with <em>act.customerAccount*Item</em>
 * acts that have an associated <em>participation.stockLocation</em>.
 *
 * @author Tim Anderson
 */
public class ChargeStockUpdater {

    /**
     * The set of objects to save on completion.
     */
    private Set<IMObject> toSave = new LinkedHashSet<IMObject>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The stock rules.
     */
    private final StockRules rules;


    /**
     * Constructs a {@link ChargeStockUpdater}.
     *
     * @param service the service
     */
    public ChargeStockUpdater(IArchetypeService service) {
        this.service = service;
        this.rules = new StockRules(service);
    }

    /**
     * Updates stock quantities at the stock location when a charge item
     * is saved.
     *
     * @param act the charge item act
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void update(FinancialAct act) {
        try {
            updateChargeItem(act);
            if (!toSave.isEmpty()) {
                service.save(toSave);
            }
        } finally {
            toSave.clear();
        }
    }

    /**
     * Updates stock quantities at the stock location when a charge or
     * charge item is removed.
     *
     * @param act the charge or charge item
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void remove(FinancialAct act) {
        try {
            if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE,
                               CustomerAccountArchetypes.COUNTER,
                               CustomerAccountArchetypes.CREDIT)) {
                removeCharge(act);
            } else {
                removeChargeItem(act);
            }
            if (!toSave.isEmpty()) {
                service.save(toSave);
            }
        } finally {
            toSave.clear();
        }
    }

    /**
     * Updates stock quantities when a charge item is saved.
     *
     * @param act the charge item act
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateChargeItem(FinancialAct act) {
        ActBean bean = new ActBean(act, service);
        boolean credit = bean.isA(CustomerAccountArchetypes.CREDIT_ITEM);

        StockQty current = new StockQty(bean);
        StockQty prior = getSavedStockQty(act);
        BigDecimal priorQty = BigDecimal.ZERO;

        if (prior != null && prior.isValid()) {
            priorQty = prior.getQuantity();
            if (!prior.hasProduct(current)) {
                updateStockQuantities(prior, priorQty.negate(), credit);
                priorQty = BigDecimal.ZERO;
            }
        }
        BigDecimal quantity = current.getQuantity();
        if (current.isValid()) {
            if (!MathRules.equals(quantity, priorQty)) {
                BigDecimal diff = quantity.subtract(priorQty);
                updateStockQuantities(current, diff, credit);
            }
        } else {
            bean.removeParticipation(STOCK_LOCATION_PARTICIPATION);
        }
    }

    /**
     * Updates stock quantities.
     *
     * @param stock    the product and location state
     * @param quantity the quantity to add/remove
     * @param credit   determines if the act is a credit
     */
    private void updateStockQuantities(StockQty stock, BigDecimal quantity,
                                       boolean credit) {
        Party location = stock.getLocation();
        Product product = stock.getProduct();
        if (location != null && product != null
            && TypeHelper.isA(product, ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE)) {
            if (!credit) {
                quantity = quantity.negate();
            }
            EntityRelationship relationship
                    = rules.getStockRelationship(product, location);
            if (relationship == null) {
                List<IMObject> objects = rules.calcStock(product, location,
                                                         quantity);
                toSave.addAll(objects);
            } else {
                rules.calcStock(relationship, quantity);
                toSave.add(relationship);
            }
        }
    }

    /**
     * Updates stock quantities when a charge is removed.
     *
     * @param act the charge act
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void removeCharge(FinancialAct act) {
        ActBean bean = new ActBean(act, service);
        for (FinancialAct item
                : bean.getNodeActs("items", FinancialAct.class)) {
            removeChargeItem(item);
        }
    }

    /**
     * Updates stock quantities when a charge item is removed.
     *
     * @param act the charge item act
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void removeChargeItem(FinancialAct act) {
        if (!act.isNew()) {
            ActBean bean = new ActBean(act, service);
            StockQty stockQty = new StockQty(bean);
            if (stockQty.isValid()) {
                boolean credit = bean.isA(
                        CustomerAccountArchetypes.CREDIT_ITEM);
                BigDecimal quantity = stockQty.getQuantity();
                updateStockQuantities(stockQty, quantity.negate(),
                                      credit);

            }
        }
    }

    /**
     * Returns the stock quantity associated with the persistent instance of an act.
     *
     * @param act the act
     * @return the saved instance, or {@code null} if there is none
     * @throws ArchetypeServiceException for any archetype service error
     */
    private StockQty getSavedStockQty(FinancialAct act) {
        FinancialAct result = (FinancialAct) get(act.getObjectReference());
        return (result != null) ? new StockQty(result) : null;
    }

    /**
     * Retrieves an object using its reference.
     *
     * @param ref the reference. May be {@code null}
     * @return the object or tt>
     */
    private IMObject get(IMObjectReference ref) {
        if (ref != null) {
            return service.get(ref);
        }
        return null;
    }

    /**
     * Stock quantity details.
     */
    private class StockQty {

        /**
         * The stock location reference.
         */
        private IMObjectReference location;

        /**
         * The product reference.
         */
        private IMObjectReference product;

        /**
         * The quantity.
         */
        private BigDecimal quantity;


        /**
         * Creates a new {@code StockQty}.
         *
         * @param act the act
         */
        public StockQty(FinancialAct act) {
            this(new ActBean(act, service));
        }

        /**
         * Creates a new {@code StockQty}.
         *
         * @param bean the act bean
         */
        public StockQty(ActBean bean) {
            product = bean.getNodeParticipantRef("product");
            location = bean.getNodeParticipantRef("stockLocation");
            quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        }

        /**
         * Returns the product reference.
         *
         * @return the product reference. May be {@code null}
         */
        public IMObjectReference getProductRef() {
            return product;
        }

        /**
         * Returns the product.
         *
         * @return the product, or {@code null} if none is found
         * @throws ArchetypeServiceException for any archetype service error
         */
        public Product getProduct() {
            return (Product) get(product);
        }

        /**
         * Returns the stock location.
         *
         * @return the stock location, or {@code null} if none is found
         * @throws ArchetypeServiceException for any archetype service error
         */
        public Party getLocation() {
            return (Party) get(location);
        }

        /**
         * Returns the stock quantity.
         *
         * @return the stock quantity
         */
        public BigDecimal getQuantity() {
            return quantity;
        }

        /**
         * Determines if the act state is valid.
         *
         * @return {@code true} if the product and location references are
         *         non-null, and the quantity is non-zero
         */
        public boolean isValid() {
            return product != null && location != null
                   && quantity.compareTo(BigDecimal.ZERO) != 0;
        }

        /**
         * Determines if this stock quantity has the same product as another.
         *
         * @param other the other stock quantity
         * @return {@code true} if they have the same product
         */
        public boolean hasProduct(StockQty other) {
            return ObjectUtils.equals(product, other.getProductRef());
        }
    }

}

