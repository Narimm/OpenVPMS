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

package org.openvpms.web.workspace.customer.charge;

import nextapp.echo2.app.Component;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.message.InformationMessage;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.order.OrderCharger;

/**
 * Manages charging orders for an editor.
 * <p/>
 * This automatically charges completed orders, and displays status information.
 *
 * @author Tim Anderson
 */
public class OrderChargeManager {

    /**
     * The order charger.
     */
    private final OrderCharger charger;

    /**
     * The container to hold messages.
     */
    private final Component container;

    /**
     * The current order message.
     */
    private Component message;


    /**
     * Constructs an {@link OrderChargeManager}.
     *
     * @param charger   the charger
     * @param container the container
     */
    public OrderChargeManager(OrderCharger charger, Component container) {
        this.charger = charger;
        this.container = container;
    }

    /**
     * Checks if there are orders pending for the customer.
     * If so, adds a message to the container.
     */
    public void check() {
        if (message != null) {
            container.remove(message);
            message = null;
        }
        if (charger.hasOrders()) {
            message = new InformationMessage(Messages.format("customer.order.pending",
                                                             charger.getCustomer().getName()));
            container.add(message, 0);
        }
    }

    /**
     * Charges all orders.
     * <p/>
     * For completed orders. these are charged automatically.
     * For other orders, a popup is displayed to charge them.
     *
     * @param editor the editor to add charges to
     */
    public void charge(final AbstractCustomerChargeActEditor editor) {
        chargeCompleted(editor);
        if (charger.hasOrders()) {
            String title = Messages.get("customer.order.incomplete.title");
            String prompt = Messages.format("customer.order.incomplete.message", charger.getCustomer().getName());
            ConfirmationDialog dialog = new ConfirmationDialog(title, prompt, ConfirmationDialog.YES_NO);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onYes() {
                    chargeSelected(editor);
                }
            });
            editor.queue(dialog);
        }
    }

    /**
     * Charges completed orders to the editor.
     * <p/>
     * A message will be added to the container indicating if there are incomplete orders.
     */
    public void chargeCompleted(AbstractCustomerChargeActEditor editor) {
        if (message != null) {
            container.remove(message);
            message = null;
        }
        int before = 0;
        int after = 0;
        if (editor != null) {
            before = charger.getCharged();
            charger.chargeComplete(editor);
            after = charger.getCharged();
        }
        StringBuilder buffer = new StringBuilder();

        if (charger.hasOrders()) {
            buffer.append(Messages.format("customer.order.pending", charger.getCustomer().getName()));
        }
        if (before != after) {
            if (buffer.length() != 0) {
                buffer.append("\n\n");
            }
            buffer.append(Messages.format("customer.order.charged", after - before));
        }
        if (buffer.length() != 0) {
            message = new InformationMessage(buffer.toString());
            container.add(message, 0);
        }
    }

    /**
     * Displays a popup to charge pending orders and returns.
     *
     * @param editor the editor to add charges to
     */
    public void chargeSelected(final AbstractCustomerChargeActEditor editor) {
        charger.charge(editor, new OrderCharger.CompletionListener() {
            @Override
            public void completed() {
                check();
            }
        });
    }

    /**
     * Saves any charged orders.
     *
     * @return {@code true} if the orders were successfully saved
     */
    public boolean save() {
        return charger.save();
    }

    /**
     * Clears any charged orders.
     * <p/>
     * This should be invoked after a successful {@link #save()}
     */
    public void clear() {
        charger.clear();
    }

}
