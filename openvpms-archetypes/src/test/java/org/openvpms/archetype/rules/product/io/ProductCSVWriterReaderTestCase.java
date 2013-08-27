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

package org.openvpms.archetype.rules.product.io;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createFixedPrice;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createUnitPrice;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Tests the {@link ProductCSVWriter} and {@link ProductReader} classes.
 *
 * @author Tim Anderson
 */
public class ProductCSVWriterReaderTestCase extends AbstractProductIOTest {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookups;

    /**
     * The product price rules.
     */
    private ProductPriceRules rules;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The first fixed price.
     */
    private ProductPrice fixed1;

    /**
     * The second fixed price.
     */
    private ProductPrice fixed2;

    /**
     * The first unit price.
     */
    private ProductPrice unit1;

    /**
     * The second unit price.
     */
    private ProductPrice unit2;

    /**
     * The product.
     */
    private Product product;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new ProductPriceRules(getArchetypeService(), lookups);
        handlers = new DocumentHandlers();

        product = createProduct("Product A", "A");
        fixed1 = createFixedPrice("1.0", "0.5", "100", "2013-02-01", "2013-04-01", false);
        fixed2 = createFixedPrice("1.08", "0.6", "80", "2013-04-02", "2013-06-01", true);
        unit1 = createUnitPrice("1.92", "1.2", "60", "2013-02-02", "2013-04-02");
        unit2 = createUnitPrice("2.55", "1.5", "70", "2013-04-03", "2013-06-02");
        product.addProductPrice(fixed1);
        product.addProductPrice(fixed2);
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);
    }

    /**
     * Tests writing the latest prices, and reading them back again.
     */
    @Test
    public void testWriteReadLatestPrices() {
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), true);

        ProductCSVReader reader = new ProductCSVReader(handlers);
        List<ProductData> products = reader.read(document);
        assertEquals(1, products.size());

        ProductData data = products.get(0);
        checkProduct(data, product);
        assertEquals(1, data.getFixedPrices().size());
        checkPrice(data.getFixedPrices().get(0), fixed2);

        assertEquals(1, data.getUnitPrices().size());
        checkPrice(data.getUnitPrices().get(0), unit2);
    }

    /**
     * Tests writing all prices, and reading them back again.
     */
    @Test
    public void testWriteReadAllPrices() {
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, handlers);
        Document document = writer.write(Arrays.asList(product).iterator(), false);

        ProductCSVReader reader = new ProductCSVReader(handlers);
        List<ProductData> products = reader.read(document);
        assertEquals(1, products.size());

        ProductData data = products.get(0);
        checkProduct(data, product);
        assertEquals(2, data.getFixedPrices().size());
        checkPrice(data.getFixedPrices().get(0), fixed2);
        checkPrice(data.getFixedPrices().get(1), fixed1);

        assertEquals(2, data.getUnitPrices().size());
        checkPrice(data.getUnitPrices().get(0), unit2);
        checkPrice(data.getUnitPrices().get(1), unit1);
    }

    /**
     * Tests writing prices matching a date range, and reading them back again.
     */
    @Test
    public void testWriteReadRangePrices() {
        ProductCSVWriter writer = new ProductCSVWriter(getArchetypeService(), rules, handlers);
        Date from = getDate("2013-02-01");
        Date to = getDate("2013-03-01");
        Document document = writer.write(Arrays.asList(product).iterator(), from, to);

        ProductCSVReader reader = new ProductCSVReader(handlers);
        List<ProductData> products = reader.read(document);
        assertEquals(1, products.size());

        ProductData data = products.get(0);
        checkProduct(data, product);
        assertEquals(1, data.getFixedPrices().size());
        checkPrice(data.getFixedPrices().get(0), fixed1);

        assertEquals(1, data.getUnitPrices().size());
        checkPrice(data.getUnitPrices().get(0), unit1);
    }

    /**
     * Verifies a product matches that expected.
     *
     * @param data     the product data
     * @param expected the expected product
     */
    private void checkProduct(ProductData data, Product expected) {
        IMObjectBean bean = new IMObjectBean(expected);
        assertEquals(expected.getId(), data.getId());
        assertEquals(expected.getName(), data.getName());
        assertEquals(bean.getString("printedName"), data.getPrintedName());
    }

    /**
     * Verifies a price matches that expected.
     *
     * @param data     the price data
     * @param expected the expected price
     */
    private void checkPrice(PriceData data, ProductPrice expected) {
        IMObjectBean bean = new IMObjectBean(expected);
        assertEquals(expected.getPrice(), data.getPrice());
        assertEquals(bean.getBigDecimal("cost"), data.getCost());
        assertEquals(expected.getFromDate(), data.getFrom());
        assertEquals(expected.getToDate(), data.getTo());
    }

}
