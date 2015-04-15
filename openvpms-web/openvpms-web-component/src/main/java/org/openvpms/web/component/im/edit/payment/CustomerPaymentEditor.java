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

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountPayment</em> or <em>act.customerAccountRefund</em>.
 *
 * @author Tim Anderson
 */
public class CustomerPaymentEditor extends AbstractCustomerPaymentEditor {

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
     * Constructs a {@link CustomerPaymentEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public CustomerPaymentEditor(Act act, IMObject parent, LayoutContext context) {
        this(act, parent, context, BigDecimal.ZERO);
    }

    /**
     * Constructs a {@link CustomerPaymentEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     * @param invoice the invoice amount
     */
    public CustomerPaymentEditor(Act act, IMObject parent, LayoutContext context, BigDecimal invoice) {
        super(act, parent, context);
        setInvoiceAmount(invoice);
        previousBalance = createProperty("previousBalance", "customer.payment.previousBalance");
        overdueAmount = createProperty("overdueAmount", "customer.payment.overdue");
        totalBalance = createProperty("totalBalance", "customer.payment.totalBalance");

        updateSummary();
        getProperty("customer").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateSummary();
            }
        });
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy(getItems());
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
            CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);

            total = rules.getBalance(customer);
            overdue = rules.getOverdueBalance(customer, new Date());
            BigDecimal invoice = getInvoiceAmount();
            previous = total.subtract(overdue).subtract(invoice);
        }
        previousBalance.setValue(previous);
        overdueAmount.setValue(overdue);
        totalBalance.setValue(total);
    }

    protected class LayoutStrategy extends ActLayoutStrategy {

        /**
         * Creates a new {@code LayoutStrategy}.
         *
         * @param editor the act items editor
         */
        public LayoutStrategy(IMObjectCollectionEditor editor) {
            super(editor);
        }

        /**
         * Lays out child components in a grid.
         *
         * @param object     the object to lay out
         * @param parent     the parent object. May be {@code null}
         * @param properties the properties
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                      Component container, LayoutContext context) {
            ComponentSet set = createComponentSet(object, properties, context);
            ComponentGrid grid = new ComponentGrid();
            grid.set(0, 0, createComponent(getInvoiceAmountProperty(), object, context));
            grid.set(0, 2, createComponent(previousBalance, object, context));
            grid.set(1, 0, createComponent(overdueAmount, object, context));
            grid.set(1, 2, createComponent(totalBalance, object, context));
            grid.add(set);
            doGridLayout(grid, container);
        }

    }
}
