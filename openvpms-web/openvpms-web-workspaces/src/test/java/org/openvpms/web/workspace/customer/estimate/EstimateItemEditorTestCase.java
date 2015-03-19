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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.event.WindowPaneListener;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.discount.DiscountTestHelper;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.finance.estimate.EstimateTestHelper;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private List<String> errors = new ArrayList<String>();

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

        // register an ErrorHandler to collect errors
        ErrorHandler.setInstance(new ErrorHandler() {
            @Override
            public void error(Throwable cause) {
                errors.add(cause.getMessage());
            }

            public void error(String title, String message, Throwable cause, WindowPaneListener listener) {
                errors.add(message);
            }
        });
        Context context = new LocalContext();
        context.setPractice(getPractice());
        context.setLocation(TestHelper.createLocation());
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
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        Entity discount = DiscountTestHelper.createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);

        EntityBean patientBean = new EntityBean(patient);
        patientBean.addNodeRelationship("discounts", discount);
        patientBean.save();

        Product product = createProduct(ProductArchetypes.MEDICATION, fixedCost, fixedPrice, unitCost, unitPrice);
        EntityBean productBean = new EntityBean(product);
        productBean.addNodeRelationship("discounts", discount);
        productBean.save();

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate, layout);
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
        checkItem(item, patient, product, author, lowQuantity, highQuantity, unitPrice, unitPrice, fixedPrice,
                  lowDiscount1, highDiscount1, lowTotal1, highTotal1);

        // now remove the discounts
        editor.setLowDiscount(BigDecimal.ZERO);
        editor.setHighDiscount(BigDecimal.ZERO);
        checkSave(estimate, editor);

        item = get(item);
        BigDecimal lowTotal2 = new BigDecimal("12.00");
        BigDecimal highTotal2 = new BigDecimal("22.00");
        checkItem(item, patient, product, author, lowQuantity, highQuantity, unitPrice, unitPrice, fixedPrice,
                  BigDecimal.ZERO, BigDecimal.ZERO, lowTotal2, highTotal2);
    }

    /**
     * Verifies that prices and totals are correct when the customer has tax exemptions.
     */
    @Test
    public void testTaxExemption() {
        addTaxExemption(customer);

        BigDecimal quantity = BigDecimal.valueOf(2);
        BigDecimal unitCost = BigDecimal.valueOf(5);
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.valueOf(1);
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;
        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate, layout);
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
        BigDecimal fixedPriceExTax = new BigDecimal("1.82");
        BigDecimal unitPriceExTax = new BigDecimal("9.09");
        BigDecimal totalExTax = new BigDecimal("20");
        checkItem(item, patient, product, author, quantity, quantity, unitPriceExTax, unitPriceExTax, fixedPriceExTax,
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
        BigDecimal unitPrice = BigDecimal.valueOf(10);
        BigDecimal fixedCost = BigDecimal.ONE;
        BigDecimal fixedPrice = BigDecimal.valueOf(2);
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal ratio = BigDecimal.valueOf(2);
        Product product = createProduct(ProductArchetypes.MERCHANDISE, fixedCost, fixedPrice, unitCost, unitPrice);
        Entity productType = ProductTestHelper.createProductType("Z Product Type");
        ProductTestHelper.addProductType(product, productType);
        ProductTestHelper.addServiceRatio(layout.getContext().getLocation(), productType, ratio);

        Act item = (Act) create(EstimateArchetypes.ESTIMATE_ITEM);
        Act estimate = EstimateTestHelper.createEstimate(customer, author, item);

        // create the editor
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate, layout);
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
        BigDecimal fixedPriceExTax = new BigDecimal("1.82").multiply(ratio);
        BigDecimal unitPriceExTax = new BigDecimal("9.09").multiply(ratio);
        BigDecimal totalExTax = new BigDecimal("40");
        checkItem(item, patient, product, author, quantity, quantity, unitPriceExTax, unitPriceExTax, fixedPriceExTax,
                  discount, discount, totalExTax, totalExTax);

        // verify no errors were logged
        assertTrue(errors.isEmpty());
    }

    /**
     * Saves an estimate and estimate item editor in a single transaction.
     *
     * @param estimate the estimate
     * @param editor   the charge item editor
     */
    private void checkSave(final Act estimate, final EstimateItemEditor editor) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        boolean saved = template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return SaveHelper.save(estimate) && editor.save();
            }
        });
        assertTrue(saved);
    }

}
