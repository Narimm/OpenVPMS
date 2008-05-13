/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.ActStatusHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Updates stock levels associated with <em>act.customerAccountCharge*</em>
 * acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockUpdater {

    /**
     * The <em>act.customerAccountCharges*</em> act.
     */
    private final FinancialAct act;

    /**
     * The set of objects to save on completion.
     */
    private Set<IMObject> toSave = new LinkedHashSet<IMObject>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Creates a new <tt>StockUpdater</tt>.
     *
     * @param act     the charge act
     * @param service the service
     */
    public StockUpdater(FinancialAct act, IArchetypeService service) {
        this.act = act;
        this.service = service;
    }

    /**
     * Updates stock quantities, if the act is POSTED and hasn't already been
     * posted.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void update() {
        if (ActStatus.POSTED.equals(act.getStatus())
                && !ActStatusHelper.isPosted(act, service)) {
            ActBean bean = new ActBean(act, service);
            Party location = (Party) bean.getNodeParticipant("location");
            if (location != null) {
                for (FinancialAct item : bean.getNodeActs("items",
                                                          FinancialAct.class)) {
                    update(item, location);
                }
                if (!toSave.isEmpty()) {
                    service.save(toSave);
                }
            }
        }
    }

    /**
     * Updates product quantities at the location used by the charge item.
     *
     * @param item     the charge item
     * @param location an <em>party.organisationLocation</em>
     */
    private void update(FinancialAct item, Party location) {
        BigDecimal quantity = item.getQuantity();
        ActBean itemBean = new ActBean(item, service);
        Product product = (Product) itemBean.getNodeParticipant("product");
        if (product != null && quantity.compareTo(BigDecimal.ZERO) != 0) {
            if (!act.isCredit()) {
                quantity = quantity.negate();
            }
            updateProductStockLocation(product, location, quantity);
        }
    }

    /**
     * Updates product quantities at the location.
     * <p/>
     * Looks for an <em>party.organisationStockLocation</em> associated with
     * the supplied <em>party.organisationLocation</em>. If a stock location
     * exists that already has a relationship to the supplied product, this will
     * be updated.
     * <p/>If there are stock locations, but none have a relationship
     * to the product, then an arbitrary one will be selected, and a
     * relationship created (todo - not ideal).
     * <p/>If there are no stock locations associated with the location, the
     * update is ignored.
     *
     * @param product  the product
     * @param location an <em>party.organisationLocation</em>
     * @param quantity the quantity used/returned. Negative if quantities are
     *                 being used
     */
    private void updateProductStockLocation(Product product,
                                            Party location,
                                            BigDecimal quantity) {
        EntityBean prodBean = new EntityBean(product, service);
        EntityBean locBean = new EntityBean(location, service);
        List<Entity> entities = locBean.getNodeTargetEntities("stockLocations");
        EntityRelationship productStockLocation = null;
        Party stockLocation = null;
        for (Entity stockLoc : entities) {
            stockLocation = (Party) stockLoc;
            Predicate predicate = AndPredicate.getInstance(
                    IsActiveRelationship.ACTIVE_NOW,
                    RefEquals.getTargetEquals(stockLocation));
            List<EntityRelationship> relationships
                    = prodBean.getNodeRelationships("stockLocations",
                                                    predicate);
            if (!relationships.isEmpty()) {
                productStockLocation = relationships.get(0);
                break;
            }
        }
        if (stockLocation != null) {
            if (productStockLocation == null) {
                productStockLocation = prodBean.addRelationship(
                        "entityRelationship.productStockLocation",
                        stockLocation);
                toSave.add(product);
                toSave.add(stockLocation);
            } else {
                toSave.add(productStockLocation);
            }
            IMObjectBean relBean = new IMObjectBean(productStockLocation,
                                                    service);
            BigDecimal old = relBean.getBigDecimal("quantity");
            BigDecimal now = old.add(quantity);
            relBean.setValue("quantity", now);
        }
    }

}
