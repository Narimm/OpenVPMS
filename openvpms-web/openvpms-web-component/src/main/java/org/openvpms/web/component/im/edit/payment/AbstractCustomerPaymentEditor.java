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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.payment;

import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;

/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountPayment</em> or <em>act.customerAccountRefund</em>.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCustomerPaymentEditor extends PaymentEditor {

    /**
     * The amount of the invoice that this payment relates to.
     */
    private final SimpleProperty invoiceAmount;

    /**
     * Determines the expected payment amount. If {@code null}, there
     * is no limit on the payment amount. If non-null, validation will fail
     * if the act total is not that specified.
     */
    private BigDecimal expectedAmount;

    /**
     * Constructs an {@link AbstractCustomerPaymentEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public AbstractCustomerPaymentEditor(Act act, IMObject parent, LayoutContext context) {
        super(act, parent, context);
        invoiceAmount = createProperty("invoiceAmount", "customer.payment.currentInvoice");

        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("location", context.getContext().getLocation());
        getItems().setCreationListener(new IMObjectCreationListener() {
            public void created(IMObject object) {
                onCreated((FinancialAct) object);
            }
        });
    }

    /**
     * Sets the invoice amount.
     *
     * @param amount the invoice amount
     */
    public void setInvoiceAmount(BigDecimal amount) {
        invoiceAmount.setValue(amount);
    }

    /**
     * Returns the invoice amount.
     *
     * @return the invoice amount
     */
    public BigDecimal getInvoiceAmount() {
        return invoiceAmount.getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Determines the expected amount of the payment. If {@code null}, there
     * is no limit on the payment amount. If non-null, validation will fail
     * if the act total is not that specified.
     *
     * @param amount the expected payment amount. May be {@code null}
     */
    public void setExpectedAmount(BigDecimal amount) {
        expectedAmount = amount;
    }

    /**
     * Sets the till.
     *
     * @param till the till. May be {@code null}
     */
    public void setTill(Entity till) {
        setParticipant("till", till);
    }

    /**
     * Validates the object.
     * <p/>
     * This extends validation by ensuring that the payment amount matches the expected amount, if present.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid = super.doValidation(validator);
        if (valid && expectedAmount != null) {
            Property property = getProperty("amount");
            BigDecimal amount = (BigDecimal) property.getValue();
            if (amount.compareTo(expectedAmount) != 0) {
                valid = false;
                // need to pre-format the amounts as the Messages uses the browser's locale which may have different
                // currency format
                String msg = Messages.format("customer.payment.amountMismatch",
                                             NumberFormatter.formatCurrency(expectedAmount));
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
    protected void onCreated(FinancialAct act) {
        Party customer = (Party) getParticipant("customer");
        if (customer != null) {
            BigDecimal runningTotal = getRunningTotal();
            BigDecimal balance;
            if (expectedAmount == null) {
                // default the amount to the outstanding balance +/- the running total.
                boolean payment = TypeHelper.isA(act, "act.customerAccountPayment*");
                CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
                balance = rules.getBalance(customer, runningTotal, payment);
                act.setTotal(new Money(balance));
            } else {
                // default the amount to the expected amount - the running total.
                balance = expectedAmount.subtract(runningTotal);
                if (balance.signum() >= 0) {
                    act.setTotal(new Money(balance));
                }
            }
            getItems().setModified(act, true);
        }
    }

    /**
     * Returns the invoice amount property.
     *
     * @return the property
     */
    protected Property getInvoiceAmountProperty() {
        return invoiceAmount;
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

    /**
     * Helper to create a decimal property.
     *
     * @param name the property name
     * @param key  the resource bundle key
     * @return a new property
     */
    protected SimpleProperty createProperty(String name, String key) {
        SimpleProperty property = new SimpleProperty(name, BigDecimal.class);
        property.setDisplayName(Messages.get(key));
        property.setReadOnly(true);
        return property;
    }
}
