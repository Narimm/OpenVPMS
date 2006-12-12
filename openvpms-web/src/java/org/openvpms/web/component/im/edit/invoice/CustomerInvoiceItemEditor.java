/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.invoice;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.archetype.rules.discount.DiscountRules;
import org.openvpms.archetype.rules.tax.TaxRuleException;
import org.openvpms.archetype.rules.tax.TaxRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.Validator;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.PatientMedicationActEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.math.BigDecimal;
import java.util.List;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountInvoiceItem</em>, <em>act.customerAccountCreditItem</em>
 * or <em>act.customerAccountCounterItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerInvoiceItemEditor extends ActItemEditor {

    /**
     * Node filter, used to disable properties when a product template is
     * selected.
     */
    private static final NodeFilter TEMPLATE_FILTER = new NamedNodeFilter(
            "quantity", "fixedPrice", "unitPrice", "total");

    /**
     * Node filter, used to hide the dispensing node when a non-medication
     * product is selected
     */
    private static final NodeFilter DISPENSING_FILTER = new NamedNodeFilter(
            "dispensing");

    /**
     * The no. of medication dialogs currently popped up. The invoice item
     * is invalid until this is <code>0</code>
     */
    private int medicationPopups = 0;


    /**
     * Construct a new <code>CustomerInvoiceItemEditor</code>.
     * This recalculates the tax amount.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public CustomerInvoiceItemEditor(Act act, Act parent,
                                     LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.customerAccountInvoiceItem",
                            "act.customerAccountCreditItem",
                            "act.customerAccountCounterItem")) {
            throw new IllegalArgumentException("Invalid act type:"
                    + act.getArchetypeId().getShortName());
        }

        calculateTax();

        IMObjectReference ref = getProduct();
        if (!TypeHelper.isA(ref, "product.medication")) {
            setFilter(DISPENSING_FILTER);
        }

        // add a listener to update the tax amount when the total changes
        ModifiableListener totalListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateTaxAmount();
            }
        };
        getProperty("total").addModifiableListener(totalListener);

        // add a listener to update the discount amount when the quantity,
        // fixed or unit price changes.
        ModifiableListener discountListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateDiscount();
            }
        };
        getProperty("fixedPrice").addModifiableListener(discountListener);
        getProperty("quantity").addModifiableListener(discountListener);
        getProperty("unitPrice").addModifiableListener(discountListener);
    }

    /**
     * Sets the product quantity.
     *
     * @param quantity the product quantity
     */
    public void setQuantity(BigDecimal quantity) {
        getProperty("quantity").setValue(quantity);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    @Override
    public boolean validate(Validator validator) {
        return (medicationPopups == 0) && super.validate(validator);
    }


    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean doSave() {
        CollectionProperty dispensing
                = (CollectionProperty) getProperty("dispensing");
        if (dispensing != null
                && !TypeHelper.isA(getProduct(), "product.medication")) {
            // need to remove any redundant dispensing act, to avoid spurious
            // results when printing etc.
            if (!dispensing.getValues().isEmpty()) {
                Object[] values = dispensing.getValues().toArray();
                ActRelationship relationship = (ActRelationship) values[0];
                Act act = (Act) getObject();
                act.removeActRelationship(relationship);
            }
        }
        return super.doSave();
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        // add a listener to update the dispensing acts when the patient
        // changes if there is a patient participation.
        PatientParticipationEditor patient = getPatientEditor();
        if (patient != null) {
	        patient.addModifiableListener(new ModifiableListener() {
	            public void modified(Modifiable modifiable) {
	                updateMedicationPatient();
	            }
	        });
        }
    }

    /**
     * Invoked when the participation product is changed, to update prices
     * and dispensing acts.
     *
     * @param participation the product participation instance
     */
    protected void productModified(Participation participation) {
        IMObjectReference entity = participation.getEntity();
        IMObject object = IMObjectHelper.getObject(entity);
        if (object instanceof Product) {
            Product product = (Product) object;
            if (TypeHelper.isA(product, "product.template")) {
                if (getFilter() != TEMPLATE_FILTER) {
                    changeLayout(TEMPLATE_FILTER);
                }
                // zero out the fixed and unit prices.
                Property fixedPrice = getProperty("fixedPrice");
                Property unitPrice = getProperty("unitPrice");
                fixedPrice.setValue(BigDecimal.ZERO);
                unitPrice.setValue(BigDecimal.ZERO);
            } else {
                if (!TypeHelper.isA(product, "product.medication")) {
                    if (getFilter() != DISPENSING_FILTER) {
                        changeLayout(DISPENSING_FILTER);
                    }
                } else {
                    if (getFilter() != null) {
                        changeLayout(null);
                    }
                    updateMedicationProduct(product);
                }
                Property fixedPrice = getProperty("fixedPrice");
                Property unitPrice = getProperty("unitPrice");
                ProductPrice fixed = getPrice("productPrice.fixedPrice",
                                              product);
                ProductPrice unit = getPrice("productPrice.unitPrice", product);
                if (fixed != null) {
                    fixedPrice.setValue(fixed.getPrice());
                }
                if (unit != null) {
                    unitPrice.setValue(unit.getPrice());
                }
            }
        }
    }

    /**
     * Calculates the tax amount.
     *
     * @throws ArchetypeServiceException for any archetype service error
     * @throws TaxRuleException          for any tax error
     */
    protected void calculateTax() {
        Party customer = (Party) IMObjectHelper.getObject(getCustomer());
        if (customer != null && getProduct() != null) {
            FinancialAct act = (FinancialAct) getObject();
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            BigDecimal previousTax = act.getTaxAmount();
            BigDecimal tax = TaxRules.calculateTax(act, customer, service);
            if (tax.compareTo(previousTax) != 0) {
                Property property = getProperty("tax");
                property.refresh();
            }
        }
    }

    /**
     * Calculates the tax amount.
     */
    private void updateTaxAmount() {
        try {
            calculateTax();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Calculates the discount amount.
     */
    private void updateDiscount() {
        try {
            Party customer = (Party) IMObjectHelper.getObject(getCustomer());
            Party patient = (Party) IMObjectHelper.getObject(getPatient());
            Product product = (Product) IMObjectHelper.getObject(
                    getProduct());

            // calculate the discount
            if (customer != null && product != null) {
                FinancialAct act = (FinancialAct) getObject();
                BigDecimal fixedPrice = act.getFixedAmount();
                BigDecimal unitPrice = act.getUnitAmount();
                BigDecimal quantity = act.getQuantity();
                if (fixedPrice == null) {
                    fixedPrice = BigDecimal.ZERO;
                }
                if (unitPrice == null) {
                    unitPrice = BigDecimal.ZERO;
                }
                if (quantity == null) {
                    quantity = BigDecimal.ZERO;
                }
                BigDecimal amount = DiscountRules.calculateDiscountAmount(
                        customer, patient, product, fixedPrice, unitPrice,
                        quantity, ArchetypeServiceHelper.getArchetypeService());
                Property discount = getProperty("discount");
                discount.setValue(amount);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Updates any medication acts with the product.
     *
     * @param product the product
     */
    private void updateMedicationProduct(Product product) {
        final ActRelationshipCollectionEditor editors = getMedicationEditors();
        if (editors != null) {
            List<Act> acts = editors.getActs();
            PatientMedicationActEditor current
                    = (PatientMedicationActEditor) editors.getCurrentEditor();
            if (!acts.isEmpty() || current != null) {
                for (Act a : acts) {
                    PatientMedicationActEditor editor
                            = (PatientMedicationActEditor) editors.getEditor(a);
                    editor.setProduct(product.getObjectReference());
                }
                editors.refresh();

                if (current != null) {
                    // update the current editor as well. If this refers to a
                    // new object, it may not be in the list of 'committed' acts
                    // returned by the above.
                    current.setPatient(getPatient());
                }
            } else {
                // create a new act
                IMObject object = editors.create();
                if (object != null) {
                    LayoutContext context = new DefaultLayoutContext(true);
                    final IMObjectEditor editor = editors.createEditor(
                            object, context);
                    final EditDialog dialog = new EditDialog(editor, false);
                    dialog.addWindowPaneListener(new WindowPaneListener() {
                        public void windowPaneClosing(WindowPaneEvent event) {
                            if (EditDialog.OK_ID.equals(dialog.getAction())) {
                                editors.addEdited(editor);
                            }
                            --medicationPopups;
                        }
                    });
                    ++medicationPopups;
                    dialog.show();
                }
            }
        }
    }

    /**
     * Updates any medication acts with the patient.
     */
    private void updateMedicationPatient() {
        ActRelationshipCollectionEditor editors = getMedicationEditors();
        if (editors != null) {
            for (Act act : editors.getActs()) {
                PatientMedicationActEditor editor
                        = (PatientMedicationActEditor) editors.getEditor(act);
                editor.setPatient(getPatient());
            }

            // update any current editor as well. If this refers to a new
            // object, it may not be in the list of 'committed' acts
            // returned by the above.
            PatientMedicationActEditor current
                    = (PatientMedicationActEditor) editors.getCurrentEditor();
            if (current != null) {
                current.setPatient(getPatient());
            }
        }
    }

    /**
     * Returns the dispensing items collection editor.
     *
     * @return the dispensing items collection editor, or <code>null</code>
     *         if none is found
     */
    private ActRelationshipCollectionEditor getMedicationEditors() {
        return (ActRelationshipCollectionEditor) getEditor("dispensing");
    }

}
