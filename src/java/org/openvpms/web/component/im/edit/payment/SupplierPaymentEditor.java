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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.payment;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.supplierAccountPayment</em> or <em>act.supplierAccountRefund</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class SupplierPaymentEditor extends PaymentEditor {

    /**
     * Constructs a new <code>SupplierPaymentEditor</code>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     */
    public SupplierPaymentEditor(Act act, IMObject parent,
                                 LayoutContext context) {
        super(act, parent, context);
        initParticipant("supplier", context.getContext().getSupplier());
        getEditor().setCreationListener(new IMObjectCreationListener() {
            public void created(IMObject object) {
                onCreated((FinancialAct) object);
            }
        });
    }

    /**
     * Invoked when a child act is created.
     * If the act is a payment, defaults the total to the outstanding balance.
     *
     * @param act the act
     */
    private void onCreated(FinancialAct act) {
        if (TypeHelper.isA(act, "act.supplierAccountPayment*")) {
            // Default the amount to the outstanding balance
            Party supplier = (Party) getParticipant("supplier");
            if (supplier != null) {
                FinancialAct parent = (FinancialAct) getObject();
                BigDecimal diff = ActHelper.sum(parent, "amount");
                BigDecimal current = ActHelper.getSupplierAccountBalance(supplier);
                BigDecimal balance = current.subtract(diff);
                act.setTotal(new Money(balance));
                getEditor().setModified(act, true);
            }
        }
    }

}
