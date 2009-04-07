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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.finance.tax;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.rules.party.ContactArchetypes;
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

import java.math.BigDecimal;
import java.util.Random;


/**
 * Tests the {@link CustomerTaxRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Tests the {@link CustomerTaxRules#calculateTax(FinancialAct, Party)}
     * method when the customer and product don't have any associated taxes.
     */
    public void testCalculateTaxForNoTaxes() {
        Party customer = createCustomer();
        Product product = createProduct();
        checkCalculateTax(customer, product, BigDecimal.ZERO);
    }

    /**
     * Tests the {@link CustomerTaxRules#calculateTax(FinancialAct, Party)}
     * method where the product has an associated tax.
     */
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
    public void testCalculateTaxForProductTypeTax() {
        Party customer = createCustomer();
        Product product = createProductWithProductTypeTax();

        checkCalculateTax(customer, product, new BigDecimal("0.091"));
    }

    /**
     * Tests the {@link CustomerTaxRules#calculateTax(FinancialAct, Party)}
     * method where the product has a 10% tax, but the customer has a tax
     * exemption.
     */
    public void testCalculateTaxForCustomerTaxExemption() {
        Party customer = createCustomerWithTaxExemption();
        Product product = createProductWithTax();

        checkCalculateTax(customer, product, BigDecimal.ZERO);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        taxType = createTaxType();
        Party practice = (Party) create("party.organisationPractice");

        rules = new CustomerTaxRules(practice);
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

    /**
     * Helper to create and save a new tax type classification.
     *
     * @return a new tax classification
     */
    private Lookup createTaxType() {
        Lookup tax = (Lookup) create("lookup.taxType");
        IMObjectBean bean = new IMObjectBean(tax);
        bean.setValue("code", "XTAXRULESTESTCASE_CLASSIFICATION_"
                + Math.abs(new Random().nextInt()));
        bean.setValue("rate", new BigDecimal(10));
        save(tax);
        return tax;
    }

}
