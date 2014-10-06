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
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
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
 * This class is responsible for creating charges from <em>act.customerOrderPharmacy</em> and
 * <em>act.customerReturnPharmacy</em> acts.
 *
 * @author Tim Anderson
 */
public class PharmacyOrderCharger extends AbstractInvoicer {

    /**
     * The order/return.
     */
    private final Act act;

    /**
     * The customer.
     */
    private final IMObjectReference customer;

    /**
     * The order/return items.
     */
    private final List<Item> items;

    /**
     * Constructs a {@link PharmacyOrderCharger}.
     *
     * @param act the order/return act
     */
    public PharmacyOrderCharger(Act act) {
        this.act = act;
        ActBean bean = new ActBean(act);
        customer = bean.getNodeParticipantRef("customer");
        items = new ArrayList<Item>();
        for (Act item : bean.getNodeActs("items")) {
            items.add(new Item(item));
        }
    }

    /**
     * Determines if the order can be charged.
     *
     * @return {@code true} if the order can be charged
     */
    public boolean isValid() {
        boolean result = !items.isEmpty() && customer != null;
        if (result) {
            for (Item item : items) {
                result = item.isValid();
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if the order or return can be charged to a single patient.
     *
     * @param patient the patient
     * @return {@code true} if the act can be charged to the patient
     */
    public boolean canCharge(Party patient) {
        boolean result = isValid();
        if (result) {
            IMObjectReference ref = patient.getObjectReference();
            for (Item item : items) {
                result = item.hasPatient(ref);
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Creates charge for the pharmacy order.
     * <p/>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     *
     * @param charge  the charge to add to, or {@code null} to create a new charge
     * @param context the layout context
     * @return an editor for the charge, or {@code null} if the editor cannot be created
     * @throws IllegalStateException if the order cannot be invoiced
     * @throws OpenVPMSException     for any error
     */
    public CustomerChargeActEditDialog charge(FinancialAct charge, LayoutContext context) {
        if (!isValid()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        if (charge == null) {
            if (TypeHelper.isA(act, OrderArchetypes.PHARMACY_ORDER)) {
                charge = createInvoice(customer);
            } else {
                charge = createCharge(CustomerAccountArchetypes.CREDIT, customer);
            }
        }

        CustomerChargeActEditor editor = createChargeEditor(charge, context);

        // NOTE: need to display the dialog as the process of populating medications and reminders can display
        // popups which would parent themselves on the wrong window otherwise.
        ChargeDialog dialog = new ChargeDialog(editor, act, context.getContext());
        dialog.show();
        doCharge(editor);
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
        if (!isValid()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        doCharge(editor);
    }

    /**
     * Charges an order/return.
     * <p/>
     * Note that the caller is responsible for saving the act.
     *
     * @param editor the editor to add invoice items to
     */
    private void doCharge(AbstractCustomerChargeActEditor editor) {
        ActRelationshipCollectionEditor charges = editor.getItems();

        for (Item item : items) {
            CustomerChargeActItemEditor itemEditor = getItemEditor(editor);
            item.invoice(editor, itemEditor);
        }
        act.setStatus(ActStatus.POSTED);
        charges.refresh();
    }

    /**
     * Creates a new {@link CustomerChargeActEditor}.
     *
     * @param charge  the charge
     * @param context the layout context
     * @return a new charge editor
     */
    protected CustomerChargeActEditor createChargeEditor(FinancialAct charge, LayoutContext context) {
        return new CustomerChargeActEditor(charge, null, context, false);
    }

    private class Item {

        final IMObjectReference patient;
        final BigDecimal quantity;
        final IMObjectReference product;
        final IMObjectReference clinician;

        public Item(Act item) {
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
