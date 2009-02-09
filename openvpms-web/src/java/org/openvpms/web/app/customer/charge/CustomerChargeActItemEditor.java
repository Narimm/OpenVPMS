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
 *  $Id:CustomerInvoiceItemEditor.java 2287 2007-08-13 07:56:33Z tanderson $
 */

package org.openvpms.web.app.customer.charge;

import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.finance.tax.TaxRuleException;
import static org.openvpms.archetype.rules.product.ProductArchetypes.*;
import static org.openvpms.archetype.rules.stock.StockArchetypes.STOCK_LOCATION_PARTICIPATION;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.PriceActItemEditor;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.ClinicianParticipationEditor;
import org.openvpms.web.component.im.edit.act.PatientMedicationActEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.edit.medication.PatientMedicationActLayoutStrategy;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.layout.EditLayoutStrategyFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.customerAccountInvoiceItem</em>,
 * <em>act.customerAccountCreditItem</em>
 * or <em>act.customerAccountCounterItem</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class CustomerChargeActItemEditor extends PriceActItemEditor {

    /**
     * Node filter, used to disable properties when a product template is
     * selected.
     */
    private static final NodeFilter TEMPLATE_FILTER = new NamedNodeFilter(
            "quantity", "fixedPrice", "unitPrice", "total");

    /**
     * Node filter, used to hide the dispensing node when a non-medication
     * product is selected.
     */
    private static final NodeFilter DISPENSING_FILTER = new NamedNodeFilter(
            "dispensing");

    /**
     * The no. of medication dialogs currently popped up. The invoice item
     * is invalid until this is <code>0</tt>
     */
    private int medicationPopups = 0;

    /**
     * The medication manager.
     */
    private MedicationManager medicationMgr;

    /**
     * Listener for changes to the quantity.
     */
    private final ModifiableListener quantityListener;

    /**
     * Listener for changes to the medication quantity.
     */
    private final ModifiableListener medicationQuantityListener;

    /**
     * Stock rules.
     */
    private StockRules rules;

    /**
     * Layout strategy factory that returns customized instances of
     * {@link PatientMedicationActLayoutStrategy}.
     */
    private static final IMObjectLayoutStrategyFactory FACTORY
            = new MedicationLayoutStrategyFactory();


    /**
     * Construct a new <code>CustomerChargeActItemEditor</tt>.
     * This recalculates the tax amount.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public CustomerChargeActItemEditor(Act act, Act parent,
                                       LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, "act.customerAccountInvoiceItem",
                            "act.customerAccountCreditItem",
                            "act.customerAccountCounterItem")) {
            throw new IllegalArgumentException("Invalid act type:"
                    + act.getArchetypeId().getShortName());
        }
        rules = new StockRules();

        if (act.isNew()) {
            // default the act start time to today
            act.setActivityStartTime(new Date());
        }

        calculateTax();

        IMObjectReference ref = getProductRef();
        if (!TypeHelper.isA(ref, MEDICATION)) {
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

        quantityListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateMedicationQuantity();
            }
        };
        getProperty("quantity").addModifiableListener(quantityListener);

        medicationQuantityListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateQuantity();
            }
        };

        ModifiableListener startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateMedicationStartTime();
            }
        };
        getProperty("startTime").addModifiableListener(startTimeListener);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</tt> if the object and its descendents are valid
     *         otherwise <code>false</tt>
     */
    @Override
    public boolean validate(Validator validator) {
        return (medicationPopups == 0) && super.validate(validator);
    }

    /**
     * Sets the medication manager.
     *
     * @param manager the medication manager. May be <code>null</tt>
     */
    public void setMedicationManager(MedicationManager manager) {
        medicationMgr = manager;
    }

    /**
     * Save any edits.
     *
     * @return <code>true</tt> if the save was successful
     */
    @Override
    protected boolean doSave() {
        CollectionProperty dispensing
                = (CollectionProperty) getProperty("dispensing");
        Act medication = null;
        if (dispensing != null && !TypeHelper.isA(getProductRef(), MEDICATION))
        {
            // need to remove any redundant dispensing act
            if (!dispensing.getValues().isEmpty()) {
                Object[] values = dispensing.getValues().toArray();
                ActRelationship relationship = (ActRelationship) values[0];
                Act act = (Act) getObject();
                act.removeActRelationship(relationship);
                medication = (Act) IMObjectHelper.getObject(
                        relationship.getTarget());
            }
        }
        boolean saved = super.doSave();
        if (saved) {
            if (medication != null) {
                // need to delete the medication after the parent act is saved
                // to avoid stale object exceptions
                saved = SaveHelper.delete(medication);
            }
        }
        return saved;
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
        final ActRelationshipCollectionEditor editors
                = getDispensingCollection();
        if (editors != null) {
            editors.addModifiableListener(medicationQuantityListener);
        }

        // add a listener to update the dispensing acts when the clinician
        // changes if there is a clinician participation.
        ClinicianParticipationEditor clinician = getClinicianEditor();
        if (clinician != null) {
            clinician.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    updateMedicationClinician();
                }
            });
        }
    }

    /**
     * Invoked when the product is changed, to update prices
     * and dispensing acts.
     *
     * @param product the product. May be <tt>null</tt>
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);

        Property discount = getProperty("discount");
        discount.setValue(BigDecimal.ZERO);

        if (TypeHelper.isA(product, TEMPLATE)) {
            if (getFilter() != TEMPLATE_FILTER) {
                changeLayout(TEMPLATE_FILTER);
            }
            Property fixedPrice = getProperty("fixedPrice");
            Property unitPrice = getProperty("unitPrice");
            Property fixedCost = getProperty("fixedCost");
            Property unitCost = getProperty("unitCost");
            fixedPrice.setValue(BigDecimal.ZERO);
            unitPrice.setValue(BigDecimal.ZERO);
            fixedCost.setValue(BigDecimal.ZERO);
            unitCost.setValue(BigDecimal.ZERO);
        } else {
            if (TypeHelper.isA(product, MEDICATION)) {
                if (getFilter() != null) {
                    changeLayout(null);
                }
                updateMedicationProduct(product);
            } else {
                if (getFilter() != DISPENSING_FILTER) {
                    changeLayout(DISPENSING_FILTER);
                }
            }
            Property fixedPrice = getProperty("fixedPrice");
            Property unitPrice = getProperty("unitPrice");
            Property fixedCost = getProperty("fixedCost");
            Property unitCost = getProperty("unitCost");

            ProductPrice fixedProductPrice = null;
            ProductPrice unitProductPrice = null;
            if (product != null) {
                fixedProductPrice = getDefaultFixedProductPrice(product);
                unitProductPrice = getDefaultUnitProductPrice(product);
            }

            if (fixedProductPrice != null) {
                fixedPrice.setValue(fixedProductPrice.getPrice());
                fixedCost.setValue(getCost(fixedProductPrice));
            } else {
                fixedPrice.setValue(BigDecimal.ZERO);
                fixedCost.setValue(BigDecimal.ZERO);
            }
            if (unitProductPrice != null) {
                unitPrice.setValue(unitProductPrice.getPrice());
                unitCost.setValue(getCost(unitProductPrice));
            } else {
                unitPrice.setValue(BigDecimal.ZERO);
                unitCost.setValue(BigDecimal.ZERO);
            }
            updateStockLocation(product);
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
        Context context = getLayoutContext().getContext();
        Party practice = context.getPractice();
        if (customer != null && getProductRef() != null && practice != null) {
            FinancialAct act = (FinancialAct) getObject();
            BigDecimal previousTax = act.getTaxAmount();
            CustomerTaxRules rules
                    = new CustomerTaxRules(practice);
            BigDecimal tax = rules.calculateTax(act, customer);
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
     * Updates any medication acts with the start time.
     */
    private void updateMedicationStartTime() {
        Act parent = (Act) getObject();
        for (PatientMedicationActEditor editor : getMedicationActEditors()) {
            editor.setStartTime(parent.getActivityStartTime());
        }
        ActRelationshipCollectionEditor editors = getDispensingCollection();
        if (editors != null) {
            editors.refresh();
        }
    }

    /**
     * Updates any medication acts with the product.
     *
     * @param product the product
     */
    private void updateMedicationProduct(Product product) {
        ActRelationshipCollectionEditor editors = getDispensingCollection();
        if (editors != null) {
            Set<PatientMedicationActEditor> medicationEditors
                    = getMedicationActEditors();
            if (!medicationEditors.isEmpty()) {
                // set the product on the existing acts
                for (PatientMedicationActEditor editor : medicationEditors) {
                    editor.setProduct(product.getObjectReference());
                }
                editors.refresh();
            } else {
                // add a new medication act
                Act act = (Act) editors.create();
                if (act != null) {
                    boolean dispensingLabel = hasDispensingLabel(product);
                    IMObjectEditor editor = createMedicationEditor(
                            act, dispensingLabel);
                    if (dispensingLabel) {
                        // queue editing of the act
                        ++medicationPopups;
                        medicationMgr.queue(
                                editor, new MedicationManager.Listener() {
                            public void completed() {
                                --medicationPopups;
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Creates a new editor for a medication act.
     *
     * @param act             the medication act
     * @param dispensingLabel <tt>true</tt> if a dispensing label is required
     * @return a new editor for the act
     */
    private IMObjectEditor createMedicationEditor(Act act,
                                                  boolean dispensingLabel) {
        ActRelationshipCollectionEditor editors = getDispensingCollection();

        LayoutContext context = new DefaultLayoutContext(true);
        if (dispensingLabel) {
            context.setLayoutStrategyFactory(FACTORY);
        }
        IMObjectEditor editor = editors.createEditor(act, context);
        editors.addEdited(editor);
        return editor;
    }

    /**
     * Updates the medication quantity from the invoice.
     */
    private void updateMedicationQuantity() {
        ActRelationshipCollectionEditor editors = getDispensingCollection();
        BigDecimal quantity = (BigDecimal) getProperty("quantity").getValue();
        if (editors != null && quantity != null) {
            editors.removeModifiableListener(medicationQuantityListener);
            try {
                PatientMedicationActEditor editor
                        = (PatientMedicationActEditor)
                        editors.getCurrentEditor();
                if (editor != null) {
                    editor.setQuantity(quantity);
                } else {
                    for (Act act : editors.getActs()) {
                        // should only be 1 dispensing act, but zero out any
                        // additional ones just in case...
                        ActBean bean = new ActBean(act);
                        bean.setValue("quantity", quantity);
                        quantity = BigDecimal.ZERO;
                    }
                }
                editors.refresh();
            } finally {
                editors.addModifiableListener(medicationQuantityListener);
            }
        }
    }

    /**
     * Updates the invoice quantity when a medication act changes.
     */
    private void updateQuantity() {
        Property property = getProperty("quantity");
        property.removeModifiableListener(quantityListener);
        try {
            ActRelationshipCollectionEditor editors = getDispensingCollection();
            if (editors != null) {
                Set<Act> acts = new HashSet<Act>(editors.getActs());
                PatientMedicationActEditor current
                        = (PatientMedicationActEditor)
                        editors.getCurrentEditor();
                if (current != null) {
                    acts.add((Act) current.getObject());
                }
                if (!acts.isEmpty()) {
                    BigDecimal total = BigDecimal.ZERO;
                    for (Act act : acts) {
                        ActBean bean = new ActBean(act);
                        BigDecimal quantity = bean.getBigDecimal(
                                "quantity", BigDecimal.ZERO);
                        total = total.add(quantity);
                    }
                    property.setValue(total);
                }
                editors.refresh();
            }
        } finally {
            property.addModifiableListener(quantityListener);
        }
    }

    /**
     * Updates any medication acts with the patient.
     */
    private void updateMedicationPatient() {
        for (PatientMedicationActEditor editor : getMedicationActEditors()) {
            editor.setPatient(getPatient());
        }
    }

    /**
     * Updates any medication acts with the clinician.
     */
    private void updateMedicationClinician() {
        for (PatientMedicationActEditor editor : getMedicationActEditors()) {
            editor.setClinician(getClinician());
        }
    }

    /**
     * Returns editors for each of the <em>act.patientMedication</em> acts.
     *
     * @return the editors
     */
    private Set<PatientMedicationActEditor> getMedicationActEditors() {
        Set<PatientMedicationActEditor> result
                = new HashSet<PatientMedicationActEditor>();
        ActRelationshipCollectionEditor editors = getDispensingCollection();
        if (editors != null) {
            for (Act act : editors.getActs()) {
                PatientMedicationActEditor editor
                        = (PatientMedicationActEditor) editors.getEditor(act);
                result.add(editor);
            }

            // add current editor as well. If this refers to a new
            // object, it may not be in the list of 'committed' acts
            // returned by the above.
            PatientMedicationActEditor current
                    = (PatientMedicationActEditor) editors.getCurrentEditor();
            if (current != null) {
                result.add(current);
            }
        }
        return result;
    }

    /**
     * Determines if a medication product requires a dispensing label.
     *
     * @param product the product
     * @return <tt>true</tt> if the product requires a dispensing label
     */
    private boolean hasDispensingLabel(Product product) {
        IMObjectBean bean = new IMObjectBean(product);
        return bean.getBoolean("label");
    }

    /**
     * Returns the dispensing items collection editor.
     *
     * @return the dispensing items collection editor, or <tt>null</tt>
     *         if none is found
     */
    private ActRelationshipCollectionEditor getDispensingCollection() {
        return (ActRelationshipCollectionEditor) getEditor("dispensing");
    }

    /**
     * Updates the stock location associated with the product.
     *
     * @param product the new product
     */
    private void updateStockLocation(Product product) {
        Party stockLocation = null;
        if (TypeHelper.isA(product, MEDICATION, MERCHANDISE)) {
            Act parent = (Act) getParent();
            if (parent != null) {
                ActBean bean = new ActBean(parent);
                Party location = (Party) bean.getNodeParticipant("location");
                if (location != null) {
                    stockLocation = rules.getStockLocation(product, location);
                }
            }
        }
        ActBean bean = new ActBean((Act) getObject());
        if (stockLocation != null) {
            bean.setParticipant(STOCK_LOCATION_PARTICIPATION, stockLocation);
        } else {
            bean.removeParticipation(STOCK_LOCATION_PARTICIPATION);
        }
    }

    /**
     * Returns the value of the cost node of a price.
     *
     * @param price the product price
     * @return the cost
     */
    private BigDecimal getCost(ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price);
        return bean.getBigDecimal("cost", BigDecimal.ZERO);
    }

    /**
     * Factory that invokes <code>setProductReadOnly(true)</code> on
     * {@link PatientMedicationActLayoutStrategy} instances.
     */
    private static class MedicationLayoutStrategyFactory
            extends EditLayoutStrategyFactory {

        /**
         * Creates a new layout strategy for an object.
         *
         * @param object the object to create the layout strategy for
         * @param parent the parent object. May be <code>null</code>
         */
        @Override
        public IMObjectLayoutStrategy create(IMObject object, IMObject parent) {
            IMObjectLayoutStrategy result = super.create(object, parent);
            if (result instanceof PatientMedicationActLayoutStrategy) {
                PatientMedicationActLayoutStrategy strategy
                        = ((PatientMedicationActLayoutStrategy) result);
                strategy.setProductReadOnly(true);
            }
            return result;
        }
    }
}
