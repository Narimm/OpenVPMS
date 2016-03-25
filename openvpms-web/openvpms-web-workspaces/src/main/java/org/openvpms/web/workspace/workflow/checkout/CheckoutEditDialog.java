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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.web.component.app.Context;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.DefaultCustomerChargeActEditDialog;

/**
 * An edit dialog that charges any boarding charges on Check-Out.
 *
 * @author Tim Anderson
 */
public class CheckoutEditDialog extends CustomerChargeActEditDialog {

    /**
     * Constructs a {@link DefaultCustomerChargeActEditDialog}.
     *
     * @param editor  the editor
     * @param context the context
     */
    public CheckoutEditDialog(CheckoutChargeEditor editor, Context context) {
        super(editor, context);
    }

    /**
     * Returns the editor.
     *
     * @return the editor, or {@code null} if none has been set
     */
    @Override
    public CheckoutChargeEditor getEditor() {
        return (CheckoutChargeEditor) super.getEditor();
    }

    /**
     * Show the window.
     */
    @Override
    public void show() {
        super.show();
        invoiceBoarding();
    }

    /**
     * Invoked to display a message that saving failed, and the editor has been reverted.
     *
     * @param title   the message title
     * @param message the message
     */
    @Override
    protected void reloaded(String title, String message) {
        super.reloaded(title, message);
        invoiceBoarding(); // need to re-invoice any boarding charges that may not have been saved
    }

    /**
     * Invoices any boarding charges.
     */
    protected void invoiceBoarding() {
        BoardingInvoicer invoicer = new BoardingInvoicer();
        invoicer.invoice(getEditor().getVisits(), getEditor());
    }

}
