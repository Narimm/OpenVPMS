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

package org.openvpms.web.workspace.customer.order;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Charges customer orders.
 *
 * @author Tim Anderson
 */
public class OrderCharger {

    public interface CompletionListener {

        void completed();

    }

    /**
     * The customer to query orders for.
     */
    private final Party customer;

    /**
     * The patient to query orders for.
     */
    private final Party patient;

    /**
     * The customer order rules.
     */
    private final OrderRules rules;

    /**
     * The current context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The orders that have been charged, but not saved.
     */
    private List<Act> charged = new ArrayList<Act>();

    /**
     * Constructs an {@link OrderCharger}.
     *
     * @param customer the customer
     * @param rules    the order rules
     * @param context  the context
     * @param help     the help context
     */
    public OrderCharger(Party customer, OrderRules rules, Context context, HelpContext help) {
        this(customer, null, rules, context, help);
    }

    /**
     * Constructs an {@link OrderCharger}.
     *
     * @param customer the customer
     * @param patient  the patient. May be {@code null}
     * @param rules    the order rules
     * @param context  the context
     * @param help     the help context
     */
    public OrderCharger(Party customer, Party patient, OrderRules rules, Context context, HelpContext help) {
        this.customer = customer;
        this.patient = patient;
        this.rules = rules;
        this.context = context;
        this.help = help;
    }

    /**
     * Returns the customer.
     *
     * @return the customer
     */
    public Party getCustomer() {
        return customer;
    }

    /**
     * Determines if the customer has pending orders.
     * <p/>
     * If a patient was supplied at construction, this limits the query to those orders that are for the patient.
     *
     * @return {@code true} if the customer has pending orders
     */
    public boolean hasOrders() {
        return rules.hasOrders(customer, patient);
    }

    /**
     * Saves any charged orders.
     *
     * @return {@code true} if the orders were successfully saved
     */
    public boolean save() {
        return charged.isEmpty() || SaveHelper.save(charged);
    }

    /**
     * Clears any charged orders.
     * <p/>
     * This should be invoked after a successful {@link #save()}
     */
    public void clear() {
        charged.clear();
    }

    /**
     * Charges an order or return.
     *
     * @param act      the order/return
     * @param listener the listener to notify when charging completes
     */
    public void charge(FinancialAct act, CompletionListener listener) {
        final PharmacyOrderCharger charger = new PharmacyOrderCharger(act, rules);
        if (charger.isValid()) {
            FinancialAct invoice = charger.getInvoice();
            if (invoice == null || ActStatus.POSTED.equals(invoice.getStatus())) {
                // the order doesn't relate to an invoice, or the invoice is POSTED
                if (charger.canInvoice()) {
                    invoice(act, charger, listener);
                } else if (charger.canCredit()) {
                    credit(act, charger, listener);
                } else {
                    InformationDialog.show(Messages.format("customer.order.invoice.unsupported",
                                                           DescriptorHelper.getDisplayName(act)));
                }
            } else {
                if (charger.canInvoice()) {
                    doCharge(act, charger, invoice, new DefaultLayoutContext(context, help), listener);
                } else if (charger.canCredit()) {
                    credit(act, charger, listener);
                } else {
                    InformationDialog.show(Messages.format("customer.order.invoice.unsupported",
                                                           DescriptorHelper.getDisplayName(act)));
                }
            }
        } else {
            InformationDialog.show(Messages.format("customer.order.invalid", DescriptorHelper.getDisplayName(act)));
        }
    }

