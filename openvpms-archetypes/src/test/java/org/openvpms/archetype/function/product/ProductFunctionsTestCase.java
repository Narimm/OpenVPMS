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

package org.openvpms.archetype.function.product;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.math.BigDecimal;

/**
 * Tests the {@link ProductFunctions}.
 *
 * @author Tim Anderson
 */
public class ProductFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * The practice service.
     */
    private PracticeService practiceService;

    /**
     * The practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * The functions.
     */
    private ProductFunctions functions;

    /**
     * Thread pool required by the {@link PracticeService}.
     */
    private ThreadPoolTaskExecutor executor;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Party practice = TestHelper.getPractice(BigDecimal.TEN); // 10% practice wide tax rate
        IMObjectBean bean = new IMObjectBean(practice);
        bean.setValue("showPricesTaxInclusive", true);
        bean.save();

        ProductPriceRules rules = new ProductPriceRules(getArchetypeService());
        executor = new ThreadPoolTaskExecutor();
        executor.setThreadGroupName("PracticeService");
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.afterPropertiesSet();
        practiceService = new PracticeService(getArchetypeService(), practiceRules, executor);
        functions = new ProductFunctions(rules, practiceService, getArchetypeService());
    }

    /**
     * Cleans up after the test.
     */
    @After
    public void tearDown() {
        practiceService.dispose();
        executor.destroy();
    }

    /**
     * Tests the {@link ProductFunctions#priceById(long, BigDecimal)},
     * {@link ProductFunctions#priceById(long, BigDecimal, boolean)} and {@link ProductFunctions#price} methods.
     */
    @Test
    public void testPrice() {
        Product product = TestHelper.createProduct();
        BigDecimal eleven = new BigDecimal("11");

        // practice determines tax inc/ex
        checkValue(eleven, new Object(), "product:price(" + product.getId() + ", 10)");

        // ex-tax
        checkValue(BigDecimal.TEN, new Object(), "product:price(" + product.getId() + ", 10, false())");

        // inc-tax
        checkValue(eleven, new Object(), "product:price(" + product.getId() + ", 10, true())");

        // practice determines tax inc/ex
        checkValue(eleven, product, "product:price(., 10)");
        checkValue(BigDecimal.TEN, product, "product:price(., 10, false())");
        checkValue(eleven, product, "product:price(., 10, true())");
    }

    /**
     * Tests the {@link ProductFunctions#round(BigDecimal)} method.
     */
    @Test
    public void testRound() {
        checkValue(new BigDecimal("10.25"), new Object(), "product:round(10.253)");
        checkValue(new BigDecimal("10.25"), new Object(), "product:round(10.245)");
    }

    /**
     * Tests the {@link ProductFunctions#taxRate(Product)} and {@link ProductFunctions#taxRateById(long)} methods.
     */
    @Test
    public void testTaxRate() {
        Product product = TestHelper.createProduct();
        checkValue(BigDecimal.TEN, new Object(), "product:taxRate(" + product.getId() + ")");
        checkValue(BigDecimal.TEN, product, "product:taxRate(.)");
    }

    /**
     * Verifies an expression returns the expected value.
     *
     * @param expected   the expected value
     * @param context    the context to evaluate the expression against
     * @param expression the expression to evaluatre
     */
    private void checkValue(BigDecimal expected, Object context, String expression) {
        JXPathContext ctx = JXPathHelper.newContext(context, functions);
        BigDecimal price = (BigDecimal) ctx.getValue(expression);
        checkEquals(expected, price);
    }
}
