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

package org.openvpms.web.workspace.customer.charge;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.invoice.ChargeItemEventLinker;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.hl7.laboratory.LaboratoryOrderService;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.hl7.patient.PatientInformationService;
import org.openvpms.hl7.pharmacy.Pharmacies;
import org.openvpms.hl7.pharmacy.PharmacyOrderService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditorFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.FinancialActEditor;
import org.openvpms.web.component.im.edit.act.TemplateProductListener;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.charge.TemplateChargeItems;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * An editor for {@link org.openvpms.component.business.domain.im.act.Act}s which have an archetype of
 * <em>act.customerAccountChargesInvoice</em>,
 * <em>act.customerAccountChargesCredit</em>
 * or <em>act.customerAccountChargesCounter</em>.
 *
 * @author Tim Anderson
 */
public abstract class CustomerChargeActEditor extends FinancialActEditor {

    /**
     * Determines if a default item should added if no items are present.
     */
    private boolean addDefaultItem;

    /**
     * The customer notes editor.
     */
    private ActRelationshipCollectionEditor customerNotes;

    /**
     * The documents editor.
     */
    private ActRelationshipCollectionEditor documents;

    /**
     * The pharmacy order placer, used to place orders when invoicing.
     */
    private OrderPlacer orderPlacer;

    /**
     * Tracks unprinted documents.
     */
    private CustomerChargeDocuments unprintedDocuments;

    /**
     * The reminder rules.
     */
    private final ReminderRules reminderRules;

    /**
     * Constructs an {@link CustomerChargeActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public CustomerChargeActEditor(FinancialAct act, IMObject parent, LayoutContext context) {
        this(act, parent, context, true);
    }

    /**
     * Constructs an {@code CustomerChargeActEditor}.
     *
     * @param act            the act to edit
     * @param parent         the parent object. May be {@code null}
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
     */
    public CustomerChargeActEditor(FinancialAct act, IMObject parent, LayoutContext context, boolean addDefaultItem) {
        super(act, parent, context);
        Party location = initLocation();
        Party customer = context.getContext().getCustomer();
        initParticipant("customer", customer);
        this.addDefaultItem = addDefaultItem;
        reminderRules = ServiceHelper.getBean(ReminderRules.class);
        initialise();
        if (TypeHelper.isA(act, CustomerAccountArchetypes.INVOICE)) {
            getItems().setTemplateProductListener(new TemplateProductListener() {
                public void expanded(Product product) {
                    templateProductExpanded(product);
                }
            });

            orderPlacer = createOrderPlacer(customer, location, context.getContext().getUser());
            List<Act> acts = getOrderActs();
            orderPlacer.initialise(acts);
        }
        unprintedDocuments = new CustomerChargeDocuments(this, context);
    }

    /**
     * Determines if a default item should be added if the charge doesn't have one.
     * <p/>
     * This only applies prior to the creation of the component. After that, it is ignored.
     *
     * @param addDefaultItem if {@code true} add a default item if the charge has none
     */
    public void setAddDefaultItem(boolean addDefaultItem) {
        this.addDefaultItem = addDefaultItem;
    }

    /**
     * Registers a listener that is invoked when the user adds an item.
     * <p/>
     * Note that this is not invoked for template expansion.
     *
     * @param listener the listener to invoke. May be {@code null}
     */
    public void setAddItemListener(Runnable listener) {
        getItems().setAddItemListener(listener);
    }

    /**
     * Returns the customer associated with the charge.
     *
     * @return the customer. May be {@code null}
     */
    public Party getCustomer() {
        return (Party) getParticipant("customer");
    }

    /**
     * Returns the location associated with the charge.
     *
     * @return the location. May be {@code null}
     */
    public Party getLocation() {
        return (Party) getParticipant("location");
    }

    /**
     * Returns the items collection editor.
     *
     * @return the items collection editor. May be {@code null}
     */
    @Override
    public ChargeItemRelationshipCollectionEditor getItems() {
        return (ChargeItemRelationshipCollectionEditor) super.getItems();
    }

