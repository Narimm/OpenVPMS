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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoices <em>act.customerOrderPharmacy</em> orders.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderInvoicer extends AbstractInvoicer {

    private final Act order;
    private final IMObjectReference customer;
    private final List<OrderItem> items;

    public PharmacyOrderInvoicer(Act order) {
        this.order = order;
        ActBean bean = new ActBean(order);
        customer = bean.getNodeParticipantRef("customer");
        items = new ArrayList<OrderItem>();
        for (Act act : bean.getNodeActs("items")) {
            OrderItem item = new OrderItem(act);
            items.add(item);
        }
    }

    /**
     * Determines if the order can be invoiced.
     *
     * @return {@code true} if the order can be invoiced
     */
    public boolean canInvoice() {
        boolean invoice = !items.isEmpty() && customer != null;
        if (invoice) {
            for (OrderItem item : items) {
                invoice = item.isValid();
                if (!invoice) {
                    break;
                }
            }
        }
        return invoice;
    }

    /**
     * Determines if ther order can be invoiced to a single patient.
     *
     * @param patient the patient
     * @return {@code true} if the order can be invoiced to the patient
     */
    public boolean canInvoice(Party patient) {
        boolean invoice = canInvoice();
        if (invoice) {
            IMObjectReference ref = patient.getObjectReference();
            for (OrderItem item : items) {
                invoice = item.hasPatient(ref);
                if (!invoice) {
                    break;
                }
            }
        }
        return invoice;
    }

    /**
     * Creates an invoice for the pharmacy order.
     * <p/>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     *
     * @param invoice the invoice to add to, or {@code null} to create a new invoice
     * @param context the layout context
     * @return an editor for the invoice, or {@code null} if the editor cannot be created
     * @throws IllegalStateException if the order cannot be invoiced
     * @throws OpenVPMSException     for any error
     */
    public CustomerChargeActEditDialog invoice(FinancialAct invoice, LayoutContext context) {
        if (!canInvoice()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        ActBean orderBean = new ActBean(order);

        if (invoice == null) {
            invoice = createInvoice(orderBean.getNodeParticipantRef("customer"));
        }

        CustomerChargeActEditor editor = createChargeEditor(invoice, context);

        // NOTE: need to display the dialog as the process of populating medications and reminders can display
        // popups which would parent themselves on the wrong window otherwise.
        InvoicerDialog dialog = new InvoicerDialog(editor, order, context.getContext());
        dialog.show();
        doInvoice(editor);
        return dialog;
    }

    /**
     * Invoices an order.
     * <p/>
     * Note that the caller is responsible for saving the order.
     *
     * @param editor the editor to add invoice items to
     */
    public void invoice(AbstractCustomerChargeActEditor editor) {
        if (!canInvoice()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        doInvoice(editor);
    }

    /**
     * Invoices an order.
     * <p/>
     * Note that the caller is responsible for saving the order.
     *
     * @param editor the editor to add invoice items to
     */
    private void doInvoice(AbstractCustomerChargeActEditor editor) {
        ActRelationshipCollectionEditor charges = editor.getItems();

        for (OrderItem item : items) {
            CustomerChargeActItemEditor itemEditor = getItemEditor(editor);
            item.invoice(editor, itemEditor);
        }
        order.setStatus(ActStatus.POSTED);
        charges.refresh();
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

    private class OrderItem {

        final IMObjectReference patient;
        final BigDecimal quantity;
        final IMObjectReference product;
        final IMObjectReference clinician;

        public OrderItem(Act item) {
            ActBean bean = new ActBean(item);
            this.patient = bean.getNodeParticipantRef("patient");
            this.quantity = bean.getBigDecimal("quantity");
            this.product = bean.getNodeParticipantRef("product");
            this.clinician = bean.getNodeParticipantRef("clinician");
        }

        public boolean isValid() {
            return patient != null && quantity != null && product != null;
        }

        public void invoice(AbstractCustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor) {
            itemEditor.setPatientRef(patient);
            itemEditor.setQuantity(quantity);
            itemEditor.setProductRef(product);
            itemEditor.setClinicianRef(clinician);
            editor.setOrdered((Act) itemEditor.getObject());
        }

        public boolean hasPatient(IMObjectReference patient) {
            return ObjectUtils.equals(patient, this.patient);
        }
    }

}
