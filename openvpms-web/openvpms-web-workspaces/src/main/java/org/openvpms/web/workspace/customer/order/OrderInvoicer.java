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

package org.openvpms.web.workspace.customer.order;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.PropertySetBuilder;
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
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;

/**
 * This class is responsible for creating charges from <em>act.customerOrder*</em> and <em>act.customerReturn*</em>
 * acts.
 *
 * @author Tim Anderson
 */
public abstract class OrderInvoicer extends AbstractInvoicer {

    public static class Status {

        private final String reason;

        private Status(String reason) {
            this.reason = reason;
        }

        public boolean canCharge() {
            return reason == null;
        }

        public String getReason() {
            return reason;
        }

        public static Status valid() {
            return new Status(null);
        }

        public static Status invalid(String reason) {
            if (reason == null) {
                throw new IllegalArgumentException("Argument 'reason' is null");
            }
            return new Status(reason);
        }

    }

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
        IArchetypeRuleService service = ServiceHelper.getArchetypeService();

        for (FinancialAct item : bean.getNodeActs("items", FinancialAct.class)) {
            Reference invoiceItemRef = rules.getInvoiceItemRef(item);
            FinancialAct invoiceItem = (invoiceItemRef != null) ? (FinancialAct) service.get(invoiceItemRef) : null;
            FinancialAct invoice = null;
            if (invoiceItem != null) {
                invoice = getInvoice(invoiceItem, invoices);
            }
            boolean ordered = invoiceItemRef != null;
            items.add(createItem(item, ordered, invoiceItem, invoice));
        }

