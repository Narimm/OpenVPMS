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

package org.openvpms.web.app.workflow.checkout;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.payment.CustomerPaymentEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.workflow.EditAccountActTask;
import org.openvpms.web.component.workflow.TaskContext;

import java.math.BigDecimal;


/**
 * Task to edit {@link Act}s which have an archetype of
 * <em>act.customerAccountPayment</em> or <em>act.customerAccountRefund</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PaymentEditTask extends EditAccountActTask {

    /**
     * The charge amount that triggered the payment task.
     */
    private final BigDecimal chargeAmount;


    /**
     * Constructs a new <tt>PaymentEditTask</tt> to edit an object
     * in the {@link TaskContext}.
     *
     * @param chargeAmount the charge amount that triggered the payment
     *                     workflow. If <tt>0</tt>, the context will be examined
     *                     for an invoice to determine the amount
     */
    public PaymentEditTask(BigDecimal chargeAmount) {
        super(CustomerAccountArchetypes.PAYMENT, true);
        this.chargeAmount = chargeAmount;
    }

    /**
     * Creates a new editor for an object.
     *
     * @param object  the object to edit
     * @param context the task context
     * @return a new editor
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object,
                                          TaskContext context) {
        LayoutContext layout = new DefaultLayoutContext(true);
        layout.setContext(context);
        BigDecimal amount = chargeAmount;
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            FinancialAct invoice = (FinancialAct) context.getObject(
                    CustomerAccountArchetypes.INVOICE);
            if (invoice != null) {
                amount = invoice.getTotal();
            }
        }
        return new CustomerPaymentEditor((Act) object, null, layout, amount);
    }
}
