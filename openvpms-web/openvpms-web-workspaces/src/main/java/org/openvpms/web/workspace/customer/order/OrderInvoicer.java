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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.DefaultCustomerChargeActEditDialog;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.ZERO;

/**
 * This class is responsible for creating charges from <em>act.customerOrder*</em> and <em>act.customerReturn*</em>
 * acts.
 *
 * @author Tim Anderson
 */
public abstract class OrderInvoicer extends AbstractInvoicer {

    /**
     * The order/return.
     */
    private final FinancialAct act;

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
    private final Map<IMObjectReference, FinancialAct> invoices = new HashMap<>();

    /**
     * The properties, used for validation.
     */
    private final PropertySet properties;


    /**
     * Constructs a {@link OrderInvoicer}.
     *
     * @param act   the order/return act
     * @param rules the order rules
     */
    public OrderInvoicer(FinancialAct act, OrderRules rules) {
        this.act = act;
        ActBean bean = new ActBean(act);
        customer = bean.getNodeParticipantRef("customer");
        items = new ArrayList<>();

        for (FinancialAct item : bean.getNodeActs("items", FinancialAct.class)) {
            FinancialAct invoiceItem = rules.getInvoiceItem(item);
            FinancialAct invoice = null;
            if (invoiceItem != null) {
                invoice = getInvoice(invoiceItem, invoices);
            }
            items.add(createItem(item, invoiceItem, invoice));
        }

        invoice = (invoices.size() == 1) ? invoices.values().iterator().next() : null;
        properties = new PropertySet(act);
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
        return validate(new DefaultValidator());
    }

