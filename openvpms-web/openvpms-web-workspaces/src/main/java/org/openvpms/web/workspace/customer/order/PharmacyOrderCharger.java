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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

/**
 * This class is responsible for creating charges from <em>act.customerOrderPharmacy</em> and
 * <em>act.customerReturnPharmacy</em> acts.
 * <p/>
 * NOTE that there is limited support to charge orders and returns when the existing invoice has been POSTED.
 * <p/>
 * In this case, the difference will be charged. This does not take into account multiple orders/returnes for the one
 * POSTED invoice.
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
     * The related invoice, if there is only one related invoice.
     */
    private final FinancialAct invoice;

    /**
     * The related invoices.
     */
    private final Map<IMObjectReference, FinancialAct> invoices = new HashMap<IMObjectReference, FinancialAct>();


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
            FinancialAct invoiceItem = rules.getInvoiceItem(item);
            FinancialAct invoice = null;
            if (invoiceItem != null) {
                invoice = getInvoice(invoiceItem, invoices);
            }
            items.add(new Item(item, invoiceItem, invoice));
        }

        invoice = (invoices.size() == 1) ? invoices.values().iterator().next() : null;
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
        if (invoice != null && invoice.getId() == charge.getId() && !ActStatus.POSTED.equals(editor.getStatus())) {
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
     * @param charger the order charger, used to manage charging multiple orders. May be {@code null}
     * @param context the layout context
     * @return an editor for the charge, or {@code null} if the editor cannot be created
     * @throws IllegalStateException if the order cannot be invoiced
     * @throws OpenVPMSException     for any error
     */
    public CustomerChargeActEditDialog charge(FinancialAct charge, OrderCharger charger, LayoutContext context) {
        if (charge != null && ActStatus.POSTED.equals(charge.getStatus())) {
            throw new IllegalStateException("Cannot charge orders/returns to POSTED " + charge.getArchetypeId());
        }
        if (!isValid()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        if (charge == null) {
            if (canInvoice()) {
                charge = createInvoice(customer);
            } else if (canCredit()) {
                charge = createCharge(CustomerAccountArchetypes.CREDIT, customer);
            } else {
                throw new IllegalStateException("Can neither invoice nor credit the " + act.getArchetypeId());
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
     * Charges an order or return.
     * <p/>
     * Note that the caller is responsible for saving the order/return.
     *
     * @param editor the editor to add invoice items to
     * @throws IllegalStateException if the editor cannot be used, or the order/return is invalid
     */
    public void charge(AbstractCustomerChargeActEditor editor) {
        if (!canCharge(editor)) {
            throw new IllegalStateException("Cannot charge " + act.getArchetypeId() + " to editor");
        }
        if (!isValid()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        doCharge(editor);
    }

    /**
     * Determines if the order/return can be invoiced..
     *
     * @return {@code true} if the order/return can be invoiced
     */
    public boolean canInvoice() {
        boolean result = true;
        for (Item item : items) {
            if (!item.canInvoice()) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Determines if the order/return can be credited.
     * <p/>
     * Orders can only be credited if they apply to an existing invoice that has been posted,
     * and the quantity is less than that invoiced.
     *
     * @return {@code true} if the return can be credited, {@code false} if it cannot
     */
    public boolean canCredit() {
        boolean result = true;
        for (Item item : items) {
            if (!item.canCredit()) {
                result = false;
                break;
            }
        }
        return result;
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
            FinancialAct current = item.getCurrentInvoiceItem(editor);
            CustomerChargeActItemEditor itemEditor;
            if (current != null) {
                itemEditor = getItemEditor(current, editor);
            } else {
                itemEditor = getItemEditor(editor);
            }
            item.charge(editor, itemEditor);
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

    /**
     * Returns the invoice associated with an invoice item.
     *
     * @param invoiceItem the invoice item
     * @param invoices    cache of invoices, keyed on reference
     * @return the corresponding invoice or {@code null} if none is found
     */
    private FinancialAct getInvoice(FinancialAct invoiceItem, Map<IMObjectReference, FinancialAct> invoices) {
        FinancialAct invoice = null;
        IMObjectBean bean = new IMObjectBean(invoiceItem);
        IMObjectReference ref = bean.getSourceObjectRef(invoiceItem.getTargetActRelationships(),
                                                        CustomerAccountArchetypes.INVOICE_ITEM_RELATIONSHIP);
        if (ref != null) {
            invoice = invoices.get(ref);
            if (invoice == null) {
                invoice = (FinancialAct) IMObjectHelper.getObject(ref, null);
                if (invoice != null) {
                    invoices.put(ref, invoice);
                }
            }
        }
        return invoice;

    }

    private class Item {

        final IMObjectReference patient;
        final BigDecimal quantity;
        final IMObjectReference product;
        final IMObjectReference clinician;
        final FinancialAct invoiceItem;
        final BigDecimal invoiceQty;
        final boolean isOrder;
        final boolean posted;

        public Item(FinancialAct orderItem, FinancialAct invoiceItem, FinancialAct invoice) {
            ActBean bean = new ActBean(orderItem);
            this.patient = bean.getNodeParticipantRef("patient");
            this.product = bean.getNodeParticipantRef("product");
            this.clinician = bean.getNodeParticipantRef("clinician");
            this.quantity = bean.getBigDecimal("quantity", ZERO);
            this.invoiceItem = invoiceItem;
            isOrder = !orderItem.isCredit();
            invoiceQty = (invoiceItem != null) ? invoiceItem.getQuantity() : ZERO;
            posted = (invoice != null) && ActStatus.POSTED.equals(invoice.getStatus());
        }

        public boolean isValid() {
            return patient != null && quantity != null && product != null &&
                   TypeHelper.isA(product, ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE);
        }

        public boolean canInvoice() {
            boolean result;
            if (isOrder) {
                result = !(invoiceItem != null && posted) || quantity.compareTo(invoiceQty) >= 0;
            } else if (invoiceItem == null) {
                // no existing invoice to add the return to
                result = false;
            } else if (!posted) {
                // can only add the return if the associated invoice isn't posted
                IMObjectBean bean = new IMObjectBean(invoiceItem);
                BigDecimal receivedQuantity = bean.getBigDecimal("receivedQuantity", ZERO);
                BigDecimal returnedQuantity = bean.getBigDecimal("returnedQuantity", ZERO);
                BigDecimal newQuantity = receivedQuantity.subtract(returnedQuantity).subtract(quantity);
                result = newQuantity.compareTo(ZERO) >= 0;
            } else {
                result = false;
            }
            return result;
        }

        /**
         * Determines if an order or return can be credited.
         *
         * @return {@code true} if it can be credited, otherwise {@code false}
         */
        public boolean canCredit() {
            return !isOrder || (invoiceItem != null && (invoiceQty.compareTo(quantity) > 0));
        }

        public void charge(AbstractCustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor) {
            FinancialAct object = (FinancialAct) itemEditor.getObject();
            if (TypeHelper.isA(object, CustomerAccountArchetypes.INVOICE_ITEM)) {
                BigDecimal received = itemEditor.getReceivedQuantity();
                BigDecimal returned = itemEditor.getReturnedQuantity();
                BigDecimal newInvoiceQty;
                if (isOrder) {
                    received = received.add(quantity);
                    if (invoiceItem != null && posted) {
                        // the original invoice has been posted, so invoice the difference
                        // NOTE that if the new quantity is less than that invoiced, canInvoice() should have
                        // returned false
                        newInvoiceQty = received.subtract(invoiceQty).subtract(returned).max(ZERO);
                    } else {
                        newInvoiceQty = received;
                    }
                    itemEditor.setReceivedQuantity(received);
                } else {
                    returned = returned.add(quantity);
                    itemEditor.setReturnedQuantity(returned);
                    newInvoiceQty = received.subtract(returned).max(ZERO);
                }
                itemEditor.setQuantity(newInvoiceQty);
            } else {
                itemEditor.setQuantity(quantity);
            }
            itemEditor.setPatientRef(patient);
            if (clinician != null) {
                itemEditor.setClinicianRef(clinician);
            }
            itemEditor.setProductRef(product); // TODO - protect against product change
            editor.setOrdered((Act) itemEditor.getObject());
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

    }

}
