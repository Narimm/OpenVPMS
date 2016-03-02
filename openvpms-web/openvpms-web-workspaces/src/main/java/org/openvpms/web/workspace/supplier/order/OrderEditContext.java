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

package org.openvpms.web.workspace.supplier.order;

import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.StockOnHand;

/**
 * Edit context for {@link OrderItemEditor}s, to enable them to share state.
 *
 * @author Tim Anderson
 */
public class OrderEditContext {

    /**
     * Tracks the stock on hand.
     */
    private final StockOnHand stock;

    /**
     * Constructs an {@link OrderEditContext}.
     */
    public OrderEditContext() {
        this.stock = new StockOnHand(ServiceHelper.getBean(StockRules.class));
    }

    /**
     * Returns the stock on hand.
     *
     * @return the stock on hand
     */
    public StockOnHand getStock() {
        return stock;
    }

}
