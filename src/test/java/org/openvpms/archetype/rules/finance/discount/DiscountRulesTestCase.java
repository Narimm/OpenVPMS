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
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.discount;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Tests the {@link DiscountRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * 15% group discount type.
     */
    private Entity discountGroup;

    /**
     * The rules.
     */
    private DiscountRules rules;

    /**
     * 100% discount.
     */
    private static final BigDecimal HUNDRED = new BigDecimal("100");


    /**
     * Tests the {@link DiscountRules#calculateDiscount}
     * method when the intersection of customer, patient, and product discounts
     * result in no discount.
     */
    @Test
    public void testCalculateDiscountForNoDiscounts() {
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product productNoDisc = createProduct();
        Product productWith10Disc = createProductWithDiscounts(discount10);
        Product productWith5Disc = createProductWithDiscounts(discount5);

        Date now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, productNoDisc,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, productNoDisc,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc,
                               productNoDisc, BigDecimal.ZERO);
        checkCalculateDiscount(now, custNoDisc, patientNoDisc,
                               productWith10Disc, BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc,
                               productWith5Disc, BigDecimal.ZERO);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount} method where the
     * product has a 10% discount.
     */
    @Test
    public void testCalculateDiscountForProductDiscount() {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal cents5 = new BigDecimal("0.05");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithDiscounts(discount10);

        Date now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               cents10);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               cents10);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               cents10);

        // now expire the product discount by setting the end time of the
        // discount relationship, and verify the discount no longer applies
        EntityBean bean = new EntityBean(product);
        EntityRelationship r = bean.getRelationship(discount10);
        r.setActiveEndTime(new Date(now.getTime() - 1));
        bean.save();

        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               BigDecimal.ZERO);

        // add a new 5% discount to the product, customer and patient
        addDiscount(product, discount5, null);
        addDiscount(patientWithDisc, discount5, null);
        addDiscount(custWithDisc, discount5, null);

        now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               cents5);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               cents5);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               cents5);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}
     * method where the product type has an associated 10% discount.
     */
    @Test
    public void testCalculateDiscountForProductTypeDiscount() {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal cents5 = new BigDecimal("0.05");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(discount10);

        Date now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               cents10);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               cents10);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               cents10);

        // now expire the product type discount by setting the end time of the
        // discount relationship, and verify the discount no longer applies
        EntityBean bean = new EntityBean(product);
        Entity productType = bean.getNodeSourceEntity("type");
        bean = new EntityBean(productType);
        EntityRelationship r = bean.getRelationship(discount10);
        r.setActiveEndTime(new Date(now.getTime() - 1000));
        bean.save();

        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               BigDecimal.ZERO);

        // add a new 5% discount to the product type, customer and patient
        addDiscount(productType, discount5, null);
        addDiscount(patientWithDisc, discount5, null);
        addDiscount(custWithDisc, discount5, null);

        now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               cents5);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               cents5);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               cents5);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}
     * method where the product has a 10% discount, and the product
     * type has a 5% discount.
     */
    @Test
    public void testCalculateDiscountForProductProductTypeDiscount() {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal cents15 = new BigDecimal("0.15");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discountGroup);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(discount5);
        addDiscount(product, discount10, null);

        Date now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               cents15);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               cents10);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               cents15);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}
     * method where the discount has a 10% discount that applies only to the
     * unit * price.
     */
    @Test
    public void testCalculateDiscountForProductWithNoFixedDiscount() {
        Entity discount = createDiscount(BigDecimal.TEN, false);
        BigDecimal cents5 = new BigDecimal("0.05");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount);
        Product product = createProductWithDiscounts(discount, discount10);

        Date now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               cents5);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               cents5);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               cents5);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscount}
     * method where the product has a 10% discount, and the product
     * type has a 5% discount, and there is a maximum discount of 10%.
     */
    @Test
    public void testCalculateDiscountForMaxDiscount() {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal percent10 = new BigDecimal("10.00");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discountGroup);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(discount5);
        addDiscount(product, discount10, null);

        Date now = new Date();
        checkCalculateDiscount(now, custNoDisc, patientNoDisc, product,
                               percent10, percent10, BigDecimal.ZERO);
        checkCalculateDiscount(now, custWithDisc, patientNoDisc, product,
                               percent10, percent10, cents10);
        checkCalculateDiscount(now, custNoDisc, patientWithDisc, product,
                               percent10, percent10, cents10);
        checkCalculateDiscount(now, custWithDisc, patientWithDisc, product,
                               percent10, percent10, cents10);
    }

    /**
     * Tests the {@link DiscountRules#getDiscounts} method.
     */
    @Test
    public void testGetDiscounts() {
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount5,
                                                        discountGroup);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(discount5);
        addDiscount(product, discount10, null);

        Date now = new Date();
        checkDiscounts(now, custNoDisc, patientNoDisc, product);

        checkDiscounts(now, custWithDisc, patientNoDisc, product, discount5,
                       discount10);
        checkDiscounts(now, custNoDisc, patientWithDisc, product, discount10);
        checkDiscounts(now, custWithDisc, patientWithDisc, product, discount5,
                       discount10);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        discount10 = createDiscount(BigDecimal.TEN, true);
        discount5 = createDiscount(new BigDecimal("5"), true);
        discountGroup = createDiscountGroup(discount10, discount5);
        rules = new DiscountRules();
    }

    /**
     * Verifies that the discount is calculated correctly by
     * {@link DiscountRules#calculateDiscount},
     * for an act with a total value of <code>1.00</code>.
     *
     * @param date             the date, used to determine if a discount applies
     * @param customer         the customer
     * @param patient          the patient
     * @param product          the product
     * @param expectedDiscount the expected discount
     */
    private void checkCalculateDiscount(Date date, Party customer,
                                        Party patient,
                                        Product product,
                                        BigDecimal expectedDiscount) {
        checkCalculateDiscount(date, customer, patient, product,
                               HUNDRED, HUNDRED, expectedDiscount);
    }

    /**
     * Verifies that the discount is calculated correctly by
     * {@link DiscountRules#calculateDiscount},
     * for an act with a total value of <code>1.00</code>.
     *
     * @param date             the date, used to determine if a discount applies
     * @param patient          the patient
     * @param customer         the customer
     * @param product          the product
     * @param maxFixedDiscount the maximum fixed price discount %
     * @param maxUnitDiscount  the maximum unit price discount %
     * @param expectedDiscount the expected discount
     */
    private void checkCalculateDiscount(Date date, Party customer,
                                        Party patient,
                                        Product product,
                                        BigDecimal maxFixedDiscount,
                                        BigDecimal maxUnitDiscount,
                                        BigDecimal expectedDiscount) {
        BigDecimal fixedPrice = new BigDecimal("0.50");
        BigDecimal unitPrice = new BigDecimal("0.50");
        BigDecimal quantity = BigDecimal.ONE;

        BigDecimal discount = rules.calculateDiscount(
                date, customer, patient, product, fixedPrice, unitPrice,
                quantity, maxFixedDiscount, maxUnitDiscount);
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
    private void checkDiscounts(Date date, Party customer, Party patient,
                                Product product, Entity... expected) {
        List<Entity> discounts = rules.getDiscounts(date, customer, patient,
                                                    product);
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
        Party customer = (Party) create("party.customerperson");
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("firstName", "J");
        bean.setValue("lastName", "Zoo");
        Contact contact = (Contact) create(ContactArchetypes.PHONE);
        assertNotNull(contact);
        customer.addContact(contact);
        save(customer);
        return customer;
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
     * @return a new product
     */
    private Product createProduct() {
        Product product = (Product) create("product.medication");
        assertNotNull(product);
        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("name",
                      "DiscountRulesTestCase-product" + product.hashCode());
        bean.save();
        return product;
    }

    /**
     * Helper to create and save a product with discounts.
     *
     * @param discounts the discounts
     * @return a new product
     */
    private Product createProductWithDiscounts(Entity... discounts) {
        return createProductWithDiscounts(null, discounts);
    }

    /**
     * Helper to create and save a product with discounts.
     *
     * @param endTime   the discount end time. May be <tt>null</tt>
     * @param discounts the discounts
     * @return a new product
     */
    private Product createProductWithDiscounts(Date endTime,
                                               Entity... discounts) {
        Product product = createProduct();
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
     * @param discount the discount
     * @return a new product
     */
    private Product createProductWithProductTypeDiscount(Entity discount) {
        Product product = createProduct();
        Entity type = (Entity) create("entity.productType");
        type.setName("DiscountRulesTestCase-entity" + type.hashCode());
        addDiscount(type, discount, null);
        EntityBean typeBean = new EntityBean(type);
        typeBean.addRelationship("entityRelationship.productTypeProduct",
                                 product);
        save(type);
        save(product);
        return product;
    }

    /**
     * Adds a discount to an entity.
     *
     * @param entity   the entity
     * @param discount the discount to add
     * @param endTime  the end time of the discount. May be <tt>null</tt>
     */
    private void addDiscount(Entity entity, Entity discount, Date endTime) {
        EntityBean bean = new EntityBean(entity);
        String shortName = null;
        if (bean.isA("product.*")) {
            shortName = "entityRelationship.discountProduct";
        } else if (bean.isA("party.customer*")) {
            shortName = "entityRelationship.discountCustomer";
        } else if (bean.isA("party.patientpet")) {
            shortName = "entityRelationship.discountPatient";
        } else if (bean.isA("entity.productType")) {
            shortName = "entityRelationship.discountProductType";
        } else {
            fail("Invalid entity for discounts: " + entity.getArchetypeId());
        }
        EntityRelationship r = bean.addRelationship(shortName, discount);
        r.setActiveEndTime(endTime);
        bean.save();
    }

    /**
     * Helper to create and save a new discount type entity.
     *
     * @param rate          the discount rate
     * @param fixedDiscount determines if the discount applies to the fixed
     *                      price. If <tt>false</tt> it only applies to the
     *                      unit price
     * @return a new discount
     */
    private Entity createDiscount(BigDecimal rate, boolean fixedDiscount) {
        Entity discount = (Entity) create("entity.discountType");
        IMObjectBean bean = new IMObjectBean(discount);
        bean.setValue("name", "XDISCOUNT_RULES_TESTCASE_"
                              + Math.abs(new Random().nextInt()));
        bean.setValue("rate", rate);
        bean.setValue("discountFixed", fixedDiscount);
        save(discount);
        return discount;
    }

    /**
     * Helper to create and save a new discount group type entity.
     *
     * @param discounts the discounts to add to the group
     * @return a new discount group
     */
    private Entity createDiscountGroup(Entity... discounts) {
        Entity result = (Entity) create("entity.discountGroupType");
        EntityBean bean = new EntityBean(result);
        bean.setValue("name", "XDISCOUNT_RULES_TESTCASE_"
                              + Math.abs(new Random().nextInt()));
        for (Entity discount : discounts) {
            bean.addRelationship("entityRelationship.discountType", discount);
        }
        save(result);
        return result;
    }

}
