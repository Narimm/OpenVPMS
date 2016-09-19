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

package org.openvpms.web.component.im.edit.act;

import org.junit.Test;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.math.WeightUnits;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.product.ProductTestHelper.addInclude;
import static org.openvpms.archetype.rules.product.ProductTestHelper.addLocationExclusion;
import static org.openvpms.archetype.rules.product.ProductTestHelper.createTemplate;
import static org.openvpms.archetype.rules.product.ProductTestHelper.setStockQuantity;

/**
 * Tests the {@link ProductTemplateExpander}.
 *
 * @author Tim Anderson
 */
public class ProductTemplateExpanderTestCase extends AbstractAppTest {

    /**
     * Tests template expansion.
     */
    @Test
    public void testExpand() {
        Product productX = TestHelper.createProduct();
        Product productY = TestHelper.createProduct();
        Product productZ = TestHelper.createProduct();

        Product templateA = createTemplate("templateA");
        Product templateB = createTemplate("templateB");
        Product templateC = createTemplate("templateC");

        addInclude(templateA, templateB, 1, 2);
        addInclude(templateA, templateC, 2, 2);
        addInclude(templateB, productX, 5, 5);
        addInclude(templateB, productY, 2, 2);
        addInclude(templateC, productX, 1, 1);
        addInclude(templateC, productZ, 10, 10);

        Collection<TemplateProduct> includes1 = expand(templateA, Weight.ZERO, BigDecimal.ONE);
        assertEquals(3, includes1.size());

        checkInclude(includes1, productX, 7, 12, false, 0);
        checkInclude(includes1, productY, 2, 4, false, 1);
        checkInclude(includes1, productZ, 20, 20, false, 2);

        Collection<TemplateProduct> includes2 = expand(templateA, Weight.ZERO, BigDecimal.TEN);
        assertEquals(3, includes2.size());
        checkInclude(includes2, productX, 70, 120, false, 0);
        checkInclude(includes2, productY, 20, 40, false, 1);
        checkInclude(includes2, productZ, 200, 200, false, 2);
    }

    /**
     * Tests template expansion where products are included based on weight ranges.
     */
    @Test
    public void testExpandWeightRange() {
        Product productX = TestHelper.createProduct();
        Product productY = TestHelper.createProduct();
        Product productZ = TestHelper.createProduct();

        Product templateA = createTemplate("templateA");
        Product templateB = createTemplate("templateB");
        Product templateC = createTemplate("templateC");

        addInclude(templateA, templateB, 1, 1, 0, 2);
        addInclude(templateA, templateC, 2, 4, 2, 4);
        addInclude(templateB, productX, 1, 1, 0, 2);
        addInclude(templateB, productY, 1, 1, 2, 4);
        addInclude(templateC, productX, 1, 1, 0, 2);
        addInclude(templateC, productZ, 1, 1, 2, 4);

        Collection<TemplateProduct> includes = expand(templateA, Weight.ZERO, BigDecimal.ONE);
        assertEquals(0, includes.size());  // failed to expand as no weight specified

        includes = expand(templateA, new Weight(BigDecimal.ONE, WeightUnits.KILOGRAMS), BigDecimal.ONE);
        assertEquals(1, includes.size());
        checkInclude(includes, productX, 1, 1, false, 0);

        includes = expand(templateA, new Weight(BigDecimal.valueOf(2), WeightUnits.KILOGRAMS), BigDecimal.ONE);
        assertEquals(1, includes.size());
        checkInclude(includes, productZ, 2, 4, false, 0);

        includes = expand(templateA, new Weight(BigDecimal.valueOf(4), WeightUnits.KILOGRAMS), BigDecimal.ONE);
        assertEquals(0, includes.size()); // nothing in the weight range
    }

