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

import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype in
 * <em>act.customerAccountPayment*</em>, and <em>act.customerAccountRefund*</em>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerPaymentItemEditor extends PaymentItemEditor {

    /**
     * Construct a new <tt>CustomerPaymentItemEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public CustomerPaymentItemEditor(FinancialAct act, FinancialAct parent,
                                     LayoutContext context) {
        super(act, parent, context);
        if (act.isNew()) {
            // default the amount to the outstanding balance +/- the running
            // total
            Party customer = context.getContext().getCustomer();
            if (customer != null) {
                boolean payment = TypeHelper.isA(act,
                                                 "act.customerAccountPayment*");
                CustomerAccountRules rules = new CustomerAccountRules();
                BigDecimal balance = rules.getBalance(
                        customer, getRunningTotal(parent), payment);
                Property amount = getProperty("amount");
                amount.setValue(balance);
            }
        }
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object  the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        LayoutContext context) {
        IMObjectEditor result = null;
        if (TypeHelper.isA(object, "act.customerAccountPayment*",
                           "act.customerAccountRefund*")
                && !TypeHelper.isA(object, "act.customerAccountPayment",
                                   "act.customerAccountRefund")
                && parent instanceof Act) {
            result = new CustomerPaymentItemEditor((FinancialAct) object,
                                                   (FinancialAct) parent,
                                                   context);
        }
        return result;
    }

    /**
     * Returns the running total of the parent act.
     * This is the current total of the parent act minus any committed
     * acts which are already included in the balance.
     *
     * @param parent the parent act
     * @return the running total
     */
    private BigDecimal getRunningTotal(FinancialAct parent) {
        BigDecimal total = parent.getTotal();
        BigDecimal committed = ActHelper.sum(parent, "amount");
        return total.subtract(committed);
    }

}
