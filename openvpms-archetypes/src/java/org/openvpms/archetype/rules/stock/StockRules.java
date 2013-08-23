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

package org.openvpms.archetype.rules.stock;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActiveNow;


/**
 * Stock rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Creates a new <tt>StockRules</tt>.
     */
    public StockRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>StockRules</tt>.
     *
     * @param service the archetype service
     */
    public StockRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the stock location for a product at a practice location.
     * <p/>
     * Looks for an <em>party.organisationStockLocation</em> associated with
     * the supplied <em>party.organisationLocation</em>. If a stock location
     * exists that has a relationship to the supplied product, this will
     * be returned.
     * <p/>
     * If there are stock locations, but none have a relationship to the
     * product, then an arbitrary one will be selected.
     *
     * @param product  the product
     * @param location the practice location
     * @return the stock location, or <tt>null</tt> if none is found or
     *         the <em>stockControl</em> flag of the practice location is
     *         <tt>false</tt>
     */
    public Party getStockLocation(Product product, Party location) {
        Party result = null;
        EntityBean locBean = new EntityBean(location, service);
        if (locBean.getBoolean("stockControl")) {
            List<Entity> entities = locBean.getNodeTargetEntities(
                    "stockLocations");
            Party stockLocation = null;
            for (Entity stockLoc : entities) {
                stockLocation = (Party) stockLoc;
                if (getStockRelationship(product, stockLocation) != null) {
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
     * Returns the quantity of a product at the specified stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @return the quantity
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getStock(Product product, Party stockLocation) {
        BigDecimal result = BigDecimal.ZERO;
        EntityRelationship relationship = getStockRelationship(product,
                                                               stockLocation);
        if (relationship != null) {
            IMObjectBean bean = new IMObjectBean(relationship, service);
            result = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        }
        return result;
    }

    /**
     * Updates the stock quantity for a product at the specified stock
     * location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param quantity      the quantity to add/remove
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void updateStock(Product product, Party stockLocation,
                            BigDecimal quantity) {
        List<IMObject> toSave = calcStock(product, stockLocation, quantity);
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
    public void transferStock(Product product, Party from, Party to,
                              BigDecimal quantity) {

        List<IMObject> toSave = transfer(product, from, to, quantity);
        service.save(toSave);
    }

    /**
     * Returns the <em>entityRelationship.productStockLocation</em> for the
     * specified product and stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @return the corresponding relationship, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected EntityRelationship getStockRelationship(Product product,
                                                      Party stockLocation) {
        EntityRelationship result = null;
        EntityBean prodBean = new EntityBean(product, service);
        Predicate predicate = AndPredicate.getInstance(isActiveNow(), RefEquals.getTargetEquals(stockLocation));
        List<EntityRelationship> relationships
                = prodBean.getNodeRelationships("stockLocations",
                                                predicate);
        if (!relationships.isEmpty()) {
            result = relationships.get(0);
        }
        return result;
    }

    /**
     * Transfers a quantity of a product from one stock location to another.
     *
     * @param product  the product to transfer
     * @param from     the from location
     * @param to       the to location
     * @param quantity the quantity to transfer
     * @return the list updated objects. These must be saved to complete the
     *         transfer
     */
    protected List<IMObject> transfer(Product product, Party from, Party to,
                                      BigDecimal quantity) {
        List<IMObject> result = new ArrayList<IMObject>();
        result.addAll(calcStock(product, from, quantity.negate()));
        result.addAll(calcStock(product, to, quantity));
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
    protected List<IMObject> calcStock(Product product, Party stockLocation,
                                       BigDecimal quantity) {
        EntityBean prodBean = new EntityBean(product, service);
        EntityRelationship relationship
                = getStockRelationship(product, stockLocation);
        List<IMObject> toSave = new ArrayList<IMObject>();
        if (relationship == null) {
            relationship = prodBean.addRelationship(
                    StockArchetypes.PRODUCT_STOCK_LOCATION_RELATIONSHIP,
                    stockLocation);
            toSave.add(product);
            toSave.add(stockLocation);
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
    protected void calcStock(EntityRelationship relationship,
                             BigDecimal quantity) {
        IMObjectBean relBean = new IMObjectBean(relationship,
                                                service);
        BigDecimal old = relBean.getBigDecimal("quantity");
        BigDecimal now = old.add(quantity);
        relBean.setValue("quantity", now);
    }

}