    /**
     * Verifies that no includes are returned if a template is included recursively.
     */
    @Test
    public void testRecursion() {
        Product productX = TestHelper.createProduct();

        Product templateA = createTemplate("templateA");
        Product templateB = createTemplate("templateB");
        Product templateC = createTemplate("templateC");

        addInclude(templateA, templateB, 1, 1);
        addInclude(templateA, productX, 1, 1);
        addInclude(templateB, templateC, 1, 1);
        addInclude(templateB, productX, 1, 1);
        addInclude(templateC, templateA, 1, 1);
        addInclude(templateC, productX, 1, 1);

        Collection<TemplateProduct> includes = expand(templateA, Weight.ZERO, BigDecimal.ONE);
        assertEquals(0, includes.size());
    }

    /**
     * Verifies that the zero price flag is inherited by included templates and products if it is set {@code true}.
     */
    @Test
    public void testZeroPrice() {
        Product productX = TestHelper.createProduct();
        Product productY = TestHelper.createProduct();
        Product productZ = TestHelper.createProduct();

        Product templateA = createTemplate("templateA");
        Product templateB = createTemplate("templateB");
        Product templateC = createTemplate("templateC");

        addInclude(templateA, templateB, 1, false);
        addInclude(templateA, templateC, 1, true);
        addInclude(templateA, productY, 1, true);

        addInclude(templateB, productX, 1, false);
        addInclude(templateB, productY, 1, false);

        addInclude(templateC, productY, 1, false);
        addInclude(templateC, productZ, 1, false);

        Collection<TemplateProduct> includes = expand(templateA, Weight.ZERO, BigDecimal.ONE);
        assertEquals(4, includes.size());

        checkInclude(includes, productX, 1, 1, false, 0); // included by template B
        checkInclude(includes, productY, 1, 1, false, 1); // included by template B
        checkInclude(includes, productY, 2, 2, true, 2);  // included by template A and template C
        checkInclude(includes, productZ, 1, 1, true, 3);  // included by template C
    }

    /**
     * Tests the behaviour of filtering products by location in conjunction with the skipIfMissing option.
     */
    @Test
    public void testUseLocationProducts() {
        Party locationA = TestHelper.createLocation();
        Party stockLocationA = ProductTestHelper.createStockLocation();
        Party locationB = TestHelper.createLocation();
        Party stockLocationB = ProductTestHelper.createStockLocation();

        // create 2 products not linked to either a stock location or location
        Product product1 = ProductTestHelper.createMedication();
        Product product2 = ProductTestHelper.createService();

        // create a template with 2 products with skipIfMissing set to true, and verify only the service is in the
        // expansion
        Product templateA = createTemplate("templateA");
        addInclude(templateA, product1, 1, 1, 0, 0, false, true);
        addInclude(templateA, product2, 1, 1, 0, 0, false, true);

        Collection<TemplateProduct> includes1 = expand(templateA, Weight.ZERO, BigDecimal.ONE, locationA,
                                                       stockLocationA);
        assertEquals(1, includes1.size());
        checkInclude(includes1, product2, 1, 1, false, 0);

        // create a template with 2 products with skipIfMissing set to false
        Product product3 = ProductTestHelper.createMerchandise();
        Product product4 = ProductTestHelper.createService();
        setStockQuantity(product3, stockLocationB, BigDecimal.ONE);
        addLocationExclusion(product4, locationA);

        Product templateB = createTemplate("templateB");
        addInclude(templateB, product3, 1, 1, 0, 0, false, false);
        addInclude(templateB, product4, 1, 1, 0, 0, false, false);

        // perform expansion for locationA and stockLocationA. It should fail as product3 is only available at
        // stockLocationB, and product4 is not available at locationA
        Collection<TemplateProduct> includes2 = expand(templateB, Weight.ZERO, BigDecimal.ONE, locationA,
                                                       stockLocationA);
        assertEquals(0, includes2.size());

        // now perform expansion for locationB and stockLocationB. Expansion should succeed
        Collection<TemplateProduct> includes3 = expand(templateB, Weight.ZERO, BigDecimal.ONE, locationB,
                                                       stockLocationB);
        checkInclude(includes3, product3, 1, 1, false, 0);
        checkInclude(includes3, product4, 1, 1, false, 1);

        // create a template with 3 products, 2 with skipIfMissing set to true.
        Product templateC = createTemplate("templateC");
        addInclude(templateC, product2, 1, 1, 0, 0, false, false);
        addInclude(templateC, product3, 1, 1, 0, 0, false, true);
        addInclude(templateC, product4, 1, 1, 0, 0, false, true);

        // perform expansion against locationA, stockLocationA. Only product2 should be included
        Collection<TemplateProduct> includes4 = expand(templateC, Weight.ZERO, BigDecimal.ONE, locationA,
                                                       stockLocationA);
        assertEquals(1, includes4.size());
        checkInclude(includes4, product2, 1, 1, false, 0);

        // perform expansion against locationB, stockLocationB. All 3 products should be included
        Collection<TemplateProduct> includes5 = expand(templateC, Weight.ZERO, BigDecimal.ONE, locationB,
                                                       stockLocationB);
        assertEquals(3, includes5.size());
        checkInclude(includes5, product2, 1, 1, false, 0);
        checkInclude(includes5, product3, 1, 1, false, 1);
        checkInclude(includes5, product4, 1, 1, false, 2);
    }

