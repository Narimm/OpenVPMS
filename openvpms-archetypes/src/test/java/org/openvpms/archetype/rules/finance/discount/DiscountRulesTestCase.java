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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.discount;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.finance.discount.DiscountTestHelper.createDiscount;


/**
 * Tests the {@link DiscountRules} class.
 *
 * @author Tim Anderson
 */
public class DiscountRulesTestCase extends ArchetypeServiceTest {

    /**
     * 10% discount type.
     */
    private Entity discount10;

    /**
     * 5% discount type.
     */
    private Entity discount5;

    /**
     * At-cost discount, with 0% rate.
     */
    private Entity costDiscount0;

    /**
     * At-cost discount, with 10% rate.
     */
    private Entity costDiscount10;

    /**
     * 15% group discount type.
     */
    private Entity discountGroup;

    /**
     * Cost discount group.
     */
    private Entity costDiscountGroup;

    /**
     * The practice.
     */
    private Party practice;

    /**
     * The practice tax type.
     */
    private Lookup taxType;

    /**
     * The rules.
     */
    private DiscountRules rules;

    /**
     * 100% discount.
     */
    private static final BigDecimal HUNDRED = MathRules.ONE_HUNDRED;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        discount10 = createDiscount(BigDecimal.TEN, true, DiscountRules.PERCENTAGE);
        discount5 = createDiscount(new BigDecimal("5"), true, DiscountRules.PERCENTAGE);
        costDiscount0 = createDiscount(BigDecimal.ZERO, true, DiscountRules.COST_RATE);
        costDiscount10 = createDiscount(BigDecimal.TEN, true, DiscountRules.COST_RATE);
        discountGroup = createDiscountGroup(discount10, discount5);
        costDiscountGroup = createDiscountGroup(costDiscount0, costDiscount10);

        rules = new DiscountRules(getArchetypeService());

