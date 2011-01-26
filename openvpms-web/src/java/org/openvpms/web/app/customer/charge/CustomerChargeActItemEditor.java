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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.finance.tax.TaxRuleException;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MERCHANDISE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.SERVICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.TEMPLATE;
import static org.openvpms.archetype.rules.stock.StockArchetypes.STOCK_LOCATION_PARTICIPATION;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.app.customer.PriceActItemEditor;
import org.openvpms.web.app.patient.mr.PatientInvestigationActEditor;
import org.openvpms.web.app.patient.mr.PatientMedicationActEditor;
import org.openvpms.web.app.patient.mr.PatientMedicationActLayoutStrategy;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Editors;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditorFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.ClinicianParticipationEditor;
import org.openvpms.web.component.im.edit.act.PatientActEditor;
import org.openvpms.web.component.im.edit.act.PatientParticipationEditor;
import org.openvpms.web.component.im.edit.reminder.ReminderEditor;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.layout.EditLayoutStrategyFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
     * Dispensing act editor. May be <tt>null</tt>
     */
    private ActRelationshipCollectionEditor dispensing;

    /**
     * Investigation act editor. May be <tt>null</tt>
     */
    private ActRelationshipCollectionEditor investigations;

    /**
     * Reminders act editor. May be <tt>null</tt>
     */
    private ActRelationshipCollectionEditor reminders;

    /**
     * The no. of medication dialogs currently popped up. The invoice item
     * is invalid until this is <code>0</tt>
     */
    private int patientActPopups = 0;

    /**
     * The medication and investigation act editor manager.
     */
    private PatientActEditorManager patientActMgr;

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
     * Selling units label.
     */
    private Label sellingUnits;

    /**
     * Layout strategy factory that returns customized instances of
     * {@link PatientMedicationActLayoutStrategy}.
     */
    private static final IMObjectLayoutStrategyFactory FACTORY
            = new MedicationLayoutStrategyFactory();

    /**
     * Dispensing node name.
     */
    private static final String DISPENSING = "dispensing";

    /**
     * Reminders node name.
     */
    private static final String REMINDERS = "reminders";

    /**
     * Investigations node name.
     */
    private static final String INVESTIGATIONS = "investigations";

    /**
     * Node filter, used to disable properties when a product template is selected.
     */
    private static final NodeFilter TEMPLATE_FILTER = new NamedNodeFilter(
            "quantity", "fixedPrice", "unitPrice", "discount", "clinician", "total", DISPENSING, INVESTIGATIONS,
            REMINDERS);


    /**
     * Constructs a <code>CustomerChargeActItemEditor</tt>.
     * <p/>
     * This recalculates the tax amount.
     *
     * @param act     the act to edit
     * @param parent  the parent act
     * @param context the layout context
     */
    public CustomerChargeActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE_ITEM,
                            CustomerAccountArchetypes.CREDIT_ITEM,
                            CustomerAccountArchetypes.COUNTER_ITEM)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }
        dispensing = createCollectionEditor(DISPENSING, act);
        investigations = createCollectionEditor(INVESTIGATIONS, act);
        reminders = createCollectionEditor(REMINDERS, act);

        rules = new StockRules();
        quantityListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateMedicationQuantity();
            }
        };
        medicationQuantityListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateQuantity();
            }
        };
        if (dispensing != null) {
            dispensing.addModifiableListener(medicationQuantityListener);
        }

        sellingUnits = LabelFactory.create();

        if (act.isNew()) {
            // default the act start time to today
            act.setActivityStartTime(new Date());
        }

        calculateTax();

        NodeFilter filter = getFilterForProduct(getProductRef());
        setFilter(filter);

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
        getProperty("quantity").addModifiableListener(quantityListener);

        ModifiableListener startTimeListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePatientActsStartTime();
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
        return (patientActPopups == 0) && super.validate(validator);
    }

    /**
     * Sets the medication manager.
     *
     * @param manager the medication manager. May be <code>null</tt>
     */
    public void setMedicationManager(PatientActEditorManager manager) {
        patientActMgr = manager;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new PriceItemLayoutStrategy() {
            {
                if (dispensing != null) {
                    addComponent(new ComponentState(dispensing));
                }
                if (investigations != null) {
                    addComponent(new ComponentState(investigations));
                }
                if (reminders != null) {
                    addComponent(new ComponentState(reminders));
                }
            }

            @Override
            protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
                ComponentState state = super.createComponent(property, parent, context);
                if ("quantity".equals(property.getName())) {
                    Component component = RowFactory.create("CellSpacing", state.getComponent(), sellingUnits);
                    state = new ComponentState(component, property);
                }
                return state;
            }
        };
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();

        // need to re-register editors as these are de-registered when the layout changes
        Editors editors = getEditors();
        if (dispensing != null) {
            editors.add(dispensing);
        }
        if (investigations != null) {
            editors.add(investigations);
        }
        if (reminders != null) {
            editors.add(reminders);
        }

        PatientParticipationEditor patient = getPatientEditor();
        if (patient != null) {
            // add a listener to update the dispensing, investigation and reminder acts when the patient changes
            patient.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    updatePatientActsPatient();
                }
            });
        }

        ClinicianParticipationEditor clinician = getClinicianEditor();
        if (clinician != null) {
            // add a listener to update the dispensing, investigation and reminder acts when the clinician changes
            clinician.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    updatePatientActsClinician();
                }
            });
        }
    }

    /**
     * Invoked when the product is changed, to update prices, dispensing and reminder acts.
     *
     * @param product the product. May be <tt>null</tt>
     */
    @Override
    protected void productModified(Product product) {
        super.productModified(product);

        updatePatientMedication(product);
        updateInvestigations(product);
        updateReminders(product);

        // update the layout if nodes require filtering
        NodeFilter currentFilter = getFilter();
        IMObjectReference productRef = (product != null) ? product.getObjectReference() : null;
        NodeFilter expectedFilter = getFilterForProduct(productRef);
        if (currentFilter != expectedFilter) {
            changeLayout(expectedFilter);
        }

        Property discount = getProperty("discount");
        discount.setValue(BigDecimal.ZERO);

        if (TypeHelper.isA(product, TEMPLATE)) {
            Property fixedPrice = getProperty("fixedPrice");
            Property unitPrice = getProperty("unitPrice");
            Property fixedCost = getProperty("fixedCost");
            Property unitCost = getProperty("unitCost");
            fixedPrice.setValue(BigDecimal.ZERO);
            unitPrice.setValue(BigDecimal.ZERO);
            fixedCost.setValue(BigDecimal.ZERO);
            unitCost.setValue(BigDecimal.ZERO);
            updateSellingUnits(null);
        } else {
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
            updateSellingUnits(product);
        }
    }

    /**
     * Calculates the tax amount.
     *
     * @throws ArchetypeServiceException for any archetype service error
     * @throws TaxRuleException          for any tax error
     */
    protected void calculateTax() {
        Party customer = getCustomer();
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
     * Updates any patient acts with the start time.
     */
    private void updatePatientActsStartTime() {
        Act parent = (Act) getObject();
        for (PatientActEditor editor : getMedicationActEditors()) {
            editor.setStartTime(parent.getActivityStartTime());
        }
        for (PatientInvestigationActEditor editor : getInvestigationActEditors()) {
            editor.setStartTime(parent.getActivityStartTime());
        }
        for (ReminderEditor editor : getReminderEditors()) {
            editor.setStartTime(parent.getActivityStartTime());
        }
        if (dispensing != null) {
            dispensing.refresh();
        }
        if (investigations != null) {
            investigations.refresh();
        }
        if (reminders != null) {
            reminders.refresh();
        }
    }

    /**
     * Invoked when the product changes to update patient medications.
     * <p/>
     * If the new product is a medication and there is:
     * <ul>
     * <li>an existing act, the existing act will be updated.
     * <li>no existing act, a new medication will be created
     * </ul>
     * <p/>
     * If the product is null, any existing act will be removed
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updatePatientMedication(Product product) {
        if (dispensing != null) {
            if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                Set<PatientMedicationActEditor> medicationEditors = getMedicationActEditors();
                if (!medicationEditors.isEmpty()) {
                    // set the product on the existing acts
                    for (PatientMedicationActEditor editor : medicationEditors) {
                        editor.setProduct(product);
                    }
                    dispensing.refresh();
                } else {
                    // add a new medication act
                    Act act = (Act) dispensing.create();
                    if (act != null) {
                        boolean dispensingLabel = hasDispensingLabel(product);
                        IMObjectEditor editor = createMedicationEditor(act, dispensingLabel);
                        if (dispensingLabel) {
                            // queue editing of the act
                            queuePatientActEditor(editor);
                        }
                    }
                }
            } else {
                // product is not a medication or is null. Remove any existing act
                for (Act act : dispensing.getCurrentActs()) {
                    dispensing.remove(act);
                }
            }
        }
    }

    /**
     * Invoked when the product changes to update investigation acts.
     * <p/>
     * This removes any existing investigations, and creates new ones, if required.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updateInvestigations(Product product) {
        if (investigations != null) {
            for (Act act : investigations.getCurrentActs()) {
                investigations.remove(act);
            }
            if (product != null) {
                // add a new investigation act for each investigation type (if any)
                for (Entity investigationType : getInvestigationTypes(product)) {
                    Act act = (Act) investigations.create();
                    if (act != null) {
                        IMObjectEditor editor = investigations.createEditor(act, getLayoutContext());
                        if (editor instanceof PatientInvestigationActEditor) {
                            ((PatientInvestigationActEditor) editor).setInvestigationType(investigationType);
                        }
                        investigations.addEdited(editor);

                        // queue editing of the act
                        queuePatientActEditor(editor);
                    }
                }
            }
        }
    }

    /**
     * Invoked when the product changes, to update reminders acts.
     * <p/>
     * This removes any existing reminders, and creates new ones, if required.
     *
     * @param product the product
     */
    private void updateReminders(Product product) {
        if (reminders != null) {
            for (Act act : reminders.getCurrentActs()) {
                reminders.remove(act);
            }
            for (EntityRelationship relationship : getReminderTypes(product)) {
                IMObjectBean bean = new IMObjectBean(relationship);
                Entity reminderType = (Entity) bean.getObject("target");
                if (reminderType != null && reminderType.isActive()) {
                    boolean interactive = bean.getBoolean("interactive");
                    Act act = (Act) reminders.create();
                    if (act != null) {
                        IMObjectEditor editor = reminders.createEditor(act, getLayoutContext());
                        if (editor instanceof ReminderEditor) {
                            ReminderEditor reminder = (ReminderEditor) editor;
                            reminder.setReminderType(reminderType);
                            reminder.setPatient(getPatient());
                            reminder.setProduct(product);
                        }
                        reminders.addEdited(editor);

                        if (interactive) {
                            // queue editing of the act
                            queuePatientActEditor(editor);
                        }
                    }
                }
            }
        }
    }

    /**
     * Updates the selling units label.
     *
     * @param product the product. May be <tt>null</tt>
     */
    private void updateSellingUnits(Product product) {
        String units = "";
        if (product != null) {
            units = LookupNameHelper.getName(product, "sellingUnits");
        }
        sellingUnits.setText(units);
    }

    /**
     * Helper to return the investigation types for a product.
     *
     * @param product the product
     * @return a list of investigation types
     */
    private List<Entity> getInvestigationTypes(Product product) {
        List<Entity> result = Collections.emptyList();
        EntityBean bean = new EntityBean(product);
        final String node = "investigationTypes";
        if (bean.hasNode(node)) {
            result = bean.getNodeTargetEntities(node);
        }
        return result;
    }

    /**
     * Helper to return the reminder type relationships for a product.
     *
     * @param product the product
     * @return a the reminder type relationships
     */
    private List<EntityRelationship> getReminderTypes(Product product) {
        List<EntityRelationship> result = Collections.emptyList();
        EntityBean bean = new EntityBean(product);
        if (bean.hasNode(REMINDERS)) {
            result = bean.getNodeRelationships(REMINDERS);
        }
        return result;
    }

    /**
     * Queues an editor for display in a popup dialog.
     * Use this when there may be multiple editors requiring display.
     *
     * @param editor the editor to queue
     */
    private void queuePatientActEditor(IMObjectEditor editor) {
        ++patientActPopups;
        patientActMgr.queue(editor, new PatientActEditorManager.Listener() {
            public void completed() {
                --patientActPopups;
            }
        });
    }

    /**
     * Creates a new editor for a medication act.
     *
     * @param act             the medication act
     * @param dispensingLabel <tt>true</tt> if a dispensing label is required
     * @return a new editor for the act
     */
    private IMObjectEditor createMedicationEditor(Act act, boolean dispensingLabel) {
        LayoutContext context = new DefaultLayoutContext(true);
        context.setContext(getLayoutContext().getContext());
        if (dispensingLabel) {
            context.setLayoutStrategyFactory(FACTORY);
        }
        IMObjectEditor editor = dispensing.createEditor(act, context);
        dispensing.addEdited(editor);
        return editor;
    }

    /**
     * Updates the medication quantity from the invoice.
     */
    private void updateMedicationQuantity() {
        BigDecimal quantity = (BigDecimal) getProperty("quantity").getValue();
        if (dispensing != null && quantity != null) {
            dispensing.removeModifiableListener(medicationQuantityListener);
            try {
                PatientMedicationActEditor editor = (PatientMedicationActEditor) dispensing.getCurrentEditor();
                if (editor != null) {
                    editor.setQuantity(quantity);
                } else {
                    for (Act act : dispensing.getActs()) {
                        // should only be 1 dispensing act, but zero out any
                        // additional ones just in case...
                        ActBean bean = new ActBean(act);
                        bean.setValue("quantity", quantity);
                        quantity = BigDecimal.ZERO;
                    }
                }
                dispensing.refresh();
            } finally {
                dispensing.addModifiableListener(medicationQuantityListener);
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
            if (dispensing != null) {
                Set<Act> acts = new HashSet<Act>(dispensing.getActs());
                PatientMedicationActEditor current = (PatientMedicationActEditor) dispensing.getCurrentEditor();
                if (current != null) {
                    acts.add((Act) current.getObject());
                }
                if (!acts.isEmpty()) {
                    BigDecimal total = BigDecimal.ZERO;
                    for (Act act : acts) {
                        ActBean bean = new ActBean(act);
                        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
                        total = total.add(quantity);
                    }
                    property.setValue(total);
                }
                dispensing.refresh();
            }
        } finally {
            property.addModifiableListener(quantityListener);
        }
    }

    /**
     * Updates any child patient acts with the patient.
     */
    private void updatePatientActsPatient() {
        IMObjectReference patient = getPatientRef();
        for (PatientActEditor editor : getMedicationActEditors()) {
            editor.setPatient(patient);
        }
        for (PatientInvestigationActEditor editor : getInvestigationActEditors()) {
            editor.setPatient(patient);
        }
        for (ReminderEditor editor : getReminderEditors()) {
            editor.setPatient(patient);
        }
    }

    /**
     * Updates any child patient acts with the clinician.
     */
    private void updatePatientActsClinician() {
        IMObjectReference clinician = getClinicianRef();
        for (PatientActEditor editor : getMedicationActEditors()) {
            editor.setClinician(clinician);
        }
        for (PatientInvestigationActEditor editor : getInvestigationActEditors()) {
            editor.setClinician(clinician);
        }
    }

    /**
     * Returns editors for each of the <em>act.patientMedication</em> acts.
     *
     * @return the editors
     */
    private Set<PatientMedicationActEditor> getMedicationActEditors() {
        return getActEditors(dispensing);
    }

    /**
     * Returns the editors for each of the <em>act.patientInvestigation</em> acts.
     *
     * @return the editors
     */
    private Set<PatientInvestigationActEditor> getInvestigationActEditors() {
        return getActEditors(investigations);
    }

    /**
     * Returns the editors for each of the <em>act.patientReminder</em> acts.
     *
     * @return the editors
     */
    private Set<ReminderEditor> getReminderEditors() {
        return getActEditors(investigations);
    }

    /**
     * Returns the act editors for the specified collection editor.
     *
     * @param editors the collection editor. May be <tt>null</tt>
     * @return a set of editors
     */
    @SuppressWarnings("unchecked")
    private <T extends IMObjectEditor> Set<T> getActEditors(ActRelationshipCollectionEditor editors) {
        Set<T> result = new HashSet<T>();
        if (editors != null) {
            for (Act act : editors.getCurrentActs()) {
                T editor = (T) editors.getEditor(act);
                result.add(editor);
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
     * Returns a node filter for the specified product reference.
     * <p/>
     * This excludes:
     * <ul>
     * <li>the dispensing node if the product isn't a <em>product.medication</em>
     * <li>the investigations node if the product isn't a <em>product.medication</em>, <em>product.merchandise</em>,
     * or <em>product.service</em>
     * <li>the reminders node is excluded if there are no reminders present.
     * </ul>
     *
     * @param product a reference to the product. May be <tt>null</tt>
     * @return a node filter for the product. If <tt>null</tt>, no nodes require filtering
     */
    private NodeFilter getFilterForProduct(IMObjectReference product) {
        NodeFilter result = null;
        if (TypeHelper.isA(product, TEMPLATE)) {
            result = TEMPLATE_FILTER;
        } else {
            List<String> filter = new ArrayList<String>();
            filter.add(DISPENSING);
            filter.add(INVESTIGATIONS);
            filter.add(REMINDERS);
            boolean medication = TypeHelper.isA(product, MEDICATION);
            if (medication) {
                filter.remove(DISPENSING);
            }
            if (medication || TypeHelper.isA(product, MERCHANDISE, SERVICE)) {
                filter.remove(INVESTIGATIONS);
            }
            if (reminders != null && reminders.getCollection().size() > 0) {
                filter.remove(REMINDERS);
            }
            if (!filter.isEmpty()) {
                result = new NamedNodeFilter(filter.toArray(new String[filter.size()]));
            }
        }
        return result;
    }

    /**
     * Helper to create and register a collection editor for an act relationship node, if the node exists.
     *
     * @param name the collection node name
     * @param act  the act
     * @return the collection editor, or <tt>null</tt> if the node doesn't exist
     */
    private ActRelationshipCollectionEditor createCollectionEditor(String name, Act act) {
        ActRelationshipCollectionEditor editor = null;
        CollectionProperty collection = (CollectionProperty) getProperty(name);
        if (collection != null && !collection.isHidden()) {
            editor = (ActRelationshipCollectionEditor) IMObjectCollectionEditorFactory.create(
                    collection, act, getLayoutContext());
            getEditors().add(editor);
        }
        return editor;
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
