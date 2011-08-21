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

import nextapp.echo2.app.event.WindowPaneListener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.util.ErrorHandler;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the {@link CustomerChargeActItemEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActItemEditorTestCase extends AbstractCustomerChargeActEditorTest {

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
            
            assertEquals(1, itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).size());
            assertEquals(1, itemBean.getActs(ReminderArchetypes.REMINDER).size());
            assertEquals(1, itemBean.getActs("act.patientDocument*").size());

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

}
