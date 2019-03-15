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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.checkout;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.AlertManager;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.im.view.IMObjectViewerDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.order.PendingOrderQuery;

import java.util.Iterator;

/**
 * A dialog to view an invoice.
 * <p/>
 * This includes alerts if there are boarding charges or pharmacy orders/returns to invoice.
 *
 * @author Tim Anderson
 */
class InvoiceViewerDialog extends IMObjectViewerDialog {

    /**
     * The alerts.
     */
    private final AlertManager alerts;

    /**
     * New button identifier.
     */
    static final String NEW_ID = "button.new";

    /**
     * The buttons to display.
     */
    private static final String[] BUTTONS = {NEW_ID, OK_ID, CANCEL_ID};

    /**
     * Constructs an {@link InvoiceViewerDialog}.
     *
     * @param invoice the invoice
     * @param visits  the visits
     * @param context the context
     * @param help    the help context
     */
    InvoiceViewerDialog(FinancialAct invoice, Visits visits, Context context, HelpContext help) {
        super(null, BUTTONS, context, null, help);
        setViewer(new IMObjectViewer(invoice, null, new DefaultLayoutContext(context, help)));
        alerts = new AlertManager(getContentPane(), 3);

        Party customer = context.getCustomer();
        PendingOrderQuery query = new PendingOrderQuery(customer);
        Iterator<Act> iterator = query.iterator();
        alerts.show(Messages.get("workflow.checkout.invoice.posted"));
        if (iterator.hasNext()) {
            alerts.show(Messages.format("customer.order.pending", customer.getName()));
        }
        for (Visit visit : visits) {
            if (visit.needsCharge()) {
                alerts.show(Messages.get("workflow.checkout.invoice.boarding"));
                break;
            }
        }
    }

    /**
     * Invoked when the 'close' button is pressed. This sets the action to CLOSE and closes the window.
     */
    @Override
    protected void onClose() {
        alerts.clear();
        super.onClose();
    }
}
