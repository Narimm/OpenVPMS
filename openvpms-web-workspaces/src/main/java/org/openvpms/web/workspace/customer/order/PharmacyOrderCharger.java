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
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.workspace.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * Related invoices references.
     */
    private final Set<IMObjectReference> invoices;

    /**
     * The related invoice, if there is only related invoice.
     */
    private final FinancialAct invoice;


    /**
     * Constructs a {@link PharmacyOrderCharger}.
     *
     * @param act   the order/return act
     * @param rules the order rules
     */
    public PharmacyOrderCharger(FinancialAct act, OrderRules rules) {
        this.act = act;
        ActBean bean = new ActBean(act);
        customer = bean.getNodeParticipantRef("customer");
        items = new ArrayList<Item>();
        for (FinancialAct item : bean.getNodeActs("items", FinancialAct.class)) {
            items.add(new Item(item, rules.getInvoiceItem(item)));
        }

        // order items should only refer to a single invoice
        invoices = new HashSet<IMObjectReference>();
        for (Item item : items) {
            IMObjectReference invoice = item.getInvoice();
            if (invoice != null) {
                invoices.add(invoice);
            }
        }
        if (invoices.size() == 1) {
            invoice = (FinancialAct) IMObjectHelper.getObject(invoices.iterator().next(), null);
        } else {
            invoice = null;
        }
    }

    /**
     * Returns the customer.
     *
     * @return the customer reference
     */
    public IMObjectReference getCustomer() {
        return customer;
    }

    /**
     * Returns the original invoice.
     *
     * @return the original invoice. May be {@code null}
     */
    public FinancialAct getInvoice() {
        return invoice;
    }

    /**
     * Determines if the order can be charged.
     *
     * @return {@code true} if the order can be charged
     */
    public boolean isValid() {
        boolean result = !items.isEmpty() && customer != null && invoices.size() <= 1;
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
     * Determines if the order or return can be used to update the specified editor.
     *
     * @param editor the editor
     * @return {@code true}  if the order or return can update the editor
     */
    public boolean canCharge(AbstractCustomerChargeActEditor editor) {
        boolean result = false;
        IMObjectReference charge = editor.getObject().getObjectReference();
        if (invoice != null && invoice.getId() == charge.getId()) {
            result = true;
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
    public CustomerChargeActEditDialog charge(FinancialAct charge, OrderCharger charger, LayoutContext context) {
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
        CustomerChargeActEditDialog dialog = new CustomerChargeActEditDialog(editor, charger, context.getContext());
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
    public void charge(AbstractCustomerChargeActEditor editor) {
        if (!isValid()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        doCharge(editor);
    }

    /**
     * Determines if the order/return can be invoiced, based on the original invoice.
     *
     * @return {@code true} if the order/return can be invoiced
     */
    public boolean canInvoice() {
        return canCharge(true);
    }

    /**
     * Determines if the order/return can be credited.
     *
     * @return {@code true} if the order/return can be credited
     */
    public boolean canCredit() {
        return canCharge(false);
    }

    /**
     * Charges an order/return.
     * <p/>
     * Note that the caller is responsible for saving the act.
     *
     * @param editor the editor to add invoice items to
     */
    private void doCharge(AbstractCustomerChargeActEditor editor) {
        ActRelationshipCollectionEditor items = editor.getItems();

        for (Item item : this.items) {
            if (item.needsCharge()) {
                CustomerChargeActItemEditor itemEditor = item.getCurrentInvoiceEditor(editor);
                if (itemEditor == null) {
                    itemEditor = getItemEditor(editor);
                }
                item.charge(editor, itemEditor);
            }
        }
        act.setStatus(ActStatus.POSTED);
        items.refresh();
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

    private boolean canCharge(boolean invoice) {
        boolean result = true;
        for (Item item : items) {
            if (item.getNewQuantity(item.invoiceQty, invoice).compareTo(BigDecimal.ZERO) < 0) {
                result = false;
                break;
            }
        }
        return result;
    }

    private class Item {

        final IMObjectReference patient;
        final BigDecimal quantity;
        final IMObjectReference product;
        final IMObjectReference clinician;
        final FinancialAct invoiceItem;
        final BigDecimal invoiceQty;
        final boolean isOrder;

        public Item(FinancialAct orderItem, FinancialAct invoiceItem) {
            ActBean bean = new ActBean(orderItem);
            this.patient = bean.getNodeParticipantRef("patient");
            this.product = bean.getNodeParticipantRef("product");
            this.clinician = bean.getNodeParticipantRef("clinician");
            this.quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
            this.invoiceItem = invoiceItem;
            isOrder = !orderItem.isCredit();
            invoiceQty = (invoiceItem != null) ? invoiceItem.getQuantity() : BigDecimal.ZERO;
        }

        public boolean isValid() {
            return patient != null && quantity != null && product != null &&
                   TypeHelper.isA(product, ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE);
        }

        public boolean needsCharge() {
            return quantity.compareTo(invoiceQty) != 0;
        }

        public void charge(AbstractCustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor) {
            FinancialAct object = (FinancialAct) itemEditor.getObject();
            BigDecimal newQuantity = getNewQuantity(object);
            if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                editor.removeItem((Act) itemEditor.getObject());
            } else {
                itemEditor.setPatientRef(patient);
                itemEditor.setClinicianRef(clinician);
                itemEditor.setQuantity(newQuantity);
                itemEditor.setProductRef(product); // TODO - protect against product change
                editor.setOrdered((Act) itemEditor.getObject());
            }
        }

        public boolean hasPatient(IMObjectReference patient) {
            return ObjectUtils.equals(patient, this.patient);
        }

        public IMObjectReference getInvoice() {
            IMObjectReference result = null;
            if (invoiceItem != null) {
                IMObjectBean bean = new IMObjectBean(invoiceItem);
                result = bean.getSourceObjectRef(invoiceItem.getTargetActRelationships(),
                                                 CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP);
            }
            return result;
        }

        public FinancialAct getCurrentInvoiceItem(AbstractCustomerChargeActEditor editor) {
            FinancialAct result = null;
            if (invoiceItem != null) {
                List<Act> acts = editor.getItems().getCurrentActs();
                int index = acts.indexOf(invoiceItem);
                if (index != -1) {
                    result = (FinancialAct) acts.get(index);
                }
            }
            return result;
        }

        public BigDecimal getNewQuantity(FinancialAct current) {
            BigDecimal currentQty = current.getQuantity() != null ? current.getQuantity() : BigDecimal.ZERO;
            return getNewQuantity(currentQty, !current.isCredit());
        }

        private BigDecimal getNewQuantity(BigDecimal current, boolean invoice) {
            return (isOrder && invoice || (!isOrder && !invoice)) ? quantity : current.subtract(quantity);
        }

        public CustomerChargeActItemEditor getCurrentInvoiceEditor(AbstractCustomerChargeActEditor editor) {
            FinancialAct current = getCurrentInvoiceItem(editor);
            if (current != null) {
                return (CustomerChargeActItemEditor) editor.getItems().getEditor(current);
            }
            return null;
        }
    }

}
