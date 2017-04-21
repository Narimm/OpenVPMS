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

package org.openvpms.archetype.rules.finance.order;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

/**
 * Manages state when processing customer pharmacy orders and returns.
 *
 * @author Tim Anderson
 */
public class CustomerPharmacyOrder extends CustomerOrder {

    /**
     * Constructs a {@link CustomerPharmacyOrder}.
     *
     * @param patient  the patient. May be {@code null}
     * @param customer the customer. May be {@code null}
     * @param note     the note. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @param service  the archetype service
     */
    public CustomerPharmacyOrder(Party patient, Party customer, String note, IMObjectReference location,
                                 IArchetypeService service) {
        super(patient, customer, note, location, service);
    }

    /**
     * Constructs a {@link CustomerPharmacyOrder}.
     *
     * @param act     the order/order return act
     * @param service the archetype service
     */
    public CustomerPharmacyOrder(Act act, IArchetypeService service) {
        super(act, TypeHelper.isA(act, OrderArchetypes.PHARMACY_ORDER), service);
    }

    /**
     * Returns the first item with the given product.
     *
     * @param product the product. May be {@code null}
     * @return the item. May be {@code null}
     */
    public ActBean getItem(Product product) {
        String archetype = hasOrder() ? OrderArchetypes.PHARMACY_ORDER_ITEM : OrderArchetypes.PHARMACY_RETURN_ITEM;
        return getItem(archetype, product);
    }

    /**
     * Creates a new order item.
     *
     * @return a new order item
     */
    @Override
    public ActBean createOrderItem() {
        return createItem(OrderArchetypes.PHARMACY_ORDER_ITEM, getOrder());
    }

    /**
     * Creates a new order return item.
     *
     * @return a new order return item
     */
    @Override
    public ActBean createReturnItem() {
        return createItem(OrderArchetypes.PHARMACY_RETURN_ITEM, getReturn());
    }

    /**
     * Creates a new order.
     *
     * @return the order
     */
    @Override
    protected ActBean createOrder() {
        return createParent(OrderArchetypes.PHARMACY_ORDER);
    }

    /**
     * Creates a new order return.
     *
     * @return the return
     */
    @Override
    protected ActBean createReturn() {
        return createParent(OrderArchetypes.PHARMACY_RETURN);
    }

}
