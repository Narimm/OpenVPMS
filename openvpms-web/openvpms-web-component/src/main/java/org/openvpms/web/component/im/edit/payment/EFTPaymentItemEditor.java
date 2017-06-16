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
import org.openvpms.plugin.manager.PluginManager;
import org.openvpms.pos.api.POSService;
import org.openvpms.pos.api.Terminal;
import org.openvpms.pos.api.Transaction;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
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
     * The POS terminal configuration.
     */
    private final Entity terminalConfig;

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
        terminalConfig = (till != null) ? (Entity) new IMObjectBean(till).getNodeTargetObject("terminal") : null;
        customer = context.getContext().getCustomer();
    }

    /**
     * Returns the payment amount.
     *
     * @return the payment amount
     */
    public BigDecimal getAmount() {
        return getProperty("amount").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Returns the cash out amount.
     *
     * @return the cash out amount
     */
    public BigDecimal getCashout() {
        return getProperty("cashout").getBigDecimal(BigDecimal.ZERO);
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
                if (terminalConfig != null) {
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
        Validator validator = new DefaultValidator();
        if (validate(validator)) {
            try {
                Terminal terminal = getTerminal();
                if (terminal == null) {
                    ErrorDialog.show(Messages.get("customer.payment.eft.title"),
                                     Messages.get("customer.payment.eft.noterminal"));
                } else if (!terminal.isAvailable()) {
                    ErrorDialog.show(Messages.get("customer.payment.eft.title"),
                                     Messages.format("customer.payment.eft.terminalunavailable",
                                                     terminalConfig.getName()));
                } else {
                    BigDecimal amount = getAmount();
                    BigDecimal cashout = getCashout();
                    Transaction transaction = terminal.pay(customer, amount, cashout);
                    EFTPaymentDialog dialog = new EFTPaymentDialog(terminal, transaction);
                    dialog.show();
                }
            } catch (Exception exception) {
                ErrorHelper.show(Messages.get("customer.payment.eft.title"), exception);
            }
        } else {
            ValidationHelper.showError(validator);
        }
    }

    /**
     * Returns the POS terminal.
     *
     * @return the POS terminal, or {@code null} if none can be found
     */
    private Terminal getTerminal() {
        PluginManager manager = ServiceHelper.getBean(PluginManager.class);
        List<POSService> services = manager.getServices(POSService.class);
        for (POSService service : services) {
            if (terminalConfig.getArchetypeId().getShortName().equals(service.getConfigurationType())) {
                return service.getTerminal(terminalConfig);
            }
        }
        return null;
    }

}
