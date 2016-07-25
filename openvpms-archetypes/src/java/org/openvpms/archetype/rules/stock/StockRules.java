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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.stock;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActiveNow;


/**
 * Stock rules.
 *
 * @author Tim Anderson
 */
public class StockRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link StockRules}.
     *
     * @param service the archetype service
     */
    public StockRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the stock location for a product at a practice location.
     * <p/>
     * Looks for an <em>party.organisationStockLocation</em> associated with the supplied
     * <em>party.organisationLocation</em>. If a stock location exists that has a relationship to the supplied product,
     * this will be returned.
     * <p/>
     * If there are stock locations, but none have a relationship to the
     * product, then an arbitrary one will be selected.
     *
     * @param product  the product
     * @param location the practice location
     * @return the stock location, or {@code null} if none is found or the <em>stockControl</em> flag of the practice
     * location is {@code false}
     */
    public Party getStockLocation(Product product, Party location) {
        Party result = null;
        EntityBean locBean = new EntityBean(location, service);
        if (locBean.getBoolean("stockControl")) {
            List<Entity> entities = locBean.getNodeTargetEntities("stockLocations");
            Party stockLocation = null;
            for (Entity stockLoc : entities) {
                stockLocation = (Party) stockLoc;
                if (getStockRelationship(product, stockLocation.getObjectReference()) != null) {
                    result = stockLocation;
                    break;
                }
            }
            if (result == null) {
                result = stockLocation;
            }
        }
        return result;
    }

    /**
     * Returns the stock for a product and stock location.
     * <p/>
     * NOTE: this implementation queries persistent values.
     *
     * @param product       the product reference
     * @param stockLocation the stock location
     * @return the stock
     */
    public BigDecimal getStock(IMObjectReference product, IMObjectReference stockLocation) {
        BigDecimal result = BigDecimal.ZERO;
        ArchetypeQuery q = new ArchetypeQuery("entityLink.productStockLocation", false, false);
        q.add(Constraints.eq("source", product));
        q.add(Constraints.eq("target", stockLocation));
        q.add(Constraints.sort("id"));  // shouldn't be required as there should only ever be one
        q.setMaxResults(1);
        IMObjectQueryIterator<IMObject> iterator = new IMObjectQueryIterator<>(service, q);
        if (iterator.hasNext()) {
            IMObjectBean bean = new IMObjectBean(iterator.next());
            result = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        }
        return result;
    }

    /**
     * Returns the quantity of a product at the specified stock location.
     * <p/>
     * NOTE: this implementation returns cached values.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @return the quantity
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getStock(Product product, Party stockLocation) {
        BigDecimal result = BigDecimal.ZERO;
        IMObjectRelationship relationship = getStockRelationship(product, stockLocation.getObjectReference());
        if (relationship != null) {
            IMObjectBean bean = new IMObjectBean(relationship, service);
            result = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        }
        return result;
    }

    /**
     * Determines if a product has a relationship with the specified stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @return {@code true} if there is ar relationship
     */
    public boolean hasStockRelationship(Product product, Party stockLocation) {
        return getStockRelationship(product, stockLocation.getObjectReference()) != null;
    }

    /**
     * Updates the stock quantity for a product at the specified stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param quantity      the quantity to add/remove
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void updateStock(Product product, Party stockLocation, BigDecimal quantity) {
        List<IMObject> toSave = calcStock(product, stockLocation.getObjectReference(), quantity);
        service.save(toSave);
    }

    /**
     * Transfers a quantity of a product from one stock location to another.
     *
     * @param product  the product to transfer
     * @param from     the from location
     * @param to       the to location
     * @param quantity the quantity to transfer
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void transferStock(Product product, Party from, Party to, BigDecimal quantity) {
        List<IMObject> toSave = transfer(product, from, to, quantity);
        service.save(toSave);
    }

    /**
     * Returns the <em>entityLink.productStockLocation</em> for the
     * specified product and stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @return the corresponding relationship, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObjectRelationship getStockRelationship(Product product, IMObjectReference stockLocation) {
        EntityBean prodBean = new EntityBean(product, service);
        Predicate predicate = AndPredicate.getInstance(isActiveNow(), RefEquals.getTargetEquals(stockLocation));
        return (IMObjectRelationship) prodBean.getValue("stockLocations", predicate);
    }

    /**
     * Transfers a quantity of a product from one stock location to another.
     *
     * @param product  the product to transfer
     * @param from     the from location
     * @param to       the to location
     * @param quantity the quantity to transfer
     * @return the list updated objects. These must be saved to complete the transfer
     */
    protected List<IMObject> transfer(Product product, Party from, Party to, BigDecimal quantity) {
        List<IMObject> result = new ArrayList<>();
        result.addAll(calcStock(product, from.getObjectReference(), quantity.negate()));
        result.addAll(calcStock(product, to.getObjectReference(), quantity));
        return result;
    }

    /**
     * Calculates the stock quantity.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param quantity      the quantity to add/remove
     * @return the list of updated objects
     */
    protected List<IMObject> calcStock(Product product, IMObjectReference stockLocation, BigDecimal quantity) {
        EntityBean prodBean = new EntityBean(product, service);
        IMObjectRelationship relationship = getStockRelationship(product, stockLocation);
        List<IMObject> toSave = new ArrayList<>();
        if (relationship == null) {
            relationship = prodBean.addNodeTarget("stockLocations", stockLocation);
            toSave.add(product);
        } else {
            toSave.add(relationship);
        }
        calcStock(relationship, quantity);
        return toSave;
    }

    /**
     * Calculates the stock quantity.
     *
     * @param relationship the product-stock location relationship
     * @param quantity     the quantity to add/remove
     */
    protected void calcStock(IMObjectRelationship relationship, BigDecimal quantity) {
        IMObjectBean relBean = new IMObjectBean(relationship, service);
        BigDecimal old = relBean.getBigDecimal("quantity");
        BigDecimal now = old.add(quantity);
        relBean.setValue("quantity", now);
    }

}
