package org.openvpms.web.app.customer.charge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActCalculator;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.patient.InvestigationActStatus;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderStatus;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;
import static org.openvpms.web.app.customer.charge.CustomerChargeTestHelper.checkSavePopup;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tests the {@link CustomerChargeActEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerChargeActEditorTestCase extends AbstractCustomerChargeActEditorTest {

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
        Party location = TestHelper.createLocation();

        layoutContext = new DefaultLayoutContext();
        layoutContext.getContext().setCustomer(customer);
        layoutContext.getContext().setUser(author);
        layoutContext.getContext().setClinician(clinician);
        layoutContext.getContext().setLocation(location);

        records = new MedicalRecordRules();
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

        Product product1 = createProduct(ProductArchetypes.SERVICE, itemTotal1);
        Product product2 = createProduct(ProductArchetypes.SERVICE, itemTotal2);
        Product product3 = createProduct(ProductArchetypes.SERVICE, itemTotal3);

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

        ChargePopupEditorManager mgr = new ChargePopupEditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = BigDecimal.ONE;
        addItem(editor, patient, product1, quantity, mgr);
        addItem(editor, patient, product2, quantity, mgr);
        addItem(editor, patient, product3, quantity, mgr);
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

        Product product1 = createProduct(ProductArchetypes.SERVICE, itemTotal1);
        Product product2 = createProduct(ProductArchetypes.SERVICE, itemTotal2);
        Product product3 = createProduct(ProductArchetypes.SERVICE, itemTotal3);

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            ChargePopupEditorManager mgr = new ChargePopupEditorManager();
            ChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
            editor.getComponent();
            assertTrue(editor.isValid());

            BigDecimal quantity = BigDecimal.ONE;
            CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, quantity, mgr);
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, mgr);
            CustomerChargeActItemEditor itemEditor3 = addItem(editor, patient, product3, quantity, mgr);
            assertTrue(SaveHelper.save(editor));

            charge = get(charge);
            assertTrue(charge.getTotal().compareTo(total) == 0);
            ActCalculator calculator = new ActCalculator(getArchetypeService());
            BigDecimal itemTotal = calculator.sum(charge, "total");
            assertTrue(itemTotal.compareTo(total) == 0);

            if (j == 0) {
                editor.delete((Act) itemEditor1.getObject());
                total = total.subtract(itemTotal1);
            } else if (j == 1) {
                editor.delete((Act) itemEditor2.getObject());
                total = total.subtract(itemTotal2);
            } else if (j == 2) {
                editor.delete((Act) itemEditor3.getObject());
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

        Product product1 = createProduct(ProductArchetypes.SERVICE, itemTotal1);
        Product product2 = createProduct(ProductArchetypes.SERVICE, itemTotal2);
        Product product3 = createProduct(ProductArchetypes.SERVICE, itemTotal3);

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            ChargePopupEditorManager mgr = new ChargePopupEditorManager();
            ChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
            editor.getComponent();
            assertTrue(editor.isValid());

            BigDecimal quantity = BigDecimal.ONE;
            CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, quantity, mgr);
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, mgr);
            CustomerChargeActItemEditor itemEditor3 = addItem(editor, patient, product3, quantity, mgr);
            assertTrue(SaveHelper.save(editor));

            charge = get(charge);
            checkTotal(charge, total);

            editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
            editor.getComponent();

            if (j == 0) {
                editor.delete((Act) itemEditor1.getObject());
                total = total.subtract(itemTotal1);
            } else if (j == 1) {
                editor.delete((Act) itemEditor2.getObject());
                total = total.subtract(itemTotal2);
            } else if (j == 2) {
                editor.delete((Act) itemEditor3.getObject());
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

        Product product1 = createProduct(ProductArchetypes.SERVICE, itemTotal1);
        Product product2 = createProduct(ProductArchetypes.SERVICE, itemTotal2);
        Product product3 = createProduct(ProductArchetypes.SERVICE, itemTotal3);

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            ChargePopupEditorManager mgr = new ChargePopupEditorManager();
            ChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
            editor.getComponent();

            BigDecimal quantity = BigDecimal.ONE;
            CustomerChargeActItemEditor itemEditor1 = addItem(editor, patient, product1, quantity, mgr);
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, mgr);
            CustomerChargeActItemEditor itemEditor3 = addItem(editor, patient, product3, quantity, mgr);

            if (j == 0) {
                editor.delete((Act) itemEditor1.getObject());
                total = total.subtract(itemTotal1);
            } else if (j == 1) {
                editor.delete((Act) itemEditor2.getObject());
                total = total.subtract(itemTotal2);
            } else if (j == 2) {
                editor.delete((Act) itemEditor3.getObject());
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

        Product product1 = createProduct(ProductArchetypes.SERVICE, itemTotal1);
        Product product2 = createProduct(ProductArchetypes.SERVICE, itemTotal2);
        Product product3 = createProduct(ProductArchetypes.SERVICE, itemTotal3);

        for (int i = 0, j = 0; i < 3; ++i) {
            FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
            BigDecimal total = itemTotal1.add(itemTotal2).add(itemTotal3);

            ChargePopupEditorManager mgr = new ChargePopupEditorManager();
            boolean addDefaultItem = (j == 0);
            ChargeEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr, addDefaultItem);
            editor.getComponent();

            BigDecimal quantity = BigDecimal.ONE;
            CustomerChargeActItemEditor itemEditor1;
            if (j == 0) {
                itemEditor1 = editor.getCurrentEditor();
                setItem(editor, itemEditor1, patient, product1, quantity, mgr);
            } else {
                itemEditor1 = addItem(editor, patient, product1, quantity, mgr);
            }
            CustomerChargeActItemEditor itemEditor2 = addItem(editor, patient, product2, quantity, mgr);

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
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes an invoice and its item.
     */
    @Test
    public void testDeleteInvoice() {
        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);

        BigDecimal fixedPrice = BigDecimal.valueOf(11);
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        Product product1 = createProduct(ProductArchetypes.MEDICATION, fixedPrice);
        Entity reminderType1 = addReminder(product1);
        Entity investigationType1 = addInvestigation(product1);
        Entity template1 = addTemplate(product1);

        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice);
        Entity reminderType2 = addReminder(product2);
        Entity investigationType2 = addInvestigation(product2);
        Entity investigationType3 = addInvestigation(product2);
        Entity template2 = addTemplate(product2);

        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);
        Entity reminderType3 = addReminder(product3);
        Entity investigationType4 = addInvestigation(product3);
        Entity investigationType5 = addInvestigation(product3);
        Entity investigationType6 = addInvestigation(product3);

        Entity template3 = addTemplate(product3);

        ChargePopupEditorManager mgr = new ChargePopupEditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = BigDecimal.ONE;
        CustomerChargeActItemEditor item1Editor = addItem(editor, patient, product1, quantity, mgr);
        CustomerChargeActItemEditor item2Editor = addItem(editor, patient, product2, quantity, mgr);
        CustomerChargeActItemEditor item3Editor = addItem(editor, patient, product3, quantity, mgr);
        FinancialAct item1 = (FinancialAct) item1Editor.getObject();
        FinancialAct item2 = (FinancialAct) item2Editor.getObject();
        FinancialAct item3 = (FinancialAct) item3Editor.getObject();

        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, BigDecimal.ZERO);

        Act investigation1 = getInvestigation(item1, investigationType1);
        Act investigation2 = getInvestigation(item2, investigationType2);
        Act investigation3 = getInvestigation(item2, investigationType3);
        Act investigation4 = getInvestigation(item3, investigationType4);
        Act investigation5 = getInvestigation(item3, investigationType5);
        Act investigation6 = getInvestigation(item3, investigationType6);

        investigation1.setStatus(InvestigationActStatus.IN_PROGRESS);
        investigation2.setStatus(InvestigationActStatus.COMPLETED);
        investigation3.setStatus(InvestigationActStatus.CANCELLED);
        investigation4.setStatus(InvestigationActStatus.PRELIMINARY);
        investigation5.setStatus(InvestigationActStatus.FINAL);
        investigation6.setStatus(InvestigationActStatus.RECEIVED);
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

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());

        assertTrue(delete(editor));
        assertNull(get(charge));
        for (FinancialAct item : items) {
            assertNull(get(item));
        }
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

        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes a credit and its item.
     */
    @Test
    public void testDeleteCredit() {
        checkDeleteCharge((FinancialAct) create(CustomerAccountArchetypes.CREDIT));
    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes a counter sale and its item.
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

        FinancialAct charge = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        ChargePopupEditorManager mgr = new ChargePopupEditorManager();

        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = BigDecimal.ONE;
        addItem(editor, patient, product1, quantity, mgr);
        assertTrue(editor.isValid());
        charge.setTotal(Money.ONE);
        Validator validator = new Validator();
        assertFalse(editor.validate(validator));
        List<ValidatorError> list = validator.getErrors(editor);
        assertEquals(1, list.size());
        String message = Messages.get("act.validation.totalMismatch", editor.getProperty("amount").getDisplayName(),
                                      charge.getTotal(), editor.getProperty("items").getDisplayName(), itemTotal);
        String expected = Messages.get(ValidatorError.MSG_KEY, message);
        assertEquals(expected, list.get(0).toString());

    }

    /**
     * Verifies that the {@link CustomerChargeActEditor#delete()} method deletes a charge and its items.
     *
     * @param charge the charge
     */
    private void checkDeleteCharge(FinancialAct charge) {
        BigDecimal fixedPrice = BigDecimal.valueOf(11);
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        Product product1 = createProduct(ProductArchetypes.MEDICATION, fixedPrice);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice);
        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);

        ChargePopupEditorManager mgr = new ChargePopupEditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = BigDecimal.ONE;
        addItem(editor, patient, product1, quantity, mgr);
        addItem(editor, patient, product2, quantity, mgr);
        addItem(editor, patient, product3, quantity, mgr);

        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, BigDecimal.ZERO);

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());

        assertTrue(delete(editor));
        assertNull(get(charge));
        for (FinancialAct item : items) {
            assertNull(get(item));
        }

        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Tests editing a charge with no items.
     *
     * @param charge the charge
     */
    private void checkEmptyCharge(FinancialAct charge) {
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, new ChargePopupEditorManager());
        editor.getComponent();
        assertTrue(editor.isValid());
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);

        editor.setStatus(ActStatus.POSTED);
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Tests editing of a charge.
     *
     * @param charge the charge
     */
    private void checkEditCharge(FinancialAct charge) {
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = createStockLocation(location);
        layoutContext.getContext().setLocation(location);
        layoutContext.getContext().setStockLocation(stockLocation);

        BigDecimal fixedPrice = BigDecimal.valueOf(11);
        BigDecimal itemTax = BigDecimal.valueOf(1);
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal tax = itemTax.multiply(BigDecimal.valueOf(3));
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        boolean invoice = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE);

        Product product1 = createProduct(ProductArchetypes.MEDICATION, fixedPrice);
        addReminder(product1);
        addInvestigation(product1);
        addTemplate(product1);
        int product1Acts = invoice ? 4 : 0;

        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice);
        addReminder(product2);
        addInvestigation(product2);
        addTemplate(product2);
        int product2Acts = invoice ? 3 : 0;

        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);
        addReminder(product3);
        addInvestigation(product3);
        addTemplate(product3);
        int product3Acts = invoice ? 3 : 0;

        BigDecimal product1Stock = BigDecimal.valueOf(100);
        BigDecimal product2Stock = BigDecimal.valueOf(50);
        initStock(product1, stockLocation, product1Stock);
        initStock(product2, stockLocation, product2Stock);

        ChargePopupEditorManager mgr = new ChargePopupEditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity = BigDecimal.ONE;
        addItem(editor, patient, product1, quantity, mgr);
        addItem(editor, patient, product2, quantity, mgr);
        addItem(editor, patient, product3, quantity, mgr);

        assertTrue(editor.isValid());
        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, BigDecimal.ZERO);
        editor.setStatus(ActStatus.POSTED);
        assertTrue(editor.save());
        checkBalance(customer, BigDecimal.ZERO, balance);

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());
        checkCharge(charge, customer, author, clinician, tax, total);

        Act event = records.getEvent(patient);  // get the clinical event. Should be null if not an invoice
        if (invoice) {
            assertNotNull(event);
            checkEvent(event, patient, author, clinician, location);
        } else {
            assertNull(event);
        }

        BigDecimal discount = BigDecimal.ZERO;
        checkItem(items, patient, product1, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, event, product1Acts);
        checkItem(items, patient, product2, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, event, product2Acts);
        checkItem(items, patient, product3, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, event, product3Acts);

        boolean add = bean.isA(CustomerAccountArchetypes.CREDIT);
        checkStock(product1, stockLocation, product1Stock, quantity, add);
        checkStock(product2, stockLocation, product2Stock, quantity, add);
        checkStock(product3, stockLocation, BigDecimal.ZERO, BigDecimal.ZERO, add);
    }

    /**
     * Verifies stock quantities update for products used in a charge.
     *
     * @param charge the charge
     */
    private void checkChargeStockUpdate(FinancialAct charge) {
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = createStockLocation(location);
        layoutContext.getContext().setLocation(location);
        layoutContext.getContext().setStockLocation(stockLocation);

        Product product1 = createProduct(ProductArchetypes.MEDICATION);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE);
        Product product3 = createProduct(ProductArchetypes.SERVICE);

        BigDecimal product1InitialStock = BigDecimal.valueOf(100);
        BigDecimal product2InitialStock = BigDecimal.valueOf(50);
        initStock(product1, stockLocation, product1InitialStock);
        initStock(product2, stockLocation, product2InitialStock);

        ChargePopupEditorManager mgr = new ChargePopupEditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
        editor.getComponent();
        assertTrue(editor.isValid());

        BigDecimal quantity1 = BigDecimal.valueOf(5);
        BigDecimal quantity2 = BigDecimal.valueOf(10);

        CustomerChargeActItemEditor item1 = addItem(editor, patient, product1, quantity1, mgr);
        CustomerChargeActItemEditor item2 = addItem(editor, patient, product2, quantity2, mgr);
        assertTrue(SaveHelper.save(editor));

        boolean add = TypeHelper.isA(charge, CustomerAccountArchetypes.CREDIT);
        BigDecimal product1Stock = checkStock(product1, stockLocation, product1InitialStock, quantity1, add);
        BigDecimal product2Stock = checkStock(product2, stockLocation, product2InitialStock, quantity2, add);

        item1.setQuantity(BigDecimal.ZERO);
        item1.setQuantity(quantity1);
        assertTrue(item1.isModified());
        item2.setQuantity(BigDecimal.ZERO);
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
    }

    /**
     * Tests template expansion.
     *
     * @param charge the charge to edit
     */
    private void checkExpandTemplate(FinancialAct charge) {
        Party location = TestHelper.createLocation(true);   // enable stock control
        Party stockLocation = createStockLocation(location);
        layoutContext.getContext().setLocation(location);
        layoutContext.getContext().setStockLocation(stockLocation);

        BigDecimal quantity = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(11);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal itemTax = BigDecimal.valueOf(1);
        BigDecimal itemTotal = BigDecimal.valueOf(11);
        BigDecimal tax = itemTax.multiply(BigDecimal.valueOf(3));
        BigDecimal total = itemTotal.multiply(BigDecimal.valueOf(3));

        Product product1 = createProduct(ProductArchetypes.MEDICATION, fixedPrice);
        Product product2 = createProduct(ProductArchetypes.MERCHANDISE, fixedPrice);
        Product product3 = createProduct(ProductArchetypes.SERVICE, fixedPrice);
        Product template = createProduct(ProductArchetypes.TEMPLATE);
        EntityBean templateBean = new EntityBean(template);
        templateBean.addNodeRelationship("includes", product1);
        templateBean.addNodeRelationship("includes", product2);
        templateBean.addNodeRelationship("includes", product3);
        save(template);

        ChargePopupEditorManager mgr = new ChargePopupEditorManager();
        CustomerChargeActEditor editor = createCustomerChargeActEditor(charge, layoutContext, mgr);
        editor.getComponent();
        CustomerChargeTestHelper.addItem(editor, patient, template, BigDecimal.ONE, mgr);

        // need to add a new item to force template to expand. As it is not populated, it won't be saved
        editor.addItem();

        boolean invoice = TypeHelper.isA(charge, CustomerAccountArchetypes.INVOICE);
        int product1Acts = 0;     // expected child acts for product1
        if (invoice) {
            // close medication popup
            checkSavePopup(mgr, PatientArchetypes.PATIENT_MEDICATION);
            product1Acts++;
        }

        assertTrue(SaveHelper.save(editor));
        BigDecimal balance = (charge.isCredit()) ? total.negate() : total;
        checkBalance(customer, balance, BigDecimal.ZERO);

        charge = get(charge);
        ActBean bean = new ActBean(charge);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(3, items.size());
        checkCharge(charge, customer, author, clinician, tax, total);
        Act event = records.getEvent(patient);  // get the clinical event. Should be null if not an invoice
        if (invoice) {
            assertNotNull(event);
            checkEvent(event, patient, author, clinician, location);
        } else {
            assertNull(event);
        }

        checkItem(items, patient, product1, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, event, product1Acts);
        checkItem(items, patient, product2, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, event, 0);
        checkItem(items, patient, product3, author, clinician, quantity, BigDecimal.ZERO, BigDecimal.ZERO,
                  BigDecimal.ZERO, fixedPrice, discount, itemTax, itemTotal, event, 0);
    }

    /**
     * Initialises stock quantities for a product at a stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param quantity      the initial stock quantity
     */
    private void initStock(Product product, Party stockLocation, BigDecimal quantity) {
        StockRules rules = new StockRules();
        rules.updateStock(product, stockLocation, quantity);
    }

    /**
     * Checks stock for a product at a stock location.
     *
     * @param product       the product
     * @param stockLocation the stock location
     * @param initial       the initial stock quantity
     * @param change        the change in stock quantity
     * @param add           if <tt>true</tt> add the change, otherwise subtract it
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
        StockRules rules = new StockRules();
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
     */
    private void checkEvent(Act event, Party patient, User author, User clinician, Party location) {
        ActBean bean = new ActBean(event);
        assertEquals(patient.getObjectReference(), bean.getNodeParticipantRef("patient"));
        assertEquals(author.getObjectReference(), bean.getNodeParticipantRef("author"));
        assertEquals(clinician.getObjectReference(), bean.getNodeParticipantRef("clinician"));
        assertEquals(location.getObjectReference(), bean.getNodeParticipantRef("location"));
    }

    /**
     * Deletes a charge.
     *
     * @param editor the editor to use
     * @return <tt>true</tt> if the delete was successful
     */
    private boolean delete(final CustomerChargeActEditor editor) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        return template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return editor.delete();
            }
        });
    }

    /**
     * Checks the balance for a customer.
     *
     * @param customer the customer
     * @param unbilled the expected unbilled amount
     * @param balance  the expected balance
     */
    private void checkBalance(Party customer, BigDecimal unbilled, BigDecimal balance) {
        CustomerAccountRules rules = new CustomerAccountRules();
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
     * @param manager the popup editor manager
     * @return a new customer charge act editor
     */
    private ChargeEditor createCustomerChargeActEditor(final FinancialAct invoice, final LayoutContext context,
                                                       final PopupEditorManager manager) {
        return createCustomerChargeActEditor(invoice, context, manager, false);
    }

    /**
     * Creates a customer charge act editor.
     *
     * @param invoice        the charge to edit
     * @param context        the layout context
     * @param manager        the popup editor manager
     * @param addDefaultItem if <tt>true</tt> add a default item if the act has none
     * @return a new customer charge act editor
     */
    private ChargeEditor createCustomerChargeActEditor(final FinancialAct invoice, final LayoutContext context,
                                                       final PopupEditorManager manager, boolean addDefaultItem) {
        return new ChargeEditor(invoice, context, addDefaultItem) {
            @Override
            protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
                ActRelationshipCollectionEditor editor = super.createItemsEditor(act, items);
                if (editor instanceof ChargeItemRelationshipCollectionEditor) {
                    // register a handler for act popups
                    ((ChargeItemRelationshipCollectionEditor) editor).setPopupEditorManager(manager);
                }
                return editor;
            }
        };
    }

    /**
     * Helper to create a new stock location, linked to a location.
     *
     * @param location the location
     * @return the stock location
     */
    private Party createStockLocation(Party location) {
        Party stockLocation = (Party) create(StockArchetypes.STOCK_LOCATION);
        stockLocation.setName("STOCK-LOCATION-" + stockLocation.hashCode());
        EntityBean locationBean = new EntityBean(location);
        locationBean.addRelationship("entityRelationship.locationStockLocation", stockLocation);
        save(location, stockLocation);
        return stockLocation;
    }

    private static class ChargeEditor extends CustomerChargeActEditor {

        /**
         * Constructs a <tt>ChargeEditor</tt>.
         *
         * @param act            the act to edit
         * @param context        the layout context
         * @param addDefaultItem if <tt>true</tt> add a default item if the act has none
         */
        public ChargeEditor(FinancialAct act, LayoutContext context, boolean addDefaultItem) {
            super(act, null, context, addDefaultItem);
        }

        /**
         * Deletes an item.
         *
         * @param item the item to delete
         */
        public void delete(Act item) {
            getEditor().remove(item);
        }

        /**
         * Returns the current editor.
         *
         * @return the current editor. May be <tt>null</tt>
         */
        public CustomerChargeActItemEditor getCurrentEditor() {
            return (CustomerChargeActItemEditor) getEditor().getCurrentEditor();
        }

    }
}
