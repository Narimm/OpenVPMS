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

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Random;


/**
 * Tests the {@link DiscountRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DiscountRulesTestCase extends ArchetypeServiceTest {

    /**
     * 10% discount classification.
     */
    private Lookup discount10;

    /**
     * 5% discount classification.
     */
    private Lookup discount5;


    /**
     * Tests the {@link DiscountRules#calculateDiscountAmount}
     * method when the intersection of customer, patient, and product discounts
     * result in no discount.
     */
    public void testCalculateDiscountForNoDiscounts() {
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product productNoDisc = createProduct();
        Product productWith10Disc = createProductWithDiscount(discount10);
        Product productWith5Disc = createProductWithDiscount(discount5);

        checkCalculateDiscount(custNoDisc, patientNoDisc, productNoDisc,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custWithDisc, patientNoDisc, productNoDisc,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custWithDisc, patientWithDisc, productNoDisc,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custNoDisc, patientNoDisc, productWith10Disc,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custWithDisc, patientWithDisc, productWith5Disc,
                               BigDecimal.ZERO);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscountAmount} method where the
     * product has a 10% discount.
     */
    public void testCalculateDiscountForProductDiscount() {
        BigDecimal tenCents = new BigDecimal("0.10");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithDiscount(discount10);

        checkCalculateDiscount(custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custWithDisc, patientNoDisc, product, tenCents);
        checkCalculateDiscount(custNoDisc, patientWithDisc, product, tenCents);
        checkCalculateDiscount(custWithDisc, patientWithDisc, product,
                               tenCents);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscountAmount}
     * method where the product type has an associated 10% discount.
     */
    public void testCalculateDiscountForProductTypeDiscount() {
        BigDecimal cents10 = new BigDecimal("0.10");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(discount10);

        checkCalculateDiscount(custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custWithDisc, patientNoDisc, product, cents10);
        checkCalculateDiscount(custNoDisc, patientWithDisc, product, cents10);
        checkCalculateDiscount(custWithDisc, patientWithDisc, product,
                               cents10);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscountAmount}
     * method where the product has a 10% discount, and the product
     * type has a 5% discount.
     */
    public void testCalculateDiscountForProductProductTypeDiscount() {
        BigDecimal cents10 = new BigDecimal("0.10");
        BigDecimal cents15 = new BigDecimal("0.15");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount5,
                                                        discount10);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount10);
        Product product = createProductWithProductTypeDiscount(discount5);
        product.addClassification(discount10);
        save(product);

        checkCalculateDiscount(custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custWithDisc, patientNoDisc, product, cents15);
        checkCalculateDiscount(custNoDisc, patientWithDisc, product, cents10);
        checkCalculateDiscount(custWithDisc, patientWithDisc, product,
                               cents15);
    }

    /**
     * Tests the {@link DiscountRules#calculateDiscountAmount}
     * method where the discount has a 10% discount that applies only to the
     * unit * price.
     */
    public void testCalculateDiscountForProductWithNoFixedDiscount() {
        Lookup discount = createDiscount(BigDecimal.TEN, false);
        BigDecimal cents5 = new BigDecimal("0.05");
        Party custNoDisc = createCustomer();
        Party custWithDisc = createCustomerWithDiscount(discount);
        Party patientNoDisc = createPatient();
        Party patientWithDisc = createPatientWithDiscount(discount);
        Product product = createProductWithDiscount(discount);
        product.addClassification(discount);
        save(product);

        checkCalculateDiscount(custNoDisc, patientNoDisc, product,
                               BigDecimal.ZERO);
        checkCalculateDiscount(custWithDisc, patientNoDisc, product, cents5);
        checkCalculateDiscount(custNoDisc, patientWithDisc, product, cents5);
        checkCalculateDiscount(custWithDisc, patientWithDisc, product, cents5);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        discount10 = createDiscount(BigDecimal.TEN, true);
        discount5 = createDiscount(new BigDecimal("5"), true);
    }

    /**
     * Verifies that the discount is calculated correctly by
     * {@link DiscountRules#calculateDiscountAmount},
     * for an act with a total value of <code>1.00</code>.
     *
     * @param customer         the customer
     * @param product          the product
     * @param expectedDiscount the expected discount
     */
    private void checkCalculateDiscount(Party customer, Party patient,
                                        Product product,
                                        BigDecimal expectedDiscount) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        BigDecimal fixedPrice = new BigDecimal("0.50");
        BigDecimal unitPrice = new BigDecimal("0.50");
        BigDecimal quantity = BigDecimal.ONE;

        BigDecimal discount = DiscountRules.calculateDiscountAmount(
                customer, patient, product, fixedPrice, unitPrice, quantity,
                service);
        assertTrue(expectedDiscount.compareTo(discount) == 0);
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
        Contact contact = (Contact) create("contact.phoneNumber");
        assertNotNull(contact);
        customer.addContact(contact);
        save(customer);
        return customer;
    }

    /**
     * Helper to create and save a customer with a 10% discount.
     *
     * @param discounts the discount classifications
     * @return a new customer
     */
    private Party createCustomerWithDiscount(Lookup ... discounts) {
        Party customer = createCustomer();
        for (Lookup discount : discounts) {
            customer.addClassification(discount);
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
    private Party createPatientWithDiscount(Lookup discount) {
        Party patient = createPatient();
        patient.addClassification(discount);
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
     * Helper to create and save a product with a discount.
     *
     * @param discount the discount
     * @return a new product
     */
    private Product createProductWithDiscount(Lookup discount) {
        Product product = createProduct();
        product.addClassification(discount);
        save(product);
        return product;
    }

    /**
     * Helper to create and save a product with a product type relationship.
     * The associated <em>entity.productType</em> has a discount classification.
     *
     * @param discount the discount
     * @return a new product
     */
    private Product createProductWithProductTypeDiscount(Lookup discount) {
        Product product = createProduct();
        Entity type = (Entity) create("entity.productType");
        type.setName("DiscountRulesTestCase-entity" + type.hashCode());
        type.addClassification(discount);
        save(type);
        EntityRelationship relationship
                = (EntityRelationship) create(
                "entityRelationship.productTypeProduct");
        relationship.setSource(type.getObjectReference());
        relationship.setTarget(product.getObjectReference());
        product.addEntityRelationship(relationship);
        save(product);
        return product;
    }

    /**
     * Helper to create and save a new discount classification.
     *
     * @param rate          the discount rate
     * @param fixedDiscount determines if the discount applies to the fixed
     *                      price. If <code>false</code> it only applies to the
     *                      unit price
     * @return a new discount classification
     */
    private Lookup createDiscount(BigDecimal rate, boolean fixedDiscount) {
        Lookup discount = (Lookup) create("lookup.discountType");
        IMObjectBean bean = new IMObjectBean(discount);
        bean.setValue("code", "XDISCOUNT_RULES_TESTCASE_"
                + Math.abs(new Random().nextInt()));
        bean.setValue("rate", rate);
        bean.setValue("discountFixed", fixedDiscount);
        save(discount);
        return discount;
    }

}
