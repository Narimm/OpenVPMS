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

package org.openvpms.web.workspace.patient.mr.prescription;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.product.ProductHelper;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.charge.DefaultCustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.DefaultEditorQueue;
import org.openvpms.web.workspace.customer.charge.EditorQueue;

import static org.openvpms.component.model.bean.Policies.active;

/**
 * Dispenses a prescription, charging the product to an invoice.
 *
 * @author Tim Anderson
 */
public class PrescriptionDispenser {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The customer account rules.
     */
    private CustomerAccountRules rules;

    /**
     * Constructs a {@link PrescriptionDispenser}.
     *
     * @param context the context
     * @param help    the help context
     */
    public PrescriptionDispenser(Context context, HelpContext help) {
        this.context = context;
        this.help = help;
        rules = ServiceHelper.getBean(CustomerAccountRules.class);
    }

    /**
     * Dispenses a prescription.
     * <p>
     * The medication will be charged to the customer's most recent non-finalised invoice will be used, if one is
     * available.<br/>
     * If not, one will be created.
     *
     * @param prescription       the prescription
     * @param customer           the customer to invoice
     * @param completionListener the listener to notify on successful completion. May be {@code null}
     */
    public void dispense(Act prescription, Party customer, Runnable completionListener) {
        Prescription state = prepare(prescription);
        if (state != null) {
            FinancialAct invoice = rules.getInvoice(customer);
            Party location = null;
            if (invoice == null) {
                invoice = (FinancialAct) IMObjectCreator.create(CustomerAccountArchetypes.INVOICE);
                IMObjectBean invoiceBean = new IMObjectBean(invoice);
                invoiceBean.setTarget("customer", customer);
            } else {
                IMObjectBean invoiceBean = new IMObjectBean(invoice);
                location = invoiceBean.getTarget("location", Party.class);
            }
            if (location == null) {
                location = context.getLocation();
            }
            if (checkDispense(state.getProduct(), location)) {
                DefaultLayoutContext layout = new DefaultLayoutContext(new LocalContext(context), help);
                CustomerChargeActEditor editor = new DefaultCustomerChargeActEditor(invoice, null, layout, false);
                editor.getComponent();
                dispense(state, editor, completionListener);
            }
        }
    }

    /**
     * Dispenses a prescription.
     *
     * @param prescription       the prescription
     * @param editor             the editor to charge the prescription to
     * @param completionListener the listener to notify on successful completion. May be {@code null}
     */
    public void dispense(Act prescription, CustomerChargeActEditor editor, Runnable completionListener) {
        Prescription state = prepare(prescription);
        if (state != null) {
            Party location = editor.getLocation();
            if (checkDispense(state.getProduct(), location)) {
                dispense(state, editor, completionListener);
            }
        }
    }

    /**
     * Dispenses a prescription.
     *
     * @param prescription       the prescription state
     * @param editor             the charge editor
     * @param completionListener the listener to notify on successful completion. May be {@code null}
     * @return the charge item editor
     */
    protected CustomerChargeActItemEditor dispense(Prescription prescription, CustomerChargeActEditor editor,
                                                   Runnable completionListener) {
        CustomerChargeActItemEditor result = null;
        if (!editor.isValid()) {
            // don't add prescription to invalid invoice
            errorSaveInvoice();
        } else {
            CustomerChargeActItemEditor item = editor.addItem();
            if (item == null) {
                // shouldn't happen, but prompt user to save just in case
                errorSaveInvoice();
            } else {
                item.getComponent();
                item.setPromptForPrescriptions(false);
                item.setCancelPrescription(true);
                item.getPrescriptions().add(prescription.getAct());
                EditorQueue existing = item.getEditorQueue();
                item.setEditorQueue(new DefaultEditorQueue(context) {
                    @Override
                    protected void completed() {
                        super.completed();
                        item.setEditorQueue(existing);
                        SaveHelper.save(editor);
                        if (completionListener != null) {
                            completionListener.run();
                        }
                    }

                    /**
                     * Invoked when an edit is cancelled. Skips all subsequent edits.
                     */
                    @Override
                    protected void cancelled() {
                        super.cancelled();
                        editor.removeItem(item.getObject());
                    }
                });
                item.setPatient(prescription.getPatient());
                item.setProduct(prescription.getProduct());
            }
            result = item;
        }
        return result;
    }

    /**
     * Displays an error message indicating that the invoice should be saved.
     */
    private void errorSaveInvoice() {
        ErrorHelper.show(Messages.get("patient.prescription.dispense"),
                         Messages.get("patient.prescription.saveinvoice"));
    }

    /**
     * Verifies a product can be dispensed at the specified location.
     * <p>
     * If not, displays an error.
     *
     * @param product  the product
     * @param location the location
     * @return {@code true} if the product can be dispensed
     */
    private boolean checkDispense(Product product, Party location) {
        boolean result = false;
        if (!canDispense(product, location)) {
            if (!ObjectUtils.equals(location, context.getLocation())) {
                ErrorHelper.show(Messages.format("patient.prescription.notatinvoicelocation",
                                                 product.getName(), location.getName()));
            } else {
                ErrorHelper.show(Messages.format("patient.prescription.notatlocation", product.getName()));
            }
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Checks if a product can be dispensed at the specified location.
     *
     * @param product  the product
     * @param location the location
     * @return {@code true} if the product can be dispensed at the location
     */
    private boolean canDispense(Product product, Party location) {
        boolean result;
        if (ProductHelper.useLocationProducts(context)) {
            LocationRules locationRules = ServiceHelper.getBean(LocationRules.class);
            StockRules rules = ServiceHelper.getBean(StockRules.class);
            Party stockLocation = locationRules.getDefaultStockLocation(location);
            result = stockLocation != null && rules.hasStockRelationship(product, stockLocation);
        } else {
            result = true;
        }
        return result;
    }

    /**
     * Prepares for dispense.
     *
     * @param prescription the prescription
     * @return the prescription state, if the prescription can be dispensed, otherwise {@code null}
     */
    private Prescription prepare(Act prescription) {
        Prescription result = null;
        IMObjectBean bean = new IMObjectBean(prescription);
        Party patient = bean.getTarget("patient", Party.class);
        if (patient == null) {
            throw new IllegalStateException("Prescription has no patient");
        }
        Product product = bean.getTarget("product", Product.class, active());
        if (product == null) {
            // can't dispense from an inactive product
            ErrorHelper.show(Messages.get("patient.prescription.noproduct"));
        } else {
            result = new Prescription(prescription, patient, product);
        }
        return result;
    }

    /**
     * Prescription state.
     */
    protected static class Prescription {

        /**
         * The prescription.
         */
        private final Act act;

        /**
         * The patient.
         */
        private final Party patient;

        /**
         * The product.
         */
        private final Product product;

        /**
         * Constructs a {@link Prescription}.
         *
         * @param act     the prescription act
         * @param patient the patient
         * @param product the product
         */
        public Prescription(Act act, Party patient, Product product) {
            this.act = act;
            this.patient = patient;
            this.product = product;
        }

        /**
         * Returns the prescription act.
         *
         * @return the act
         */
        public Act getAct() {
            return act;
        }

        /**
         * Returns the product to dispense.
         *
         * @return the product
         */
        public Product getProduct() {
            return product;
        }

        /**
         * Returns the patient.
         *
         * @return the patient
         */
        public Party getPatient() {
            return patient;
        }
    }

}