    /**
     * Returns the editor queue.
     *
     * @return the editor queue
     */
    public EditorQueue getEditorQueue() {
        return getItems().getEditorQueue();
    }

    /**
     * Returns the customer notes collection editor.
     *
     * @return the customer notes collection editor. May be {@code null}
     */
    public ActRelationshipCollectionEditor getCustomerNotes() {
        if (customerNotes == null) {
            CollectionProperty notes = (CollectionProperty) getProperty("customerNotes");
            if (notes != null && !notes.isHidden()) {
                customerNotes = createCustomerNotesEditor(getObject(), notes);
                getEditors().add(customerNotes);
            }
        }
        return customerNotes;
    }

    /**
     * Returns the document collection editor.
     *
     * @return the document collection editor. May be {@code null}
     */
    public ActRelationshipCollectionEditor getDocuments() {
        if (documents == null) {
            CollectionProperty notes = (CollectionProperty) getProperty("documents");
            if (notes != null && !notes.isHidden()) {
                documents = createDocumentsEditor(getObject(), notes);
                getEditors().add(documents);
            }
        }
        return documents;
    }

    /**
     * Adds a new charge item, returning its editor.
     *
     * @return the charge item editor, or {@code null} if an item couldn't be created
     */
    public CustomerChargeActItemEditor addItem() {
        ActRelationshipCollectionEditor items = getItems();
        CustomerChargeActItemEditor result = (CustomerChargeActItemEditor) items.add();
        if (result == null) {
            // the existing editor is invalid, preventing a new item being added, so force creation of the editor.
            // Note that this won't be made the current editor
            IMObject object = items.create();
            if (object != null) {
                result = (CustomerChargeActItemEditor) items.getEditor(object);
                items.addEdited(result);
            }
        }
        if (result != null && items.getCurrentEditor() == result) {
            // set the default focus to that of the item editor
            getFocusGroup().setDefault(result.getFocusGroup().getDefaultFocus());
        }
        return result;
    }

