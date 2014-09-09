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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.tax;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CustomerTaxRules} class.
 *
 * @author Tim Anderson
 */
public class CustomerTaxRulesTestCase extends ArchetypeServiceTest {

    /**
     * The tax type classification.
     */
    private Lookup taxType;

    /**
     * The tax rules.
     */
    private CustomerTaxRules rules;

    /**
     * The practice.
     */
    private Party practice;


    /**
     * Tests the {@link CustomerTaxRules#calculateTax(FinancialAct, Party)}
     * method when the customer and product don't have any associated taxes.
     */
    @Test
    public void testCalculateTaxForNoTaxes() {
        Party customer = createCustomer();
        Product product = createProduct();
        checkCalculateTax(customer, product, BigDecimal.ZERO);
    }

    /**
     * Tests the {@link CustomerTaxRules#calculateTax(FinancialAct, Party)}
     * method where the product has an associated tax.
     */
    @Test
    public void testCalculateTaxForProductTax() {
        Party customer = createCustomer();
        Product product = createProduct();
        product.addClassification(taxType);
        save(product);

        checkCalculateTax(customer, product, new BigDecimal("0.091"));
    }

    /**
     * Tests the {@link CustomerTaxRules#calculateTax(FinancialAct, Party)}
     * method where the product type has an associated tax.
     */
    @Test
    public void testCalculateTaxForProductTypeTax() {
        Party customer = createCustomer();
        Product product = createProductWithProductTypeTax();

        checkCalculateTax(customer, product, new BigDecimal("0.091"));
    }

    /**
     * Tests the {@link CustomerTaxRules#getTaxExemptions(Party)} method.
     */
    @Test
    public void testGetTaxExemptions() {
        Party customer = createCustomer();
        assertTrue(rules.getTaxExemptions(customer).isEmpty());
        customer.addClassification(taxType);
        List<Lookup> exemptions = rules.getTaxExemptions(customer);
        assertEquals(1, exemptions.size());
        assertTrue(exemptions.contains(taxType));
    }

    /**
     * Tests the {@link CustomerTaxRules#getTaxExAmount(BigDecimal, Product, Party)} method.
     */
    @Test
    public void getTaxExAmount() {
        Party customer = createCustomer();
        Product product = createProduct();
        product.addClassification(taxType);

        BigDecimal amount1 = rules.getTaxExAmount(BigDecimal.TEN, product, customer);
        checkEquals(BigDecimal.TEN, amount1);

        customer.addClassification(taxType); // tax exemption

        BigDecimal amount2 = rules.getTaxExAmount(BigDecimal.TEN, product, customer);
        checkEquals(new BigDecimal("9.091"), amount2);
    }

    /**
     * Tests the {@link CustomerTaxRules#calculateTax(FinancialAct, Party)} method where the product has a 10% tax, but
     * the customer has a tax exemption.
     */
    @Test
    public void testCalculateTaxForCustomerTaxExemption() {
        Party customer = createCustomerWithTaxExemption();
        Product product = createProductWithTax();

        checkCalculateTax(customer, product, BigDecimal.ZERO);
    }

    /**
     * Tests the {@link CustomerTaxRules#getTaxRate(Product, Party)} method.
     */
    @Test
    public void testGetTaxRate() {
        Party customer = createCustomer();
        Product product = createProduct();

        // none of customer, product, or practice have tax classifications
        checkEquals(BigDecimal.ZERO, rules.getTaxRate(product, customer));

        // add a 10% tax to practice
        practice.addClassification(taxType);

        // need to refresh cache
        rules = new CustomerTaxRules(practice, getArchetypeService(), LookupServiceHelper.getLookupService());

        // product is now charged at 10% tax rate
        checkEquals(BigDecimal.TEN, rules.getTaxRate(product, customer));

        // make customer tax exempt
        customer.addClassification(taxType);
        checkEquals(BigDecimal.ZERO, rules.getTaxRate(product, customer));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        taxType = TestHelper.createTaxType(BigDecimal.TEN);
        practice = (Party) create("party.organisationPractice");
        rules = new CustomerTaxRules(practice, getArchetypeService(), LookupServiceHelper.getLookupService());
    }

    /**
     * Verifies that tax is calculated correctly by
     * {@link CustomerTaxRules#calculateTax(FinancialAct, Party)},
     * for an act with a total value of <code>1.00</code>.
     *
     * @param customer    the customer
     * @param product     the product
     * @param expectedTax the expected tax
     */
    private void checkCalculateTax(Party customer, Product product,
                                   BigDecimal expectedTax) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();

        ActBean bean = createAct("act.customerAccountInvoiceItem");
        bean.setValue("quantity", BigDecimal.ONE);
        bean.setValue("unitPrice", BigDecimal.ONE);
        bean.setValue("fixedPrice", BigDecimal.ZERO);
        bean.setParticipant("participation.product", product);
        FinancialAct act = (FinancialAct) bean.getAct();
        service.deriveValue(act, "total");

        rules.calculateTax(act, customer);

        BigDecimal tax = bean.getBigDecimal("tax");
        BigDecimal total = bean.getBigDecimal("total");
        assertTrue(expectedTax.compareTo(tax) == 0);
        assertTrue(BigDecimal.ONE.compareTo(total) == 0);
    }

    /**
     * Helper to create and save a customer.
     *
     * @return a new customer
     */
    private Party createCustomer() {
        Party customer = (Party) create("party.customerperson");
        assertNotNull(customer);
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
     * Helper to create and save a customer with a tax exemption.
     *
     * @return a new customer
     */
    private Party createCustomerWithTaxExemption() {
        Party customer = createCustomer();
        customer.addClassification(taxType);
        return customer;
    }

    /**
     * Helper to create a product.
     *
     * @return a new product
     */
    private Product createProduct() {
        Product product = (Product) create("product.medication");
        assertNotNull(product);
        IMObjectBean bean = new IMObjectBean(product);
        bean.setValue("name", "TaxRulesTestCase-product" + product.hashCode());
        bean.save();
        return product;
    }

    /**
     * Helper to create and save a product with a 10% tax type classification.
     *
     * @return a new product
     */
    private Product createProductWithTax() {
        Product product = createProduct();
        product.addClassification(taxType);
        save(product);
        return product;
    }

    /**
     * Helper to create and save a product with a product type relationship.
     * The associated <em>entity.productType</em> has a 10% tax type
     * classification.
     *
     * @return a new product
     */
    private Product createProductWithProductTypeTax() {
        Product product = createProduct();
        Entity type = (Entity) create("entity.productType");
        type.setName("TaxRulesTestCase-entity" + type.hashCode());
        type.addClassification(taxType);
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
     * Helper to create a new act, wrapped in a bean.
     *
     * @param shortName the act short name
     * @return a new act wrapped in a bean
     */
    private ActBean createAct(String shortName) {
        Act act = (Act) create(shortName);
        return new ActBean(act);
    }

}
