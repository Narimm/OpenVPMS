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

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;

/**
 * Invoices <em>act.customerOrderPharmacy</em> orders.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderInvoicer extends AbstractInvoicer {

    /**
     * Creates an invoice for a pharmacy order.
     * <p/>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     *
     * @param order   the order to invoice
     * @param invoice the invoice to add to, or {@code null} to create a new invoice
     * @param context the layout context
     * @return an editor for the invoice, or {@code null} if the editor cannot be created
     * @throws OpenVPMSException for any error
     */
    public CustomerChargeActEditDialog invoice(Act order, FinancialAct invoice, LayoutContext context) {
        order.setStatus(ActStatus.POSTED);
        ActBean orderBean = new ActBean(order);

        if (invoice == null) {
            invoice = createInvoice(orderBean.getNodeParticipantRef("customer"));
        }

        CustomerChargeActEditor editor = createChargeEditor(invoice, context);

        // NOTE: need to display the dialog as the process of populating medications and reminders can display
        // popups which would parent themselves on the wrong window otherwise.
        InvoicerDialog dialog = new InvoicerDialog(editor, order, context.getContext());
        dialog.show();
        invoice(order, editor);
        return dialog;
    }

    /**
     * Invoices an order.
     * <p/>
     * Note that the caller is responsible for saving the order.
     *
     * @param order  the order to invoice
     * @param editor the editor to add invoice items to
     */
    public void invoice(Act order, AbstractCustomerChargeActEditor editor) {
        ActBean bean = new ActBean(order);
        ActRelationshipCollectionEditor items = editor.getItems();

        for (Act orderItem : bean.getNodeActs("items")) {
            ActBean itemBean = new ActBean(orderItem);
            CustomerChargeActItemEditor itemEditor = getItemEditor(editor);
            itemEditor.setPatientRef(itemBean.getNodeParticipantRef("patient"));
            itemEditor.setQuantity(itemBean.getBigDecimal("quantity"));
            itemEditor.setProductRef(itemBean.getNodeParticipantRef("product"));
        }
        items.refresh();
    }

    /**
     * Creates a new {@link CustomerChargeActEditor}.
     *
     * @param invoice the invoice
     * @param context the layout context
     * @return a new charge editor
     */
    protected CustomerChargeActEditor createChargeEditor(FinancialAct invoice, LayoutContext context) {
        return new CustomerChargeActEditor(invoice, null, context, false);
    }

}