    /**
     * Verifies that inactive products aren't included.
     */
    @Test
    public void testInactiveInclude() {
        Product productX = TestHelper.createProduct();
        Product productY = TestHelper.createProduct();
        Product productZ = TestHelper.createProduct();

        Product templateA = createTemplate("templateA");
        Product templateB = createTemplate("templateB");
        Product templateC = createTemplate("templateC");

        addInclude(templateA, templateB, 1, 2);
        addInclude(templateA, templateC, 2, 2);
        addInclude(templateB, productX, 5, 5);
        addInclude(templateB, productY, 2, 2);
        addInclude(templateC, productX, 1, 1);
        addInclude(templateC, productZ, 10, 10);

        templateB.setActive(false);
        productZ.setActive(false);
        save(templateB, productZ);
        Collection<TemplateProduct> includes1 = expand(templateA, Weight.ZERO, BigDecimal.ONE);
        assertEquals(1, includes1.size());

        checkInclude(includes1, productX, 2, 2, false, 0);
    }

    /**
     * Expands a template.
     *
     * @param template the template to expand
     * @param weight   the patient weight
     * @param quantity the quantity
     * @return the expanded template
     */
    private Collection<TemplateProduct> expand(Product template, Weight weight, BigDecimal quantity) {
        ProductTemplateExpander expander = new ProductTemplateExpander();
        return expander.expand(template, weight, quantity, new SoftRefIMObjectCache(getArchetypeService()));
    }

    /**
     * Expands a template, restricting products to those available at a location/stock location.
     *
     * @param template      the template to expand
     * @param weight        the patient weight
     * @param quantity      the quantity
     * @param location      the practice location
     * @param stockLocation the stock location
     * @return the expanded template
     */
    private Collection<TemplateProduct> expand(Product template, Weight weight, BigDecimal quantity, Party location,
                                               Party stockLocation) {
        ProductTemplateExpander expander = new ProductTemplateExpander(true, location, stockLocation);
        return expander.expand(template, weight, quantity, new SoftRefIMObjectCache(getArchetypeService()));
    }

    /**
     * Verifies a product and quantity is included.
     *
     * @param includes    the includes
     * @param product     the expected product
     * @param lowQuantity the expected quantity
     * @param zeroPrice   the expected zero price indicator
     * @param index       the expected index
     */
    private void checkInclude(Collection<TemplateProduct> includes, Product product, int lowQuantity,
                              int highQuantity, boolean zeroPrice, int index) {
        assertTrue(index < includes.size());
        TemplateProduct include = new ArrayList<>(includes).get(index);
        assertEquals(product, include.getProduct());
        assertEquals(zeroPrice, include.getZeroPrice());
        checkEquals(BigDecimal.valueOf(lowQuantity), include.getLowQuantity());
        checkEquals(BigDecimal.valueOf(highQuantity), include.getHighQuantity());
    }

}
