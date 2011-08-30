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
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountPayment</em> or <em>act.customerAccountRefund</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerPaymentEditor extends PaymentEditor {

    /**
     * Determines the expected payment amount. If <tt>null</tt>, there
     * is no limit on the payment amount. If non-null, validation will fail
     * if the act total is not that specified.
     */
    private BigDecimal expectedAmount;


    /**
     * Constructs a new <tt>CustomerPaymentEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public CustomerPaymentEditor(Act act, IMObject parent,
                                 LayoutContext context) {
        super(act, parent, context);
        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("location", context.getContext().getLocation());
        getEditor().setCreationListener(new IMObjectCreationListener() {
            public void created(IMObject object) {
                onCreated((FinancialAct) object);
            }
        });
    }

    /**
     * Determines the expected amount of the payment. If <tt>null</tt>, there
     * is no limit on the payment amount. If non-null, validation will fail
     * if the act total is not that specified.
     *
     * @param amount the expected payment amount. May be <tt>null</tt>
     */
    public void setExpectedAmount(BigDecimal amount) {
        expectedAmount = amount;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid = super.validate(validator);
        if (valid && expectedAmount != null) {
            Property property = getProperty("amount");
            BigDecimal amount = (BigDecimal) property.getValue();
            if (amount.compareTo(expectedAmount) != 0) {
                valid = false;
                String msg = Messages.get("customer.payment.amountMismatch",
                                          expectedAmount);
                validator.add(property, new ValidatorError(msg));
            }
        }
        return valid;
    }

    /**
     * Invoked when a child act is created. This sets the total to the:
     * <ul>
     * <li>outstanding balance +/- the running total, if there is no expected
     * amount; or</li>
     * <li>expected amount - the running total</li>
     * </ul>
     *
     * @param act the act
     */
    private void onCreated(FinancialAct act) {
        Party customer = (Party) getParticipant("customer");
        if (customer != null) {
            BigDecimal runningTotal = getRunningTotal();
            BigDecimal balance;
            if (expectedAmount == null) {
                // default the amount to the outstanding balance +/- the running
                // total.
                boolean payment = TypeHelper.isA(act,
                                                 "act.customerAccountPayment*");
                CustomerAccountRules rules = new CustomerAccountRules();
                balance = rules.getBalance(customer, runningTotal,
                                           payment);
                act.setTotal(new Money(balance));
            } else {
                // default the amount to the expected amount - the running
                // total.
                balance = expectedAmount.subtract(runningTotal);
                if (balance.signum() >= 0) {
                    act.setTotal(new Money(balance));
                }
            }
        }
    }

    /**
     * Returns the running total. This is the current total of the act
     * minus any committed child acts which are already included in the balance.
     *
     * @return the running total
     */
    private BigDecimal getRunningTotal() {
        FinancialAct act = (FinancialAct) getObject();
        BigDecimal total = act.getTotal();
        BigDecimal committed = ActHelper.sum(act, "amount");
        return total.subtract(committed);
    }

}