    /**
     * Removes an item.
     *
     * @param item the item to remove
     */
    public void removeItem(Act item) {
        getItems().remove(item);

        // if the item wasn't committed, then removal doesn't trigger onItemsChanged(), so do it manually.
        onItemsChanged();
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician. May be {@code null}
     */
    public void setClinician(User clinician) {
        setParticipant("clinician", clinician);
    }

    /**
     * Flags an invoice item as being ordered via a pharmacy/laboratory.
     * <p/>
     * This suppresses it from being ordered again when the invoice is saved and updates the display.
     *
     * @param item the invoice item
     */
    public void setOrdered(Act item) {
        if (orderPlacer != null) {
            orderPlacer.initialise(item);
        }
        if (getItems().hasEditor(item)) {
            CustomerChargeActItemEditor editor = getItems().getEditor(item);
            editor.ordered();
        }
    }

    /**
     * Returns invoice items that have been ordered via a pharmacy but have not been dispensed or have been partially
     * dispensed.
     *
     * @return invoice items that have
     */
    public List<Act> getNonDispensedItems() {
        List<Act> result = new ArrayList<>();
        if (orderPlacer != null) {
            for (Act item : getItems().getCurrentActs()) {
                CustomerChargeActItemEditor editor = getItems().getEditor(item);
                if (editor.isOrdered() || orderPlacer.isPharmacyProduct(editor.getProduct())) {
                    BigDecimal quantity = editor.getQuantity();
                    BigDecimal received = editor.getReceivedQuantity();
                    if (!MathRules.equals(quantity, received)) {
                        result.add(item);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Queues a popup dialog to display after any other dialog queued by the editor.
     *
     * @param dialog the dialog to display
     */
    public void queue(PopupDialog dialog) {
        getItems().getEditorQueue().queue(dialog);
    }

    /**
     * Returns the unprinted documents.
     *
     * @return the unprinted documents
     */
    public CustomerChargeDocuments getUnprintedDocuments() {
        return unprintedDocuments;
    }

    /**
     * Initialises the practice location.
     * <p/>
     * This populates the charge with the location if it is unset, as the location determines pricing.
     * <p/>
     * The context is updated with the charge's location and the stock location as this determines which products
     * are available.
     *
     * @return the practice location for the charge
     */
    protected Party initLocation() {
        Context context = getLayoutContext().getContext();
        Party location = getLocation();
        if (location == null) {
            location = context.getLocation();
            initParticipant("location", location);
        }
        context.setLocation(location);
        Party stockLocation = null;
        if (location != null) {
            LocationRules rules = ServiceHelper.getBean(LocationRules.class);
            stockLocation = rules.getDefaultStockLocation(location);
        }
        context.setStockLocation(stockLocation);
        return location;
    }

    /**
     * Save any edits.
     * <p/>
     * For invoices, this links items to their corresponding clinical events, creating events as required, and marks
     * matching reminders completed.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        ChargeSaveContext chargeContext = null;
        try {
            ChargeItemRelationshipCollectionEditor items = getItems();
            List<Act> reminders = getNewReminders();
            List<Act> alerts  = getNewAlerts();

            boolean invoice = TypeHelper.isA(getObject(), CustomerAccountArchetypes.INVOICE);
            PatientHistoryChanges changes = new PatientHistoryChanges(getLayoutContext().getContext().getUser(),
                                                                      getLayoutContext().getContext().getLocation(),
                                                                      ServiceHelper.getArchetypeService());
            List<Act> orderActs = invoice ? getOrderActs() : Collections.<Act>emptyList();
            if (invoice) {
                // cancel any orders associated with deleted invoice items prior to physical deletion
                Set<Act> updated = orderPlacer.cancelDeleted(orderActs, changes);
                if (!updated.isEmpty()) {
                    // need to save updated items before performing deletion
                    ServiceHelper.getArchetypeService().save(updated);
                }
            }

            chargeContext = items.getSaveContext();
            chargeContext.setHistoryChanges(changes);

            super.doSave();

            if (invoice) {
                // link the items to their corresponding clinical events
                linkToEvents(changes);
                if (ActStatus.POSTED.equals(getStatus())) {
                    changes.complete(new Date());
                }
            }
            chargeContext.save();

            // mark reminders that match the new reminders completed
            if (!reminders.isEmpty()) {
                reminderRules.markMatchingRemindersCompleted(reminders);
            }

            // mark alerts that match the new alerts completed
            if (!alerts.isEmpty()) {
                reminderRules.markMatchingAlertsCompleted(alerts);
            }

            if (invoice) {
                Set<Act> updated = orderPlacer.order(orderActs, changes);
                if (!updated.isEmpty()) {
                    // need to save the items again. This time do it skipping rules
                    ServiceHelper.getArchetypeService(false).save(updated);

                    // notify the editors that orders have been placed
                    for (Act item : updated) {
                        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
                            CustomerChargeActItemEditor editor = getItems().getEditor(item);
                            editor.ordered();
                        }
                    }
                }
                if (FinancialActStatus.POSTED.equals(getStatus())) {
                    // need to discontinue orders as Cubex will leave them visible, even after patient check-out
                    orderPlacer.discontinue();
                }
            }
        } finally {
            if (chargeContext != null) {
                chargeContext.setHistoryChanges(null);  // clear the history changes
            }
        }
    }

    /**
     * Deletes the object.
     * <p/>
     * This uses {@link #deleteChildren()} to delete the children prior to invoking {@link #deleteObject()}.
     *
     * @throws OpenVPMSException     if the delete fails
     * @throws IllegalStateException if the act is POSTED
     */
    @Override
    protected void doDelete() {
        if (orderPlacer != null) {
            orderPlacer.cancel();
        }
        super.doDelete();
    }

    /**
     * Links the charge items to their corresponding clinical events.
     *
     * @param changes the patient history changes
     */
    protected void linkToEvents(PatientHistoryChanges changes) {
        ChargeItemEventLinker linker = new ChargeItemEventLinker(ServiceHelper.getArchetypeService());
        List<FinancialAct> items = new ArrayList<>();
        for (Act act : getItems().getActs()) {
            items.add((FinancialAct) act);
        }
        linker.prepare(items, changes);

        addTemplateNotes(linker, changes);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        initLayoutStrategy(strategy);
        return strategy;
    }

    /**
     * Initialises a layout strategy with the customerNotes and documents collections, if they are present.
     *
     * @param strategy the layout strategy to initialise
     */
    protected void initLayoutStrategy(IMObjectLayoutStrategy strategy) {
        ActRelationshipCollectionEditor notes = getCustomerNotes();
        ActRelationshipCollectionEditor documents = getDocuments();
        if (notes != null) {
            strategy.addComponent(new ComponentState(notes));
        }
        if (documents != null) {
            strategy.addComponent(new ComponentState(documents));
        }
    }

    /**
     * Invoked when layout has completed.
     * <p/>
     * This invokes {@link #initItems()}.
     */
    @Override
    protected void onLayoutCompleted() {
        super.onLayoutCompleted();
        initItems();
    }

    /**
     * Updates the amount and tax when an act item changes.
     */
    @Override
    protected void onItemsChanged() {
        super.onItemsChanged();
        calculateCosts();
    }

    /**
     * Creates a collection editor for the customer notes collection.
     *
     * @param act   the act
     * @param notes the customer notes collection
     * @return a new collection editor
     */
    protected ActRelationshipCollectionEditor createCustomerNotesEditor(Act act, CollectionProperty notes) {
        return (ActRelationshipCollectionEditor) IMObjectCollectionEditorFactory.create(notes, act, getLayoutContext());
    }

    /**
     * Creates a collection editor for the documents collection.
     *
     * @param act       the act
     * @param documents the documents collection
     * @return a new collection editor
     */
    protected ActRelationshipCollectionEditor createDocumentsEditor(Act act, CollectionProperty documents) {
        return (ActRelationshipCollectionEditor) IMObjectCollectionEditorFactory.create(documents, act,
                                                                                        getLayoutContext());
    }

    /**
     * Creates a new {@link OrderPlacer}.
     *
     * @param customer the customer
     * @param location the practice location
     * @param user     the user responsible for the orders
     * @return a new pharmacy order placer
     */
    protected OrderPlacer createOrderPlacer(Party customer, Party location, User user) {
        OrderServices services = new OrderServices(ServiceHelper.getBean(PharmacyOrderService.class),
                                                   ServiceHelper.getBean(Pharmacies.class),
                                                   ServiceHelper.getBean(LaboratoryOrderService.class),
                                                   ServiceHelper.getBean(Laboratories.class),
                                                   ServiceHelper.getBean(PatientContextFactory.class),
                                                   ServiceHelper.getBean(PatientInformationService.class),
                                                   ServiceHelper.getBean(MedicalRecordRules.class));
        return new OrderPlacer(customer, location, user, getLayoutContext().getCache(), services);
    }

    /**
     * Determines if a default item should added if no items are present.
     *
     * @return {@code true} if a default item should added if no items are present
     */
    protected boolean getAddDefaultIem() {
        return addDefaultItem;
    }

    /**
     * Returns all acts that may be associated with pharmacy or laboratory orders.
     *
     * @return the acts
     */
    private List<Act> getOrderActs() {
        List<Act> acts = new ArrayList<>();
        for (Act item : getItems().getActs()) {
            CustomerChargeActItemEditor editor = getItems().getEditor(item);
            acts.add(item);
            acts.addAll(editor.getInvestigations());
        }
        return acts;
    }

    /**
     * Adds a default invoice item if there are no items present and {@link #addDefaultItem} is {@code true}.
     */
    private void initItems() {
        if (addDefaultItem) {
            ActRelationshipCollectionEditor items = getItems();
            CollectionProperty property = items.getCollection();
            if (property.getValues().isEmpty()) {
                // no invoice items, so add one
                addItem();
            }
        }
    }

    /**
     * Calculates the fixed and unit costs.
     */
    private void calculateCosts() {
        Property fixedCost = getProperty("fixedCost");
        BigDecimal fixed = ActHelper.sum(getObject(), getItems().getCurrentActs(), "fixedCost");
        fixedCost.setValue(fixed);

        Property unitCost = getProperty("unitCost");
        BigDecimal cost = BigDecimal.ZERO;
        for (Act act : getItems().getCurrentActs()) {
            cost = cost.add(calcTotalUnitCost(act));
        }
        unitCost.setValue(cost);
    }

    /**
     * Calculates the total unit cost for an act, based on its <em>unitCost</em>
     * and <em>quantity</em>.
     *
     * @param act the act
     * @return the total unit cost
     */
    private BigDecimal calcTotalUnitCost(Act act) {
        IMObjectBean bean = new IMObjectBean(act);
        BigDecimal unitCost = bean.getBigDecimal("unitCost", BigDecimal.ZERO);
        BigDecimal quantity = bean.getBigDecimal("quantity", BigDecimal.ZERO);
        return unitCost.multiply(quantity);
    }

    /**
     * Returns new reminders from each of the charge items.
     *
     * @return a list of new reminders
     */
    private List<Act> getNewReminders() {
        ActRelationshipCollectionEditor items = getItems();
        List<Act> reminders = new ArrayList<>();
        for (IMObjectEditor editor : items.getEditors()) {
            if (editor instanceof CustomerChargeActItemEditor) {
                CustomerChargeActItemEditor charge = (CustomerChargeActItemEditor) editor;
                for (Act reminder : charge.getReminders()) {
                    if (reminder.isNew()) {
                        reminders.add(reminder);
                    }
                }
            }
        }
        return reminders;
    }

    /**
     * Returns new alerts from each of the charge items.
     *
     * @return a list of new alerts
     */
    private List<Act> getNewAlerts() {
        return getItems().getEditContext().getAlerts().getNewAlerts();
    }

    /**
     * Invoked when a template product is expanded on an invoice.
     * <p/>
     * This appends any invoiceNote to the notes.
     *
     * @param product the template product
     */
    private void templateProductExpanded(Product product) {
        Property property = getProperty("notes");
        if (property != null) {
            IMObjectBean bean = new IMObjectBean(product);
            String invoiceNote = bean.getString("invoiceNote");
            if (!StringUtils.isEmpty(invoiceNote)) {
                String value = invoiceNote;
                if (property.getValue() != null) {
                    value = property.getValue().toString();
                    if (!StringUtils.isEmpty(value)) {
                        value = value + "\n" + invoiceNote;
                    } else {
                        value = invoiceNote;
                    }
                }
                property.setValue(value);
            }
        }
    }

    /**
     * Creates <em>act.patientClinicalNote</em> acts for any notes associated with template products, linking them to
     * the event.
     *
     * @param linker  the event linker
     * @param changes the patient history changes
     */
    protected void addTemplateNotes(ChargeItemEventLinker linker, PatientHistoryChanges changes) {
        List<TemplateChargeItems> templates = getItems().getTemplates();
        if (!templates.isEmpty()) {
            List<Act> items = getItems().getActs();
            List<Act> notes = new ArrayList<>();
            MedicalRecordRules rules = ServiceHelper.getBean(MedicalRecordRules.class);
            for (TemplateChargeItems template : templates) {
                Act item = template.findFirst(items);
                if (item != null) {
                    String visitNote = template.getVisitNote();
                    if (!StringUtils.isEmpty(visitNote)) {
                        ActBean bean = new ActBean(item);
                        Party patient = (Party) getObject(bean.getNodeParticipantRef("patient"));
                        if (patient != null) {
                            Date itemStartTime = bean.getDate("startTime");
                            Date startTime = getStartTime();
                            if (DateRules.getDate(itemStartTime).compareTo(DateRules.getDate(startTime)) != 0) {
                                // use the item start time if its date is different to that of the invoice
                                startTime = itemStartTime;
                            }
                            User clinician = (User) getObject(bean.getNodeParticipantRef("clinician"));
                            User author = (User) getObject(bean.getNodeParticipantRef("author"));
                            Act note = rules.createNote(startTime, patient, visitNote, clinician, author);
                            notes.add(note);
                        }
                    }
                }
            }
            if (!notes.isEmpty()) {
                ServiceHelper.getArchetypeService().save(notes);
                linker.prepareNotes(notes, changes);
            }
            getItems().clearTemplates();
        }
    }

}