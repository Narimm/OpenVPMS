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

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.finance.estimate.EstimateTestHelper;
import org.openvpms.archetype.rules.math.Currencies;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.patient.PatientTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.ChargeEditContext;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link EstimateItemEditor} class.
 *
 * @author Tim Anderson
 */
public class EstimateItemEditorTestCase extends AbstractEstimateEditorTestCase {

    /**
     * Tracks errors logged.
     */
    private List<String> errors = new ArrayList<>();

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
     * The practice location.
     */
    private Party location;

    /**
     * The layout context.
     */
    private LayoutContext layout;


    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        customer = TestHelper.createCustomer();
        patient = TestHelper.createPatient();
        author = TestHelper.createUser();
        location = TestHelper.createLocation();

        // register an ErrorHandler to collect errors
        initErrorHandler(errors);

        Context context = new LocalContext();
        context.setPractice(getPractice());
        context.setLocation(location);
        context.setUser(author);
        layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
    }

    /**
     * Tests a product with a 10% discount.
     */
    @Test
    public void testDiscounts() {
        BigDecimal lowQuantity = BigDecimal.ONE;
        BigDecimal highQuantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal fixedPriceIncTax = BigDecimal.valueOf(2);
        Entity discount = DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        addDiscount(patient, discount);
        addDiscount(product, discount);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        editor.setPatient(patient);
        editor.setProduct(product);
        editor.setLowQuantity(lowQuantity);
        editor.setHighQuantity(highQuantity);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(estimate, editor);

        item = get(item);
        BigDecimal lowDiscount1 = new BigDecimal("1.20");
        BigDecimal highDiscount1 = new BigDecimal("2.20");
        BigDecimal lowTotal1 = new BigDecimal("10.80");
        BigDecimal highTotal1 = new BigDecimal("19.80");
        checkItem(item, patient, product, author, lowQuantity, highQuantity, unitPriceIncTax, unitPriceIncTax,
                  fixedPriceIncTax, lowDiscount1, highDiscount1, lowTotal1, highTotal1);

        // set low quantity to zero, and verify low discount, low zero.
        // NOTE: the lowTotal goes to 2 as the fixedPrice is still incorporated, for historical reasons. TODO
        editor.setLowQuantity(BigDecimal.ZERO);
        checkSave(estimate, editor);
        item = get(item);
        checkItem(item, patient, product, author, BigDecimal.ZERO, highQuantity, unitPriceIncTax, unitPriceIncTax,
                  fixedPriceIncTax, BigDecimal.ZERO, highDiscount1, new BigDecimal(2), highTotal1);

        // set low quantity and remove the discounts
        editor.setLowQuantity(lowQuantity);
        editor.setLowDiscount(BigDecimal.ZERO);
        editor.setHighDiscount(BigDecimal.ZERO);
        checkSave(estimate, editor);

        item = get(item);
        BigDecimal lowTotal2 = new BigDecimal("12.00");
        BigDecimal highTotal2 = new BigDecimal("22.00");
        checkItem(item, patient, product, author, lowQuantity, highQuantity, unitPriceIncTax, unitPriceIncTax,
                  fixedPriceIncTax, BigDecimal.ZERO, BigDecimal.ZERO, lowTotal2, highTotal2);
    }

    /**
     * Tests a product with a 10% discount where discounts are disabled at the practice location.
     * <p>
     * The calculated discount should be zero.
     */
    @Test
    public void testDisableDiscounts() {
        IMObjectBean bean = new IMObjectBean(layout.getContext().getLocation());
        bean.setValue("disableDiscounts", true);

        BigDecimal lowQuantity = BigDecimal.ONE;
        BigDecimal highQuantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal fixedPriceIncTax = BigDecimal.valueOf(2);
        Entity discount = DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        addDiscount(patient, discount);
        addDiscount(product, discount);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        editor.setPatient(patient);
        editor.setProduct(product);
        editor.setLowQuantity(lowQuantity);
        editor.setHighQuantity(highQuantity);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(estimate, editor);

        item = get(item);
        BigDecimal lowDiscount1 = BigDecimal.ZERO;
        BigDecimal highDiscount1 = BigDecimal.ZERO;
        BigDecimal lowTotal1 = new BigDecimal("12.00");
        BigDecimal highTotal1 = new BigDecimal("22.00");
        checkItem(item, patient, product, author, lowQuantity, highQuantity, unitPriceIncTax, unitPriceIncTax,
                  fixedPriceIncTax, lowDiscount1, highDiscount1, lowTotal1, highTotal1);
    }

    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions.
     */
    @Test
    public void testTaxExemption() {
        addTaxExemption(customer);

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal discount = BigDecimal.ZERO;
        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(estimate, editor);

        estimate = get(estimate);
        item = get(item);
        assertNotNull(estimate);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal totalExTax = new BigDecimal("20");
        checkItem(item, patient, product, author, quantity, quantity, unitPrice, unitPrice, fixedPrice,
                  discount, discount, totalExTax, totalExTax);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions.
     */
    @Test
    public void testTaxExemptionWithServiceRatios() {
        addTaxExemption(customer);

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal fixedCost = BigDecimal.ONE;
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal ratio = BigDecimal.valueOf(2);
        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);
        Entity productType = ProductTestHelper.createProductType("Z Product Type");
        ProductTestHelper.addProductType(product, productType);
        ProductTestHelper.addServiceRatio(layout.getContext().getLocation(), productType, ratio);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(estimate, editor);

        estimate = get(estimate);
        item = get(item);
        assertNotNull(estimate);
        assertNotNull(item);

        // verify the item matches that expected
        BigDecimal fixedPriceWithRatio = new BigDecimal("1.82").multiply(ratio);
        BigDecimal unitPriceWithRatio = new BigDecimal("9.09").multiply(ratio);
        BigDecimal total = new BigDecimal("40");
        checkItem(item, patient, product, author, quantity, quantity, unitPriceWithRatio, unitPriceWithRatio,
                  fixedPriceWithRatio, discount, discount, total, total);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that when the currency has a minPrice, the fixedPrice and unitPrice are rounded after the service ratio
     * is applied.
     */
    @Test
    public void testServiceRatioWithMinPrice() {
        // set a minimum price for calculated prices. This should only apply to prices calculated using a service ratio
        Lookup currency = TestHelper.getLookup(Currencies.LOOKUP, "AUD");
        IMObjectBean bean = new IMObjectBean(currency);
        bean.setValue("minPrice", new BigDecimal("0.20"));
        bean.save();

        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(5.05);
        BigDecimal fixedCost = BigDecimal.valueOf(0.5);
        BigDecimal fixedPrice = BigDecimal.valueOf(5.05);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal ratio = BigDecimal.valueOf(2);

        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);
        Entity productType = ProductTestHelper.createProductType("Z Product Type");
        ProductTestHelper.addProductType(product, productType);
        ProductTestHelper.addServiceRatio(layout.getContext().getLocation(), productType, ratio);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();

        // populate quantity, patient, clinician.
        editor.setQuantity(quantity);
        editor.setPatient(patient);
        editor.setProduct(product);
        checkSave(estimate, editor);

        item = get(item);

        // when the service ratio is applied, unitPrice and fixedPrice will be calculated as 11.10, then rounded
        // according to minPrice
        BigDecimal roundedPrice = BigDecimal.valueOf(11.20);
        BigDecimal total = BigDecimal.valueOf(22.40);

        // fixedPrice and unitPrice are calculated as 11.10, then rounded to minPrice
        checkItem(item, patient, product, author, quantity, quantity, roundedPrice, roundedPrice, roundedPrice,
                  discount, discount, total, total);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Verifies that when a product with a dose is selected, both the low and high quantities are updated with
     * the dose.
     */
    @Test
    public void testProductDose() {
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = new BigDecimal("9.09");
        BigDecimal unitPriceIncTax = BigDecimal.TEN;
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = new BigDecimal("1.82");
        BigDecimal fixedPriceIncTax = BigDecimal.valueOf(2);
        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("concentration", BigDecimal.ONE);
        Entity dose = ProductTestHelper.createDose(null, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ONE,
                                                   BigDecimal.ONE);
        ProductTestHelper.addDose(product, dose);

        PatientTestHelper.createWeight(patient, new Date(), new BigDecimal("4.2"), WeightUnits.KILOGRAMS);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        editor.setPatient(patient);
        editor.setProduct(product);

        // editor should now be valid
        assertTrue(editor.isValid());

        checkSave(estimate, editor);

        item = get(item);
        BigDecimal lowQuantity = new BigDecimal("4.2");
        BigDecimal highQuantity = new BigDecimal("4.2");
        BigDecimal lowDiscount = BigDecimal.ZERO;
        BigDecimal highDiscount = BigDecimal.ZERO;
        BigDecimal lowTotal1 = new BigDecimal("44.00");
        BigDecimal highTotal1 = new BigDecimal("44.00");
        checkItem(item, patient, product, author, lowQuantity, highQuantity, unitPriceIncTax, unitPriceIncTax,
                  fixedPriceIncTax, lowDiscount, highDiscount, lowTotal1, highTotal1);
    }

    /**
     * Verifies that the editor is invalid if a low quantity is less than a minimum quantity.
     * <p/>
     * Note that in practice, the minimum quantity is set by expanding a template.
     */
    @Test
    public void testMinimumQuantities() {
        BigDecimal two = BigDecimal.valueOf(2);
        Product product = createProduct(ProductArchetypes.MEDICATION);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        editor.setPatient(patient);
        editor.setProduct(product);
        editor.setMinimumQuantity(two);
        editor.setLowQuantity(two);
        editor.setHighQuantity(two);
        assertTrue(editor.isValid());

        // editor should be invalid when low quantity set below minimum
        editor.setLowQuantity(BigDecimal.ONE);
        assertFalse(editor.isValid());

        // now set above
        editor.setLowQuantity(two);
        assertTrue(editor.isValid());
    }

    /**
     * Verifies that a user with the appropriate user type can override minimum quantities.
     * <p/>
     * Note that in practice, the minimum quantity is set by expanding a template.
     */
    @Test
    public void testMinimumQuantitiesOverride() {
        Lookup userType = TestHelper.getLookup("lookup.userType", "MINIMUM_QTY_OVERRIDE");
        Party practice = getPractice();
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("minimumQuantitiesOverride", userType.getCode());
        bean.save();
        author.addClassification(userType);

        BigDecimal two = BigDecimal.valueOf(2);
        Product product = createProduct(ProductArchetypes.MEDICATION);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, location, layout), layout);
        editor.getComponent();
        assertFalse(editor.isValid());

        editor.setPatient(patient);
        editor.setProduct(product);
        editor.setLowQuantity(two);
        editor.setHighQuantity(BigDecimal.TEN);
        editor.setMinimumQuantity(two);
        assertTrue(editor.isValid());

        // set the low quantity above the minimum. The minimum quantity shouldn't change
        editor.setLowQuantity(BigDecimal.TEN);
        checkEquals(two, editor.getMinimumQuantity());

        // now set the low quantity. As the user has the override type, the minimum quantity should update
        editor.setLowQuantity(BigDecimal.ONE);
        checkEquals(BigDecimal.ONE, editor.getMinimumQuantity());
        assertTrue(editor.isValid());

        // now set an invalid low quantity and verify the minimum quantity doesn't update
        editor.setLowQuantity(BigDecimal.valueOf(-1));
        assertFalse(editor.isValid());
        checkEquals(BigDecimal.ONE, editor.getMinimumQuantity());

        // set the low quantity to zero. This should disable the minimum quantity
        editor.setLowQuantity(BigDecimal.ZERO);
        checkEquals(BigDecimal.ZERO, editor.getMinimumQuantity());
        assertTrue(editor.isValid());

        // verify the minimum is disabled
        editor.setLowQuantity(BigDecimal.ONE);
        checkEquals(BigDecimal.ZERO, editor.getMinimumQuantity());
        assertTrue(editor.isValid());
    }

    /**
     * Saves an estimate and estimate item editor in a single transaction.
     *
     * @param estimate the estimate
     * @param editor   the charge item editor
     */
    private void checkSave(final Act estimate, final EstimateItemEditor editor) {
        boolean saved = SaveHelper.save(editor, new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                ServiceHelper.getArchetypeService().save(estimate);
            }
        });
        assertTrue(saved);
    }

}