        invoice = (invoices.size() == 1) ? invoices.values().iterator().next() : null;
        properties = new PropertySetBuilder(act).build();
    }

    /**
     * Determines if the act is an order.
     *
     * @return {@code true} if it is an order, {@code false} if it is an order return
     */
    public boolean isOrder() {
        return !act.isCredit();
    }

    /**
     * Determines if the act is an order return.
     *
     * @return {@code true} if it is an order return, {@code false} if it is an order
     */
    public boolean isOrderReturn() {
        return !isOrder();
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
        return getChargeStatus(editor).canCharge();
    }

    public Status getChargeStatus(CustomerChargeActEditor editor) {
        Status result;
        FinancialAct charge = editor.getObject();
        if (!ActStatus.POSTED.equals(editor.getStatus())) {
            if (invoice != null && invoice.getId() == charge.getId()) {
                result = Status.valid();
            } else if (invoice == null) {
                if (charge.isCredit() == act.isCredit()) {
                    // orders can always be applied to invoices, and order returns can always be applied to
                    // credits
                    result = Status.valid();
                } else if (charge.isA(INVOICE) && isOrderReturn() && notFromInvoice()) {
                    // can only apply an order return not related to an invoice if there are line items it can update
                    Map<Item, CustomerChargeActItemEditor> matches = new HashMap<>();
                    result = getEditorsForReturn(editor, matches);
                } else {
                    result = Status.invalid(Messages.format("customer.order.cannotcharge", act.getId(),
                                                            DescriptorHelper.getDisplayName(act),
                                                            editor.getDisplayName()));

                }
            } else if (invoice.getId() != editor.getObject().getId()) {
                result = Status.invalid(Messages.format("customer.order.differentInvoice", act.getId(),
                                                        DescriptorHelper.getDisplayName(act), invoice.getId()));
            } else {
                result = Status.valid();
            }
        } else {
            result = Status.invalid(Messages.format("customer.order.chargefinalised", editor.getDisplayName()));
        }
        return result;
    }

    /**
     * Creates charge for the pharmacy order.
     * <p>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     *
     * @param charge  the charge to add to, or {@code null} to create a new charge
     * @param charger the order charger, used to manage charging multiple orders. May be {@code null}
     * @param context the layout context
     * @return an editor for the charge
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
            if (canInvoice() && isOrder()) {
                // can only apply returns to existing invoices
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
     * <p>
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
     * <p>
     * Note that the caller is responsible for saving the order/return.
     *
     * @param editor the editor to add invoice items to
     * @throws IllegalStateException if the editor cannot be used, or the order/return is invalid
     */
    public void charge(CustomerChargeActEditor editor) {
        if (!isValid()) {
            throw new IllegalStateException("The order is incomplete and cannot be invoiced");
        }
        if (!canCharge(editor)) {
            throw new IllegalStateException("Cannot charge " + act.getArchetypeId() + " to editor");
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
     * <p>
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
     * Determines if all items in the order are unrelated to invoice items.
     * <p>
     * This could be because the original items have been deleted, or because the order/return originated
     * in an external system.
     *
     * @return {@code true}
     */
    public boolean notFromInvoice() {
        boolean result = true;
        for (Item item : items) {
            if (item.isOrdered()) {
                result = false;
                break;
            }
        }
        return result;
    }

    public CustomerChargeActEditDialog returnItems(FinancialAct invoice, OrderCharger charger, LayoutContext context) {
        CustomerChargeActEditDialog result = null;
        CustomerChargeActEditor editor = createChargeEditor(invoice, context);
        CustomerChargeActEditDialog dialog = new DefaultCustomerChargeActEditDialog(editor, charger,
                                                                                    context.getContext(), false);
        if (applyOrderReturnToInvoice(editor)) {
            dialog.show();
            result = dialog;
        }
        return result;
    }

    /**
     * Creates a new {@link Item}.
     *
     * @param item        the order/return item
     * @param ordered     determines if the order/return originated in an invoice
     * @param invoiceItem the invoice item that triggered the original order. May be {@code null} if the
     *                    order/return doesn't originate from an invoice, or it has been deleted
     * @param invoice     the invoice associated with {@code invoiceItem}. May be {@code null}
     * @return a new item
     */
    protected Item createItem(FinancialAct item, boolean ordered, FinancialAct invoiceItem, FinancialAct invoice) {
        return new Item(item, ordered, invoiceItem, invoice);
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
     * <p>
     * Note that the caller is responsible for saving the act.
     *
     * @param editor the editor to add invoice items to
     * @return {@code true} if the charge was successful
     */
    private boolean doCharge(CustomerChargeActEditor editor) {
        boolean result;
        ActRelationshipCollectionEditor items = editor.getItems();

        if (isOrderReturn() && notFromInvoice() && editor.getObject().isA(INVOICE)) {
            result = applyOrderReturnToInvoice(editor);
        } else {
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
            result = true;
        }
        return result;
    }

    private Status getEditorsForReturn(CustomerChargeActEditor editor, Map<Item, CustomerChargeActItemEditor> editors) {
        Status result;
        StringBuilder reason = new StringBuilder();
        Map<Item, CustomerChargeActItemEditor> matches = new HashMap<>();
        ActRelationshipCollectionEditor invoiceItems = editor.getItems();
        List<Act> exclusions = new ArrayList<>();
        for (Item item : items) {
            FinancialAct invoiceItem = null;
            List<Act> acts = editor.getItems().getCurrentActs();
            boolean patientAndProductMatch = false;
            for (Act act : acts) {
                if (!exclusions.contains(act)) {
                    IMObjectBean bean = new IMObjectBean(act);
                    if (!isOrdered(act) && item.samePatientAndProduct(bean)) {
                        patientAndProductMatch = true;
                        if (item.getQuantity().compareTo(bean.getBigDecimal("quantity")) <= 0) {
                            invoiceItem = (FinancialAct) act;
                            break;
                        }
                    }
                }
            }
            CustomerChargeActItemEditor itemEditor = null;
            if (invoiceItem != null) {
                IMObjectEditor currentEditor = invoiceItems.getCurrentEditor();
                // TODO shouldn't need to check the current editor. getEditor() should handle this.
                if (currentEditor != null && currentEditor.getObject().equals(invoiceItem)) {
                    itemEditor = (CustomerChargeActItemEditor) currentEditor;
                } else {
                    itemEditor = (CustomerChargeActItemEditor) invoiceItems.getEditor(invoiceItem);
                }
            }
            if (itemEditor != null) {
                matches.put(item, itemEditor);
                exclusions.add(invoiceItem);
            } else {
                if (reason.length() != 0) {
                    reason.append("\n");
                }
                String returnDisplayname = DescriptorHelper.getDisplayName(act);
                String invoiceDisplayName = editor.getDisplayName();
                String productName = IMObjectHelper.getName(item.getProduct());
                if (patientAndProductMatch) {
                    reason.append(Messages.format("customer.order.return.qtyexceeded", act.getId(),
                                                  returnDisplayname, invoiceDisplayName, productName));
                } else {
                    reason.append(Messages.format("customer.order.return.notinvoiced", act.getId(),
                                                  returnDisplayname, invoiceDisplayName, productName));
                }
                break;
            }
        }
        if (reason.length() == 0) {
            editors.putAll(matches);
            result = Status.valid();
        } else {
            result = Status.invalid(reason.toString());
        }
        return result;
    }

    /**
     * Determines if an invoice item has been ordered.
     *
     * @param item the invoice item
     * @return {@code true} if the item has been ordered, otherwise {@code false}
     */
    private boolean isOrdered(Act item) {
        return item.getStatus() != null;
    }

    /**
     * Tries to adjust the quantities of products that have been returned.
     * <p>
     * Note that the caller is responsible for saving the act.
     *
     * @param editor the editor
     * @return {@code true} if each line item could be updated in the invoice, otherwise {@code false}
     */
    private boolean applyOrderReturnToInvoice(CustomerChargeActEditor editor) {
        boolean result = false;
        Map<Item, CustomerChargeActItemEditor> matches = new HashMap<>();
        Status status = getEditorsForReturn(editor, matches);
        if (status.canCharge()) {
            for (Map.Entry<Item, CustomerChargeActItemEditor> entry : matches.entrySet()) {
                Item item = entry.getKey();
                CustomerChargeActItemEditor itemEditor = entry.getValue();
                BigDecimal newInvoiceQty = itemEditor.getQuantity().subtract(item.getQuantity());
                if (MathRules.isZero(newInvoiceQty) && MathRules.isZero(itemEditor.getMinimumQuantity())) {
                    // only remove the item if the quantity is zero, and there is no minimum quantity
                    editor.removeItem(itemEditor.getObject());
                } else {
                    itemEditor.setStartTime(item.getStartTime());
                    itemEditor.setQuantity(newInvoiceQty);
                }
            }
            act.setStatus(ActStatus.POSTED);
            editor.getItems().refresh();
            result = true;
        }
        return result;
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

        private final boolean ordered;

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

        /**
         * Creates a new {@link Item}.
         *
         * @param orderItem   the order/return item
         * @param ordered     determines if the order/return originated in an invoice
         * @param invoiceItem the invoice item that triggered the original order. May be {@code null} if the
         *                    order/return doesn't originate from an invoice, or it has been deleted
         * @param invoice     the invoice associated with {@code invoiceItem}. May be {@code null}
         */
        public Item(FinancialAct orderItem, boolean ordered, FinancialAct invoiceItem, FinancialAct invoice) {
            ActBean bean = new ActBean(orderItem);
            this.ordered = ordered;
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
            properties = new PropertySetBuilder(orderItem).build();
        }

        /**
         * Determines if the order/return originated in an invoice.
         *
         * @return {@code true} if the order/return originated in an invoice, otherwise {@code false}
         */
        public boolean isOrdered() {
            return ordered;
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

        /**
         * Returns the quantity.
         *
         * @return the quantity
         */
        public BigDecimal getQuantity() {
            return quantity;
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
                // its a return, with no existing invoice to add the return to.
                // If it wasn't ordered from an invoice in the first place, it may be able to be invoiced if the
                // current invoice has a matching product
                result = !ordered;
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
                if (ordered && receivedQty.subtract(returnedQty).add(quantity).compareTo(invoiceQty) <= 0) {
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
         * <p>
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

        /**
         * Charges the order/return.
         *
         * @param editor     the charge editor
         * @param itemEditor the charge item editor to update
         */
        public void charge(CustomerChargeActEditor editor, CustomerChargeActItemEditor itemEditor) {
            FinancialAct object = (FinancialAct) itemEditor.getObject();
            itemEditor.setStartTime(startTime);
            itemEditor.setPatientRef(patient);
            itemEditor.setProductRef(product); // TODO - protect against product change
            if (object.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
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

        /**
         * Returns the current instance of the invoice item held by the editor, corresponding that of this item.
         *
         * @param editor the editor
         * @return the current instance of the invoice item, or {@code null} if one is not found
         */
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
                IMObjectBean bean = new IMObjectBean(invoiceItem);
                return samePatientAndProduct(bean);
            }
            return false;
        }

        public Date getStartTime() {
            return startTime;
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
            if (product.isA(getProductArchetypes())) {
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

        /**
         * Determines if a charge has the same patient and product as the item.
         *
         * @param bean the charge bean
         * @return {@code true} if they have the same patient and product
         */
        private boolean samePatientAndProduct(IMObjectBean bean) {
            return ObjectUtils.equals(bean.getTargetRef("patient"), patient)
                   && ObjectUtils.equals(bean.getTargetRef("product"), product);
        }

    }

}
