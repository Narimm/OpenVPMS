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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.tax;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link TaxRules} class.
 *
 * @author Tim Anderson
 */
public class TaxRulesTestCase extends ArchetypeServiceTest {

    /**
     * The tax type classification.
     */
    private Lookup taxType;

    /**
     * The tax rules.
     */
    private TaxRules rules;


    /**
     * Tests the {@link TaxRules#getTaxRate(Product)} method.
     */
    @Test
    public void testGetTaxRate() {
        Product productNoTax = createProduct();
        BigDecimal noTax = rules.getTaxRate(productNoTax);
        assertTrue(BigDecimal.ZERO.compareTo(noTax) == 0);

        Product product10Tax = createProductWithTax();
        BigDecimal percent10 = new BigDecimal(10);
        assertTrue(percent10.compareTo(rules.getTaxRate(product10Tax)) == 0);

        Product productType10Tax = createProductWithProductTypeTax();
        assertTrue(percent10.compareTo(
                rules.getTaxRate(productType10Tax)) == 0);
    }

    /**
     * Tests the {@link TaxRules#calculateTax(BigDecimal, Product, boolean)}
     * method.
     */
    @Test
    public void testCalculateTax() {
        Product productNoTax = createProduct();

        BigDecimal ten = new BigDecimal(10);
        checkEquals(BigDecimal.ZERO, rules.calculateTax(ten, productNoTax, false));
        checkEquals(BigDecimal.ZERO, rules.calculateTax(ten, productNoTax, true));

        Product product10Tax = createProductWithTax();
        checkEquals(BigDecimal.ONE, rules.calculateTax(ten, product10Tax, false));
        checkEquals(new BigDecimal("0.909"), rules.calculateTax(ten, product10Tax, true));

        Product productType10Tax = createProductWithProductTypeTax();
        checkEquals(BigDecimal.ONE, rules.calculateTax(ten, productType10Tax, false));
        checkEquals(new BigDecimal("0.909"), rules.calculateTax(ten, productType10Tax, true));
    }

    /**
     * Verifies that if a product has a product type relationship, but with no product type, a zero tax rate
     * is returned.
     * <p/>
     * This tests the fix for <em>OVPMS-946 NullPointerException on supplier change, when editing
     * products</em>.
     */
    @Test
    public void testGetTaxRateForInvalidProductType() {
        Product productNoTax = createProduct();
        Entity productType = (Entity) create("entity.productType");
        EntityRelationship relationship = (EntityRelationship) create("entityRelationship.productTypeProduct");
        relationship.setSource(productType.getObjectReference());
        relationship.setTarget(productNoTax.getObjectReference());
        productNoTax.addEntityRelationship(relationship);
        relationship.setSource(null);
        BigDecimal rate = rules.getTaxRate(productNoTax);
        checkEquals(BigDecimal.ZERO, rate);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        taxType = TestHelper.createTaxType(BigDecimal.TEN);
        Party practice = (Party) create("party.organisationPractice");
        rules = new TaxRules(practice);
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
        Product product = TestHelper.createProduct();
        Entity type = (Entity) create("entity.productType");
        type.setName("TaxRulesTestCase-entity" + type.hashCode());
        type.addClassification(taxType);
        EntityBean bean = new EntityBean(type);
        bean.addRelationship("entityRelationship.productTypeProduct", product);
        save(product, type);
        return product;
    }

}
