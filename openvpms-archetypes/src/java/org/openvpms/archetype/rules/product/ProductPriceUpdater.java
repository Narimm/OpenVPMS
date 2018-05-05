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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.apache.commons.collections.Transformer;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.CurrencyException;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.archetype.rules.product.ProductPriceUpdaterException.ErrorCode.NoPractice;


/**
 * Updates <em>productPrice.unitPrice</em>s associated with a product.
 *
 * @author Tim Anderson
 */
public class ProductPriceUpdater {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The practice currency.
     */
    private Currency currency;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The product price rules.
     */
    private final ProductPriceRules rules;

    /**
     * The practice rules
     */
    private final PracticeRules practiceRules;

    /**
     * Constructs a {@link ProductPriceUpdater}.
     *
     * @param priceRules    the product price rules
     * @param practiceRules the practice rules
     * @param service       the archetype service
     */
    public ProductPriceUpdater(ProductPriceRules priceRules, PracticeRules practiceRules, IArchetypeService service) {
        this.service = service;
        this.rules = priceRules;
        this.practiceRules = practiceRules;
    }

    /**
     * Updates any <em>productPrice.unitPrice</em> product prices associated with a product.
     *
     * @param product the product
     * @return a list of updated prices
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     */
    public List<ProductPrice> update(Product product) {
        return update(product, true);
    }

    /**
     * Updates any <em>productPrice.unitPrice</em> product prices associated with a product.
     *
     * @param product the product
     * @param save    if {@code true}, save updated prices
     * @return a list of updated prices
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     */
    public List<ProductPrice> update(final Product product, boolean save) {
        List<ProductPrice> result = Collections.emptyList();
        if (needsUpdate(product)) {
            EntityBean bean = new EntityBean(product, service);
            List<EntityLink> relationships = bean.getValues("suppliers", EntityLink.class);
            Transformer transformer = new Transformer() {
                public Object transform(Object object) {
                    ProductSupplier ps = new ProductSupplier((EntityLink) object, service);
                    return update(product, ps, false);
                }
            };
            result = collect(relationships, transformer, save);
        }
        return result;
    }

    /**
     * Updates an <em>productPrice.unitPrice</em> product prices associated with products for the specified supplier.
     *
     * @param supplier the supplier
     * @return a list of updated prices
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     */
    public List<ProductPrice> update(Party supplier) {
        return update(supplier, true);
    }

    /**
     * Updates an <em>productPrice.unitPrice</em> product prices associated with products for the specified supplier.
     *
     * @param supplier the supplier
     * @param save     if {@code true}, save updated prices
     * @return a list of updated prices
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     */
    public List<ProductPrice> update(Party supplier, boolean save) {
        EntityBean bean = new EntityBean(supplier, service);
        List<EntityLink> products = bean.getValues("products", EntityLink.class);
        Transformer transformer = new Transformer() {
            public Object transform(Object object) {
                ProductSupplier ps = new ProductSupplier((EntityRelationship) object, service);
                return update(ps, false);
            }
        };
        return collect(products, transformer, save);
    }

    /**
     * Updates any <em>productPrice.unitPrice</em> product prices associated with a product.
     *
     * @param product         the product
     * @param productSupplier the product-supplier relationship
     * @param save            if {@code true}, save updated prices
     * @return a list of updated prices
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     * @throws IllegalArgumentException     if the product is not that referred to by the product-supplier
     *                                      relationship
     */
    public List<ProductPrice> update(Product product, ProductSupplier productSupplier, boolean save) {
        return update(product, productSupplier, false, save);
    }

