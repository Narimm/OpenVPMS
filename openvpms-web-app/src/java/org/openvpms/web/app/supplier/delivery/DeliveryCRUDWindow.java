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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.supplier.delivery;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.OrderRules;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.supplier.SupplierHelper;
import org.openvpms.web.app.supplier.order.ESCISupplierCRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.i18n.Messages;


/**
 * CRUD window for supplier deliveries.
 *
 * @author Tim Anderson
 */
public class DeliveryCRUDWindow extends ESCISupplierCRUDWindow {

    /**
     * Invoice button identifier.
     */
    private static final String INVOICE_ID = "invoice";

    /**
     * Reverse button identifier.
     */
    private static final String REVERSE_ID = "reverse";

    /**
     * The order rules.
     */
    private final OrderRules rules;


    /**
     * Constructs a {@code DeliveryCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param help       the help context
     */
    public DeliveryCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, DeliveryActions.INSTANCE, context, help);
        rules = SupplierHelper.createOrderRules(context.getPractice());
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(final FinancialAct act) {
        boolean delivery = TypeHelper.isA(act, SupplierArchetypes.DELIVERY);
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        final OrderTableBrowser browser = new OrderTableBrowser(delivery, context);
        String displayName = DescriptorHelper.getDisplayName(act);
        String title = Messages.get("supplier.delivery.selectorders.title", displayName);
        String message = Messages.get("supplier.delivery.selectorders.message", displayName);
        HelpContext help = getHelpContext().subtopic("order");
        PopupDialog dialog = new OrderSelectionBrowserDialog(title, message, browser, help);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            public void onOK() {
                onCreated(act, browser);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when an act is posted.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(FinancialAct act) {
        try {
            if (TypeHelper.isA(act, SupplierArchetypes.DELIVERY)) {
                onInvoice(act);
            } else {
                onCredit(act);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        Button invoice = ButtonFactory.create(INVOICE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onInvoice(getObject());
            }
        });
        Button reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onReverse();
            }
        });
        buttons.add(createPostButton());
        buttons.add(invoice);
        buttons.add(reverse);
        buttons.add(createCheckInboxButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        boolean editEnabled = false;
        boolean deleteEnabled = false;
        boolean postEnabled = false;
        boolean invoiceReverseEnabled = false;
        if (enable) {
            FinancialAct object = getObject();
            ActActions<FinancialAct> operations = getActions();
            editEnabled = operations.canEdit(object);
            deleteEnabled = operations.canDelete(object);
            postEnabled = operations.canPost(object);
            invoiceReverseEnabled = !postEnabled;  // can't invoice or reverse a non-posted act
        }
        buttons.setEnabled(EDIT_ID, editEnabled);
        buttons.setEnabled(DELETE_ID, deleteEnabled);
        buttons.setEnabled(POST_ID, postEnabled);
        buttons.setEnabled(INVOICE_ID, invoiceReverseEnabled);
        buttons.setEnabled(REVERSE_ID, invoiceReverseEnabled);
    }

    /**
     * Creates a new edit dialog with Apply button disabled for <em>POSTED</em> acts, to workaround OVPMS-733.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new ActEditDialog(editor, getContext());
    }

    /**
     * Posts the act. This changes the act's status to POSTED, and saves it.
     *
     * @return {@code true} if the act was saved
     */
    @Override
    protected boolean post(FinancialAct act) {
        boolean result = false;
        // use the editor to ensure that the validation rules are invoked
        HelpContext context = getHelpContext().subtopic("finalise");
        DeliveryEditor editor = new DeliveryEditor(getObject(), null, createLayoutContext(context));
        editor.setStatus(ActStatus.POSTED);
        Validator validator = new Validator();
        if (!editor.validate(validator)) {
            // pop up an editor for the delivery and display the errors
            edit(editor);
            ValidationHelper.showError(validator);
        } else {
            result = SaveHelper.save(editor);
        }
        return result;
    }

    /**
     * Invoked when a delivery or return is created.
     * <p/>
     * Creates delivery/return items for each of the selected order items, and adds them to the supplied act.
     *
     * @param act     the delivery/return
     * @param browser the order browser
     */
    private void onCreated(FinancialAct act, OrderTableBrowser browser) {
        addParticipations(act, browser.getSupplier(),
                          browser.getStockLocation());
        boolean delivery = TypeHelper.isA(act, SupplierArchetypes.DELIVERY);
        HelpContext edit = createEditTopic(act);
        DeliveryEditor editor = new DeliveryEditor(act, null, createLayoutContext(edit));
        for (FinancialAct orderItem : browser.getSelectedOrderItems()) {
            FinancialAct item = (delivery) ? rules.createDeliveryItem(orderItem) : rules.createReturnItem(orderItem);
            editor.addItem(item, orderItem);
        }
        edit(editor);
    }

    /**
     * Invoked when the "Invoice" button is pressed.
     *
     * @param act the delivery act
     */
    private void onInvoice(final Act act) {
        String title = Messages.get("supplier.delivery.invoice.title");
        String message = Messages.get("supplier.delivery.invoice.message");
        ConfirmationDialog dialog = new ConfirmationDialog(title, message, getHelpContext().subtopic("invoice"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            public void onOK() {
                invoice(act);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when a return is posted.
     *
     * @param act the return act
     */
    private void onCredit(final Act act) {
        String title = Messages.get("supplier.delivery.credit.title");
        String message = Messages.get("supplier.delivery.credit.message");
        ConfirmationDialog dialog = new ConfirmationDialog(title, message, getHelpContext().subtopic("credit"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            public void onOK() {
                credit(act);
            }
        });
        dialog.show();
    }

    /**
     * Reverse a delivery or return.
     */
    private void onReverse() {
        final Act act = getObject();
        String title;
        String message;
        if (TypeHelper.isA(act, SupplierArchetypes.DELIVERY)) {
            title = Messages.get("supplier.delivery.reverseDelivery.title");
            message = Messages.get("supplier.delivery.reverseDelivery.message");
        } else {
            title = Messages.get("supplier.delivery.reverseReturn.title");
            message = Messages.get("supplier.delivery.reverseReturn.message");
        }
        ConfirmationDialog dialog = new ConfirmationDialog(title, message, getHelpContext().subtopic("reverse"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            public void onOK() {
                reverse(act);
            }
        });
        dialog.show();
    }

    /**
     * Invoices a supplier.
     *
     * @param act the delivery
     */
    private void invoice(Act act) {
        try {
            rules.invoiceSupplier(act);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Credits a supplier from a return.
     *
     * @param act the return
     */
    private void credit(Act act) {
        try {
            rules.creditSupplier(act);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Reverse a delivery/return.
     *
     * @param act the delivery/return to reverse
     */
    private void reverse(Act act) {
        try {
            if (TypeHelper.isA(act, SupplierArchetypes.DELIVERY)) {
                rules.reverseDelivery(act);
            } else {
                rules.reverseReturn(act);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

}
