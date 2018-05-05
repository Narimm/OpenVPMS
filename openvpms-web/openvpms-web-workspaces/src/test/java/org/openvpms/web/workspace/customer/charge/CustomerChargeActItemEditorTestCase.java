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
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceTestHelper;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.entity.EntityIdentity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.openvpms.web.workspace.customer.charge.CustomerChargeTestHelper.checkSavePopup;

/**
 * Tests the {@link CustomerChargeActItemEditor} class.
 *
 * @author Tim Anderson
 */
public class CustomerChargeActItemEditorTestCase extends AbstractCustomerChargeActEditorTest {

    /**
     * The customer.
     */
    private Party customer;

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<>();

    /**
     * The context.
     */
    private Context context;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();

        // register an ErrorHandler to collect errors
        initErrorHandler(errors);
        context = new LocalContext();
        context.setPractice(getPractice());
        Party location = TestHelper.createLocation(true); // enable stock control
        context.setLocation(location);
        context.setStockLocation(ProductTestHelper.createStockLocation(location));

        // set a minimum price for calculated prices.
        Lookup currency = TestHelper.getLookup(Currencies.LOOKUP, "AUD");
        IMObjectBean bean = new IMObjectBean(currency);
        bean.setValue("minPrice", new BigDecimal("0.20"));
        bean.save();
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
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
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
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new BigDecimal(100), customer, null,
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
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new BigDecimal(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkItemWithTemplate(credit, item);
    }

    /**
     * Verifies that the clinician can be cleared, as a test for OVPMS-1104.
     */
    @Test
    public void testClearClinician() {
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.valueOf(2);
        BigDecimal total = new BigDecimal("22");

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        CustomerChargeEditContext editContext = createEditContext(layout);

        // create the editor
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, editContext, layout);
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setClinician(clinician);

