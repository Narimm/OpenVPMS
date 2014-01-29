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

package org.openvpms.archetype.rules.product.io;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.addPriceTemplate;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createFixedPrice;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createPriceTemplate;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createUnitPrice;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Tests the {@link ProductUpdater} class.
 *
 * @author Tim Anderson
 */
public class ProductUpdaterTestCase extends AbstractProductIOTest {

    /**
     * The lookup service.
     */
    @Autowired
    private ILookupService lookups;

    /**
     * The product updater.
     */
    private ProductUpdater updater;

    /**
     * The practice.
     */
    private Party practice;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        ProductPriceRules rules = new ProductPriceRules(getArchetypeService(), lookups);
        updater = new ProductUpdater(rules, getArchetypeService());
        practice = (Party) create(PracticeArchetypes.PRACTICE);
    }

    /**
     * Verifies that new prices set the end date of old prices, if the old price has an unbounded date.
     */
    @Test
    public void testAddPriceForOpenPriorPrice() {
        ProductPrice fixed1 = createFixedPrice("1.0", "0.5", "100", "2013-01-01", null, true);
        ProductPrice unit1 = createUnitPrice("1.92", "1.2", "60", "2013-02-02", null);
        Product product = createProduct("P1", "Product 1", fixed1, unit1);

        ProductData data = createProduct(product, false);
        data.addPrice(createFixedPriceData(-1, "1.08", "0.6", "2013-04-01", null, true));
        data.addPrice(createUnitPriceData(-1, "2.55", "1.5", "2013-04-03", null));

        updater.update(product, data, practice);
        assertEquals(4, product.getProductPrices().size());

        assertEquals(getDate("2013-04-01"), fixed1.getToDate()); // price to date now the new price start date

        checkPrice(product, createFixedPrice("1.0", "0.5", "100", "2013-01-01", "2013-04-01", true)); // old fixed price
        checkPrice(product, createFixedPrice("1.08", "0.6", "80", "2013-04-01", null, true));         // new fixed price

        checkPrice(product, createUnitPrice("1.92", "1.2", "60", "2013-02-02", "2013-04-03"));       // old unit price
        checkPrice(product, createUnitPrice("2.55", "1.5", "70", "2013-04-03", null));               // new unit price
    }

    /**
     * Verifies that unit prices with an id update existing prices.
     */
    @Test
    public void testUpdateUnitPrices() {
        ProductPrice unit1 = createUnitPrice("1.0", "0.5", "100", "2013-01-01", "2013-03-31");
        Product product = createProduct("P1", "Product 1", unit1);

        ProductData data1 = createProduct(product, false);
        PriceData price = createUnitPriceData(unit1.getId(), "1.08", "0.6", "2013-04-01", null);
        data1.addPrice(price);

        updater.update(product, data1, practice);
        assertEquals(1, product.getProductPrices().size());

        checkPrice(product, createUnitPrice("1.08", "0.6", "80", "2013-04-01", null));
    }

    /**
     * Verifies that non-overlapping unit prices can be added historically, but that overlapping prices throw an
     * exception.
     */
    @Test
    public void testAddHistoricalUnitPrice() {
        ProductPrice unit1 = createUnitPrice("1.08", "0.6", "60", "2013-04-01", null);
        Product product = createProduct("P1", "Product 1", unit1);

        // verify that a unit price can be added that is dated prior to unit1
        ProductData data1 = createProduct(product, false);
        data1.addPrice(createUnitPriceData(-1, "1.0", "0.5", "2013-01-01", "2013-03-31")); // unit0. Prior to unit1.

        updater.update(product, data1, practice);
        assertEquals(2, product.getProductPrices().size());

        checkPrice(product, createUnitPrice("1.0", "0.5", "100", "2013-01-01", "2013-03-31")); // unit0
        checkPrice(product, createUnitPrice("1.08", "0.6", "60", "2013-04-01", null));         // unit1

        save(product);

        // verify that a unit price can't be added that is dated prior to the existing prices, and overlaps them all
        ProductData data2 = createProduct(product, false);
        data2.addPrice(createUnitPriceData(-1, "1.92", "1.2", "2013-01-02", null)); // overlaps unit1, unit0
        try {
            updater.update(product, data2, practice);
            fail("Expected ProductIOException");
        } catch (ProductIOException expected) {
            assertEquals("Unit price dates overlap an existing unit price", expected.getMessage());
        }

        // verify that a unit price can't be added that is dated prior to the existing prices, and overlaps the first
        ProductData data3 = createProduct(product, false);
        data3.addPrice(createUnitPriceData(-1, "1.92", "1.2", "2012-12-31", "2013-01-02")); // overlaps unit0
        try {
            updater.update(product, data3, practice);
            fail("Expected ProductIOException");
        } catch (ProductIOException expected) {
            assertEquals("Unit price dates overlap an existing unit price", expected.getMessage());
        }
    }

    /**
     * Verifies that fixed prices can be added historically, and can overlap existing fixed prices.
     */
    @Test
    public void testAddHistoricalFixedPrice() {
        ProductPrice fixed1 = createFixedPrice("1.08", "0.6", "60", "2013-04-01", null, true);
        Product product = createProduct("P1", "Product 1", fixed1);

        // verify that a unit fixed can be added that is dated prior to fixed1
        ProductData data1 = createProduct(product, false);
        data1.addPrice(createFixedPriceData(-1, "1.0", "0.5", "2013-01-01", "2013-03-31", true));
        // fixed0. Prior to fixed1.

        updater.update(product, data1, practice);
        assertEquals(2, product.getProductPrices().size());

        checkPrice(product, createFixedPrice("1.0", "0.5", "100", "2013-01-01", "2013-03-31", true)); // fixed0
        checkPrice(product, createFixedPrice("1.08", "0.6", "60", "2013-04-01", null, true));         // fixed1

        save(product);

        // add a price that overlaps both existing prices. This shouldn't change any dates.
        ProductData data2 = createProduct(product, false);
        data2.addPrice(createFixedPriceData(-1, "1.92", "1.2", "2013-01-02", null, true)); // overlaps fixed0, fixed1
        updater.update(product, data2, practice);

        checkPrice(product, createFixedPrice("1.0", "0.5", "100", "2013-01-01", "2013-03-31", true)); // fixed0
        checkPrice(product, createFixedPrice("1.08", "0.6", "60", "2013-04-01", null, true));         // fixed1
        checkPrice(product, createFixedPrice("1.92", "1.2", "60", "2013-01-02", null, true));         // fixed2
    }

    /**
     * Verifies that duplicate prices are ignored.
     */
    @Test
    public void testDuplicatePrice() {
        ProductPrice fixed1 = createFixedPrice("1.0", "0.5", "100", "2013-01-01", null, true);
        ProductPrice unit1 = createUnitPrice("1.92", "1.2", "60", "2013-02-02", null);
        Product product = createProduct("P1", "Product 1", fixed1, unit1);

        ProductData data = createProduct(product, false);
        PriceData fixed2 = createFixedPriceData(-1, "1.08", "0.6", "2013-04-02", null, true);
        data.addPrice(fixed2);
        data.addPrice(fixed2);
        PriceData unit2 = createUnitPriceData(-1, "2.55", "1.5", "2013-04-01", null);
        data.addPrice(unit2);
        data.addPrice(unit2);

        updater.update(product, data, practice);
        assertEquals(4, product.getProductPrices().size());
    }

    /**
     * Verifies that non-existent prices throw an exception.
     */
    @Test
    public void testPriceNotFound() {
        Product product = createProduct("P1", "Product 1");
        ProductData data = createProduct(product);
        data.addPrice(createFixedPriceData(23, "1.0", "0.5", "2013-01-01", null, true));

        try {
            updater.update(product, data, practice);
            fail("Expected ProductIOException to be thrown");
        } catch (ProductIOException expected) {
            assertEquals("Price with identifier 23 not found", expected.getMessage());
        }
    }

    /**
     * Verifies that linked prices are not updated.
     * <p/>
     * If the linked price is different, an exception will be raised.
     */
    @Test
    public void testLinkedPrice() {
        Product product = createProduct("P1", "Product 1");
        ProductPrice fixedPrice = createFixedPrice("2013-01-01", null, true);
        Product template = createPriceTemplate(fixedPrice);
        addPriceTemplate(product, template, "2013-01-01", null);

        assertEquals(0, product.getProductPrices().size());

        ProductData data = createProduct(product);
        assertEquals(1, data.getFixedPrices().size()); // pulls in the linked price

        // update the product, and verify the linked price hasn't changed
        updater.update(product, data, practice);
        assertEquals(0, product.getProductPrices().size());

        data.getFixedPrices().get(0).setPrice(BigDecimal.TEN);
        try {
            updater.update(product, data, practice);
            fail("Expected ProductIOException to be thrown");
        } catch (ProductIOException expected) {
            assertEquals("Cannot update linked price", expected.getMessage());
        }
    }

    /**
     * Verifies that the printed name can be updated.
     */
    @Test
    public void testUpdatePrintedName() {
        Product product = createProduct("P1", "Product 1");
        ProductData data = createProduct(product);
        data.setPrintedName("New Product 1");
        updater.update(product, data, practice);
        IMObjectBean bean = new IMObjectBean(product);

        assertEquals("New Product 1", bean.getString("printedName"));
    }

    /**
     * Checks the behaviour of a new unit price duplicating an existing unit price. In this case, the new price should
     * be ignored.
     */
    @Test
    public void testDuplicateExistingUnitPrice() {
        ProductPrice unit1 = createUnitPrice("1.08", "0.6", "60", "2013-02-02", "2013-04-01");
        ProductPrice unit2 = createUnitPrice("1.92", "1.2", "60", "2013-04-02", null);
        Product product = createProduct("P1", "Product 1", unit1, unit2);

        ProductData data = createProduct(product, true);
        PriceData unit3 = createUnitPriceData(-1, "1.92", "1.2", "2013-04-02", null); // duplicates unit2
        data.addPrice(unit3);

        updater.update(product, data, practice);
        assertEquals(2, product.getProductPrices().size());

        checkPrice(product, createUnitPrice("1.08", "0.6", "60", "2013-02-02", "2013-04-01"));
        checkPrice(product, createUnitPrice("1.92", "1.2", "60", "2013-04-02", null));
    }

    /**
     * Checks the behaviour of a new fixed price duplicating an existing fixed price. In this case, the new price should
     * be ignored.
     */
    @Test
    public void testDuplicateExistingFixedPrice() {
        ProductPrice fixed1 = createFixedPrice("1.08", "0.6", "60", "2013-02-02", "2013-04-01", true);
        ProductPrice fixed2 = createFixedPrice("1.92", "1.2", "60", "2013-04-02", null, true);
        Product product = createProduct("P1", "Product 1", fixed1, fixed2);

        ProductData data = createProduct(product, true);
        PriceData fixed3 = createFixedPriceData(-1, "1.92", "1.2", "2013-04-02", null, true); // duplicates fixed2
        data.addPrice(fixed3);

        updater.update(product, data, practice);
        assertEquals(2, product.getProductPrices().size());

        checkPrice(product, createFixedPrice("1.08", "0.6", "60", "2013-02-02", "2013-04-01", true));
        checkPrice(product, createFixedPrice("1.92", "1.2", "60", "2013-04-02", null, true));
    }

    /**
     * Verifies that the 'default' flag for fixed prices can be updated.
     */
    @Test
    public void testUpdateDefaultFixedPrice() {
        ProductPrice fixed1 = createFixedPrice("1.08", "0.6", "80", "2013-02-02", "2013-04-01", true);
        ProductPrice fixed2 = createFixedPrice("1.92", "1.2", "60", "2013-04-02", null, true);
        Product product = createProduct("P1", "Product 1", fixed1, fixed2);

        ProductData data = createProduct(product, true);
        data.getFixedPrices().get(0).setDefault(false);
        data.getFixedPrices().get(1).setDefault(false);

        updater.update(product, data, practice);
        assertEquals(2, product.getProductPrices().size());

        checkPrice(product, createFixedPrice("1.08", "0.6", "80", "2013-02-02", "2013-04-01", false));
        checkPrice(product, createFixedPrice("1.92", "1.2", "60", "2013-04-02", null, false));
    }


    private PriceData createFixedPriceData(long id, String price, String cost, String from, String to,
                                           boolean isDefault) {
        return createPriceData(id, ProductArchetypes.FIXED_PRICE, price, cost, from, to, isDefault);
    }

    private PriceData createUnitPriceData(long id, String price, String cost, String from, String to) {
        return createPriceData(id, ProductArchetypes.UNIT_PRICE, price, cost, from, to, false);
    }

    private PriceData createPriceData(long id, String shortName, String price, String cost, String from, String to,
                                      boolean isDefault) {
        return new PriceData(id, shortName, new BigDecimal(price), new BigDecimal(cost), getDate(from), getDate(to),
                             isDefault, 1);
    }
}
