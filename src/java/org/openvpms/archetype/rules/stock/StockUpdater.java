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
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Updates stock levels associated with <em>act.stockTransfer</em>, and <em>act.stockAdjust</em> acts.
 * acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockUpdater {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The stock rules.
     */
    private final StockRules rules;


    /**
     * Constructs a {@code StockUpdater}.
     *
     * @param service the service
     */
    public StockUpdater(IArchetypeService service) {
        this.service = service;
        this.rules = new StockRules(service);
    }

    /**
     * Updates stock quantities, if the act is POSTED and hasn't already been posted.
     *
     * @param act the act to update stock quantities
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void update(Act act) {
        if (ActStatus.POSTED.equals(act.getStatus())) {
            Set<IMObject> toSave = null;
            if (TypeHelper.isA(act, StockArchetypes.STOCK_TRANSFER)) {
                toSave = transferStock(act);
            } else if (TypeHelper.isA(act, StockArchetypes.STOCK_ADJUST)) {
                toSave = adjustStock(act);
            }
            if (toSave != null && !toSave.isEmpty()) {
                service.save(toSave);
            }
        }
    }

    /**
     * Transfers stock using an <em>act.stockTransfer</em> act.
     *
     * @param act the <em>act.stockTransfer</em>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Set<IMObject> transferStock(Act act) {
        Set<IMObject> toSave = new HashSet<IMObject>();
        ActBean bean = new ActBean(act, service);
        Party from = (Party) bean.getNodeParticipant("stockLocation");
        Party to = (Party) bean.getNodeParticipant("to");
        if (from != null && to != null) {
            for (Act item : bean.getNodeActs("items")) {
                toSave.addAll(transferStock(item, from, to));
            }
        }
        return toSave;
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
     * @param act the <em>act.stockTransfer</em> act
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Set<IMObject> adjustStock(Act act) {
        Set<IMObject> toSave = new HashSet<IMObject>();
        ActBean bean = new ActBean(act, service);
        Party stockLocation = (Party) bean.getNodeParticipant("stockLocation");
        if (stockLocation != null) {
            for (Act item : bean.getNodeActs("items")) {
                toSave.addAll(adjustStock(item, stockLocation));
            }
        }
        return toSave;
    }

    /**
     * Adjusts stock using an <em>act.stockAdjustItem</em>.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Set<IMObject> adjustStock(Act item, Party stockLocation) {
        Set<IMObject> toSave = new HashSet<IMObject>();
        ActBean itemBean = new ActBean(item, service);
        Product product = (Product) itemBean.getNodeParticipant("product");
        BigDecimal quantity = itemBean.getBigDecimal("quantity",
                                                     BigDecimal.ZERO);
        if (product != null && quantity.compareTo(BigDecimal.ZERO) != 0) {
            toSave.addAll(rules.calcStock(product, stockLocation, quantity));
        }
        return toSave;
    }

}
