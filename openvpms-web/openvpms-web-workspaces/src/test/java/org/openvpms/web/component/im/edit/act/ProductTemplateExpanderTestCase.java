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

import org.apache.commons.lang.ObjectUtils;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.web.test.AbstractAppTest;

import java.math.BigDecimal;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.product.ProductTestHelper.addInclude;
import static org.openvpms.archetype.rules.product.ProductTestHelper.createTemplate;

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

        Collection<TemplateProduct> includes = expand(templateA, BigDecimal.ZERO);
        assertEquals(3, includes.size());

        checkInclude(includes, productX, 7, 12, false);
        checkInclude(includes, productY, 2, 4, false);
        checkInclude(includes, productZ, 20, 20, false);
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

        Collection<TemplateProduct> includes = expand(templateA, BigDecimal.ZERO);
        assertEquals(0, includes.size());  // failed to expand as no weight specified

        includes = expand(templateA, BigDecimal.ONE);
        assertEquals(1, includes.size());
        checkInclude(includes, productX, 1, 1, false);

        includes = expand(templateA, BigDecimal.valueOf(2));
        assertEquals(1, includes.size());
        checkInclude(includes, productZ, 2, 4, false);

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

        Collection<TemplateProduct> includes = expand(templateA, BigDecimal.ZERO);
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

        Collection<TemplateProduct> includes = expand(templateA, BigDecimal.ZERO);
        assertEquals(4, includes.size());

        checkInclude(includes, productY, 2, 2, true);  // included by template A and template C
        checkInclude(includes, productX, 1, 1, false); // included by template B
        checkInclude(includes, productY, 1, 1, false); // included by template B
        checkInclude(includes, productZ, 1, 1, true);  // included by template C
    }

    /**
     * Expands a template.
     *
     * @param template the template to expand
     * @param weight   the patient weigtht. May be {@code null}
     * @return the expanded template
     */
    private Collection<TemplateProduct> expand(Product template, BigDecimal weight) {
        ProductTemplateExpander expander = new ProductTemplateExpander();
        return expander.expand(template, weight, new SoftRefIMObjectCache(getArchetypeService()));
    }

    /**
     * Verifies a product and quantity is included.
     *
     * @param includes    the includes
     * @param product     the expected product
     * @param lowQuantity the expected quantity
     * @param zeroPrice   the expected zero price indicator
     */
    private void checkInclude(Collection<TemplateProduct> includes, Product product, int lowQuantity,
                              int highQuantity, boolean zeroPrice) {
        for (TemplateProduct include : includes) {
            if (ObjectUtils.equals(product, include.getProduct()) && zeroPrice == include.getZeroPrice()) {
                checkEquals(BigDecimal.valueOf(lowQuantity), include.getLowQuantity());
                checkEquals(BigDecimal.valueOf(highQuantity), include.getHighQuantity());
                return;
            }
        }
        fail("TemplateProduct not found");
    }


}