    /**
     * Determines if the order can be charged.
     *
     * @param validator the validator
     * @return {@code true} if the order can be charged
     */
    public boolean validate(Validator validator) {
        boolean valid = false;
        Property property = properties.get("items");
        if (items.isEmpty()) {
            String message = Messages.format("property.error.minSize", property.getDisplayName(), 1);
            validator.add(property, new ValidatorError(property, message));
        } else if (invoices.size() > 1) {
            validator.add(property, new ValidatorError(Messages.get("customer.order.invoice.unsupported")));
        } else {
            valid = validateRequired(validator, properties, "customer", customer);
        }
        if (valid) {
            for (Item item : items) {
                valid = item.validate(validator);
                if (!valid) {
                    break;
                }
            }
        }
        return valid;
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
    public boolean canCharge(CustomerChargeActEditor editor) {
        boolean result = false;
        FinancialAct charge = editor.getObject();
        if (!ActStatus.POSTED.equals(editor.getStatus())) {
            if (invoice != null && invoice.getId() == charge.getId()) {
                result = true;
            } else if (invoice == null && charge.isCredit() == act.isCredit()) {
                result = true;
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
        CustomerChargeActEditDialog dialog = new DefaultCustomerChargeActEditDialog(editor, charger,
                                                                                    context.getContext(), false);
        dialog.show();
        doCharge(editor);
        return dialog;
    }

    /**
     * Determines if an order/return must charged via an editor.
     *
     * @return {@code true} if an editor is required
     */
    public boolean requiresEdit() {
        for (Item item : items) {
            if (item.requiresEdit()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Charges an order/return.
     * <p/>
     * This should only be invoked if {@link #requiresEdit()} has returned {@code false}
     */
    public void charge() {
        List<FinancialAct> updated = new ArrayList<>();
        for (Item item : items) {
            if (!item.updateReceivedQuantity()) {
                throw new IllegalStateException("Failed to update received quantity");
            }
            updated.add(item.getInvoiceItem());
        }
        act.setStatus(ActStatus.POSTED);
        updated.add(act);
        ServiceHelper.getArchetypeService(false).save(updated);
    }

    /**
     * Charges an order or return.
     * <p/>
     * Note that the caller is responsible for saving the order/return.
     *
     * @param editor the editor to add invoice items to
     * @throws IllegalStateException if the editor cannot be used, or the order/return is invalid
     */
    public void charge(CustomerChargeActEditor editor) {
        if (!canCharge(editor)) {
            throw new IllegalStateException("Cannot charge " + act.getArchetypeId() + " to editor");
        }
        if (!isValid()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        doCharge(editor);
    }

    /**
     * Determines if the order/return can be invoiced.
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

    protected Item createItem(FinancialAct item, FinancialAct invoiceItem, FinancialAct invoice) {
        return new Item(item, invoiceItem, invoice);
    }

    /**
     * Helper to validate if a required property is set.
     *
     * @param validator  the validator
     * @param properties the properties
     * @param name       the property name
     * @param value      the property value
     * @return {@code true} if the property is set, otherwise {@code false}
     */
    protected static boolean validateRequired(Validator validator, PropertySet properties, String name, Object value) {
        boolean valid = false;
        if (value != null) {
            valid = true;
        } else {
            Property property = properties.get(name);
            validator.add(property, new ValidatorError(property, Messages.format("property.error.required",
                                                                                 property.getDisplayName())));
        }
        return valid;
    }

    /**
     * Charges an order/return.
     * <p/>
     * Note that the caller is responsible for saving the act.
     *
     * @param editor the editor to add invoice items to
     */
    private void doCharge(CustomerChargeActEditor editor) {
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
                invoice = (FinancialAct) IMObjectHelper.getObject(ref);
                if (invoice != null) {
                    invoices.put(ref, invoice);
                }
            }
        }
        return invoice;
    }

    protected class Item {

        private final Date startTime;
        private final IMObjectReference patient;
        private final BigDecimal quantity;
        private final IMObjectReference product;
        private final IMObjectReference clinician;
        private final FinancialAct invoiceItem;
        private final BigDecimal invoiceQty;
        private final BigDecimal receivedQty;
        private final BigDecimal returnedQty;
        private final boolean isOrder;
        private final boolean posted;
        private final PropertySet properties;

        public Item(FinancialAct orderItem, FinancialAct invoiceItem, FinancialAct invoice) {
            ActBean bean = new ActBean(orderItem);
            this.startTime = orderItem.getActivityStartTime();
            this.patient = bean.getNodeParticipantRef("patient");
            this.product = bean.getNodeParticipantRef("product");
            this.clinician = bean.getNodeParticipantRef("clinician");
            this.quantity = bean.getBigDecimal("quantity", ZERO);
            this.invoiceItem = invoiceItem;
            isOrder = !orderItem.isCredit();
            if (invoiceItem != null) {
                invoiceQty = invoiceItem.getQuantity();
                ActBean invoiceBean = new ActBean(invoiceItem);
                receivedQty = invoiceBean.getBigDecimal("receivedQuantity", ZERO);
                returnedQty = invoiceBean.getBigDecimal("returnedQuantity", ZERO);
            } else {
                invoiceQty = ZERO;
                receivedQty = ZERO;
                returnedQty = ZERO;
            }
            posted = (invoice != null) && ActStatus.POSTED.equals(invoice.getStatus());
            properties = new PropertySet(orderItem);
        }

        public boolean isOrder() {
            return isOrder;
        }

        public boolean isPosted() {
            return posted;
        }

        public FinancialAct getInvoiceItem() {
            return invoiceItem;
        }

        public IMObjectReference getProduct() {
            return product;
        }

        public boolean isValid() {
            return validate(new DefaultValidator());
        }


        public boolean validate(Validator validator) {
            return validateRequired(validator, "patient", patient)
                   && validateRequired(validator, "quantity", quantity)
                   && validateRequired(validator, "product", product)
                   && validateProduct(validator);
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
                BigDecimal newQuantity = receivedQty.subtract(returnedQty).subtract(quantity);
                result = newQuantity.compareTo(ZERO) >= 0;
            } else {
                result = false;
            }
            return result;
        }

        /**
         * Determines if the charging must be performed in an editor.
         *
         * @return {@code true} if the charging must be performed in an editor
         */
        public boolean requiresEdit() {
            boolean result = true;
            if (canInvoice()) {
                if (receivedQty.subtract(returnedQty).add(quantity).compareTo(invoiceQty) <= 0) {
                    result = !sameDetails();
                }
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

        /**
         * Updates the received quantity on a POSTED invoice, iff it doesn't exceed the ordered quantity.
         * <p/>
         * NOTE: the caller is responsible for saving the item, ensuring that business rules aren't invoked
         *
         * @return {@code true} if the received quantity was updated
         */
        public boolean updateReceivedQuantity() {
            boolean result = false;
            if (isOrder && invoiceItem != null && posted) {
                ActBean bean = new ActBean(invoiceItem);
                BigDecimal received = receivedQty.add(quantity);
                if (received.subtract(returnedQty).compareTo(invoiceQty) <= 0) {
                    bean.setValue("receivedQuantity", received);
                    result = true;
                }
            }
            return result;
        }

        public void charge(CustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor) {
            FinancialAct object = (FinancialAct) itemEditor.getObject();
            itemEditor.setStartTime(startTime);
            itemEditor.setPatientRef(patient);
            itemEditor.setProductRef(product); // TODO - protect against product change
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
            if (clinician != null) {
                itemEditor.setClinicianRef(clinician);
            }
            editor.setOrdered(itemEditor.getObject());
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

        public FinancialAct getCurrentInvoiceItem(CustomerChargeActEditor editor) {
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

        /**
         * Determines if the order has the same patient and product as the original invoice.
         *
         * @return {@code true} if the patient and product are the same
         */
        public boolean sameDetails() {
            if (invoiceItem != null) {
                ActBean bean = new ActBean(invoiceItem);
                return ObjectUtils.equals(bean.getNodeParticipantRef("patient"), patient)
                       && ObjectUtils.equals(bean.getNodeParticipantRef("product"), product);
            }
            return false;
        }

        protected boolean validateRequired(Validator validator, String name, Object value) {
            return OrderInvoicer.validateRequired(validator, properties, name, value);
        }

        /**
         * Ensures that the product is one of {@link #getProductArchetypes()}.
         *
         * @param validator the validator
         * @return {@code true} if the product is valid
         */
        protected boolean validateProduct(Validator validator) {
            boolean valid = false;
            if (TypeHelper.isA(product, getProductArchetypes())) {
                valid = true;
            } else {
                Property property = properties.get("product");
                String msg = Messages.format("imobject.invalidreference", property.getDisplayName());
                validator.add(property, new ValidatorError(property, msg));
            }
            return valid;
        }

        protected String[] getProductArchetypes() {
            return new String[]{ProductArchetypes.MEDICATION, ProductArchetypes.MERCHANDISE, ProductArchetypes.SERVICE};
        }

    }

}
