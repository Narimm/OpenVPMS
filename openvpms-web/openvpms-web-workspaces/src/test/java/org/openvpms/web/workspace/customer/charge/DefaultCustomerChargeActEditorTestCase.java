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

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.TestLaboratoryOrderService.LabOrder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.product.ProductArchetypes.MEDICATION;
import static org.openvpms.archetype.rules.product.ProductArchetypes.SERVICE;
import static org.openvpms.archetype.rules.product.ProductTestHelper.addDose;
import static org.openvpms.archetype.rules.product.ProductTestHelper.createDose;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.checkOrder;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.createLaboratory;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.createPharmacy;
import static org.openvpms.web.workspace.customer.charge.TestPharmacyOrderService.Order;

/**
 * Tests the {@link DefaultCustomerChargeActEditor} class.
 *
 * @author Tim Anderson
 */
public class DefaultCustomerChargeActEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The author.
     */
    private User author;

    /**
     * The clinician.
     */
    private User clinician;

    /**
     * The practice location.
     */
    private Party location;

    /**
     * The layout context.
     */
    private LayoutContext layoutContext;

    /**
     * Medical record rules.
     */
    private MedicalRecordRules records;


    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient(customer);
        author = TestHelper.createUser();
        clinician = TestHelper.createClinician();
        location = TestHelper.createLocation();

        layoutContext = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        layoutContext.getContext().setPractice(getPractice());
        layoutContext.getContext().setCustomer(customer);
        layoutContext.getContext().setUser(author);
        layoutContext.getContext().setClinician(clinician);
        layoutContext.getContext().setLocation(location);

        records = ServiceHelper.getBean(MedicalRecordRules.class);
    }

    /**
     * Tests creation and saving of an empty invoice.
     */
    @Test
    public void testEmptyInvoice() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Tests creation and saving of an empty invoice.
     */
    @Test
    public void testEmptyCredit() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Tests creation and saving of an empty counter sale.
     */
    @Test
    public void testEmptyCounterSale() {
        checkEmptyCharge((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests invoicing.
     */
    @Test
    public void testInvoice() {
        checkEditCharge((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Tests counter sales.
     */
    @Test
    public void testCounterSale() {
        checkEditCharge((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Tests credits.
     */
    @Test
    public void testCredit() {
        checkEditCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Tests the addition of 3 items to an invoice.
     */
    @Test
    public void testAdd3Items() {
        BigDecimal itemTotal1 = BigDecimal.valueOf(20);
        BigDecimal itemTotal2 = BigDecimal.valueOf(50);
        BigDecimal itemTotal3 = new BigDecimal("41.25");

        Product product1 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("18.18"));
        Product product2 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("45.45"));
        Product product3 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("37.50"));

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = ONE;
        addItem(editor, patient, product1, quantity, queue);
        addItem(editor, patient, product2, quantity, queue);
        addItem(editor, patient, product3, quantity, queue);
        assertTrue(SaveHelper.save(editor));

        checkTotal(charge, total);
    }

    /**
     * Tests the addition of 3 items to an invoice, followed by the deletion of 1, verifying totals.
     */
    @Test
    public void testAdd3ItemsWithDeletion() {
        BigDecimal itemTotal1 = BigDecimal.valueOf(20);
        BigDecimal itemTotal2 = BigDecimal.valueOf(50);
        BigDecimal itemTotal3 = new BigDecimal("41.25");

        Product product1 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("18.18"));
        Product product2 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("45.45"));
        Product product3 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("37.50"));

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
            editor.getComponent();
            assertTrue(editor.isValid());

            BigDecimal quantity = ONE;
            EditorQueue queue = editor.getQueue();
            CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, quantity, queue);
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, queue);
            CustomerChargeActItemEditor itemEditor3 = addItem(editor, patient, product3, quantity, queue);
            assertTrue(SaveHelper.save(editor));

            charge = get(charge);
            assertTrue(charge.getTotal().compareTo(total) == 0);
            ActCalculator calculator = new ActCalculator(getArchetypeService());
            BigDecimal itemTotal = calculator.sum(charge, "total");
            assertTrue(itemTotal.compareTo(total) == 0);

            if (j == 0) {
                editor.delete(itemEditor1.getObject());
                total = total.subtract(itemTotal1);
            } else if (j == 1) {
                editor.delete(itemEditor2.getObject());
                total = total.subtract(itemTotal2);
            } else if (j == 2) {
                editor.delete(itemEditor3.getObject());
                total = total.subtract(itemTotal3);
            }
            ++j;
            if (j > 2) {
                j = 0;
            }
            assertTrue(SaveHelper.save(editor));
            charge = get(charge);
            checkTotal(charge, total);
        }
    }

    /**
     * Tests the addition of 3 items to an invoice, followed by the deletion of 1 in a new editor, verifying totals.
     */
    @Test
    public void testAdd3ItemsWithDeletionAfterReload() {
        BigDecimal itemTotal1 = BigDecimal.valueOf(20);
        BigDecimal itemTotal2 = BigDecimal.valueOf(50);
        BigDecimal itemTotal3 = new BigDecimal("41.25");

        Product product1 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("18.18"));
        Product product2 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("45.45"));
        Product product3 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("37.50"));

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
            EditorQueue queue = editor.getQueue();
            editor.getComponent();
            assertTrue(editor.isValid());

            BigDecimal quantity = ONE;
            CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, quantity, queue);
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, queue);
            CustomerChargeActItemEditor itemEditor3 = addItem(editor, patient, product3, quantity, queue);
            assertTrue(SaveHelper.save(editor));

            charge = get(charge);
            checkTotal(charge, total);

            editor = createCustomerChargeActEditor(charge, layoutContext);
            editor.getComponent();

            if (j == 0) {
                editor.delete(itemEditor1.getObject());
                total = total.subtract(itemTotal1);
            } else if (j == 1) {
                editor.delete(itemEditor2.getObject());
                total = total.subtract(itemTotal2);
            } else if (j == 2) {
                editor.delete(itemEditor3.getObject());
                total = total.subtract(itemTotal3);
            }
            ++j;
            if (j > 2) {
                j = 0;
            }
            assertTrue(SaveHelper.save(editor));
            charge = get(charge);
            checkTotal(charge, total);
        }
    }

    /**
     * Tests the addition of 3 items to an invoice, followed by the deletion of 1 before saving.
     */
    @Test
    public void test3ItemsAdditionWithDeletionBeforeSave() {
        BigDecimal itemTotal1 = BigDecimal.valueOf(20);
        BigDecimal itemTotal2 = BigDecimal.valueOf(50);
        BigDecimal itemTotal3 = new BigDecimal("41.25");

        Product product1 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("18.18"));
        Product product2 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("45.45"));
        Product product3 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("37.50"));

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
            EditorQueue queue = editor.getQueue();
            editor.getComponent();

            BigDecimal quantity = ONE;
            CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, quantity, queue);
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, queue);
            CustomerChargeActItemEditor itemEditor3 = addItem(editor, patient, product3, quantity, queue);

            if (j == 0) {
                editor.delete(itemEditor1.getObject());
                total = total.subtract(itemTotal1);
            } else if (j == 1) {
                editor.delete(itemEditor2.getObject());
                total = total.subtract(itemTotal2);
            } else if (j == 2) {
                editor.delete(itemEditor3.getObject());
                total = total.subtract(itemTotal3);
            }
            ++j;
            if (j > 2) {
                j = 0;
            }

            assertTrue(SaveHelper.save(editor));
            charge = get(charge);
            checkTotal(charge, total);
        }
    }

    /**
     * Tests the addition of 2 items to an invoice, followed by the change of product of 1 before saving.
     */
    @Test
    public void testItemChange() {
        BigDecimal itemTotal1 = BigDecimal.valueOf(20);
        BigDecimal itemTotal2 = BigDecimal.valueOf(50);
        BigDecimal itemTotal3 = new BigDecimal("41.25");

        Product product1 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("18.18"));
        Product product2 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("45.45"));
        Product product3 = createProduct(ProductArchetypes.SERVICE, new BigDecimal("37.50"));

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            boolean addDefaultItem = (j == 0);
            TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext, addDefaultItem);
            editor.getComponent();

            BigDecimal quantity = ONE;
            CustomerChargeActItemEditor itemEditor1;
            if (j == 0) {
                itemEditor1 = editor.getCurrentEditor();
                setItem(editor, itemEditor1, patient, product1, quantity, editor.getQueue());
            } else {
                itemEditor1 = addItem(editor, patient, product1, quantity, editor.getQueue());
            }
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, editor.getQueue());

            if (j == 0) {
                itemEditor1.setProduct(product3);
                total = total.subtract(itemTotal1);
            } else if (j == 1) {
                itemEditor2.setProduct(product3);
                total = total.subtract(itemTotal2);
            }
            ++j;
            if (j > 1) {
                j = 0;
            }

            assertTrue(SaveHelper.save(editor));
            charge = get(charge);
            checkTotal(charge, total);
        }
    }

    /**
     * Verifies that the {@link DefaultCustomerChargeActEditor#delete()} method deletes an invoice and its item.
     * <p>
     * If any pharmacy or lab orders have been created, these are cancelled.
     */
    @Test
    public void testDeleteInvoice() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        BigDecimal fixedPrice = BigDecimal.TEN;
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        Entity pharmacy = CustomerChargeTestHelper.createPharmacy(location);
        Entity laboratory = CustomerChargeTestHelper.createLaboratory(location);

        Product product1 = createProduct(MEDICATION, fixedPrice, pharmacy);
        Entity reminderType1 = addReminder(product1);
        Entity investigationType1 = addInvestigation(product1);
        Entity template1 = addTemplate(product1);
        Entity alertType1 = addAlertType(product1);

        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice, pharmacy);
        Entity reminderType2 = addReminder(product2);
        Entity investigationType2 = addInvestigation(product2);
        Entity investigationType3 = addInvestigation(product2);
        Entity template2 = addTemplate(product2);
        Entity alertType2 = addAlertType(product2);

        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);
        Entity reminderType3 = addReminder(product3);
        Entity investigationType4 = addInvestigation(product3, laboratory); // ordered via lab
        Entity investigationType5 = addInvestigation(product3);
        Entity investigationType6 = addInvestigation(product3);
        Entity alertType3 = addAlertType(product3);

        Entity template3 = addTemplate(product3);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = ONE;
        CustomerChargeActItemEditor item1Editor = addItem(editor, patient, product1, quantity, queue);
        CustomerChargeActItemEditor item2Editor = addItem(editor, patient, product2, quantity, queue);
        CustomerChargeActItemEditor item3Editor = addItem(editor, patient, product3, quantity, queue);
        FinancialAct item1 = (FinancialAct) item1Editor.getObject();
        FinancialAct item2 = (FinancialAct) item2Editor.getObject();
        FinancialAct item3 = (FinancialAct) item3Editor.getObject();

        assertTrue(SaveHelper.save(editor));

        // check pharmacy orders
        List<Order> orders = editor.getPharmacyOrderService().getOrders();
        assertEquals(2, orders.size());
        editor.getPharmacyOrderService().clear();

        // check lab orders
        List<LabOrder> labOrders = editor.getLaboratoryOrderService().getOrders();
        assertEquals(1, labOrders.size());
        editor.getLaboratoryOrderService().clear();

        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, ZERO);

        DocumentAct investigation1 = getInvestigation(item1, investigationType1);
        DocumentAct investigation2 = getInvestigation(item2, investigationType2);
        DocumentAct investigation3 = getInvestigation(item2, investigationType3);
        DocumentAct investigation4 = getInvestigation(item3, investigationType4);
        DocumentAct investigation5 = getInvestigation(item3, investigationType5);
        DocumentAct investigation6 = getInvestigation(item3, investigationType6);

        PatientTestHelper.addReport(investigation4);
        PatientTestHelper.addReport(investigation5);
        investigation1.setStatus(ActStatus.IN_PROGRESS);
        investigation2.setStatus(ActStatus.POSTED);
        investigation3.setStatus(ActStatus.IN_PROGRESS);
        investigation4.setStatus(ActStatus.IN_PROGRESS);
        investigation5.setStatus(ActStatus.CANCELLED);
        investigation6.setStatus(ActStatus.POSTED);
        save(investigation1, investigation2, investigation3, investigation4, investigation5, investigation6);
        Act reminder1 = getReminder(item1, reminderType1);
        Act reminder2 = getReminder(item2, reminderType2);
        Act reminder3 = getReminder(item3, reminderType3);
        reminder1.setStatus(ReminderStatus.IN_PROGRESS);
        reminder2.setStatus(ReminderStatus.COMPLETED);
        reminder3.setStatus(ReminderStatus.CANCELLED);
        save(reminder1, reminder2, reminder3);

        Act doc1 = getDocument(item1, template1);
        Act doc2 = getDocument(item2, template2);
        Act doc3 = getDocument(item3, template3);
        doc1.setStatus(ActStatus.IN_PROGRESS);
        doc2.setStatus(ActStatus.COMPLETED);
        doc3.setStatus(ActStatus.POSTED);
        save(doc1, doc2, doc3);

        Act alert1 = getAlert(item1, alertType1);
        Act alert2 = getAlert(item2, alertType2);
        Act alert3 = getAlert(item3, alertType3);
        alert1.setStatus(ActStatus.IN_PROGRESS);
        alert2.setStatus(ActStatus.COMPLETED);
        alert3.setStatus(ActStatus.COMPLETED);
        save(alert1, alert2, alert3);

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());

        assertTrue(delete(editor));
        assertNull(get(charge));
        for (FinancialAct item : items) {
            assertNull(get(item));
        }

        // check order cancellations
        orders = editor.getPharmacyOrderService().getOrders(true);
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), Order.Type.CANCEL, patient, product1, quantity, item1.getId(),
                   item1.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), Order.Type.CANCEL, patient, product2, quantity, item2.getId(),
                   item2.getActivityStartTime(), clinician, pharmacy);

        labOrders = editor.getLaboratoryOrderService().getOrders();
        assertEquals(1, labOrders.size());
        checkOrder(labOrders.get(0), LabOrder.Type.CANCEL, patient, investigation4.getId(),
                   investigation4.getActivityStartTime(), clinician, laboratory);

        assertNull(get(investigation1));
        assertNotNull(get(investigation2));
        assertNull(get(investigation3));
        assertNotNull(get(investigation4));
        assertNotNull(get(investigation5));
        assertNotNull(get(investigation6));

        assertNull(get(reminder1));
        assertNotNull(get(reminder2));
        assertNull(get(reminder3));

        assertNull(get(doc1));
        assertNotNull(get(doc2));
        assertNotNull(get(doc3));

        assertNull(get(alert1));
        assertNotNull(get(alert2));
        assertNotNull(get(alert3));

        checkBalance(customer, ZERO, ZERO);
    }

    /**
     * Verifies that the {@link DefaultCustomerChargeActEditor#delete()} method deletes a credit and its item.
     */
    @Test
    public void testDeleteCredit() {
        checkDeleteCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Verifies that the {@link DefaultCustomerChargeActEditor#delete()} method deletes a counter sale and its item.
     */
    @Test
    public void testDeleteCounterSale() {
        checkDeleteCharge((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Verifies stock quantities update for products used in an invoice.
     */
    @Test
    public void testInvoiceStockUpdate() {
        checkChargeStockUpdate((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Verifies stock quantities update for products used in a credit.
     */
    @Test
    public void testCreditStockUpdate() {
        checkChargeStockUpdate((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Verifies stock quantities update for products used in a counter sale.
     */
    @Test
    public void testCounterSaleStockUpdate() {
        checkChargeStockUpdate((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Test template expansion for an invoice.
     */
    @Test
    public void testExpandTemplateInvoice() {
        checkExpandTemplate((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Test template expansion for a credit.
     */
    @Test
    public void testExpandTemplateCredit() {
        checkExpandTemplate((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Test template expansion for a counter sale.
     */
    @Test
    public void testExpandTemplateCounterSale() {
        checkExpandTemplate((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Verifies that an act is invalid if the sum of the item totals don't add up to the charge total.
     */
    @Test
    public void testTotalMismatch() {
        BigDecimal itemTotal = BigDecimal.valueOf(20);
        Product product1 = createProduct(ProductArchetypes.SERVICE, itemTotal);

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(itemTotal, customer, patient, product1,
                                                                           ActStatus.IN_PROGRESS);
        save(acts);
        FinancialAct charge = acts.get(0);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        editor.getComponent();
        charge.setTotal(Money.ONE);
        assertFalse(editor.isValid());

        Validator validator = new DefaultValidator();
        assertFalse(editor.validate(validator));
        List<ValidatorError> list = validator.getErrors(editor);
        assertEquals(1, list.size());
        String message = Messages.format("act.validation.totalMismatch", editor.getProperty("amount").getDisplayName(),
                                         NumberFormatter.formatCurrency(charge.getTotal()),
                                         editor.getProperty("items").getDisplayName(),
                                         NumberFormatter.formatCurrency(itemTotal));
        String expected = Messages.format(ValidatorError.MSG_KEY, message);
        assertEquals(expected, list.get(0).toString());
    }

    /**
     * Verifies a prescription can be selected during invoicing.
     */
    @Test
    public void testPrescription() {
        Product product1 = createProduct(MEDICATION, ONE);

        Act prescription = PrescriptionTestHelper.createPrescription(patient, product1, clinician);
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, ONE, queue);
        assertTrue(SaveHelper.save(editor));

        checkPrescription(prescription, itemEditor);
    }

    /**
     * Verifies that an invoice item linked to a prescription can be deleted, and that the medication is removed
     * from the prescription.
     */
    @Test
    public void testDeleteInvoiceItemLinkedToPrescription() {
        Product product1 = createProduct(MEDICATION, ONE);
        Product product2 = createProduct(MEDICATION, ONE);

        Act prescription = PrescriptionTestHelper.createPrescription(patient, product1, clinician);
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, ONE, queue);
        addItem(editor, patient, product2, ONE, queue);
        assertTrue(SaveHelper.save(editor));

        checkPrescription(prescription, itemEditor1);
        editor.removeItem(itemEditor1.getObject());
        assertTrue(SaveHelper.save(editor));

        prescription = get(prescription);
        ActBean bean = new ActBean(prescription);
        assertTrue(bean.getNodeActs("dispensing").isEmpty());
    }

    /**
     * Verifies that an unsaved invoice item can be deleted, when there is a prescription associated with the item's
     * product.
     */
    @Test
    public void testDeleteUnsavedInvoiceItemWithPrescription() {
        checkDeleteUnsavedItemWithPrescription((FinancialAct) create(CustomerAccountArchetypes.INVOICE));
    }

    /**
     * Verifies that an unsaved credit item can be deleted, when there is a prescription associated with the item's
     * product. As it is a credit item, the prescription shouldn't be used.
     */
    @Test
    public void testDeleteUnsavedCreditItem() {
        checkDeleteUnsavedItemWithPrescription((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Verifies that an unsaved counter item can be deleted, when there is a prescription associated with the item's
     * product. As it is a counter item, the prescription shouldn't be used.
     */
    @Test
    public void testDeleteUnsavedCounterItem() {
        checkDeleteUnsavedItemWithPrescription((FinancialAct) create(CustomerAccountArchetypes.COUNTER));
    }

    /**
     * Verifies that the clinician is propagated to child acts.
     */
    @Test
    public void testInitClinician() {
        Product product1 = createProduct(MEDICATION, BigDecimal.ONE);
        Entity reminderType1 = addReminder(product1);
        Entity alertType1 = addAlertType(product1);
        Entity investigationType1 = addInvestigation(product1);
        Entity template1 = addTemplate(product1);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = ONE;
        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, quantity, queue);
        FinancialAct item = (FinancialAct) itemEditor.getObject();

        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));

        Act medication = (Act) new ActBean(item).getNodeTargetObject("dispensing");
        assertNotNull(medication);

        Act reminder = getReminder(item, reminderType1);
        Act alert = getAlert(item, alertType1);
        Act investigation = getInvestigation(item, investigationType1);
        Act document = getDocument(item, template1);
        checkClinician(medication, clinician);
        checkClinician(reminder, clinician);
        checkClinician(alert, clinician);
        checkClinician(investigation, clinician);
        checkClinician(document, clinician);
    }

    /**
     * Verifies that the patient on an invoice item can be changed, and that this is reflected in the patient history.
     */
    @Test
    public void testChangePatient() {
        Product product1 = createProduct(MEDICATION, BigDecimal.ONE);
        Entity investigationType1 = addInvestigation(product1);
        Entity template1 = addTemplate(product1);

        Party patient2 = TestHelper.createPatient(customer);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = ONE;
        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, quantity, queue);
        FinancialAct item = (FinancialAct) itemEditor.getObject();

        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));

        Act event1 = records.getEvent(patient);  // get the clinical event
        Act medication = (Act) new ActBean(item).getNodeTargetObject("dispensing");
        assertNotNull(event1);
        assertNotNull(medication);

        Act investigation = getInvestigation(item, investigationType1);
        Act document = getDocument(item, template1);

        checkEventRelationships(event1, item, medication, investigation, document);

        // recreate the editor, and change the patient to patient2.
        editor = createCustomerChargeActEditor(charge, layoutContext);
        editor.getComponent();
        assertTrue(editor.isValid());

        item = (FinancialAct) editor.getItems().getActs().get(0);
        itemEditor = editor.getEditor(item);
        itemEditor.getComponent();
        itemEditor.setPatient(patient2);
        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));

        event1 = get(event1);
        medication = get(medication);

        // verify that the records are now linked to event2
        Act event2 = records.getEvent(patient2);
        investigation = getInvestigation(item, investigationType1);
        document = getDocument(item, template1);

        checkEventRelationships(event2, item, medication, investigation, document);

        // event1 should no longer be linked to any acts
        assertEquals(0, event1.getSourceActRelationships().size());
    }

    /**
     * Verifies that changing a quantity on a pharmacy order sends an update.
     */
    @Test
    public void testChangePharmacyOrderQuantity() {
        Entity pharmacy = createPharmacy(location);
        Product product = createProduct(MEDICATION, TEN, pharmacy);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product, TEN, queue);
        Act item = itemEditor.getObject();
        assertTrue(SaveHelper.save(editor));

        List<Order> orders = editor.getPharmacyOrderService().getOrders(true);
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.CREATE, patient, product, TEN, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
        editor.getPharmacyOrderService().clear();

        itemEditor.setQuantity(ONE);
        assertTrue(SaveHelper.save(editor));

        orders = editor.getPharmacyOrderService().getOrders(true);
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.UPDATE, patient, product, ONE, item.getId(),
                   item.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Verifies that deleting an invoice item with a pharmacy order cancels the order.
     */
    @Test
    public void testDeleteInvoiceItemWithPharmacyOrder() {
        Entity pharmacy = createPharmacy(location);
        Product product1 = createProduct(MEDICATION, TEN, pharmacy);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, TEN, pharmacy);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        Act item1 = addItem(editor, patient, product1, TEN, queue).getObject();
        Act item2 = addItem(editor, patient, product2, ONE, queue).getObject();
        assertTrue(SaveHelper.save(editor));

        List<Order> orders = editor.getPharmacyOrderService().getOrders(true);
        assertEquals(2, orders.size());
        checkOrder(orders.get(0), Order.Type.CREATE, patient, product1, TEN, item1.getId(),
                   item1.getActivityStartTime(), clinician, pharmacy);
        checkOrder(orders.get(1), Order.Type.CREATE, patient, product2, ONE, item2.getId(),
                   item2.getActivityStartTime(), clinician, pharmacy);
        editor.getPharmacyOrderService().clear();

        editor.delete(item1);
        assertTrue(SaveHelper.save(editor));

        orders = editor.getPharmacyOrderService().getOrders(true);
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), Order.Type.CANCEL, patient, product1, TEN, item1.getId(),
                   item1.getActivityStartTime(), clinician, pharmacy);
    }

    /**
     * Verifies that deleting an invoice item with a laboratory order cancels the order.
     */
    @Test
    public void testDeleteInvoiceItemWithLaboratoryOrder() {
        Entity laboratory = createLaboratory(location);
        Product product1 = createProduct(SERVICE, TEN);
        Entity investigationType1 = addInvestigation(product1, laboratory);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, TEN);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        Act item1 = addItem(editor, patient, product1, TEN, queue).getObject();
        addItem(editor, patient, product2, ONE, queue).getObject();
        assertTrue(SaveHelper.save(editor));

        Act investigation1 = getInvestigation(item1, investigationType1);

        List<LabOrder> orders = editor.getLaboratoryOrderService().getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), LabOrder.Type.CREATE, patient, investigation1.getId(),
                   investigation1.getActivityStartTime(), clinician, laboratory);
        editor.getLaboratoryOrderService().clear();

        editor.delete(item1);
        assertTrue(SaveHelper.save(editor));

        orders = editor.getLaboratoryOrderService().getOrders();
        assertEquals(1, orders.size());
        checkOrder(orders.get(0), LabOrder.Type.CANCEL, patient, investigation1.getId(),
                   investigation1.getActivityStartTime(), clinician, laboratory);
    }

    /**
     * Tests expansion for invoices.
     */
    @Test
    public void testTemplateExpansionForInvoice() {
        checkTemplateExpansion(CustomerAccountArchetypes.INVOICE, 1);
    }

    /**
     * Tests expansion for credits.
     */
    @Test
    public void testTemplateExpansionForCredit() {
        checkTemplateExpansion(CustomerAccountArchetypes.CREDIT, 0);
    }

    /**
     * Tests expansion for counter sales.
     */
    @Test
    public void testTemplateExpansionForCounter() {
        checkTemplateExpansion(CustomerAccountArchetypes.COUNTER, 0);
    }

    /**
     * Verifies that if a template is expanded, and a product is subsequently replaced with one not from a template,
     * the template reference is removed.
     */
    @Test
    public void testChangeTemplateProduct() {
        BigDecimal fixedPrice = ONE;

        Product template = ProductTestHelper.createTemplate("templateA");
        Product product1 = createProduct(MEDICATION, fixedPrice);
        Product product2 = createProduct(MEDICATION, fixedPrice);

        ProductTestHelper.addInclude(template, product1, 1, false);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, template, null, queue);
        assertEquals(product1, itemEditor.getProduct());
        assertEquals(template, itemEditor.getTemplate());

        itemEditor.setProduct(product2);
        assertNull(itemEditor.getTemplate());

        assertTrue(SaveHelper.save(editor));

        itemEditor.setProduct(template);
        assertEquals(product1, itemEditor.getProduct());
        assertEquals(template, itemEditor.getTemplate());
    }

    /**
     * Verifies that existing reminders are completed if a product is used with the same reminder type.
     */
    @Test
    public void testMarkMatchingRemindersCompleted() {
        Product product1 = createProduct(MEDICATION);
        Entity reminderType = addReminder(product1);

        Act existing = ReminderTestHelper.createReminder(patient, reminderType);
        assertEquals(ActStatus.IN_PROGRESS, existing.getStatus());
        save(existing);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, BigDecimal.ONE, queue);
        assertTrue(SaveHelper.save(editor));

        Act reminder = getReminder(itemEditor.getObject(), reminderType);
        existing = get(existing);
        reminder = get(reminder);

        assertEquals(ActStatus.COMPLETED, existing.getStatus());
        assertEquals(ActStatus.IN_PROGRESS, reminder.getStatus());
    }

    /**
     * Verifies that when products are changed from one with a reminder to one without, the reminder is deleted.
     */
    @Test
    public void testChangeProductWithReminderToOneWithout() {
        // create a product with a reminder
        Product product1 = createProduct(MEDICATION);
        Entity reminderType1 = addReminder(product1);

        // and one without
        Product product2 = createProduct(MEDICATION);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, BigDecimal.ONE, queue);
        assertTrue(SaveHelper.save(editor));

        Act reminder = getReminder(itemEditor.getObject(), reminderType1);
        assertEquals(ActStatus.IN_PROGRESS, reminder.getStatus());

        itemEditor.setProduct(product2);
        assertTrue(SaveHelper.save(editor));

        assertNull(get(reminder));
    }

    /**
     * Verifies that the product can be changed multiple times with reminders.
     */
    @Test
    public void testChangeProductWithReminders() {
        // create a product with a reminder
        Product product1 = createProduct(MEDICATION);
        Entity reminderType1 = addReminder(product1);

        // and one without
        Product product2 = createProduct(MEDICATION);

        // and a 3rd with the same reminder type
        Product product3 = createProduct(MEDICATION);
        addReminder(product3, reminderType1);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, BigDecimal.ONE, queue);
        assertTrue(SaveHelper.save(editor));

        Act reminder1 = getReminder(itemEditor.getObject(), reminderType1);
        assertEquals(ActStatus.IN_PROGRESS, reminder1.getStatus());

        // change to a product with no reminder
        itemEditor.setProduct(product2);

        // immediately change to a product with a reminder
        itemEditor.setProduct(product3);

        // make sure there is popup, as the reminder is interactive
        CustomerChargeTestHelper.checkSavePopup(queue, ReminderArchetypes.REMINDER, false);
        assertTrue(SaveHelper.save(editor));

        // make sure reminder1 has been deleted, and reminder2 is IN_PROGRESS
        assertNull(get(reminder1));
        Act reminder2 = getReminder(itemEditor.getObject(), reminderType1);
        assertEquals(ActStatus.IN_PROGRESS, reminder2.getStatus());
    }

    /**
     * Verifies that existing alerts are completed if a product is used with the same alert type.
     */
    @Test
    public void testMarkMatchingAlertsCompleted() {
        Product product1 = createProduct(MEDICATION);
        Entity alertType = addAlertType(product1);

        Act existing = ReminderTestHelper.createAlert(patient, alertType);
        assertEquals(ActStatus.IN_PROGRESS, existing.getStatus());

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, BigDecimal.ONE, queue);
        assertTrue(SaveHelper.save(editor));

        Act alert = getAlert(itemEditor.getObject(), alertType);
        existing = get(existing);
        alert = get(alert);

        assertEquals(ActStatus.COMPLETED, existing.getStatus());
        assertEquals(ActStatus.IN_PROGRESS, alert.getStatus());
    }

    /**
     * Verifies that when products are changed from one with an alert to one without, the alert is deleted.
     */
    @Test
    public void testChangeProductWithAlertToOneWithout() {
        // create a product with a reminder
        Product product1 = createProduct(MEDICATION);
        Entity alertType = addAlertType(product1);

        // and one without
        Product product2 = createProduct(MEDICATION);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, BigDecimal.ONE, queue);
        assertTrue(SaveHelper.save(editor));

        Act alert = getAlert(itemEditor.getObject(), alertType);
        assertEquals(ActStatus.IN_PROGRESS, alert.getStatus());

        itemEditor.setProduct(product2);
        assertTrue(SaveHelper.save(editor));

        assertNull(get(alert));
    }

    /**
     * Verifies that the product can be changed multiple times with alerts.
     */
    @Test
    public void testChangeProductWithAlerts() {
        // create a product with an alert
        Product product1 = createProduct(MEDICATION);
        Entity alertType1 = addAlertType(product1);

        // and one without
        Product product2 = createProduct(MEDICATION);

        // and a 3rd with the same alert type
        Product product3 = createProduct(MEDICATION);
        addAlertType(product3, alertType1);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = addItem(editor, patient, product1, BigDecimal.ONE, queue);
        assertTrue(SaveHelper.save(editor));

        Act alert1 = getAlert(itemEditor.getObject(), alertType1);
        assertEquals(ActStatus.IN_PROGRESS, alert1.getStatus());

        // change to a product with no alert
        itemEditor.setProduct(product2);

        // immediately change to a product with an alert
        itemEditor.setProduct(product3);

        // make sure there is popup, as the alert is interactive
        CustomerChargeTestHelper.checkSavePopup(queue, PatientArchetypes.ALERT, false);
        assertTrue(SaveHelper.save(editor));

        // make sure alert1 has been deleted, and alert2 is IN_PROGRESS
        assertNull(get(alert1));
        Act alert2 = getAlert(itemEditor.getObject(), alertType1);
        assertEquals(ActStatus.IN_PROGRESS, alert2.getStatus());
    }

    /**
     * Tests the {@link DefaultCustomerChargeActEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        DefaultCustomerChargeActEditor editor = new DefaultCustomerChargeActEditor(charge, null, layoutContext);
        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof DefaultCustomerChargeActEditor);
    }

    /**
     * Verifies that when a template expands, any visit notes are added to the associated patient's history.
     */
    @Test
    public void testTemplateVisitNotes() {
        Party patient2 = TestHelper.createPatient(customer);
        BigDecimal fixedPrice = new BigDecimal("0.91");
        Product template1 = ProductTestHelper.createTemplate("template1");
        IMObjectBean template1Bean = new IMObjectBean(template1);
        template1Bean.setValue("visitNote", "template 1 notes");
        Product productA = createProduct(MEDICATION, fixedPrice);
        Product productB = createProduct(MEDICATION, fixedPrice);
        ProductTestHelper.addInclude(template1, productA, 1, false);
        ProductTestHelper.addInclude(template1, productB, 1, false);

        Product template2 = ProductTestHelper.createTemplate("template2");
        IMObjectBean template2Bean = new IMObjectBean(template2);
        template2Bean.setValue("visitNote", "template 2 notes");
        ProductTestHelper.addInclude(template2, productA, 1, false);
        ProductTestHelper.addInclude(template2, productB, 1, false);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();

        editor.getComponent();
        assertTrue(editor.isValid());

        addItem(editor, patient, template1, null, queue);
        addItem(editor, patient2, template2, null, queue);
        assertTrue(SaveHelper.save(editor));

        // verify that there are 4 items in the invoice, but only two notes, one for each template
        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(4, items.size());

        ActBean item1 = checkItem(items, patient, productA, template1, author, clinician, ONE, ONE, ZERO, ZERO, ZERO,
                                  ONE, ZERO, new BigDecimal("0.091"), ONE, null, 1);
        ActBean item2 = checkItem(items, patient2, productA, template2, author, clinician, ONE, ONE, ZERO, ZERO, ZERO,
                                  ONE, ZERO, new BigDecimal("0.091"), ONE, null, 1);
        checkChargeEventNote(item1, patient, "template 1 notes");
        checkChargeEventNote(item2, patient2, "template 2 notes");
    }

    /**
     * Verifies that if a clinician is set before template expansion, this appears on all acts produced by the template
     * expansion.
     */
    @Test
    public void testSetClinicianBeforeTemplateExpansion() {
        User clinician2 = TestHelper.createClinician();
        Product template = ProductTestHelper.createTemplate("templateA");
        Product product1 = createProduct(MEDICATION);
        Product product2 = createProduct(MEDICATION);
        Product product3 = createProduct(MEDICATION);
        ProductTestHelper.addInclude(template, product1, 1, false);
        ProductTestHelper.addInclude(template, product2, 2, false);
        ProductTestHelper.addInclude(template, product3, 3, false);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();

        editor.getComponent();
        assertTrue(editor.isValid());

        CustomerChargeActItemEditor itemEditor = editor.addItem();
        itemEditor.setClinician(clinician2);
        setItem(editor, itemEditor, patient, template, BigDecimal.ONE, queue);
        assertTrue(SaveHelper.save(editor));

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());

        checkItem(items, patient, product1, template, author, clinician2, ONE, ONE, ZERO, ZERO, ZERO, ZERO, ZERO,
                  ZERO, ZERO, null, 1);
        BigDecimal two = BigDecimal.valueOf(2);
        checkItem(items, patient, product2, template, author, clinician2, two, two, ZERO, ZERO, ZERO, ZERO,
                  ZERO, ZERO, ZERO, null, 1);

        BigDecimal three = BigDecimal.valueOf(3);
        checkItem(items, patient, product3, template, author, clinician2, three, three, ZERO, ZERO, ZERO, ZERO,
                  ZERO, ZERO, ZERO, null, 1);
    }

    /**
     * Tests template expansion.
     *
     * @param shortName the charge short name
     * @param childActs the expected no. of child acts
     */
    private void checkTemplateExpansion(String shortName, int childActs) {
        BigDecimal fixedPrice = new BigDecimal("0.91");
        Entity discount = DiscountTestHelper.createDiscount(TEN, true, DiscountRules.PERCENTAGE);

        Product template = ProductTestHelper.createTemplate("templateA");
        Product product1 = createProduct(MEDICATION, fixedPrice);
        Product product2 = createProduct(MEDICATION, fixedPrice);
        Product product3 = createProduct(MEDICATION, fixedPrice);
        addDiscount(product3, discount);
        addDiscount(customer, discount);                           // give customer a discount for product3
        ProductTestHelper.addInclude(template, product1, 1, false);
        ProductTestHelper.addInclude(template, product2, 2, false);
        ProductTestHelper.addInclude(template, product3, 3, true); // zero price

        FinancialAct charge = (FinancialAct) create(shortName);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();

        editor.getComponent();
        assertTrue(editor.isValid());

        addItem(editor, patient, template, null, queue);
        assertTrue(SaveHelper.save(editor));

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());

        checkItem(items, patient, product1, template, author, clinician, ONE, ONE, ZERO, ZERO, ZERO, ONE, ZERO,
                  new BigDecimal("0.091"), ONE, null, childActs);
        BigDecimal two = BigDecimal.valueOf(2);
        checkItem(items, patient, product2, template, author, clinician, two, two, ZERO, ZERO, ZERO, ONE,
                  ZERO, new BigDecimal("0.091"), ONE, null, childActs);

        // verify that product3 is charged at zero price
        BigDecimal three = BigDecimal.valueOf(3);
        checkItem(items, patient, product3, template, author, clinician, three, three, ZERO, ZERO, ZERO, ZERO,
                  ZERO, ZERO, ZERO, null, childActs);
    }

    /**
     * Verifies that an unsaved charge item can be deleted, when there is a prescription associated with the item's
     * product.
     */
    private void checkDeleteUnsavedItemWithPrescription(FinancialAct charge) {
        Product product1 = createProduct(MEDICATION, ONE);
        Product product2 = createProduct(MEDICATION, ONE);

        Act prescription = PrescriptionTestHelper.createPrescription(patient, product1, clinician);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        // add items for product1 and product2, but delete product1 item before save
        CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, ONE, queue);
        addItem(editor, patient, product2, ONE, queue);
        editor.removeItem(itemEditor1.getObject());
        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));

        // verify there are no acts linked to the prescription
        prescription = get(prescription);
        ActBean bean = new ActBean(prescription);
        assertTrue(bean.getNodeActs("dispensing").isEmpty());

        // reload the charge and verify the editor is valid
        charge = get(charge);
        editor = createCustomerChargeActEditor(charge, layoutContext);
        editor.getComponent();
        assertTrue(editor.isValid());
    }

    /**
     * Verifies that the {@link DefaultCustomerChargeActEditor#delete()} method deletes a charge and its items.
     *
     * @param charge the charge
     */
    private void checkDeleteCharge(FinancialAct charge) {
        BigDecimal fixedPrice = BigDecimal.TEN;
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        Product product1 = createProduct(MEDICATION, fixedPrice);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice);
        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = ONE;
        addItem(editor, patient, product1, quantity, queue);
        addItem(editor, patient, product2, quantity, queue);
        addItem(editor, patient, product3, quantity, queue);

        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, ZERO);

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());

        assertTrue(delete(editor));
        assertNull(get(charge));
        for (FinancialAct item : items) {
            assertNull(get(item));
        }

        checkBalance(customer, ZERO, ZERO);
    }

    /**
     * Tests editing a charge with no items.
     *
     * @param charge the charge
     */
    private void checkEmptyCharge(FinancialAct charge) {
        DefaultCustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        editor.getComponent();
        assertTrue(editor.isValid());
        editor.save();
        checkBalance(customer, ZERO, ZERO);

        editor.setStatus(ActStatus.POSTED);
        editor.save();
        checkBalance(customer, ZERO, ZERO);
    }

    /**
     * Tests editing of a charge.
     *
     * @param charge the charge
     */
    private void checkEditCharge(FinancialAct charge) {
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = ProductTestHelper.createStockLocation(location);
        layoutContext.getContext().setLocation(location);
        layoutContext.getContext().setStockLocation(stockLocation);

        BigDecimal fixedPrice = BigDecimal.TEN;
        BigDecimal fixedPriceIncTax = BigDecimal.valueOf(11);
        BigDecimal itemTax = BigDecimal.valueOf(1);
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal tax = itemTax.multiply(BigDecimal.valueOf(3));
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));
        Entity pharmacy = CustomerChargeTestHelper.createPharmacy(location);
        Entity laboratory = CustomerChargeTestHelper.createLaboratory(location);

        boolean invoice = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE);

        Product product1 = createProduct(MEDICATION, fixedPrice, pharmacy);
        addReminder(product1);
        addAlertType(product1);
        addInvestigation(product1);
        addTemplate(product1);
        int product1Acts = invoice ? 5 : 0;

        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice, pharmacy);
        addReminder(product2);
        addAlertType(product2);
        addInvestigation(product2);
        addTemplate(product2);
        int product2Acts = invoice ? 4 : 0;

        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);
        addReminder(product3);
        addAlertType(product3);
        Entity investigationType3 = addInvestigation(product3, laboratory);
        addTemplate(product3);
        int product3Acts = invoice ? 4 : 0;

        BigDecimal product1Stock = BigDecimal.valueOf(100);
        BigDecimal product2Stock = BigDecimal.valueOf(50);
        initStock(product1, stockLocation, product1Stock);
        initStock(product2, stockLocation, product2Stock);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        EditorQueue queue = editor.getQueue();
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = ONE;
        Act item1 = addItem(editor, patient, product1, quantity, queue).getObject();
        Act item2 = addItem(editor, patient, product2, quantity, queue).getObject();
        Act item3 = addItem(editor, patient, product3, quantity, queue).getObject();

        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, ZERO);
        editor.setStatus(ActStatus.POSTED);
        editor.save();
        checkBalance(customer, ZERO, balance);

        if (invoice) {
            List<Order> orders = editor.getPharmacyOrderService().getOrders(true);
            assertEquals(4, orders.size());
            checkOrder(orders.get(0), Order.Type.CREATE, patient, product1, quantity, item1.getId(),
                       item1.getActivityStartTime(), clinician, pharmacy);
            checkOrder(orders.get(1), Order.Type.DISCONTINUE, patient, product1, quantity, item1.getId(),
                       item1.getActivityStartTime(), clinician, pharmacy);
            checkOrder(orders.get(2), Order.Type.CREATE, patient, product2, quantity, item2.getId(),
                       item2.getActivityStartTime(), clinician, pharmacy);
            checkOrder(orders.get(3), Order.Type.DISCONTINUE, patient, product2, quantity, item2.getId(),
                       item2.getActivityStartTime(), clinician, pharmacy);
            editor.getPharmacyOrderService().clear();

            List<LabOrder> labOrders = editor.getLaboratoryOrderService().getOrders();
            assertEquals(1, labOrders.size());

            Act investigation = getInvestigation(item3, investigationType3);
            checkOrder(labOrders.get(0), LabOrder.Type.CREATE, patient, investigation.getId(),
                       investigation.getActivityStartTime(), clinician, laboratory);
            editor.getLaboratoryOrderService().clear();
        } else {
            assertNull(editor.getPharmacyOrderService());
            assertNull(editor.getLaboratoryOrderService());
        }

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());
        checkCharge(charge, customer, author, clinician, tax, total);

        Act event = records.getEvent(patient);  // get the clinical event. Should be null if not an invoice
        if (invoice) {
            assertNotNull(event);
            checkEvent(event, patient, author, clinician, location, ActStatus.COMPLETED);
        } else {
            assertNull(event);
        }

        BigDecimal discount = ZERO;
        checkItem(items, patient, product1, null, author, clinician, ZERO, quantity, ZERO, ZERO,
                  ZERO, fixedPriceIncTax, discount, itemTax, itemTotal, event, product1Acts);
        checkItem(items, patient, product2, null, author, clinician, ZERO, quantity, ZERO, ZERO,
                  ZERO, fixedPriceIncTax, discount, itemTax, itemTotal, event, product2Acts);
        checkItem(items, patient, product3, null, author, clinician, ZERO, quantity, ZERO, ZERO,
                  ZERO, fixedPriceIncTax, discount, itemTax, itemTotal, event, product3Acts);

        boolean add = bean.isA(CustomerAccountArchetypes.CREDIT);
        checkStock(product1, stockLocation, product1Stock, quantity, add);
        checkStock(product2, stockLocation, product2Stock, quantity, add);
        checkStock(product3, stockLocation, ZERO, ZERO, add);
    }

    /**
     * Verifies stock quantities update for products used in a charge.
     *
     * @param charge the charge
     */
    private void checkChargeStockUpdate(FinancialAct charge) {
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = ProductTestHelper.createStockLocation(location);
        layoutContext.getContext().setLocation(location);
        layoutContext.getContext().setStockLocation(stockLocation);

        Product product1 = createProduct(MEDICATION);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE);
        Product product3 = createProduct(ProductArchetypes.SERVICE);
        Product product4 = createProduct(ProductArchetypes.MERCHANDISE);

        BigDecimal product1InitialStock = BigDecimal.valueOf(100);
        BigDecimal product2InitialStock = BigDecimal.valueOf(50);
        BigDecimal product4InitialStock = BigDecimal.valueOf(25);
        initStock(product1, stockLocation, product1InitialStock);
        initStock(product2, stockLocation, product2InitialStock);
        initStock(product4, stockLocation, product4InitialStock);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity1 = BigDecimal.valueOf(5);
        BigDecimal quantity2 = TEN;
        BigDecimal quantity4a = BigDecimal.ONE;
        BigDecimal quantity4b = TEN;
        BigDecimal quantity4 = quantity4a.add(quantity4b);

        CustomerChargeActItemEditor item1 = addItem(editor, patient, product1, quantity1, editor.getQueue());
        CustomerChargeActItemEditor item2 = addItem(editor, patient, product2, quantity2, editor.getQueue());
        addItem(editor, patient, product4, quantity4a, editor.getQueue());
        addItem(editor, patient, product4, quantity4b, editor.getQueue());
        assertTrue(SaveHelper.save(editor));

        boolean add = TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT);
        BigDecimal product1Stock = checkStock(product1, stockLocation, product1InitialStock, quantity1, add);
        BigDecimal product2Stock = checkStock(product2, stockLocation, product2InitialStock, quantity2, add);
        checkStock(product4, stockLocation, product4InitialStock, quantity4, add);

        item1.setQuantity(ZERO);
        item1.setQuantity(quantity1);
        assertTrue(item1.isModified());
        item2.setQuantity(ZERO);
        item2.setQuantity(quantity2);
        assertTrue(item2.isModified());
        assertTrue(SaveHelper.save(editor));
        checkStock(product1, stockLocation, product1Stock);
        checkStock(product2, stockLocation, product2Stock);

        item1.setQuantity(BigDecimal.valueOf(10)); // change product1 stock quantity
        item2.setProduct(product3);                // change the product and verify the stock for product2 reverts
        assertTrue(SaveHelper.save(editor));
        checkStock(product1, stockLocation, product1Stock, BigDecimal.valueOf(5), add);
        checkStock(product2, stockLocation, product2Stock, quantity2, !add);

        // now delete the charge and verify the stock reverts
        assertTrue(delete(editor));
        checkStock(product1, stockLocation, product1InitialStock);
        checkStock(product2, stockLocation, product2InitialStock);
        checkStock(product4, stockLocation, product4InitialStock);
    }

    /**
     * Tests template expansion.
     *
     * @param charge the charge to edit
     */
    private void checkExpandTemplate(FinancialAct charge) {
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = ProductTestHelper.createStockLocation(location);
        layoutContext.getContext().setLocation(location);
        layoutContext.getContext().setStockLocation(stockLocation);

        // add a patient weight
        PatientTestHelper.createWeight(patient, new Date(), new BigDecimal("4.2"), WeightUnits.KILOGRAMS);

        BigDecimal quantity = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.TEN;
        BigDecimal fixedPriceIncTax = BigDecimal.valueOf(11);
        BigDecimal discount = ZERO;
        BigDecimal itemTax = BigDecimal.valueOf(1);
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal tax = itemTax.multiply(BigDecimal.valueOf(3));
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        Product product1 = createProduct(MEDICATION, fixedPrice);
        IMObjectBean productBean = new IMObjectBean(product1);
        productBean.setValue("concentration", ONE);
        addDose(product1, createDose(null, ZERO, TEN, ONE, ONE));
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice);
        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);
        Product template = createProduct(ProductArchetypes.TEMPLATE);
        EntityBean templateBean = new EntityBean(template);
        templateBean.addNodeTarget("includes", product1);
        templateBean.addNodeTarget("includes", product2);
        templateBean.addNodeTarget("includes", product3);
        save(template);

        TestChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext);
        editor.getComponent();
        CustomerChargeTestHelper.addItem(editor, patient, template, null, editor.getQueue());

        boolean invoice = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE);
        int product1Acts = 0;     // expected child acts for product1
        if (invoice) {
            product1Acts++;
        }

        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, ZERO);

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());
        checkCharge(charge, customer, author, clinician, tax, total);
        Act event = records.getEvent(patient);  // get the clinical event. Should be null if not an invoice
        if (invoice) {
            assertNotNull(event);
            checkEvent(event, patient, author, clinician, location, ActStatus.IN_PROGRESS);
        } else {
            assertNull(event);
        }

        if (TypeHelper.isA(charge, CustomerAccountArchetypes.COUNTER)) {
            checkItem(items, patient, product1, template, author, clinician, BigDecimal.ONE, quantity, ZERO, ZERO,
                      ZERO, fixedPriceIncTax, discount, itemTax, itemTotal, event, product1Acts);
        } else {
            // quantity derived from the product dose. As there is no unit price, the totals don't change
            checkItem(items, patient, product1, template, author, clinician, BigDecimal.ONE, new BigDecimal("4.2"),
                      ZERO, ZERO, ZERO, fixedPriceIncTax, discount, itemTax, itemTotal, event, product1Acts);
        }

        checkItem(items, patient, product2, template, author, clinician, BigDecimal.ONE, quantity, ZERO, ZERO,
                  ZERO, fixedPriceIncTax, discount, itemTax, itemTotal, event, 0);
        checkItem(items, patient, product3, template, author, clinician, BigDecimal.ONE, quantity, ZERO, ZERO,
                  ZERO, fixedPriceIncTax, discount, itemTax, itemTotal, event, 0);
    }

    /**
     * Initialises stock quantities for a product at a stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param quantity      the initial stock quantity
     */
    private void initStock(Product product, Party stockLocation, BigDecimal quantity) {
        StockRules rules = new StockRules(getArchetypeService());
        rules.updateStock(product, stockLocation, quantity);
    }

    /**
     * Checks stock for a product at a stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param initial       the initial stock quantity
     * @param change        the change in stock quantity
     * @param add           if {@code true} add the change, otherwise subtract it
     * @return the new stock quantity
     */
    private BigDecimal checkStock(Product product, Party stockLocation, BigDecimal initial, BigDecimal change,
                                  boolean add) {
        BigDecimal expected = add ? initial.add(change) : initial.subtract(change);
        checkStock(product, stockLocation, expected);
        return expected;
    }

    /**
     * Checks stock for a product at a stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param expected      the expected stock quantity
     */
    private void checkStock(Product product, Party stockLocation, BigDecimal expected) {
        StockRules rules = new StockRules(getArchetypeService());
        checkEquals(expected, rules.getStock(get(product), get(stockLocation)));
    }

    /**
     * Verifies a patient clinical event matches that expected.
     *
     * @param event     the event
     * @param patient   the expected patient
     * @param author    the expected author
     * @param clinician the expected clinician
     * @param location  the expected location
     * @param status    the expected status
     */
    private void checkEvent(Act event, Party patient, User author, User clinician, Party location, String status) {
        ActBean bean = new ActBean(event);
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        assertEquals(location.getObjectReference(), bean.getNodeParticipantRef("location"));
        assertEquals(status, event.getStatus());
        if (ActStatus.COMPLETED.equals(event.getStatus())) {
            assertNotNull(event.getActivityEndTime());
        } else {
            assertNull(event.getActivityEndTime());
        }
    }

    /**
     * Verifies a prescription has a link to a charge item
     *
     * @param prescription the prescription
     * @param itemEditor   the charge item editor
     */
    private void checkPrescription(Act prescription, CustomerChargeActItemEditor itemEditor) {
        prescription = get(prescription);
        assertNotNull(prescription);
        ActBean prescriptionBean = new ActBean(prescription);
        Act item = itemEditor.getObject();
        ActBean bean = new ActBean(item);
        List<Act> dispensing = bean.getNodeActs("dispensing");
        assertEquals(1, dispensing.size());
        Act medication = dispensing.get(0);
        assertTrue(prescriptionBean.getNodeActs("dispensing").contains(medication));
    }

    /**
     * Deletes a charge.
     *
     * @param editor the editor to use
     * @return {@code true} if the delete was successful
     */
    private boolean delete(final DefaultCustomerChargeActEditor editor) {
        boolean result = false;
        try {
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            template.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    editor.delete();
                }
            });
            result = true;
        } catch (Throwable exception) {
            logger.error(exception, exception);
        }
        return result;
    }

    /**
     * Checks the balance for a customer.
     *
     * @param customer the customer
     * @param unbilled the expected unbilled amount
     * @param balance  the expected balance
     */
    private void checkBalance(Party customer, BigDecimal unbilled, BigDecimal balance) {
        CustomerAccountRules rules = ServiceHelper.getBean(CustomerAccountRules.class);
        checkEquals(unbilled, rules.getUnbilledAmount(customer));
        checkEquals(balance, rules.getBalance(customer));
    }

    /**
     * Chekcs the total of a charge matches that expected, and that the total matches the sum of the item totals.
     *
     * @param charge the charge
     * @param total  the expected total
     */
    private void checkTotal(FinancialAct charge, BigDecimal total) {
        assertTrue(charge.getTotal().compareTo(total) == 0);
        ActCalculator calculator = new ActCalculator(getArchetypeService());
        BigDecimal itemTotal = calculator.sum(charge, "total");
        assertTrue(itemTotal.compareTo(total) == 0);
    }

    /**
     * Creates a customer charge act editor.
     *
     * @param invoice the charge to edit
     * @param context the layout context
     * @return a new customer charge act editor
     */
    private TestChargeEditor createCustomerChargeActEditor(FinancialAct invoice, LayoutContext context) {
        return createCustomerChargeActEditor(invoice, context, false);
    }

    /**
     * Creates a customer charge act editor.
     *
     * @param invoice        the charge to edit
     * @param context        the layout context
     * @param addDefaultItem if {@code true} add a default item if the act has none
     * @return a new customer charge act editor
     */
    private TestChargeEditor createCustomerChargeActEditor(FinancialAct invoice, LayoutContext context,
                                                           boolean addDefaultItem) {
        return new TestChargeEditor(invoice, context, addDefaultItem);
    }

}