    /**
     * Updates any <em>productPrice.unitPrice</em> product prices associated with a product.
     *
     * @param product            the product
     * @param productSupplier    the product-supplier relationship
     * @param ignoreCostDecrease if {@code true}, don't update any unit price if the new cost price would be less than
     *                           the existing cost price
     * @param save               if {@code true}, save updated prices
     * @return a list of updated prices
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     * @throws IllegalArgumentException     if the product is not that referred to by the product-supplier
     *                                      relationship
     */
    public List<ProductPrice> update(Product product, ProductSupplier productSupplier, boolean ignoreCostDecrease,
                                     boolean save) {
        if (!product.getObjectReference().equals(productSupplier.getRelationship().getSource())) {
            throw new IllegalArgumentException("Argument 'product' is not that referred to by 'productSupplier'");
        }
        List<ProductPrice> result;
        if (canUpdate(productSupplier)) {
            result = doUpdate(productSupplier, product, ignoreCostDecrease, save);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Updates any <em>productPrice.unitPrice</em> product prices associated with a product.
     *
     * @param productSupplier the product-supplier relationship
     * @param save            if {@code true}, save updated prices
     * @return a list of updated prices
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     */
    public List<ProductPrice> update(ProductSupplier productSupplier, boolean save) {
        List<ProductPrice> result = Collections.emptyList();
        if (canUpdate(productSupplier)) {
            Product product = productSupplier.getProduct();
            if (product != null) {
                result = doUpdate(productSupplier, product, false, save);
            }
        }
        return result;
    }

    /**
     * Returns the practice.
     *
     * @return the practice
     * @throws ArchetypeServiceException    for any archetype service error
     * @throws ProductPriceUpdaterException if there is no practice
     */
    public Party getPractice() {
        if (practice == null) {
            practice = practiceRules.getPractice();
            if (practice == null) {
                throw new ProductPriceUpdaterException(NoPractice);
            }
        }
        return practice;
    }

    /**
     * Determines if the prices associated with a product should be updated.
     *
     * @param product the product
     * @return {@code true} if prices should be updated
     */
    private boolean needsUpdate(Product product) {
        boolean update = true;
        if (!product.isNew()) {
            Product prior = (Product) service.get(product.getObjectReference());
            if (prior != null) {
                Set<EntityLink> oldSuppliers = getProductSuppliers(prior);
                Set<EntityLink> newSuppliers = getProductSuppliers(product);
                if (oldSuppliers.equals(newSuppliers)) {
                    update = !checkEquals(oldSuppliers, newSuppliers);
                }
            }
        }
        return update;
    }

    /**
     * Determines if the supplier relationships are equal based on list price and package size.
     *
     * @param oldSuppliers the old supplier relationships
     * @param newSuppliers the new supplier relationships
     * @return {@code true} if they are equal
     */
    private boolean checkEquals(Set<EntityLink> oldSuppliers, Set<EntityLink> newSuppliers) {
        Map<IMObjectReference, ProductSupplier> oldMap = getProductSuppliers(oldSuppliers);
        Map<IMObjectReference, ProductSupplier> newMap = getProductSuppliers(newSuppliers);
        for (Map.Entry<IMObjectReference, ProductSupplier> entry : newMap.entrySet()) {
            ProductSupplier supplier = entry.getValue();
            ProductSupplier old = oldMap.get(entry.getKey());
            if (old == null || !MathRules.equals(supplier.getListPrice(), old.getListPrice())
                || supplier.getPackageSize() != old.getPackageSize()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the product suppliers for a set of <em>entityLink.productSupplier</em> relationships.
     *
     * @param suppliers the product supplier relationships
     * @return the product suppliers, keyed on supplier reference.
     */
    private Map<IMObjectReference, ProductSupplier> getProductSuppliers(Set<EntityLink> suppliers) {
        Map<IMObjectReference, ProductSupplier> result = new HashMap<>();
        for (EntityLink supplier : suppliers) {
            result.put(supplier.getObjectReference(), new ProductSupplier(supplier, service));
        }
        return result;
    }

    /**
     * Determines if prices can be updated.
     * <p/>
     * Prices can be updated if:
     * <ul>
     * <li>autoPriceUpdate is {@code true}; and</li>
     * <li>costPrice &lt;&gt; 0; and</li>
     * <li>the supplier is active</li>
     * </ul>
     *
     * @param productSupplier the product-supplier relationship
     * @return {@code true} if prices can be updated
     */
    private boolean canUpdate(ProductSupplier productSupplier) {
        return productSupplier.isAutoPriceUpdate()
               && !MathRules.isZero(productSupplier.getCostPrice())
               && isActive(productSupplier.getSupplierRef());
    }

    /**
     * Updates prices.
     *
     * @param productSupplier    the product-supplier relationship
     * @param product            the product
     * @param ignoreCostDecrease if {@code true}, don't update any unit price if the new cost price would be less than
     *                           the existing cost price
     * @param save               if {@code true}, save updated prices, otherwise derive values
     * @return a list of updated prices
     */
    private List<ProductPrice> doUpdate(ProductSupplier productSupplier, Product product,
                                        boolean ignoreCostDecrease, boolean save) {
        List<ProductPrice> result;
        BigDecimal cost = productSupplier.getCostPrice();
        result = rules.updateUnitPrices(product, cost, ignoreCostDecrease, getCurrency());
        if (!result.isEmpty()) {
            if (save) {
                service.save(result);
            } else {
                for (ProductPrice price : result) {
                    service.deriveValues(price);
                }
            }
        }
        return result;
    }

    /**
     * Collects product prices from a list of product-supplier relationships, updated by the specified transformer.
     *
     * @param relationships the product-supplier relationships
     * @param transformer   returns the updated product prices associated with each relationship
     * @param save          if {@code true}, save updated prices
     * @return a list of updated prices
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    private List<ProductPrice> collect(List<EntityLink> relationships, Transformer transformer, boolean save) {
        List<ProductPrice> result = null;
        for (EntityLink relationship : relationships) {
            List<ProductPrice> prices = (List<ProductPrice>) transformer.transform(relationship);
            if (!prices.isEmpty()) {
                if (result == null) {
                    result = prices;
                } else {
                    result.addAll(prices);
                }
            }
        }
        if (result == null) {
            result = Collections.emptyList();
        } else if (save) {
            service.save(result);
        }
        return result;
    }

    /**
     * Returns the currency associated with the organisation practice.
     *
     * @return the currency
     * @throws ProductPriceUpdaterException if there is no practice
     * @throws CurrencyException            if the currency is invalid
     */
    private Currency getCurrency() {
        if (currency == null) {
            currency = practiceRules.getCurrency(getPractice());
        }
        return currency;
    }

    /**
     * Helper to determine if an object is active.
     * <p/>
     * This assumes that references to new objects that cannot be retrieved (i.e aren't yet in the transaction
     * context) are always active.
     * <p/>
     * It queries the database for persistent references to avoid pulling in large objects.
     *
     * @param reference the object's reference
     * @return {@code true} if the object is active
     */
    private boolean isActive(IMObjectReference reference) {
        boolean result = false;
        if (reference != null) {
            if (reference.isNew()) {
                IMObject object = service.get(reference);
                result = (object == null) || object.isActive();
            } else {
                ObjectRefConstraint constraint = new ObjectRefConstraint("o", reference);
                ArchetypeQuery query = new ArchetypeQuery(constraint);
                query.add(new NodeSelectConstraint("o.active"));
                query.setMaxResults(1);
                Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
                if (iter.hasNext()) {
                    ObjectSet set = iter.next();
                    result = set.getBoolean("o.active");
                }
            }
        }

        return result;
    }

    private Set<EntityLink> getProductSuppliers(Product product) {
        EntityBean bean = new EntityBean(product, service);
        return new HashSet<>(bean.getValues("suppliers", EntityLink.class));
    }
}
