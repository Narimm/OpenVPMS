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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountInvoiceItem</em>,
 * <em>act.customerAccountCreditItem</em>
 * or <em>act.customerAccountCounterItem</em>.
 *
 * @author Tim Anderson
 */
public class DefaultCustomerChargeActItemEditor extends CustomerChargeActItemEditor {

    /**
     * Constructs a {@link DefaultCustomerChargeActItemEditor}.
     *
     * @param act           the act to edit
     * @param parent        the parent act
     * @param context       the edit context
     * @param layoutContext the layout context
     */
    public DefaultCustomerChargeActItemEditor(FinancialAct act, Act parent, CustomerChargeEditContext context,
                                              LayoutContext layoutContext) {
        super(act, parent, context, layoutContext);
    }
}

