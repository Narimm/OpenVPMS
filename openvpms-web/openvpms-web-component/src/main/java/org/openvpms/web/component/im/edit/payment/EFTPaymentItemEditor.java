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

package org.openvpms.web.component.im.edit.payment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.pos.api.POSService;
import org.openvpms.pos.api.Terminal;
import org.openvpms.pos.api.Transaction;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for <em>act.customerAccountPaymentEFT</em>
 *
 * @author Tim Anderson
 */
public class EFTPaymentItemEditor extends PaymentItemEditor {

    /**
     * The POS terminal.
     */
    private final Entity terminal;

    /**
     * The customer.
     */
    private final Party customer;

    /**
     * Constructs {@link PaymentItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public EFTPaymentItemEditor(FinancialAct act, FinancialAct parent, LayoutContext context) {
        super(act, parent, context);
        Entity till = context.getContext().getTill();
        terminal = (till != null) ? (Entity) new IMObjectBean(till).getNodeTargetObject("terminal") : null;
        customer = context.getContext().getCustomer();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractLayoutStrategy() {
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
                Row row = RowFactory.create(Styles.CELL_SPACING);
                super.doSimpleLayout(object, parent, properties, row, context);
                if (terminal != null) {
                    row.add(ButtonFactory.create("button.pay", new ActionListener() {
                        @Override
                        public void onAction(ActionEvent event) {
                            onPay();
                        }
                    }));
                }
                container.add(row);
            }
        };
    }

    /**
     * Invoked when the 'pay' button is pressed.
     */
    private void onPay() {
        POSService service = ServiceHelper.getBean(POSService.class);
        Terminal posTerminal = service.getTerminal(terminal);
        BigDecimal amount = getProperty("amount").getBigDecimal(BigDecimal.ZERO);
        BigDecimal cashout = getProperty("cashout").getBigDecimal(BigDecimal.ZERO);
        Transaction transaction = posTerminal.pay(customer, amount, cashout);
        EFTPaymentDialog dialog = new EFTPaymentDialog(posTerminal, transaction);
        dialog.show();
    }

}
