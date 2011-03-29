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

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectCreationListener;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.util.Messages;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


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
     * The amount of the invoice that this payment relates to.
     */
    private final SimpleProperty invoiceAmount;

    /**
     * The previous balance.
     */
    private final SimpleProperty previousBalance;

    /**
     * The overdue amount.
     */
    private final SimpleProperty overdueAmount;

    /**
     * The total balance.
     */
    private final SimpleProperty totalBalance;


    /**
     * Constructs a new <tt>CustomerPaymentEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     */
    public CustomerPaymentEditor(Act act, IMObject parent,
                                 LayoutContext context) {
        this(act, parent, context, BigDecimal.ZERO);
    }

    /**
     * Creates a new <tt>CustomerPaymentEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context
     * @param invoice the invoice amount
     */
    public CustomerPaymentEditor(Act act, IMObject parent,
                                 LayoutContext context,
                                 BigDecimal invoice) {
        super(act, parent, context);
        invoiceAmount = createProperty("invoiceAmount",
                                       "customer.payment.currentInvoice");
        invoiceAmount.setValue(invoice);
        previousBalance = createProperty("previousBalance",
                                         "customer.payment.previousBalance");
        overdueAmount = createProperty("overdueAmount",
                                       "customer.payment.overdue");
        totalBalance = createProperty("totalBalance",
                                      "customer.payment.totalBalance");

        initParticipant("customer", context.getContext().getCustomer());
        initParticipant("location", context.getContext().getLocation());
        getEditor().setCreationListener(new IMObjectCreationListener() {
            public void created(IMObject object) {
                onCreated((FinancialAct) object);
            }
        });

        updateSummary();

        getProperty("customer").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateSummary();
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
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy(getEditor());
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
                // default the amount to the outstanding balance +/- the running total.
                boolean payment = TypeHelper.isA(act, "act.customerAccountPayment*");
                CustomerAccountRules rules = new CustomerAccountRules();
                balance = rules.getBalance(customer, runningTotal, payment);
                act.setTotal(new Money(balance));
            } else {
                // default the amount to the expected amount - the running total.
                balance = expectedAmount.subtract(runningTotal);
                if (balance.signum() >= 0) {
                    act.setTotal(new Money(balance));
                }
            }
            getEditor().setModified(act, true);
        }
    }

    /**
     * Updates the balance summary for the current customer.
     */
    private void updateSummary() {
        Party customer = (Party) getParticipant("customer");
        BigDecimal overdue = BigDecimal.ZERO;
        BigDecimal previous = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        if (customer != null) {
            CustomerAccountRules rules = new CustomerAccountRules();

            total = rules.getBalance(customer);
            overdue = rules.getOverdueBalance(customer, new Date());
            BigDecimal invoice = (BigDecimal) invoiceAmount.getValue();
            previous = total.subtract(overdue).subtract(invoice);
        }
        previousBalance.setValue(previous);
        overdueAmount.setValue(overdue);
        totalBalance.setValue(total);
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

    private SimpleProperty createProperty(String name,
                                          String key) {
        SimpleProperty property = new SimpleProperty(name, BigDecimal.class);
        property.setDisplayName(Messages.get(key));
        property.setReadOnly(true);
        return property;
    }

    private class LayoutStrategy extends ActLayoutStrategy {

        /**
         * Creates a new <tt>LayoutStrategy</tt>.
         *
         * @param editor the act items editor
         */
        public LayoutStrategy(IMObjectCollectionEditor editor) {
            super(editor);
        }

        /**
         * Lays out child components in a grid.
         *
         * @param object      the object to lay out
         * @param parent      the parent object. May be <tt>null</tt>
         * @param descriptors the property descriptors
         * @param properties  the properties
         * @param container   the container to use
         * @param context     the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                      PropertySet properties, Component container, LayoutContext context) {
            ComponentSet set = createComponentSet(object, descriptors, properties, context);
            ComponentGrid grid = new ComponentGrid();
            grid.set(0, 0, createComponent(invoiceAmount, object, context));
            grid.set(0, 1, createComponent(previousBalance, object, context));
            grid.set(1, 0, createComponent(overdueAmount, object, context));
            grid.set(1, 1, createComponent(totalBalance, object, context));
            grid.add(set);
            doGridLayout(grid, container);
        }

    }
}
