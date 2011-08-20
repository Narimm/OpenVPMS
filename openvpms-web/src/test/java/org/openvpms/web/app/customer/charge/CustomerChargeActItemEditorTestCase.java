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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.customer.charge;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.ErrorHandler;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Tests the {@link CustomerChargeActItemEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActItemEditorTestCase extends AbstractAppTest {

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The customer.
     */
    private Party customer;

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<String>();

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        practice = TestHelper.getPractice();
        practice.addClassification(createTaxType());
        save(practice);
        customer = TestHelper.createCustomer();

        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
            }
        });
    }

    /**
     * Tests populating an invoice item with a medication.
     */
    @Test
    public void testInvoiceItemMedication() {
        checkInvoiceItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating an invoice item with a merchandise product.
     */
    @Test
    public void testInvoiceItemMerchandise() {
        checkInvoiceItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating an invoice item with a service product.
     */
    @Test
    public void testInvoiceItemService() {
        checkInvoiceItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating an invoice item with a template product.
     */
    @Test
    public void testInvoiceItemTemplate() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new Money(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItemWithTemplate(invoice, item);
    }

    /**
     * Tests populating a counter sale item with a medication product.
     */
    @Test
    public void testCounterSaleItemMedication() {
        checkCounterSaleItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating a counter sale item with a merchandise product.
     */
    @Test
    public void testCounterSaleItemMerchandise() {
        checkCounterSaleItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating a counter sale item with a service product.
     */
    @Test
    public void testCounterSaleItemService() {
        checkCounterSaleItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating a counter sale item with a template product.
     */
    @Test
    public void testCounterSaleItemTemplate() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new Money(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct counterSale = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItemWithTemplate(counterSale, item);
    }

    /**
     * Tests populating a credit item with a medication product.
     */
    @Test
    public void testCreditItemMedication() {
        checkCreditItem(ProductArchetypes.MEDICATION);
    }

    /**
     * Tests populating a credit item with a merchandise product.
     */
    @Test
    public void testCreditItemMerchandise() {
        checkCreditItem(ProductArchetypes.MERCHANDISE);
    }

    /**
     * Tests populating a credit item with a service product.
     */
    @Test
    public void testCreditItemService() {
        checkCreditItem(ProductArchetypes.SERVICE);
    }

    /**
     * Tests populating a credit item with a template product.
     */
    @Test
    public void testCreditItemTemplate() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new Money(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItemWithTemplate(credit, item);
    }

    /**
     * Checks populating an invoice item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkInvoiceItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new Money(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(invoice, item, productShortName);
    }

    /**
     * Checks populating a counter sale item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkCounterSaleItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new Money(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct counterSale = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(counterSale, item, productShortName);
    }

    /**
     * Checks populating a credit item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkCreditItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new Money(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItem(credit, item, productShortName);
    }

    /**
     * Checks populating a charge item with a product.
     *
     * @param charge           the charge
     * @param item             the charge item
     * @param productShortName the product archetype short name
     */
    private void checkItem(FinancialAct charge, FinancialAct item, String productShortName) {
        LayoutContext context = new DefaultLayoutContext();
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        User author1 = TestHelper.createUser();
        User author2 = TestHelper.createUser();
        User clinician1 = TestHelper.createUser();
        User clinician2 = TestHelper.createUser();

        // create product1 with reminder and investigation type
        BigDecimal quantity1 = BigDecimal.valueOf(2);
        BigDecimal unitCost1 = BigDecimal.valueOf(5);
        BigDecimal unitPrice1 = BigDecimal.valueOf(10);
        BigDecimal fixedCost1 = BigDecimal.valueOf(1);
        BigDecimal fixedPrice1 = BigDecimal.valueOf(2);
        BigDecimal discount1 = BigDecimal.ZERO;
        BigDecimal tax1 = BigDecimal.valueOf(2);
        BigDecimal total1 = new BigDecimal("22");
        Product product1 = createProduct(productShortName, fixedCost1, fixedPrice1, unitCost1, unitPrice1);
        Entity reminderType = addReminder(product1);
        Entity investigationType = addInvestigation(product1);
        Entity template = addTemplate(product1);

        // create  product2 with no reminder no investigation type
        BigDecimal quantity2 = BigDecimal.valueOf(1);
        BigDecimal unitCost2 = BigDecimal.valueOf(5);
        BigDecimal unitPrice2 = BigDecimal.valueOf(11);
        BigDecimal fixedCost2 = BigDecimal.ZERO;
        BigDecimal fixedPrice2 = BigDecimal.ZERO;
        BigDecimal discount2 = BigDecimal.ZERO;
        BigDecimal tax2 = BigDecimal.valueOf(1);
        BigDecimal total2 = BigDecimal.valueOf(11);

        Product product2 = createProduct(productShortName, fixedCost2, fixedPrice2, unitCost2, unitPrice2);

        // set up the context
        context.getContext().setUser(author1); // to propagate to acts
        context.getContext().setClinician(clinician1);

        // create the editor
        CustomerChargeActItemEditor editor = new CustomerChargeActItemEditor(item, charge, context);
        editor.getComponent();
        assertFalse(editor.isValid());

        // register a handler for act popups
        EditorManager mgr = new EditorManager();
        editor.setPopupEditorManager(mgr);

        // populate quantity, patient, product. If product1 is a medication, it should trigger a patient medication
        // editor popup
        editor.setQuantity(quantity1);

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient1);
        }
        editor.setProduct(product1);

        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (TypeHelper.isA(product1, ProductArchetypes.MEDICATION)) {
                // invoice items have a dispensing node
                assertFalse(editor.isValid()); // not valid while popup is displayed
                checkSavePopup(mgr, PatientArchetypes.PATIENT_MEDICATION);
                // save the popup editor - should be a medication
            }

            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(mgr, InvestigationArchetypes.PATIENT_INVESTIGATION);

            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(mgr, ReminderArchetypes.REMINDER);
        }

        // editor should now be valid
        assertTrue(editor.isValid());

        // save it
        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        checkItem(item, patient1, product1, author1, clinician1, quantity1, unitCost1, unitPrice1, fixedCost1,
                  fixedPrice1, discount1, tax1, total1);
        ActBean itemBean = new ActBean(item);
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (TypeHelper.isA(product1, ProductArchetypes.MEDICATION)) {
                // verify there is a medication act
                checkMedication(item, patient1, product1, author1, clinician1);
            } else {
                assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            }
            checkInvestigation(item, patient1, investigationType, author1, clinician1);
            checkReminder(item, patient1, product1, reminderType, author1, clinician1);
            checkDocument(item, patient1, product1, template, author1, clinician1);
        } else {
            // verify there are no medication, investigation, reminder nor document acts
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            assertTrue(itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).isEmpty());
            assertTrue(itemBean.getActs(ReminderArchetypes.REMINDER).isEmpty());
            assertTrue(itemBean.getActs("act.patientDocument*").isEmpty());
        }

        // now replace the patient, product, author and clinician
        if (itemBean.hasNode("patient")) {
            editor.setPatient(patient2);
        }
        editor.setProduct(product2);
        editor.setQuantity(quantity2);
        editor.setDiscount(discount2);
        editor.setAuthor(author2);
        if (itemBean.hasNode("clinician")) {
            editor.setClinician(clinician2);
        }

        // should be no more popups. For medication products, the
        assertNull(mgr.getCurrent());  // no new popup - existing medication should update
        assertTrue(editor.isValid());

        // save it
        checkSave(charge, editor);

        item = get(item);
        assertNotNull(item);

        checkItem(item, patient2, product2, author2, clinician2, quantity2, unitCost2, unitPrice2, fixedCost2,
                  fixedPrice2, discount2, tax2, total2);
        itemBean = new ActBean(item);
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)
            && TypeHelper.isA(product2, ProductArchetypes.MEDICATION)) {
            // verify there is a medication act. Note that it retains the original author
            checkMedication(item, patient2, product2, author1, clinician2);
        } else {
            // verify there is a medication act
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
        }
        assertTrue(itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).isEmpty());
        assertTrue(itemBean.getActs(ReminderArchetypes.REMINDER).isEmpty());
        assertTrue(itemBean.getActs("act.patientDocument*").isEmpty());

        editor.setProduct(null);       // make sure nulls are handled
        assertFalse(editor.isValid());

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Checks populating a charge item with a template product.
     * <p/>
     * NOTE: currently, charge items with template products validate correctly, but fail to save.
     * <p/>This is because the charge item relationship editor will only expand templates if the charge item itself
     * is valid - marking the item invalid for having a template would prevent this.
     * TODO - not ideal.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkItemWithTemplate(FinancialAct charge, FinancialAct item) {
        LayoutContext context = new DefaultLayoutContext();
        Party patient = TestHelper.createPatient();
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        Product product = createProduct(ProductArchetypes.TEMPLATE, fixedCost, fixedPrice, unitCost, unitPrice);
        User author = TestHelper.createUser();
        User clinician = TestHelper.createUser();
        context.getContext().setUser(author); // to propagate to acts
        context.getContext().setClinician(clinician);

        CustomerChargeActItemEditor editor = new CustomerChargeActItemEditor(item, charge, context);
        editor.getComponent();
        assertFalse(editor.isValid());

        // register a handler for act popups
        EditorManager mgr = new EditorManager();
        editor.setPopupEditorManager(mgr);

        // populate quantity, patient, product
        editor.setQuantity(quantity);
        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient);
        }
        editor.setProduct(product);

        // editor should now be valid, but won't save
        assertTrue(editor.isValid());

        try {
            save(charge, editor);
            fail("Expected save to fail");
        } catch (IllegalStateException expected) {
            assertEquals("Cannot save with product template: " + product.getName(), expected.getMessage());
        }

        checkItem(item, patient, product, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        ActBean itemBean = new ActBean(item);
        // verify there are no medication acts
        assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies an item's properties match that expected.
     *
     * @param item       the item to check
     * @param patient    the expected patient
     * @param product    the expected product
     * @param author     the expected author
     * @param clinician  the expected clinician
     * @param quantity   the expected quantity
     * @param unitCost   the expected unit cost
     * @param unitPrice  the expected unit price
     * @param fixedCost  the expected fixed cost
     * @param fixedPrice the expected fixed price
     * @param discount   the expected discount
     * @param tax        the expected tax
     * @param total      the expected total
     */
    private void checkItem(FinancialAct item, Party patient, Product product, User author, User clinician,
                           BigDecimal quantity, BigDecimal unitCost, BigDecimal unitPrice, BigDecimal fixedCost,
                           BigDecimal fixedPrice, BigDecimal discount, BigDecimal tax, BigDecimal total) {
        ActBean bean = new ActBean(item);
        if (bean.hasNode("patient")) {
            assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        }
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        if (bean.hasNode("clinician")) {
            assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        }
        checkEquals(quantity, bean.getBigDecimal("quantity"));
        checkEquals(fixedCost, bean.getBigDecimal("fixedCost"));
        checkEquals(fixedPrice, bean.getBigDecimal("fixedPrice"));
        checkEquals(unitPrice, bean.getBigDecimal("unitPrice"));
        checkEquals(unitCost, bean.getBigDecimal("unitCost"));
        checkEquals(discount, bean.getBigDecimal("discount"));
        checkEquals(tax, bean.getBigDecimal("tax"));
        checkEquals(total, bean.getBigDecimal("total"));
    }

    /**
     * Verifies a patient medication act matches that expected.
     *
     * @param item      the charge item, linked to the medication
     * @param patient   the expected patient
     * @param product   the expected product
     * @param author    the expected author
     * @param clinician the expected clinician
     */
    private void checkMedication(FinancialAct item, Party patient, Product product, User author,
                                 User clinician) {
        ActBean itemBean = new ActBean(item);
        List<Act> dispensing = itemBean.getNodeActs("dispensing");
        assertEquals(1, dispensing.size());

        Act medication = dispensing.get(0);
        ActBean bean = new ActBean(medication);
        assertEquals(item.getActivityStartTime(), medication.getActivityStartTime());
        assertEquals(item.getActivityEndTime(), medication.getActivityEndTime());
        assertTrue(bean.isA(PatientArchetypes.PATIENT_MEDICATION));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        checkEquals(item.getQuantity(), bean.getBigDecimal("quantity"));
    }

    /**
     * Verifies a patient investigation act matches that expected.
     *
     * @param item              the charge item, linked to the investigation
     * @param patient           the expected patient
     * @param investigationType the expected investigation type
     * @param author            the expected author
     * @param clinician         the expected clinician
     */
    private void checkInvestigation(Act item, Party patient, Entity investigationType, User author, User clinician) {
        ActBean itemBean = new ActBean(item);
        List<Act> investigations = itemBean.getNodeActs("investigations");
        assertEquals(1, investigations.size());

        Act investigation = investigations.get(0);
        ActBean bean = new ActBean(investigation);
        assertTrue(bean.isA(InvestigationArchetypes.PATIENT_INVESTIGATION));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(investigationType.getObjectReference(), bean.getNodeParticipantRef("investigationType"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
    }

    /**
     * Verifies a patient reminder act matches that expected.
     *
     * @param item         the charge item, linked to the reminder
     * @param patient      the expected patient
     * @param product      the expected product
     * @param reminderType the expected reminder type
     * @param author       the expected author
     * @param clinician    the expected clinician
     */
    private void checkReminder(Act item, Party patient, Product product, Entity reminderType, User author,
                               User clinician) {
        ActBean itemBean = new ActBean(item);
        ReminderRules rules = new ReminderRules();
        List<Act> reminders = itemBean.getNodeActs("reminders");
        assertEquals(1, reminders.size());

        EntityBean productBean = new EntityBean(product);
        List<EntityRelationship> rels = productBean.getNodeRelationships("reminders");
        assertEquals(1, rels.size());
        Act reminder = reminders.get(0);
        ActBean bean = new ActBean(reminder);
        assertTrue(bean.isA(ReminderArchetypes.REMINDER));
        assertEquals(item.getActivityStartTime(), reminder.getActivityStartTime());
        Date dueDate = rules.calculateProductReminderDueDate(item.getActivityStartTime(), rels.get(0));
        assertEquals(0, DateRules.compareTo(reminder.getActivityEndTime(), dueDate));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(reminderType.getObjectReference(), bean.getNodeParticipantRef("reminderType"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
    }

    /**
     * Verifies a document act matches that expected.
     *
     * @param item      the charge item, linked to the document act
     * @param patient   the expected patient
     * @param product   the expected product
     * @param template  the expected document template
     * @param author    the expected author
     * @param clinician the expected clinician
     */
    private void checkDocument(Act item, Party patient, Product product, Entity template, User author, User clinician) {
        ActBean itemBean = new ActBean(item);
        List<Act> documents = itemBean.getNodeActs("documents");
        assertEquals(1, documents.size());

        ActBean bean = new ActBean(documents.get(0));
        assertTrue(bean.isA(PatientArchetypes.DOCUMENT_FORM));
        assertEquals(product.getObjectReference(), bean.getNodeParticipantRef("product"));
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(template.getObjectReference(), bean.getNodeParticipantRef("documentTemplate"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
    }

    /**
     * Saves the current popup editor.
     *
     * @param mgr       the popup editor manager
     * @param shortName the expected archetype short name of the object being edited
     */
    private void checkSavePopup(EditorManager mgr, String shortName) {
        EditDialog dialog = mgr.getCurrent();
        assertNotNull(dialog);
        IMObjectEditor editor = dialog.getEditor();
        assertTrue(TypeHelper.isA(editor.getObject(), shortName));
        assertTrue(editor.isValid());
        clickDialogOK(dialog);
    }

    /**
     * Saves a charge and charge item editor in a single transaction, verifying the save was successful.
     *
     * @param charge the charge
     * @param editor the charge item editor
     */
    private void checkSave(final FinancialAct charge, final CustomerChargeActItemEditor editor) {
        boolean result = save(charge, editor);
        assertTrue(result);
    }

    /**
     * Saves a charge and charge item editor in a single transaction.
     *
     * @param charge the charge
     * @param editor the charge item editor
     * @return <tt>true</tt> if the save was successful, otherwise <tt>false</tt>
     */
    private boolean save(final FinancialAct charge, final CustomerChargeActItemEditor editor) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        return template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return SaveHelper.save(charge) && editor.save();
            }
        });
    }

    /**
     * Adds an investigation type to a product.
     *
     * @param product the product
     * @return the investigation type
     */
    private Entity addInvestigation(Product product) {
        Entity investigation = (Entity) create(InvestigationArchetypes.INVESTIGATION_TYPE);
        investigation.setName("X-TestInvestigationType-" + investigation.hashCode());
        EntityBean productBean = new EntityBean(product);
        productBean.addNodeRelationship("investigationTypes", investigation);
        save(investigation, product);
        return investigation;
    }

    /**
     * Adds a document template to a product.
     *
     * @param product the product
     * @return the document template
     */
    private Entity addTemplate(Product product) {
        Entity template = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        template.setName("X-TestDocumentTemplate-" + template.hashCode());
        EntityBean productBean = new EntityBean(product);
        productBean.addNodeRelationship("documents", template);
        save(template, product);
        return template;
    }

    /**
     * Adds an interactive reminder type to a product.
     *
     * @param product the product
     * @return the reminder type
     */
    private Entity addReminder(Product product) {
        Entity reminderType = ReminderTestHelper.createReminderType();
        EntityBean productBean = new EntityBean(product);
        EntityRelationship rel = productBean.addNodeRelationship("reminders", reminderType);
        IMObjectBean relBean = new IMObjectBean(rel);
        relBean.setValue("interactive", true);
        save(product, reminderType);
        return reminderType;
    }

    /**
     * Helper to create a product.
     *
     * @param shortName  the product archetype short name
     * @param fixedCost  the fixed cost
     * @param fixedPrice the fixed price
     * @param unitCost   the unit cost
     * @param unitPrice  the unit price
     * @return a new product
     */
    private Product createProduct(String shortName, BigDecimal fixedCost, BigDecimal fixedPrice, BigDecimal unitCost,
                                  BigDecimal unitPrice) {
        Product product = TestHelper.createProduct(shortName, null, false);
        product.addProductPrice(createFixedPrice(product, fixedCost, fixedPrice));
        product.addProductPrice(createUnitPrice(product, unitCost, unitPrice));
        save(product);
        return product;
    }

    /**
     * Helper to create a new unit price.
     *
     * @param product the product
     * @param cost    the cost price
     * @param price   the price after markup
     * @return a new unit price
     */
    private ProductPrice createUnitPrice(Product product, BigDecimal cost, BigDecimal price) {
        return createPrice(product, ProductArchetypes.UNIT_PRICE, cost, price);
    }

    /**
     * Helper to create a new fixed price.
     *
     * @param product the product
     * @param cost    the cost price
     * @param price   the price after markup
     * @return a new unit price
     */
    private ProductPrice createFixedPrice(Product product, BigDecimal cost, BigDecimal price) {
        return createPrice(product, ProductArchetypes.FIXED_PRICE, cost, price);
    }

    /**
     * Helper to create a new product price.
     *
     * @param product   the product
     * @param shortName the product price archetype short name
     * @param cost      the cost price
     * @param price     the price after markup
     * @return a new unit price
     */
    private ProductPrice createPrice(Product product, String shortName, BigDecimal cost, BigDecimal price) {
        ProductPrice result = (ProductPrice) create(shortName);
        ProductPriceRules rules = new ProductPriceRules();
        BigDecimal markup = rules.getMarkup(product, cost, price, practice);
        result.setName("XPrice");
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("cost", cost);
        bean.setValue("markup", markup);
        bean.setValue("price", price);
        return result;
    }


    /**
     * Helper to click OK on an edit dialog.
     *
     * @param dialog the dialog
     */
    private void clickDialogOK(EditDialog dialog) {
        Button ok = dialog.getButtons().getButton(PopupDialog.OK_ID);
        assertNotNull(ok);
        assertTrue(ok.isEnabled());
        ok.fireActionPerformed(new ActionEvent(ok, ok.getActionCommand()));
    }

    /**
     * Helper to create and save a new tax type classification.
     *
     * @return a new tax classification
     */
    private Lookup createTaxType() {
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean bean = new IMObjectBean(tax);
        bean.setValue("code", "XTAXRULESTESTCASE_CLASSIFICATION_"
                              + Math.abs(new Random().nextInt()));
        bean.setValue("rate", new BigDecimal(10));
        save(tax);
        return tax;
    }

    private static class EditorManager extends PopupEditorManager {

        /**
         * The current edit dialog.
         */
        private EditDialog current;

        /**
         * Returns the current popup dialog.
         *
         * @return the current popup dialog. May be <tt>null</tt>
         */
        public EditDialog getCurrent() {
            return current;
        }

        /**
         * Displays an edit dialog.
         *
         * @param dialog the dialog
         */
        @Override
        protected void edit(EditDialog dialog) {
            super.edit(dialog);
            current = dialog;
        }

        /**
         * Invoked when the edit is completed.
         */
        @Override
        protected void editCompleted() {
            super.editCompleted();
            current = null;
        }
    }
}
