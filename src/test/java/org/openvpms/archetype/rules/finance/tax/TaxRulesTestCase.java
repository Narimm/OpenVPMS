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

package org.openvpms.archetype.rules.finance.tax;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Random;


/**
 * Tests the {@link CustomerTaxRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Tests the {@link TaxRules#calculateTax(BigDecimal, Product)} 
     */

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
        rules = new TaxRules(practice,
                             ArchetypeServiceHelper.getArchetypeService());
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
