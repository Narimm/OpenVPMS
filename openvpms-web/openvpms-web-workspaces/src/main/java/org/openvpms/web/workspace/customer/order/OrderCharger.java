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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
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
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;

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
    private List<Act> charged = new ArrayList<>();

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
        if (charged.isEmpty()) {
            return rules.hasOrders(customer, patient);
        } else {
            PendingOrderQuery query = new PendingOrderQuery(customer, patient, charged);
            List<Act> orders = QueryHelper.query(query);
            return !orders.isEmpty();
        }
    }

    /**
     * Saves any charged orders.
     *
     * @throws OpenVPMSException for any error
     */
    public void save() {
        if (!charged.isEmpty()) {
            ServiceHelper.getArchetypeService().save(charged);
        }
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
     * Returns the number of charged orders.
     *
     * @return the number of charged orders
     */
    public int getCharged() {
        return charged.size();
    }

    /**
     * Charges an order or return.
     *
     * @param act      the order/return
     * @param listener the listener to notify when charging completes
     */
    public void charge(FinancialAct act, final CompletionListener listener) {
        final OrderInvoicer charger = createInvoicer(act);
        Validator validator = new DefaultValidator();
        if (charger != null && charger.validate(validator)) {
            FinancialAct invoice = charger.getInvoice();
            if (invoice == null || ActStatus.POSTED.equals(invoice.getStatus())) {
                // the order doesn't relate to an invoice, or the invoice is POSTED
                if (charger.canInvoice()) {
                    invoice(act, charger, listener);
                } else if (charger.canCredit()) {
                    credit(act, charger, listener);
                } else {
                    show(Messages.format("customer.order.invoice.unsupported", DescriptorHelper.getDisplayName(act)),
                         listener);
                }
            } else {
                if (charger.canInvoice()) {
                    doCharge(act, charger, invoice, new DefaultLayoutContext(context, help), listener);
                } else if (charger.canCredit()) {
                    credit(act, charger, listener);
                } else {
                    show(Messages.format("customer.order.invoice.unsupported", DescriptorHelper.getDisplayName(act)),
                         listener);
                }
            }
        } else {
            String title = Messages.format("customer.order.invalid", DescriptorHelper.getDisplayName(act));
            ValidationHelper.showError(title, validator, new WindowPaneListener() {
                @Override
                public void onClose(WindowPaneEvent event) {
                    listener.completed();
                }
            });
        }
    }

    /**
     * Displays a prompt to charge pending orders, if any.
     *
     * @param editor   the editor to add charges to
     * @param listener the listener to notify when charging completes
     */
    public void charge(final CustomerChargeActEditor editor, final CompletionListener listener) {
        PendingOrderQuery query = new PendingOrderQuery(customer, null, charged);
        ResultSet<Act> set = query.query();
        if (!set.hasNext()) {
            if (charged.isEmpty()) {
                show(Messages.format("customer.order.none", customer.getName()), listener);
            } else {
                show(Messages.format("customer.order.unsaved", customer.getName()), listener);
            }
        } else {
            final PendingOrderBrowser browser = new PendingOrderBrowser(query, new DefaultLayoutContext(context, help));
            browser.query();
            PendingOrderDialog dialog = new PendingOrderDialog(Messages.get("customer.order.invoice.title"),
                                                               browser, new LocalContext(context), help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    List<Act> orders = browser.getOrders();
                    charge(orders, editor, listener);
                }
            });
            dialog.show();
        }
    }

    /**
     * Charges any complete orders.
     *
     * @param editor the editor to add charges to
     */
    public void chargeComplete(CustomerChargeActEditor editor) {
        PendingOrderQuery query = new PendingOrderQuery(customer, null, charged);
        List<Act> orders = QueryHelper.query(query);
        for (Act order : orders) {
            OrderInvoicer invoicer = createInvoicer(order);
            if (invoicer != null && invoicer.isValid() && invoicer.canCharge(editor)
                && (patient == null || invoicer.canCharge(patient))) {
                invoicer.charge(editor);
                charged.add(order);
            }
        }
    }

    /**
     * Creates an invoicer for an order or return.
     *
     * @param act the order/return
     * @return a new invoicer, or {@code null} if the act is not supported
     */
    private OrderInvoicer createInvoicer(Act act) {
        if (TypeHelper.isA(act, OrderArchetypes.PHARMACY_ORDER, OrderArchetypes.PHARMACY_RETURN)) {
            return new PharmacyOrderInvoicer((FinancialAct) act, rules);
        } else if (TypeHelper.isA(act, OrderArchetypes.INVESTIGATION_RETURN)) {
            return new InvestigationOrderInvoicer((FinancialAct) act, rules);
        }
        return null;
    }

    /**
     * Charges orders.
     *
     * @param orders   the orders to charge
     * @param editor   the editor to add charges to
     * @param listener the listener to notify when charging completes
     */
    private void charge(List<Act> orders, CustomerChargeActEditor editor, final CompletionListener listener) {
        StringBuilder messages = new StringBuilder();
        for (Act order : orders) {
            OrderInvoicer charger = createInvoicer(order);
            if (charger != null) {
                if (!charger.isValid()) {
                    append(messages, Messages.format("customer.order.incomplete", order.getId(),
                                                     DescriptorHelper.getDisplayName(order)));
                } else if (patient != null && !charger.canCharge(patient)) {
                    append(messages, Messages.format("customer.order.multiplePatient", order.getId(),
                                                     DescriptorHelper.getDisplayName(order)));
                } else if (!charger.canCharge(editor)) {
                    FinancialAct invoice = charger.getInvoice();
                    if (invoice != null && invoice.getId() != editor.getObject().getId()) {
                        append(messages, Messages.format("customer.order.differentInvoice", order.getId(),
                                                         DescriptorHelper.getDisplayName(order), invoice.getId()));
                    } else {
                        append(messages, Messages.format("customer.order.cannotcharge", order.getId(),
                                                         DescriptorHelper.getDisplayName(order),
                                                         editor.getDisplayName()));
                    }
                } else {
                    charger.charge(editor);
                    charged.add(order);
                }
            }
        }
        if (messages.length() != 0) {
            show(messages.toString(), listener);
        } else {
            listener.completed();
        }
    }

    /**
     * Invoices an order.
     *
     * @param act      the order
     * @param invoicer the order invoicer
     * @param listener the listener to notify on completion
     */
    private void invoice(FinancialAct act, OrderInvoicer invoicer, CompletionListener listener) {
        if (invoicer.requiresEdit()) {
            CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
            final FinancialAct current = rules.getInvoice(invoicer.getCustomer());
            charge(act, current, invoicer, listener);
        } else {
            invoicer.charge();
            listener.completed();
        }
    }

    /**
     * Credits a return.
     *
     * @param act      the order return
     * @param invoicer the order invoicer
     * @param listener the listener to notify on completion
     */
    private void credit(FinancialAct act, OrderInvoicer invoicer, CompletionListener listener) {
        CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
        final FinancialAct current = rules.getCredit(invoicer.getCustomer());
        charge(act, current, invoicer, listener);
    }

    /**
     * Charges an order or order return.
     * <p/>
     * If there is no current invoice, the item will be charged and the invoice/credit displayed in an editor.
     * If there is an invoice, a dialog will be displayed to invoice the order to a the current invoice, or a new
     * invoice.
     *
     * @param act      the order/order return
     * @param current  the current invoice. May be {@code null}
     * @param invoicer the order invoicer
     * @param listener the listener to notify on completion
     */
    private void charge(final FinancialAct act, final FinancialAct current, final OrderInvoicer invoicer,
                        final CompletionListener listener) {
        final DefaultLayoutContext context = new DefaultLayoutContext(this.context, help);
        if (current == null) {
            // no current invoice
            doCharge(act, invoicer, null, context, listener);
        } else {
            String displayName = DescriptorHelper.getDisplayName(current);
            String title = Messages.format("customer.order.currentcharge.title", displayName);
            String message;
            if (invoicer.getInvoice() != null && ActStatus.POSTED.equals(invoicer.getInvoice().getStatus())) {
                // original charge is posted
                message = Messages.format("customer.order.currentcharge.original",
                                          DescriptorHelper.getDisplayName(act), displayName);
            } else {
                // no original charge
                message = Messages.format("customer.order.currentcharge.current", displayName);
            }
            final SelectChargeDialog dialog = new SelectChargeDialog(title, message, displayName);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    if (dialog.createCharge()) {
                        doCharge(act, invoicer, null, context, listener);
                    } else {
                        doCharge(act, invoicer, current, context, listener);
                    }
                }
            });
            dialog.show();
        }
    }

    private void doCharge(FinancialAct act, OrderInvoicer invoicer, FinancialAct current,
                          DefaultLayoutContext context, final CompletionListener listener) {
        CustomerChargeActEditDialog dialog = invoicer.charge(current, this, context);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                listener.completed();
            }
        });
        if (ActStatus.POSTED.equals(act.getStatus())) {
            charged.add(act);
            dialog.checkOrders();
        }
    }

    private void show(String message, final CompletionListener listener) {
        InformationDialog dialog = new InformationDialog(message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            @Override
            public void onClose(WindowPaneEvent event) {
                listener.completed();
            }
        });
        dialog.show();
    }

    /**
     * Helper to append a message to a buffer, inserting a new-line if required.
     *
     * @param buffer  the buffer
     * @param message the message to append
     */
    private void append(StringBuilder buffer, String message) {
        if (buffer.length() != 0) {
            buffer.append("\n");
        }
        buffer.append(message);
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
            return newInvoice.isSelected();
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
