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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
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
import org.openvpms.web.app.supplier.SupplierActCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD window for supplier deliveries.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-04-06 14:41:46Z $
 */
public class DeliveryCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * Invoice button identifier.
     */
    private static final String INVOICE_ID = "invoice";

    /**
     * Reverse button identifier.
     */
    private static final String REVERSE_ID = "reverse";

    /**
     * The invoice button.
     */
    private Button invoice;

    /**
     * The reverse button.
     */
    private Button reverse;

    /**
     * The order rules.
     */
    private final OrderRules rules;


    /**
     * Create a new <tt>DeliveryCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public DeliveryCRUDWindow(Archetypes<FinancialAct> archetypes) {
        super(archetypes);
        rules = new OrderRules();
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <tt>true</tt> if the act status is <em>IN_PROGRESS</em>
     */
    @Override
    protected boolean canEdit(Act act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act
     * @return <tt>true</tt> if the act status is <em>IN_PROGRESS</em>
     */
    @Override
    protected boolean canDelete(Act act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param act the new act
     */
    @Override
    protected void onCreated(final FinancialAct act) {
        boolean delivery = TypeHelper.isA(act, SupplierArchetypes.DELIVERY);
        final OrderTableBrowser browser = new OrderTableBrowser(delivery);
        String displayName = DescriptorHelper.getDisplayName(act);
        String title = Messages.get("supplier.delivery.selectorders.title",
                                    displayName);
        String message = Messages.get("supplier.delivery.selectorders.message",
                                      displayName);
        PopupDialog dialog = new OrderSelectionBrowserDialog(title, message,
                                                             browser);
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
        invoice = ButtonFactory.create(INVOICE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onInvoice(getObject());
            }
        });
        reverse = ButtonFactory.create(REVERSE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onReverse();
            }
        });
        enableButtons(buttons, false);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        buttons.removeAll();
        if (enable) {
            Act object = getObject();
            if (canEdit(object)) {
                buttons.add(getEditButton());
            }
            buttons.add(getCreateButton());
            if (canDelete(object)) {
                buttons.add(getDeleteButton());
            }
            if (!ActStatus.POSTED.equals(object.getStatus())) {
                buttons.add(getPostButton());
            } else {
                buttons.add(invoice);
                buttons.add(reverse);
            }
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Creates a new edit dialog with Apply button disabled for <em>POSTED</em>
     * acts, to workaround OVPMS-733.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new ActEditDialog(editor);
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
        DeliveryEditor editor = new DeliveryEditor(act, null, createLayoutContext());
        for (FinancialAct orderItem : browser.getSelectedOrderItems()) {
            FinancialAct item = (delivery) ? rules.createDeliveryItem(orderItem) : rules.createReturnItem(orderItem);
            editor.addItem(item, orderItem);
        }
        edit(editor);
    }

    private void onInvoice(final Act act) {
        String title = Messages.get("supplier.delivery.invoice.title");
        String message = Messages.get("supplier.delivery.invoice.message");
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            public void onOK() {
                invoice(act);
            }
        });
        dialog.show();
    }

    private void onCredit(final Act act) {
        String title = Messages.get("supplier.delivery.credit.title");
        String message = Messages.get("supplier.delivery.credit.message");
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            public void onOK() {
                credit(act);
            }
        });
        dialog.show();
    }

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
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            public void onOK() {
                reverse(act);
            }
        });
        dialog.show();
    }

    private void invoice(Act act) {
        try {
            rules.invoiceSupplier(act);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    private void credit(Act act) {
        try {
            rules.creditSupplier(act);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

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
