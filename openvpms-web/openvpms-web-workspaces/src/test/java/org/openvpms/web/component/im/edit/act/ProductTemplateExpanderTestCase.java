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

package org.openvpms.web.component.im.edit.act;

import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

        Map<Product, Quantity> includes = expand(templateA, BigDecimal.ZERO);
        assertEquals(3, includes.size());

        checkInclude(includes, productX, 7, 12);
        checkInclude(includes, productY, 2, 4);
        checkInclude(includes, productZ, 20, 20);
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

        Map<Product, Quantity> includes = expand(templateA, BigDecimal.ZERO);
        assertEquals(0, includes.size());  // failed to expand as no weight specified

        includes = expand(templateA, BigDecimal.ONE);
        assertEquals(1, includes.size());
        checkInclude(includes, productX, 1, 1);

        includes = expand(templateA, BigDecimal.valueOf(2));
        assertEquals(1, includes.size());
        checkInclude(includes, productZ, 2, 4);

        includes = expand(templateA, BigDecimal.valueOf(4));
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

        Map<Product, Quantity> includes = expand(templateA, BigDecimal.ZERO);
        assertEquals(0, includes.size());
    }

    /**
     * Expands a template.
     *
     * @param template the template to expand
     * @param weight   the patient weigtht. May be {@code null}
     * @return the expanded template
     */
    private Map<Product, Quantity> expand(Product template, BigDecimal weight) {
        ProductTemplateExpander expander = new ProductTemplateExpander();
        return expander.expand(template, weight, new SoftRefIMObjectCache(getArchetypeService()));
    }

    /**
     * Verifies a product and quantity is included.
     *
     * @param includes    the includes
     * @param product     the expected product
     * @param lowQuantity the expected quantity
     */
    private void checkInclude(Map<Product, Quantity> includes, Product product, int lowQuantity, int highQuantity) {
        Quantity quantity = includes.get(product);
        assertNotNull(quantity);
        checkEquals(BigDecimal.valueOf(lowQuantity), quantity.getLowQuantity());
        checkEquals(BigDecimal.valueOf(highQuantity), quantity.getHighQuantity());
    }

    /**
     * Creates a template.
     *
     * @param name the template name
     * @return a new template
     */
    private Product createTemplate(String name) {
        Product template = (Product) create(ProductArchetypes.TEMPLATE);
        template.setName(name);
        save(template);
        return template;
    }

    /**
     * Adds an include to the template with no weight restrictions.
     *
     * @param template     the template
     * @param include      the product to include
     * @param lowQuantity  the low quantity
     * @param highQuantity the high quantity
     */
    private void addInclude(Product template, Product include, int lowQuantity, int highQuantity) {
        addInclude(template, include, lowQuantity, highQuantity, 0, 0);
    }

    /**
     * Adds an include to the template.
     *
     * @param template    the template
     * @param include     the product to include
     * @param lowQuantity the include quantity
     * @param minWeight   the minimum weight
     * @param maxWeight   the maximum weight
     */
    private void addInclude(Product template, Product include, int lowQuantity, int highQuantity, int minWeight,
                            int maxWeight) {
        EntityBean bean = new EntityBean(template);
        IMObjectRelationship relationship = bean.addNodeTarget("includes", include);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("lowQuantity", lowQuantity);
        relBean.setValue("highQuantity", highQuantity);
        relBean.setValue("minWeight", minWeight);
        relBean.setValue("maxWeight", maxWeight);
        bean.save();
    }

}