        // set the product
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());
        editor.setClinician(null);

        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        checkItem(item, patient, product, null, author, null, BigDecimal.ZERO, quantity, unitCost, new BigDecimal(10),
                  fixedCost, new BigDecimal(2), discount, tax, total);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions.
     */
    @Test
    public void testTaxExemption() {
        addTaxExemption(customer);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal discount = BigDecimal.ZERO;

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, layout);
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setClinician(clinician);

        // set the product
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());
        editor.setClinician(null);

        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal unitPriceExTax = new BigDecimal("9.00");  // rounded due to minPrice
        BigDecimal fixedPriceExTax = new BigDecimal("1.80"); // rounded due to minPrice
        checkItem(item, patient, product, null, author, null, BigDecimal.ZERO, quantity, unitCost, unitPriceExTax,
                  fixedCost, fixedPriceExTax, discount, BigDecimal.ZERO, new BigDecimal("19.80"));

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions and a service ratio is in place.
     */
    @Test
    public void testTaxExemptionWithServiceRatio() {
        addTaxExemption(customer);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal ratio = BigDecimal.valueOf(2); // double the fixed and unit prices

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);
        Entity productType = ProductTestHelper.createProductType("Z Product Type");
        ProductTestHelper.addProductType(product, productType);
        ProductTestHelper.addServiceRatio(context.getLocation(), productType, ratio);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, layout);
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician and product
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setClinician(clinician);
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());
        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal fixedPriceExTax = new BigDecimal("3.60"); // rounded due to minPrice
        BigDecimal unitPriceExTax = new BigDecimal("18.20"); // rounded due to minPrice
        BigDecimal totalExTax = unitPriceExTax.multiply(quantity).add(fixedPriceExTax);
        checkItem(item, patient, product, null, author, clinician, BigDecimal.ZERO, quantity, unitCost, unitPriceExTax,
                  fixedCost, fixedPriceExTax, discount, BigDecimal.ZERO, totalExTax);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Tests a product with a 10% discount on an invoice item.
     */
    @Test
    public void testInvoiceItemDiscounts() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);
        checkDiscounts(invoice, item);
    }

    /**
     * Tests a product with a 10% discount on a counter sale item.
     */
    @Test
    public void testCounterSaleItemDiscounts() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new BigDecimal(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct counterSale = acts.get(0);
        FinancialAct item = acts.get(1);
        checkDiscounts(counterSale, item);
    }


    /**
     * Tests a product with a 10% discount on a credit item.
     */
    @Test
    public void testCreditItemDiscounts() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new BigDecimal(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        FinancialAct credit = acts.get(0);
        FinancialAct item = acts.get(1);
        checkDiscounts(credit, item);
    }

    /**
     * Tests a product with a 10% discount where discounts are disabled at the practice location.
     * <p>
     * The calculated discount should be zero.
     */
    @Test
    public void testDisableDiscounts() {
        IMObjectBean bean = new IMObjectBean(context.getLocation());
        bean.setValue("disableDiscounts", true);

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal fixedPriceIncTax = BigDecimal.valueOf(2);
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createUser();
        Entity discount = DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        Party patient = TestHelper.createPatient();
        addDiscount(customer, discount);
        addDiscount(product, discount);

        context.setUser(author);
        context.setClinician(clinician);
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeEditContext editContext = createEditContext(layout);
        editContext.setEditorQueue(null); // disable popups

        // create the editor
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, editContext, layout);
        assertFalse(editor.isValid());

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient);
        }
        editor.setProduct(product);
        editor.setQuantity(quantity);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(charge, editor);

        item = get(item);
        // should be no discount
        BigDecimal discount1 = BigDecimal.ZERO;
        BigDecimal tax1 = new BigDecimal("2.00");
        BigDecimal total1 = new BigDecimal("22.00");
        checkItem(item, patient, product, null, author, clinician, BigDecimal.ZERO, quantity, unitCost, unitPriceIncTax,
                  fixedCost, fixedPriceIncTax, discount1, tax1, total1);
    }

    /**
     * Verifies that when a product with a dose is selected, the quantity is determined by the patient weight.
     */
    @Test
    public void testInvoiceProductDose() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        checkProductDose(acts.get(0), acts.get(1));
    }

    /**
     * Verifies that when a product with a dose is selected, the quantity is determined by the patient weight.
     */
    @Test
    public void testCreditProductDose() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new BigDecimal(100), customer, null, null,
                                                                          ActStatus.IN_PROGRESS);
        checkProductDose(acts.get(0), acts.get(1));
    }

    /**
     * Verifies that when a product with a dose is selected during a counter sale, the quantity remains unchanged
     * as there is no patient.
     */
    @Test
    public void testCounterProductDose() {
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new BigDecimal(100), customer, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        PatientTestHelper.createWeight(patient, new Date(), new BigDecimal("4.2"), WeightUnits.KILOGRAMS);
        User author = TestHelper.createUser();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");

        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        Entity dose = ProductTestHelper.createDose(null, BigDecimal.ZERO, BigDecimal.TEN,
                                                   BigDecimal.ONE, BigDecimal.ONE);
        ProductTestHelper.addDose(product, dose);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, layout);
        assertFalse(editor.isValid());

        assertFalse((editor.isDefaultQuantity()));

        // populate quantity, patient, clinician and product
        editor.setQuantity(quantity);
        assertFalse((editor.isDefaultQuantity()));
        editor.setProduct(product);
        checkEquals(quantity, item.getQuantity());
        assertFalse((editor.isDefaultQuantity()));
    }

    /**
     * Verifies that a product with a microchip Patient Identity displays a prompt to add a microchip.
     */
    @Test
    public void testMicrochip() {
        User author = TestHelper.createUser();
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layout.getContext().setUser(author); // to propagate to acts

        Party patient = TestHelper.createPatient();

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = createProduct(ProductArchetypes.MERCHANDISE);
        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("patientIdentity", PatientArchetypes.MICROCHIP);
        bean.save();

        CustomerChargeEditContext editContext = createEditContext(layout);
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, editContext, layout);
        editor.setQuantity(BigDecimal.ONE);
        editor.setPatient(patient);
        editor.setProduct(product);

        // editor should be invalid
        assertFalse(editor.isValid());
        assertTrue(editContext.getEditorQueue().getCurrent() instanceof EditDialog);
        EditDialog dialog = (EditDialog) editContext.getEditorQueue().getCurrent();
        IMObjectEditor microchip = dialog.getEditor();
        microchip.getProperty("identity").setValue("123456789");
        checkSavePopup(editContext.getEditorQueue(), PatientArchetypes.MICROCHIP, false);
        checkSave(charge, editor);

        patient = get(patient);
        assertEquals(1, patient.getIdentities().size());
        EntityIdentity identity = patient.getIdentities().iterator().next();
        assertEquals("123456789", identity.getIdentity());
        assertEquals("  (Microchip: 123456789)", patient.getDescription());

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that the editor is invalid if a quantity is less than a minimum quantity.
     * <p>
     * Note that in practice, the minimum quantity is set by expanding a template or invoicing an estimate.
     */
    @Test
    public void testMinimumQuantities() {
        BigDecimal two = BigDecimal.valueOf(2);
        User author = TestHelper.createUser();
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layout.getContext().setUser(author); // to propagate to acts

        Party patient = TestHelper.createPatient();

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = ProductTestHelper.createService();
        CustomerChargeEditContext editContext = createEditContext(layout);
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, editContext, layout);
        editor.setPatient(patient);
        editor.setProduct(product);

        editor.setMinimumQuantity(two);
        editor.setQuantity(two);
        assertTrue(editor.isValid());

        // editor should be invalid when quantity set below minimum
        editor.setQuantity(BigDecimal.ONE);
        assertFalse(editor.isValid());

        // now set above
        editor.setQuantity(two);
        assertTrue(editor.isValid());
    }

    /**
     * Verifies that a user with the appropriate user type can override minimum quantities.
     * <p>
     * Note that in practice, the minimum quantity is set by expanding a template or invoicing an estimate.
     */
    @Test
    public void testMinimumQuantitiesOverride() {
        BigDecimal two = BigDecimal.valueOf(2);

        // set up a user that can override minimum quantities
        Lookup userType = TestHelper.getLookup("lookup.userType", "MINIMUM_QTY_OVERRIDE");
        Party practice = getPractice();
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("minimumQuantitiesOverride", userType.getCode());
        bean.save();
        User author = TestHelper.createUser();
        author.addClassification(userType);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        layout.getContext().setUser(author); // to propagate to acts

        Party patient = TestHelper.createPatient();

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = ProductTestHelper.createService();
        CustomerChargeEditContext editContext = createEditContext(layout);
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, editContext, layout);
        editor.setPatient(patient);
        editor.setProduct(product);

        editor.setMinimumQuantity(two);
        editor.setQuantity(two);
        assertTrue(editor.isValid());

        // set the quantity above the minimum. The minimum quantity shouldn't change
        editor.setQuantity(BigDecimal.TEN);
        checkEquals(two, editor.getMinimumQuantity());

        // now set the quantity below the minimum. As the user has the override type, the minimum quantity should update
        editor.setQuantity(BigDecimal.ONE);
        checkEquals(BigDecimal.ONE, editor.getMinimumQuantity());
        assertTrue(editor.isValid());

        // now set a negative quantity. This is supported for charges as hack for invoice level discounts but the
        // minimum quantity doesn't support negatives, so the item will be invalid.
        BigDecimal minusOne = BigDecimal.valueOf(-1);
        editor.setQuantity(minusOne);
        checkEquals(minusOne, editor.getMinimumQuantity());
        assertFalse(editor.isValid());

        // set the quantity to zero. This should disable the minimum quantity
        editor.setQuantity(BigDecimal.ZERO);
        checkEquals(BigDecimal.ZERO, editor.getMinimumQuantity());
        assertTrue(editor.isValid());

        // verify the minimum is disabled
        editor.setQuantity(BigDecimal.ONE);
        checkEquals(BigDecimal.ZERO, editor.getMinimumQuantity());
        assertTrue(editor.isValid());
    }

    /**
     * Verifies that when a product has two fixed prices, the default is selected.
     */
    @Test
    public void testDefaultFixedPrice() {
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        User author = TestHelper.createUser();
        layout.getContext().setUser(author); // to propagate to acts

        Party patient = TestHelper.createPatient();

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        Product product = ProductTestHelper.createService();
        ProductPrice fixed1 = ProductPriceTestHelper.createFixedPrice(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, (Date) null, null, false);
        ProductPrice fixed2 = ProductPriceTestHelper.createFixedPrice(
                new BigDecimal("0.909"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, (Date) null, null, true);
        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        save(product);

        TestCustomerChargeActItemEditor editor1 = createEditor(charge, item, createEditContext(layout), layout);
        editor1.setPatient(patient);
        editor1.setProduct(product);
        editor1.setQuantity(BigDecimal.ONE);
        assertTrue(editor1.isValid());
        save(charge, editor1);

        checkEquals(BigDecimal.ONE, editor1.getFixedPrice());
        checkEquals(BigDecimal.ONE, editor1.getTotal());

        // reload and verify the price doesn't change
        charge = get(charge);
        item = get(item);
        TestCustomerChargeActItemEditor editor2 = createEditor(charge, item, createEditContext(layout), layout);
        checkEquals(BigDecimal.ONE, editor2.getFixedPrice());
        checkEquals(BigDecimal.ONE, editor2.getTotal());
    }

    /**
     * Verifies that stock counts update.
     */
    @Test
    public void testUpdateStock() {
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        User author = TestHelper.createUser();
        layout.getContext().setUser(author); // to propagate to acts

        Party patient = TestHelper.createPatient();
        Product product = createProduct(ProductArchetypes.MERCHANDISE, BigDecimal.TEN);
        checkStock(product, BigDecimal.ZERO);

        // create an item with an initial quantity of zero.
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(
                customer, patient, product, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO,
                ActStatus.IN_PROGRESS);
        FinancialAct charge = acts.get(0);
        FinancialAct item = acts.get(1);

        TestCustomerChargeActItemEditor editor1 = createEditor(charge, item, createEditContext(layout), layout);
        assertTrue(editor1.isValid());
        save(charge, editor1);
        checkStock(product, BigDecimal.ZERO);

        charge = get(charge);
        item = get(item);

        // set to 10, and verify stock goes down to -10
        TestCustomerChargeActItemEditor editor2 = createEditor(charge, item, createEditContext(layout), layout);
        editor2.setQuantity(BigDecimal.TEN);
        save(charge, editor2);
        checkStock(product, BigDecimal.TEN.negate());

        // set to 0 and verify stock goes back to zero
        editor2.setQuantity(BigDecimal.ZERO);
        save(charge, editor2);
        checkStock(product, BigDecimal.ZERO);

        // set to 1, and verify stock goes down to -1
        editor2.setQuantity(BigDecimal.ONE);
        save(charge, editor2);
        checkStock(product, BigDecimal.ONE.negate());
    }

    /**
     * Verifies that discounts are limited to those specified on the fixed price.
     */
    @Test
    public void testFixedPriceMaximumDiscount() {
        ProductPrice fixedPrice1 = ProductPriceTestHelper.createFixedPrice("20", "10", "100", "75", (Date) null, null,
                                                                           true);
        ProductPrice fixedPrice2 = ProductPriceTestHelper.createFixedPrice("40", "20", "100", "50", (Date) null, null,
                                                                           false);
        Party patient = TestHelper.createPatient();
        Product product = ProductTestHelper.createMerchandise();

        // create a 100% discount, and associate it with the product nad patient.
        Entity discount = DiscountTestHelper.createDiscount(MathRules.ONE_HUNDRED, true, DiscountRules.PERCENTAGE);
        IMObjectBean patientBean = new IMObjectBean(patient);
        patientBean.addNodeTarget("discounts", discount);

        product.addProductPrice(fixedPrice1);
        product.addProductPrice(fixedPrice2);
        IMObjectBean productBean = new IMObjectBean(product);
        productBean.addNodeTarget("discounts", discount);
        save(patient, product);

        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
                                                                           ActStatus.IN_PROGRESS);
        FinancialAct invoice = acts.get(0);
        FinancialAct item = acts.get(1);

        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        User author = TestHelper.createUser();
        layout.getContext().setUser(author); // to propagate to acts
        TestCustomerChargeActItemEditor editor = createEditor(invoice, item, createEditContext(layout), layout);

        editor.setPatient(patient);
        editor.setProduct(product);

        // fixed price should be calculated from the default $20. This has a 75% maximum discount
        checkEquals(new BigDecimal("22.0"), editor.getFixedPrice());
        checkEquals(new BigDecimal("5.50"), editor.getTotal());

        // now change the price to the $44, which calculated from the tax-inc $40 price. This has a 50% maximum discount
        editor.setFixedPrice(new BigDecimal("44.0"));
        checkEquals(new BigDecimal("22.00"), editor.getTotal());

        // now change the price to one not linked to the product. The maximum disccount should default to 100%
        editor.setFixedPrice(new BigDecimal("11.0"));
        checkEquals(BigDecimal.ZERO, editor.getTotal());
    }

    /**
     * Verifies stock matches that expected.
     *
     * @param product  the product
     * @param expected the expected stock
     */
    private void checkStock(Product product, BigDecimal expected) {
        StockRules rules = new StockRules(getArchetypeService());
        Party stockLocation = context.getStockLocation();
        assertNotNull(stockLocation);
        BigDecimal stock = rules.getStock(product.getObjectReference(), stockLocation.getObjectReference());
        checkEquals(expected, stock);
    }

    /**
     * Creates an edit context.
     *
     * @param layout the layout context
     * @return a new edit context
     */
    private CustomerChargeEditContext createEditContext(LayoutContext layout) {
        return new CustomerChargeEditContext(customer, layout.getContext().getLocation(), layout);
    }

    /**
     * Creates a charge item editor.
     *
     * @param charge  the charge
     * @param item    the charge item
     * @param context the layout context
     * @return a new editor
     */
    private TestCustomerChargeActItemEditor createEditor(FinancialAct charge, FinancialAct item,
                                                         LayoutContext context) {
        return createEditor(charge, item, createEditContext(context), context);
    }

    /**
     * Creates a charge item editor.
     *
     * @param charge        the charge
     * @param item          the charge item
     * @param context       the edit context
     * @param layoutContext the layout context
     * @return a new editor
     */
    private TestCustomerChargeActItemEditor createEditor(FinancialAct charge, FinancialAct item,
                                                         CustomerChargeEditContext context,
                                                         LayoutContext layoutContext) {
        TestCustomerChargeActItemEditor editor = new TestCustomerChargeActItemEditor(item, charge, context,
                                                                                     layoutContext);
        editor.getComponent();
        return editor;
    }

    /**
     * Checks populating an invoice item with a product.
     *
     * @param productShortName the product archetype short name
     */
    private void checkInvoiceItem(String productShortName) {
        List<FinancialAct> acts = FinancialTestHelper.createChargesInvoice(new BigDecimal(100), customer, null, null,
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
        List<FinancialAct> acts = FinancialTestHelper.createChargesCounter(new BigDecimal(100), customer, null,
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
        List<FinancialAct> acts = FinancialTestHelper.createChargesCredit(new BigDecimal(100), customer, null, null,
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
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient1 = TestHelper.createPatient();
        Party patient2 = TestHelper.createPatient();
        User author1 = TestHelper.createUser();
        User author2 = TestHelper.createUser();
        User clinician1 = TestHelper.createClinician();
        User clinician2 = TestHelper.createClinician();

        // create product1 with reminder, investigation type and alert type
        BigDecimal quantity1 = BigDecimal.valueOf(2);
        BigDecimal unitCost1 = BigDecimal.valueOf(5);
        BigDecimal unitPrice1 = new BigDecimal("9.09");
        BigDecimal unitPrice1IncTax = BigDecimal.TEN;
        BigDecimal fixedCost1 = BigDecimal.ONE;
        BigDecimal fixedPrice1 = new BigDecimal("1.82");
        BigDecimal fixedPrice1IncTax = BigDecimal.valueOf(2);
        BigDecimal discount1 = BigDecimal.ZERO;
        BigDecimal tax1 = BigDecimal.valueOf(2);
        BigDecimal total1 = BigDecimal.valueOf(22);
        Product product1 = createProduct(productShortName, fixedCost1, fixedPrice1, unitCost1, unitPrice1);
        Entity reminderType = addReminder(product1);
        Entity investigationType = addInvestigation(product1);
        Entity template = addTemplate(product1);
        Entity alertType = addAlertType(product1);

        // create  product2 with no reminder no investigation type, and a service ratio that doubles the unit and
        // fixed prices
        BigDecimal quantity2 = BigDecimal.ONE;
        BigDecimal unitCost2 = BigDecimal.valueOf(5);
        BigDecimal unitPrice2 = BigDecimal.valueOf(5.05);
        BigDecimal fixedCost2 = BigDecimal.valueOf(0.5);
        BigDecimal fixedPrice2 = BigDecimal.valueOf(5.05);
        BigDecimal discount2 = BigDecimal.ZERO;
        BigDecimal ratio = BigDecimal.valueOf(2);
        BigDecimal tax2 = BigDecimal.valueOf(2.036);

        // when the service ratio is applied, the unit and and price will be calculated as 11.10, then rounded
        // according to minPrice
        BigDecimal roundedPrice = BigDecimal.valueOf(11.20);
        BigDecimal total2 = BigDecimal.valueOf(22.40);

        Product product2 = createProduct(productShortName, fixedCost2, fixedPrice2, unitCost2, unitPrice2);
        Entity productType = ProductTestHelper.createProductType("Z Product Type");
        ProductTestHelper.addProductType(product2, productType);
        ProductTestHelper.addServiceRatio(context.getLocation(), productType, ratio);

        // set up the context
        layout.getContext().setUser(author1); // to propagate to acts
        layout.getContext().setClinician(clinician1);

        // create the editor
        CustomerChargeEditContext editContext = createEditContext(layout);
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, editContext, layout);
        assertFalse(editor.isValid());

        // populate quantity, patient, product. If product1 is a medication, it should trigger a patient medication
        // editor popup
        editor.setQuantity(quantity1);

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient1);
        }
        editor.setProduct(product1);

        EditorQueue queue = editContext.getEditorQueue();
        if (TypeHelper.isA(item, CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (TypeHelper.isA(product1, ProductArchetypes.MEDICATION)) {
                // invoice items have a dispensing node
                assertFalse(editor.isValid()); // not valid while popup is displayed
                checkSavePopup(queue, PatientArchetypes.PATIENT_MEDICATION, false);
                // save the popup editor - should be a medication
            }

            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(queue, InvestigationArchetypes.PATIENT_INVESTIGATION, false);

            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(queue, ReminderArchetypes.REMINDER, false);

            assertFalse(editor.isValid()); // not valid while popup is displayed
            checkSavePopup(queue, PatientArchetypes.ALERT, false);
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
        checkItem(item, patient1, product1, null, author1, clinician1, BigDecimal.ZERO, quantity1, unitCost1,
                  unitPrice1IncTax, fixedCost1, fixedPrice1IncTax, discount1, tax1, total1);
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
            assertEquals(1, itemBean.getActs(PatientArchetypes.ALERT).size());

            checkInvestigation(item, patient1, investigationType, author1, clinician1);
            checkReminder(item, patient1, product1, reminderType, author1, clinician1);
            checkDocument(item, patient1, product1, template, author1, clinician1);
            checkAlert(item, patient1, product1, alertType, author1, clinician1);
        } else {
            // verify there are no medication, investigation, reminder nor document acts
            assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());
            assertTrue(itemBean.getActs(InvestigationArchetypes.PATIENT_INVESTIGATION).isEmpty());
            assertTrue(itemBean.getActs(ReminderArchetypes.REMINDER).isEmpty());
            assertTrue(itemBean.getActs("act.patientDocument*").isEmpty());
            assertTrue(itemBean.getActs(PatientArchetypes.ALERT).isEmpty());
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
        assertNull(queue.getCurrent());  // no new popup - existing medication should update
        assertTrue(editor.isValid());

        // save it
        checkSave(charge, editor);

        item = get(item);
        assertNotNull(item);

        // fixedPrice2 and unitPrice2 are calculated as 11.10, then rounded to minPrice
        checkItem(item, patient2, product2, null, author2, clinician2, BigDecimal.ZERO, quantity2, unitCost2,
                  roundedPrice, fixedCost2, roundedPrice, discount2, tax2, total2);

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
        assertTrue(itemBean.getActs(PatientArchetypes.ALERT).isEmpty());

        // make sure that clinicians can be set to null, as a test for OVPMS-1104
        if (itemBean.hasNode("clinician")) {
            editor.setClinician(null);
            assertTrue(editor.isValid());
            checkSave(charge, editor);

            item = get(item);
            assertNotNull(item);

            checkItem(item, patient2, product2, null, author2, null, BigDecimal.ZERO, quantity2, unitCost2,
                      roundedPrice, fixedCost2, roundedPrice, discount2, tax2, total2);
        }

        editor.setProduct(null);       // make sure nulls are handled
        assertFalse(editor.isValid());

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Checks populating a charge item with a template product.
     * <p>
     * NOTE: currently, charge items with template products validate correctly, but fail to save.
     * <p/>This is because the charge item relationship editor will only expand templates if the charge item itself
     * is valid - marking the item invalid for having a template would prevent this.
     * TODO - not ideal.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkItemWithTemplate(FinancialAct charge, FinancialAct item) {
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        context.getContext().setPractice(getPractice());

        Party patient = TestHelper.createPatient();
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        Product product = createProduct(ProductArchetypes.TEMPLATE, fixedCost, fixedPrice, unitCost, unitPrice);
        // costs and prices should be ignored
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();
        context.getContext().setUser(author); // to propagate to acts
        context.getContext().setClinician(clinician);

        CustomerChargeActItemEditor editor = new DefaultCustomerChargeActItemEditor(
                item, charge, createEditContext(context), context);
        editor.getComponent();
        assertFalse(editor.isValid());

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

        checkItem(item, patient, product, null, author, clinician, BigDecimal.ZERO, quantity, BigDecimal.ZERO,
                  BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        ActBean itemBean = new ActBean(item);
        // verify there are no medication acts
        assertTrue(itemBean.getActs(PatientArchetypes.PATIENT_MEDICATION).isEmpty());

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Tests charging a product with a 10% discount.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkDiscounts(FinancialAct charge, FinancialAct item) {
        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal fixedPriceIncTax = new BigDecimal(2);
        User clinician = TestHelper.createClinician();
        User author = TestHelper.createUser();
        Entity discount = DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        Party patient = TestHelper.createPatient();
        addDiscount(customer, discount);
        addDiscount(product, discount);

        context.setUser(author);
        context.setClinician(clinician);
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CustomerChargeEditContext context = createEditContext(layout);
        context.setEditorQueue(null); // disable popups

        // create the editor
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, context, layout);
        assertFalse(editor.isValid());

        if (!TypeHelper.isA(item, CustomerAccountArchetypes.COUNTER_ITEM)) {
            // counter sale items have no patient
            editor.setPatient(patient);
        }
        editor.setProduct(product);
        editor.setQuantity(quantity);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(charge, editor);

        item = get(item);
        BigDecimal discount1 = new BigDecimal("2.20");
        BigDecimal tax1 = new BigDecimal("1.80");
        BigDecimal total1 = new BigDecimal("19.80");
        checkItem(item, patient, product, null, author, clinician, BigDecimal.ZERO, quantity, unitCost, unitPriceIncTax,
                  fixedCost, fixedPriceIncTax, discount1, tax1, total1);

        // now remove the discounts
        editor.setDiscount(BigDecimal.ZERO);
        checkSave(charge, editor);

        item = get(item);
        BigDecimal discount2 = BigDecimal.ZERO;
        BigDecimal tax2 = new BigDecimal("2.00");
        BigDecimal total2 = new BigDecimal("22.00");
        checkItem(item, patient, product, null, author, clinician, BigDecimal.ZERO, quantity, unitCost, unitPriceIncTax,
                  fixedCost, fixedPriceIncTax, discount2, tax2, total2);
    }

    /**
     * Tests charging a product with a dose.
     *
     * @param charge the charge
     * @param item   the charge item
     */
    private void checkProductDose(FinancialAct charge, FinancialAct item) {
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        Party patient = TestHelper.createPatient();
        PatientTestHelper.createWeight(patient, new Date(), new BigDecimal("4.2"), WeightUnits.KILOGRAMS);
        User author = TestHelper.createUser();
        User clinician = TestHelper.createClinician();

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal fixedPriceIncTax = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal doseQuantity = new BigDecimal("4.2");

        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("concentration", BigDecimal.ONE);
        Entity dose = ProductTestHelper.createDose(null, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE,
                                                   BigDecimal.ONE);
        ProductTestHelper.addDose(product, dose);

        // set up the context
        layout.getContext().setUser(author); // to propagate to acts

        // create the editor
        CustomerChargeEditContext editContext = createEditContext(layout);
        editContext.setEditorQueue(null);  // disable popups
        TestCustomerChargeActItemEditor editor = createEditor(charge, item, editContext, layout);
        assertFalse(editor.isValid());

        assertFalse((editor.isDefaultQuantity()));

        // populate quantity, patient, clinician and product
        editor.setQuantity(quantity);
        assertFalse((editor.isDefaultQuantity()));

        editor.setPatient(patient);
        editor.setClinician(clinician);
        editor.setProduct(product);
        checkEquals(doseQuantity, editor.getQuantity());
        assertTrue(editor.isDefaultQuantity());

        // editor should now be valid
        assertTrue(editor.isValid());
        checkSave(charge, editor);

        charge = get(charge);
        item = get(item);
        assertNotNull(charge);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal tax = new BigDecimal("4.0");
        BigDecimal total = new BigDecimal("44.00");
        checkItem(item, patient, product, null, author, clinician, BigDecimal.ZERO, doseQuantity, unitCost,
                  unitPriceIncTax, fixedCost, fixedPriceIncTax, discount, tax, total);

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
                PatientHistoryChanges changes = new PatientHistoryChanges(null, null, getArchetypeService());
                ChargeSaveContext context = editor.getSaveContext();
                context.setHistoryChanges(changes);
                boolean saved = SaveHelper.save(charge);
                editor.save();
                if (saved) {
                    context.save();
                }
                context.setHistoryChanges(null);
                return saved;
            }
        });
    }

    private static class TestCustomerChargeActItemEditor extends CustomerChargeActItemEditor {

        /**
         * Constructs a {@link TestCustomerChargeActItemEditor}.
         * <p>
         * This recalculates the tax amount.
         *
         * @param act           the act to edit
         * @param parent        the parent act
         * @param context       the edit context
         * @param layoutContext the layout context
         */
        public TestCustomerChargeActItemEditor(FinancialAct act, Act parent, CustomerChargeEditContext context,
                                               LayoutContext layoutContext) {
            super(act, parent, context, layoutContext);
        }

        @Override
        public ActRelationshipCollectionEditor getDispensingEditor() {
            return super.getDispensingEditor();
        }
    }
}