        // set up practice with 10% tax rate
        practice = (Party) TestHelper.create(PracticeArchetypes.PRACTICE);
        taxType = TestHelper.createTaxType(BigDecimal.TEN);
        practice.addClassification(taxType);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method when the intersection of customer, patient, and product
     * discounts result in no discount.
     */
    @Test
    public void testCalculateDiscountForNoDiscounts() {
        checkCalculatePercentageDiscountForNoDiscount(ProductArchetypes.MEDICATION);
        checkCalculatePercentageDiscountForNoDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculatePercentageDiscountForNoDiscount(ProductArchetypes.SERVICE);

        checkCalculateCostRateDiscountForNoDiscount(ProductArchetypes.MEDICATION);
        checkCalculateCostRateDiscountForNoDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculateCostRateDiscountForNoDiscount(ProductArchetypes.SERVICE);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product has a discount.
     */
    @Test
    public void testCalculateDiscountForProductDiscount() {
        checkCalculatePercentageDiscountForProductDiscount(ProductArchetypes.MEDICATION);
        checkCalculatePercentageDiscountForProductDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculatePercentageDiscountForProductDiscount(ProductArchetypes.SERVICE);

        checkCalculateCostDiscountForProductDiscount(ProductArchetypes.MEDICATION);
        checkCalculateCostDiscountForProductDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculateCostDiscountForProductDiscount(ProductArchetypes.SERVICE);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product type has a discount.
     */
    @Test
    public void testCalculateDiscountForProductTypeDiscount() {
        checkCalculatePercentageDiscountForProductTypeDiscount(ProductArchetypes.MEDICATION);
        checkCalculatePercentageDiscountForProductTypeDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculatePercentageDiscountForProductTypeDiscount(ProductArchetypes.SERVICE);

        checkCalculateCostDiscountForProductTypeDiscount(ProductArchetypes.MEDICATION);
        checkCalculateCostDiscountForProductTypeDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculateCostDiscountForProductTypeDiscount(ProductArchetypes.SERVICE);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}  method where both the product and product type have
     * different discounts.
     */
    @Test
    public void testCalculateDiscountForProductProductTypeDiscount() {
        checkCalculatePercentageDiscountForProductProductTypeDiscount(ProductArchetypes.MEDICATION);
        checkCalculatePercentageDiscountForProductProductTypeDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculatePercentageDiscountForProductProductTypeDiscount(ProductArchetypes.SERVICE);

        checkCalculateCostDiscountForProductProductTypeDiscount(ProductArchetypes.MEDICATION);
        checkCalculateCostDiscountForProductProductTypeDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculateCostDiscountForProductProductTypeDiscount(ProductArchetypes.SERVICE);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the discount has a discount that applies only
     * to the unit price.
     */
    @Test
    public void testCalculateDiscountForProductWithNoFixedDiscount() {
        checkCalculatePercentageDiscountForProductWithNoFixedDiscount(ProductArchetypes.MEDICATION);
        checkCalculatePercentageDiscountForProductWithNoFixedDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculatePercentageDiscountForProductWithNoFixedDiscount(ProductArchetypes.SERVICE);

        checkCalculateCostDiscountForProductWithNoFixedDiscount(ProductArchetypes.MEDICATION);
        checkCalculateCostDiscountForProductWithNoFixedDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculateCostDiscountForProductWithNoFixedDiscount(ProductArchetypes.SERVICE);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product has a 10% discount, and the product
     * type has a 5% discount, and there is a maximum discount of 10%.
     */
    @Test
    public void testCalculateDiscountForMaxDiscount() {
        checkCalculatePercentageDiscountForMaxDiscount(ProductArchetypes.MEDICATION);
        checkCalculatePercentageDiscountForMaxDiscount(ProductArchetypes.MERCHANDISE);
        checkCalculatePercentageDiscountForMaxDiscount(ProductArchetypes.SERVICE);
    }

    /**
     * Tests the {@link DiscountRules#getDiscounts} method.
     */
    @Test
    public void testGetDiscounts() {
        checkGetDiscounts(ProductArchetypes.MEDICATION);
        checkGetDiscounts(ProductArchetypes.MERCHANDISE);
        checkGetDiscounts(ProductArchetypes.SERVICE);
    }

    /**
     * Verifies that {@link DiscountRules#getDiscounts} can be invoked for <em>product.priceTemplate</em> and
     * <em>product.template</em>.
     */
    @Test
    public void testGetDiscountsForDummyProduct() {
        checkGetDiscountsForDummyProduct(ProductArchetypes.PRICE_TEMPLATE);
        checkGetDiscountsForDummyProduct(ProductArchetypes.TEMPLATE);
    }

    /**
     * Verifies that {@link DiscountRules#calculateDiscount} can be invoked for <em>product.priceTemplate</em> and
     * <em>product.template</em>.
     */
    @Test
    public void testCalculateDiscountForDummyProduct() {
        checkCalculateDiscountForDummyProduct(ProductArchetypes.PRICE_TEMPLATE);
        checkCalculateDiscountForDummyProduct(ProductArchetypes.TEMPLATE);
    }

    /**
     * Verifies that when two at-cost discounts are present with the same rate but different discountFixed flags:
     * <ul>
     * <li>only one rate is used; and</li>
     * <li>it is the rate with {@code discountFixed=false}</li>
     * </ul>
     */
    @Test
    public void testMultipleCostDiscountsWithSameRateSelectsNoDiscountFixed() {
        Entity discount1 = createDiscount(BigDecimal.ZERO, true, DiscountRules.COST_RATE);
        Entity discount2 = createDiscount(BigDecimal.ZERO, false, DiscountRules.COST_RATE);

        Party customer = createCustomerWithDiscount(discount1, discount2);
        Party patient = createPatient();
        Product product = createProductWithDiscounts(ProductArchetypes.MEDICATION, discount1, discount2);

        Date now = new Date();
        checkCalculateCostDiscount(now, customer, patient, product, new BigDecimal("1.20"));
    }

    /**
     * Verifies that when a customer has a percentage discount, the discount is not affected by any tax exclusions.
     */
    @Test
    public void testCalculatePercentageDiscountForCustomerWithTaxExclusions() {
        // set up two customers with the same percentage discount, but give the first a tax exclusion.
        Party customer1 = createCustomerWithDiscount(discount10);
        Party customer2 = createCustomerWithDiscount(discount10);
        customer1.addClassification(taxType);

        Product product = createProductWithDiscounts(ProductArchetypes.MEDICATION, discount10);
        Party patient = createPatient();

        Date now = new Date();
        checkCalculatePercentageDiscount(now, customer1, patient, product, new BigDecimal("0.10"));
        checkCalculatePercentageDiscount(now, customer2, patient, product, new BigDecimal("0.10"));
    }

    /**
     * Verifies that at-cost discounts are calculated correctly for a customer that has a tax exclusion.
     */
    @Test
    public void testAtCostDiscountForCustomerWithTaxExclusions() {
        // set up two customers with the same at-cost discount, but give the first a tax exclusion.
        Party customer1 = createCustomerWithDiscount(costDiscount10);
        Party customer2 = createCustomerWithDiscount(costDiscount10);
        customer1.addClassification(taxType);

        Product product = createProductWithDiscounts(ProductArchetypes.MEDICATION, costDiscount10);
        Party patient = createPatient();

        Date now = new Date();
        // customer1 should get a bigger discount as they have a tax exclusion
        checkCalculateCostDiscount(now, customer1, patient, product, new BigDecimal("4.60"));
        checkCalculateCostDiscount(now, customer2, patient, product, new BigDecimal("3.06"));
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method when the intersection of customer, patient, and product
     * discounts result in no discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculatePercentageDiscountForNoDiscount(String shortName) {
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product productNoDisc = createProduct(shortName);
        Product productWith10Disc = createProductWithDiscounts(shortName, discount10);
        Product productWith5Disc = createProductWithDiscounts(shortName, discount5);

        Date now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, productNoDisc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, productNoDisc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, productNoDisc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, productWith10Disc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, productWith5Disc, BigDecimal.ZERO);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method when the intersection of customer, patient, and product
     * discounts result in no discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculateCostRateDiscountForNoDiscount(String shortName) {
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(costDiscount0);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(costDiscount0);
        Product productNoDisc = createProduct(shortName);
        Product productWith0Disc = createProductWithDiscounts(shortName, costDiscount0);
        Product productWith10Disc = createProductWithDiscounts(shortName, costDiscount10);

        Date now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, productNoDisc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, productNoDisc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, productNoDisc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, productWith0Disc, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, productWith10Disc, BigDecimal.ZERO);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product has a 10% discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculatePercentageDiscountForProductDiscount(String shortName) {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal cents5 = new BigDecimal("0.05");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithDiscounts(shortName, discount10);

        Date now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, cents10);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, cents10);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, cents10);

        // now expire the product discount by setting the end time of the
        // discount relationship, and verify the discount no longer applies
        expireDiscount(product, discount10, now);

        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, BigDecimal.ZERO);

        // add a new 5% discount to the product, customer and patient
        addDiscount(product, discount5, null);
        addDiscount(patientWithDisc, discount5, null);
        addDiscount(custWithDisc, discount5, null);

        now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, cents5);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, cents5);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, cents5);
    }

    /**
     * Expires a relationship to a discount.
     *
     * @param entity   the entity
     * @param discount the discount to expire
     * @param time     the time relative to expire the discount to
     */
    private void expireDiscount(Entity entity, Entity discount, Date time) {
        IMObjectBean bean = new IMObjectBean(entity);
        EntityLink result = (EntityLink) bean.getValue("discounts", RefEquals.getTargetEquals(discount));
        assertNotNull(result);
        result.setActiveEndTime(new Date(time.getTime() - 1000));
        save(entity);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product has an at-cost discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculateCostDiscountForProductDiscount(String shortName) {
        BigDecimal four60 = new BigDecimal("4.60");
        BigDecimal three06 = new BigDecimal("3.06");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(costDiscount0);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(costDiscount0);
        Product product = createProductWithDiscounts(shortName, costDiscount0);

        Date now = new Date();
        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, four60);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, four60);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, four60);

        // now expire the product discount by setting the end time of the
        // discount relationship, and verify the discount no longer applies
        expireDiscount(product, costDiscount0, now);

        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, BigDecimal.ZERO);

        // add a new cost + 10% discount to the product, customer and patient
        addDiscount(product, costDiscount10, null);
        addDiscount(patientWithDisc, costDiscount10, null);
        addDiscount(custWithDisc, costDiscount10, null);

        now = new Date();
        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, three06);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, three06);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, three06);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product type has an associated 10% discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculatePercentageDiscountForProductTypeDiscount(String shortName) {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal cents5 = new BigDecimal("0.05");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(shortName, discount10);

        Date now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, cents10);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, cents10);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, cents10);

        // now expire the product type discount by setting the end time of the
        // discount relationship, and verify the discount no longer applies
        EntityBean bean = new EntityBean(product);
        Entity productType = bean.getNodeTargetEntity("type");
        expireDiscount(productType, discount10, now);

        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, BigDecimal.ZERO);

        // add a new 5% discount to the product type, customer and patient
        addDiscount(productType, discount5, null);
        addDiscount(patientWithDisc, discount5, null);
        addDiscount(custWithDisc, discount5, null);

        now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, cents5);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, cents5);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, cents5);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product type has an associated at-cost
     * discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculateCostDiscountForProductTypeDiscount(String shortName) {
        BigDecimal four60 = new BigDecimal("4.60");
        BigDecimal three06 = new BigDecimal("3.06");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(costDiscount0);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(costDiscount0);
        Product product = createProductWithProductTypeDiscount(shortName, costDiscount0);

        Date now = new Date();
        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, four60);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, four60);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, four60);

        // now expire the product type discount by setting the end time of the
        // discount relationship, and verify the discount no longer applies
        EntityBean bean = new EntityBean(product);
        Entity productType = bean.getNodeTargetEntity("type");
        expireDiscount(productType, costDiscount0, now);

        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, BigDecimal.ZERO);

        // add a new cost + 10% discount to the product, customer and patient
        addDiscount(productType, costDiscount10, null);
        addDiscount(patientWithDisc, costDiscount10, null);
        addDiscount(custWithDisc, costDiscount10, null);

        now = new Date();
        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, three06);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, three06);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, three06);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}  method where the product has a 10% discount, and the product
     * type has a 5% discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculatePercentageDiscountForProductProductTypeDiscount(String shortName) {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal cents15 = new BigDecimal("0.15");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discountGroup);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(shortName, discount5);
        addDiscount(product, discount10, null);

        Date now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, cents15);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, cents10);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, cents15);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}  method where the product has an at-cost discount, and the
     * product type has a cost + 10% discount.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculateCostDiscountForProductProductTypeDiscount(String shortName) {
        BigDecimal four60 = new BigDecimal("4.60");
        BigDecimal three06 = new BigDecimal("3.06");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(costDiscountGroup);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(costDiscount10);
        Product product = createProductWithProductTypeDiscount(shortName, costDiscount0);
        addDiscount(product, costDiscount10, null);

        Date now = new Date();
        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, four60);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, three06);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, four60);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}  method where the discount has a 10% discount that applies only
     * to the unit price.
     *
     * @param shortName the product archetype shortname
     */
    private void checkCalculatePercentageDiscountForProductWithNoFixedDiscount(String shortName) {
        Entity discount = createDiscount(BigDecimal.TEN, false, DiscountRules.PERCENTAGE);
        BigDecimal cents5 = new BigDecimal("0.05");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount);
        Product product = createProductWithDiscounts(shortName, discount, discount10);

        Date now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, cents5);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, cents5);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, cents5);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}  method where the discount has an at-cost discount that applies
     * only to the unit price.
     *
     * @param shortName the product archetype shortname
     */
    private void checkCalculateCostDiscountForProductWithNoFixedDiscount(String shortName) {
        Entity discount = createDiscount(BigDecimal.ZERO, false, DiscountRules.COST_RATE);
        BigDecimal one20 = new BigDecimal("1.20");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount);
        Product product = createProductWithDiscounts(shortName, discount, discount10);

        Date now = new Date();
        checkCalculateCostDiscount(now, custNoDisc, patientNoDisc, product, BigDecimal.ZERO);
        checkCalculateCostDiscount(now, custWithDisc, patientNoDisc, product, one20);
        checkCalculateCostDiscount(now, custNoDisc, patientWithDisc, product, one20);
        checkCalculateCostDiscount(now, custWithDisc, patientWithDisc, product, one20);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the product has a 10% discount, and the product
     * type has a 5% discount, and there is a maximum discount of 10%.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculatePercentageDiscountForMaxDiscount(String shortName) {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal percent10 = new BigDecimal("10.00");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discountGroup);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(shortName, discount5);
        addDiscount(product, discount10, null);

        Date now = new Date();
        checkCalculatePercentageDiscount(now, custNoDisc, patientNoDisc, product, percent10, percent10,
                                         BigDecimal.ZERO);
        checkCalculatePercentageDiscount(now, custWithDisc, patientNoDisc, product, percent10, percent10, cents10);
        checkCalculatePercentageDiscount(now, custNoDisc, patientWithDisc, product, percent10, percent10, cents10);
        checkCalculatePercentageDiscount(now, custWithDisc, patientWithDisc, product, percent10, percent10, cents10);
    }

    /**
     * Tests the {@link DiscountRules#getDiscounts} method.
     *
     * @param shortName the product archetype short name
     */
    private void checkGetDiscounts(String shortName) {
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount5, discountGroup);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(shortName, discount5);
        addDiscount(product, discount10, null);

        Date now = new Date();
        checkDiscounts(now, custNoDisc, patientNoDisc, product);

        checkDiscounts(now, custWithDisc, patientNoDisc, product, discount5, discount10);
        checkDiscounts(now, custNoDisc, patientWithDisc, product, discount10);
        checkDiscounts(now, custWithDisc, patientWithDisc, product, discount5, discount10);
    }


    /**
     * Verifies that {@link DiscountRules#getDiscounts} can be invoked for <em>product.priceTemplate</em> and
     * <em>product.template</em>.
     *
     * @param shortName the product archetype short name
     */
    private void checkGetDiscountsForDummyProduct(String shortName) {
        Party customer = createCustomer();
        Party patient = createPatient();
        Product product = createProduct(shortName);
        List<Entity> discounts = rules.getDiscounts(new Date(), customer, patient, product);
        assertEquals(0, discounts.size());
    }

    /**
     * Verifies that {@link DiscountRules#calculateDiscount} can be invoked for <em>product.priceTemplate</em> and
     * <em>product.template</em>.
     *
     * @param shortName the product archetype short name
     */
    private void checkCalculateDiscountForDummyProduct(String shortName) {
        Party customer = createCustomer();
        Party patient = createPatient();
        Product product = createProduct(shortName);
        checkCalculatePercentageDiscount(new Date(), customer, patient, product, BigDecimal.ZERO);
    }

    /**
     * Verifies that the percentage discount is calculated correctly by {@link DiscountRules#calculateDiscount},
     * for an act with a total value of {@code 1.00}.
     *
     * @param date             the date, used to determine if a discount applies
     * @param customer         the customer
     * @param patient          the patient
     * @param product          the product
     * @param expectedDiscount the expected discount
     */
    private void checkCalculatePercentageDiscount(Date date, Party customer, Party patient, Product product,
                                                  BigDecimal expectedDiscount) {
        checkCalculatePercentageDiscount(date, customer, patient, product, HUNDRED, HUNDRED, expectedDiscount);
    }

    /**
     * Verifies that the percentage discount is calculated correctly by {@link DiscountRules#calculateDiscount},
     * for an act with a total value of {@code 1.00}.
     *
     * @param date             the date, used to determine if a discount applies
     * @param patient          the patient
     * @param customer         the customer
     * @param product          the product
     * @param maxFixedDiscount the maximum fixed price discount %
     * @param maxUnitDiscount  the maximum unit price discount %
     * @param expectedDiscount the expected discount
     */
    private void checkCalculatePercentageDiscount(Date date, Party customer,
                                                  Party patient,
                                                  Product product,
                                                  BigDecimal maxFixedDiscount,
                                                  BigDecimal maxUnitDiscount,
                                                  BigDecimal expectedDiscount) {
        BigDecimal fixedCost = BigDecimal.ZERO;
        BigDecimal unitCost = BigDecimal.ZERO;
        BigDecimal fixedPrice = new BigDecimal("0.50");
        BigDecimal unitPrice = new BigDecimal("0.50");
        BigDecimal quantity = BigDecimal.ONE;
        BigDecimal discount = rules.calculateDiscount(date, practice, customer, patient, product, fixedCost,
                                                      unitCost, fixedPrice, unitPrice,
                                                      quantity, maxFixedDiscount, maxUnitDiscount);
        checkEquals(expectedDiscount, discount);
    }

    /**
     * Verifies that the cost discount is calculated correctly by {@link DiscountRules#calculateDiscount},
     * for an act with a total value of {@code 20.00}.
     *
     * @param date             the date, used to determine if a discount applies
     * @param customer         the customer
     * @param patient          the patient
     * @param product          the product
     * @param expectedDiscount the expected discount
     */
    private void checkCalculateCostDiscount(Date date, Party customer, Party patient, Product product,
                                            BigDecimal expectedDiscount) {
        checkCalculateCostDiscount(date, customer, patient, product, HUNDRED, HUNDRED, expectedDiscount);
    }

    /**
     * Verifies that the cost discount is calculated correctly by {@link DiscountRules#calculateDiscount},
     * for an act with a total value of {@code 20.00}.
     *
     * @param date             the date, used to determine if a discount applies
     * @param patient          the patient
     * @param customer         the customer
     * @param product          the product
     * @param maxFixedDiscount the maximum fixed price discount %
     * @param maxUnitDiscount  the maximum unit price discount %
     * @param expectedDiscount the expected discount
     */
    private void checkCalculateCostDiscount(Date date, Party customer, Party patient, Product product,
                                            BigDecimal maxFixedDiscount, BigDecimal maxUnitDiscount,
                                            BigDecimal expectedDiscount) {
        BigDecimal fixedCost = new BigDecimal("6.00");
        BigDecimal unitCost = new BigDecimal("4.00");
        BigDecimal fixedPrice = new BigDecimal("10.00");
        BigDecimal unitPrice = new BigDecimal("5.00");
        BigDecimal quantity = BigDecimal.valueOf(2);

        BigDecimal discount = rules.calculateDiscount(date, practice, customer, patient, product, fixedCost, unitCost,
                                                      fixedPrice, unitPrice, quantity, maxFixedDiscount,
                                                      maxUnitDiscount);
        checkEquals(expectedDiscount, discount);
    }

    /**
     * Verifies that the correct discounts are returned for the specified
     * date, customer, patient and product.
     *
     * @param date     the date, used to determine if a discount applies
     * @param patient  the patient
     * @param customer the customer
     * @param product  the product
     * @param expected the expected discounts
     */
    private void checkDiscounts(Date date, Party customer, Party patient, Product product, Entity... expected) {
        List<Entity> discounts = rules.getDiscounts(date, customer, patient, product);
        assertEquals(expected.length, discounts.size());
        for (Entity discount : expected) {
            assertTrue(discounts.contains(discount));
        }
    }

    /**
     * Helper to create and save a customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        return TestHelper.createCustomer();
    }

    /**
     * Helper to create and save a customer with a 10% discount.
     *
     * @param discounts the discount types
     * @return a new customer
     */
    private Party createCustomerWithDiscount(Entity... discounts) {
        Party customer = createCustomer();
        for (Entity discount : discounts) {
            addDiscount(customer, discount, null);
        }
        return customer;
    }

    /**
     * Helper to create and save a patient.
     *
     * @return a new patient
     */
    private Party createPatient() {
        Party patient = (Party) create("party.patientpet");
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("name", "XDiscountRulesTestCasse-pet"
                              + patient.hashCode());
        bean.setValue("species", "CANINE");
        bean.save();
        return patient;
    }

    /**
     * Helper to create and save a patient with a discount.
     *
     * @param discount the discount
     * @return a new patient
     */
    private Party createPatientWithDiscount(Entity discount) {
        Party patient = createPatient();
        addDiscount(patient, discount, null);
        return patient;
    }

    /**
     * Helper to create and save a product.
     *
     * @param shortName the product archetype short name
     * @return a new product
     */
    private Product createProduct(String shortName) {
        Product product = (Product) create(shortName);
        product.setName("XProduct-" + System.currentTimeMillis());
        save(product);
        return product;
    }

    /**
     * Helper to create and save a product with discounts.
     *
     * @param shortName the product archetype short name
     * @param discounts the discounts
     * @return a new product
     */
    private Product createProductWithDiscounts(String shortName, Entity... discounts) {
        return createProductWithDiscounts(shortName, null, discounts);
    }

    /**
     * Helper to create and save a product with discounts.
     *
     * @param shortName the product archetype short name
     * @param endTime   the discount end time. May be {@code null}
     * @param discounts the discounts
     * @return a new product
     */
    private Product createProductWithDiscounts(String shortName, Date endTime, Entity... discounts) {
        Product product = createProduct(shortName);
        for (Entity discount : discounts) {
            addDiscount(product, discount, endTime);
        }
        save(product);
        return product;
    }

    /**
     * Helper to create and save a product with a product type relationship.
     * The associated <em>entity.productType</em> has a discount.
     *
     * @param shortName the product archetype short name
     * @param discount  the discount
     * @return a new product
     */
    private Product createProductWithProductTypeDiscount(String shortName, Entity discount) {
        Product product = createProduct(shortName);
        Entity type = (Entity) create(ProductArchetypes.PRODUCT_TYPE);
        type.setName("DiscountRulesTestCase-entity" + type.hashCode());
        addDiscount(type, discount, null);
        EntityBean bean = new EntityBean(product);
        bean.addNodeTarget("type", type);
        save(product);
        return product;
    }

    /**
     * Adds a discount to an entity.
     *
     * @param entity   the entity
     * @param discount the discount to add
     * @param endTime  the end time of the discount. May be {@code null}
     */
    private void addDiscount(Entity entity, Entity discount, Date endTime) {
        IMObjectBean bean = new IMObjectBean(entity);
        IMObjectRelationship relationship = bean.addNodeTarget("discounts", discount);
        if (relationship instanceof PeriodRelationship) {
            ((PeriodRelationship) relationship).setActiveEndTime(endTime);
        }
        bean.save();
    }


    /**
     * Helper to create and save a new discount group type entity.
     *
     * @param discounts the discounts to add to the group
     * @return a new discount group
     */
    private Entity createDiscountGroup(Entity... discounts) {
        Entity result = (Entity) create("entity.discountGroupType");
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("name", "XDISCOUNT_RULES_TESTCASE_" + Math.abs(new Random().nextInt()));
        for (Entity discount : discounts) {
            bean.addNodeTarget("discounts", discount);
        }
        save(result);
        return result;
    }

}
