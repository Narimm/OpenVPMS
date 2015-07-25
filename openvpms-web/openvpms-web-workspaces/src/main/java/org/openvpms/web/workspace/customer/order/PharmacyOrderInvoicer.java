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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.order;

import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.FinancialAct;

/**
 * This class is responsible for creating charges from <em>act.customerOrderPharmacy</em> and
 * <em>act.customerReturnPharmacy</em> acts.
 * <p/>
 * NOTE that there is limited support to charge orders and returns when the existing invoice has been POSTED.
 * <p/>
 * In this case, the difference will be charged. This does not take into account multiple orders/returns for the one
 * POSTED invoice.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderInvoicer extends OrderInvoicer {

    /**
     * Constructs a {@link PharmacyOrderInvoicer}.
     *
     * @param act   the order/return act
     * @param rules the order rules
     */
    public PharmacyOrderInvoicer(FinancialAct act, OrderRules rules) {
        super(act, rules);
    }

}