    /**
     * Displays a prompt to charge pending orders, if any.
     *
     * @param editor the editor to add charges to
     */
    public void charge(final AbstractCustomerChargeActEditor editor) {
        PendingOrderQuery query = new PendingOrderQuery(customer, null, charged);
        ResultSet<Act> set = query.query();
        if (!set.hasNext()) {
            if (charged.isEmpty()) {
                InformationDialog.show(Messages.format("customer.order.none", customer.getName()));
            } else {
                InformationDialog.show(Messages.format("customer.order.unsaved", customer.getName()));
            }
        } else {
            final PendingOrderBrowser browser = new PendingOrderBrowser(query, new DefaultLayoutContext(context, help));
            browser.query();
            PendingOrderDialog dialog = new PendingOrderDialog(Messages.get("customer.order.invoice.title"),
                                                               browser, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    List<Act> orders = browser.getOrders();
                    charge(orders, editor);
                }
            });
            dialog.show();
        }
    }

    /**
     * Charges orders.
     *
     * @param orders the orders to charge
     * @param editor the editor to add charges to
     */
    private void charge(List<Act> orders, AbstractCustomerChargeActEditor editor) {
        int invalid = 0;
        int multiplePatient = 0;
        int differentInvoice = 0;
        for (Act order : orders) {
            if (TypeHelper.isA(order, OrderArchetypes.PHARMACY_ORDER)) {
                PharmacyOrderCharger charger = new PharmacyOrderCharger((FinancialAct) order, rules);
                if (!charger.isValid()) {
                    ++invalid;
                } else if (patient != null && !charger.canCharge(patient)) {
                    ++multiplePatient;
                } else if (!charger.canCharge(editor)) {
                    ++differentInvoice;
                } else {
                    charger.charge(editor);
                    charged.add(order);
                }
            }
        }
        if (invalid != 0 || multiplePatient != 0 || differentInvoice != 0) {
            StringBuilder message = new StringBuilder();
            if (invalid != 0) {
                message.append(Messages.format("customer.order.incomplete", invalid));
            }
            if (multiplePatient != 0) {
                if (message.length() != 0) {
                    message.append("\n\n");
                }
                message.append(Messages.format("customer.order.multiplePatient", multiplePatient));
            }
            if (differentInvoice != 0) {
                if (message.length() != 0) {
                    message.append("\n\n");
                }
                message.append(Messages.format("customer.order.differentInvoice", differentInvoice));
            }
            InformationDialog.show(message.toString());
        }
    }


    private void invoice(FinancialAct act, final PharmacyOrderCharger charger, CompletionListener listener) {
        CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
        final FinancialAct current = rules.getInvoice(charger.getCustomer());
        charge(act, current, charger, listener);
    }

    private void credit(FinancialAct act, final PharmacyOrderCharger charger, CompletionListener listener) {
        CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
        final FinancialAct current = rules.getCredit(charger.getCustomer());
        charge(act, current, charger, listener);
    }

    private void charge(final FinancialAct act, final FinancialAct current, final PharmacyOrderCharger charger,
                        final CompletionListener listener) {
        final DefaultLayoutContext context = new DefaultLayoutContext(this.context, help);
        if (current == null) {
            // no current invoice
            doCharge(act, charger, null, context, listener);
        } else {
            String displayName = DescriptorHelper.getDisplayName(current);
            String title = Messages.format("customer.order.currentcharge.title", displayName);
            String message = Messages.format("customer.order.currentcharge.message",
                                             DescriptorHelper.getDisplayName(act), displayName);
            final SelectChargeDialog dialog = new SelectChargeDialog(title, message, displayName);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    if (dialog.createCharge()) {
                        doCharge(act, charger, null, context, listener);
                    } else {
                        doCharge(act, charger, current, context, listener);
                    }
                }
            });
            dialog.show();
        }
    }

    private void doCharge(FinancialAct act, PharmacyOrderCharger charger, FinancialAct current,
                          DefaultLayoutContext context, final CompletionListener listener) {
        CustomerChargeActEditDialog dialog = charger.charge(current, this, context);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                listener.completed();
            }
        });
        if (ActStatus.POSTED.equals(act.getStatus())) {
            charged.add(act);
        }

    }

    private static class SelectChargeDialog extends ConfirmationDialog {

        /**
         * Selects the current charge.
         */
        private RadioButton currentInvoice;

        /**
         * Selects a new charge.
         */
        private RadioButton newInvoice;

        /**
         * Constructs a {@link SelectChargeDialog}.
         *
         * @param title       the window title
         * @param message     the message
         * @param displayName the charge display name
         */
        public SelectChargeDialog(String title, String message, String displayName) {
            super(title, message);
            ButtonGroup group = new ButtonGroup();
            currentInvoice = create(Messages.format("customer.order.currentcharge", displayName), group);
            newInvoice = create(Messages.format("customer.order.newcharge", displayName), group);
            currentInvoice.setSelected(true);
        }

        /**
         * Determines if a new charge should be created.
         *
         * @return {@code true} if a new charge should be created
         */
        public boolean createCharge() {
            return currentInvoice.isSelected();
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label message = LabelFactory.create(true, true);
            message.setText(getMessage());
            Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, message,
                                                 ColumnFactory.create(Styles.CELL_SPACING, currentInvoice, newInvoice));
            Row row = RowFactory.create(Styles.LARGE_INSET, column);
            getLayout().add(row);
        }

        private RadioButton create(String label, ButtonGroup group) {
            RadioButton button = ButtonFactory.create(null, group);
            button.setText(label);
            return button;
        }
    }

}
