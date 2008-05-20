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

package org.openvpms.archetype.rules.stock;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.ActStatusHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * Updates stock levels associated with <em>act.stockTransfer</em>,
 * <em>act.stockAdjust</em> and <em>act.customerAccountCharge*</em>
 * acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockUpdater {

    /**
     * The <em>act.customerAccountCharges*</em> act.
     */
    private final Act act;

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
     * Creates a new <tt>StockUpdater</tt>.
     *
     * @param act     the act
     * @param service the service
     */
    public StockUpdater(Act act, IArchetypeService service) {
        this.act = act;
        this.service = service;
        this.rules = new StockRules(service);
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
            if (TypeHelper.isA(act, StockArchetypes.STOCK_TRANSFER)) {
                transferStock();
            } else if (TypeHelper.isA(act, StockArchetypes.STOCK_ADJUST)) {
                adjustStock();
            } else {
                updateChargeStock();
            }
            if (!toSave.isEmpty()) {
                service.save(toSave);
            }
        }
    }

    /**
     * Transfers stock using an <em>act.stockTransfer</em> act.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void transferStock() {
        ActBean bean = new ActBean(act, service);
        Party from = (Party) bean.getNodeParticipant("stockLocation");
        Party to = (Party) bean.getNodeParticipant("to");
        if (from != null && to != null) {
            for (Act item : bean.getNodeActs("items")) {
                toSave.addAll(transferStock(item, from, to));
            }
        }
    }

    /**
     * Transfers a quantity of a product from one stock location to another.
     *
     * @param item the <em>act.stockTransferItem</em>
     * @param from the from location
     * @param to   the to location
     * @return the list updated objects
     * @throws ArchetypeServiceException for any archetype service error
     */
    private List<IMObject> transferStock(Act item, Party from, Party to) {
        List<IMObject> result;
        ActBean itemBean = new ActBean(item, service);
        Product product = (Product) itemBean.getNodeParticipant("product");
        BigDecimal quantity = itemBean.getBigDecimal("quantity",
                                                     BigDecimal.ZERO);
        if (product != null && quantity.compareTo(BigDecimal.ZERO) != 0) {
            result = rules.transfer(product, from, to, quantity);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Adjusts stock using an <em>act.stockTransfer</em>.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void adjustStock() {
        ActBean bean = new ActBean(act, service);
        Party stockLocation = (Party) bean.getNodeParticipant("stockLocation");
        if (stockLocation != null) {
            for (Act item : bean.getNodeActs("items")) {
                adjustStock(item, stockLocation);
            }
        }
    }

    /**
     * Adjusts stock using an <em>act.stockAdjustItem</em>.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void adjustStock(Act item, Party stockLocation) {
        ActBean itemBean = new ActBean(item, service);
        Product product = (Product) itemBean.getNodeParticipant("product");
        BigDecimal quantity = itemBean.getBigDecimal("quantity",
                                                     BigDecimal.ZERO);
        if (product != null && quantity.compareTo(BigDecimal.ZERO) != 0) {
            toSave.addAll(rules.calcStock(product, stockLocation, quantity));
        }
    }

    /**
     * Updates stocks used by an <em>act.customerAccountCharge*</em> act.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateChargeStock() {
        ActBean bean = new ActBean(act, service);
        Party location = (Party) bean.getNodeParticipant("location");
        if (location != null) {
            for (FinancialAct item
                    : bean.getNodeActs("items", FinancialAct.class)) {
                updateChargeItemStock(item, location);
            }
        }
    }

    /**
     * Updates stock quantities at the location used by the charge item,
     * if the associated product is a <em>product.medication</em>
     * or <em>product.merchandise</em>.
     *
     * @param item     the charge item
     * @param location an <em>party.organisationLocation</em>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateChargeItemStock(FinancialAct item, Party location) {
        BigDecimal quantity = item.getQuantity();
        if (quantity.compareTo(BigDecimal.ZERO) != 0) {
            ActBean itemBean = new ActBean(item, service);
            Product product = (Product) itemBean.getNodeParticipant("product");
            if (TypeHelper.isA(product, ProductArchetypes.MEDICATION,
                               ProductArchetypes.MERCHANDISE)) {
                if (!((FinancialAct) act).isCredit()) {
                    quantity = quantity.negate();
                }
                updateProductStockLocation(product, location, quantity);
            }
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
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateProductStockLocation(Product product, Party location,
                                            BigDecimal quantity) {
        EntityBean locBean = new EntityBean(location, service);
        List<Entity> entities = locBean.getNodeTargetEntities("stockLocations");
        EntityRelationship relationship = null;
        Party stockLocation = null;
        for (Entity stockLoc : entities) {
            stockLocation = (Party) stockLoc;
            relationship = rules.getStockRelationship(product, stockLocation);
            if (relationship != null) {
                break;
            }
        }
        if (stockLocation != null) {
            if (relationship == null) {
                List<IMObject> objects = rules.calcStock(product, stockLocation,
                                                         quantity);
                toSave.addAll(objects);
            } else {
                rules.calcStock(relationship, quantity);
                toSave.add(relationship);
            }
        }
    }

}
