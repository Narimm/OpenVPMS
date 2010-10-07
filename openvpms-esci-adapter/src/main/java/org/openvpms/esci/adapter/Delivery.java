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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Contains the results of mapping an UBL Invoice to an <em>act.supplierDelivery</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Delivery {

    private IMObjectReference order;

    private FinancialAct delivery;

    private List<FinancialAct> deliveryItems = new ArrayList<FinancialAct>();

    public void setOrder(IMObjectReference reference) {
        this.order = reference;
    }

    public IMObjectReference getOrder() {
        return order;
    }

    public void setDelivery(FinancialAct delivery) {
        this.delivery = delivery;
    }

    public FinancialAct getDelivery() {
        return delivery;
    }

    public void addDeliveryItem(FinancialAct item) {
        deliveryItems.add(item);
    }

    public void setDeliveryItems(List<FinancialAct> items) {
        this.deliveryItems = items;
    }

    public void addDeliveryToOrderMapping(IMObjectReference deliveryItem, IMObjectReference orderItem) {

    }

    public List<FinancialAct> getDeliveryItems() {
        return deliveryItems;
    }

    public List<FinancialAct> getActs() {
        List<FinancialAct> acts = new ArrayList<FinancialAct>();
        acts.add(delivery);
        acts.addAll(deliveryItems);
        return acts;
    }
}
