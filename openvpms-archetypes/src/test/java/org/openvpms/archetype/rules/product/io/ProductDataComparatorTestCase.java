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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product.io;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createFixedPrice;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createUnitPrice;
import static org.openvpms.archetype.test.TestHelper.getDate;

/**
 * Tests the {@link ProductDataComparator}.
 *
 * @author Tim Anderson
 */
public class ProductDataComparatorTestCase extends AbstractProductIOTest {

    /**
     * The test product.
     */
    private Product product;

    /**
     * The product fixed price.
     */
    private ProductPrice fixed1;

    /**
     * The product unit price.
     */
    private ProductPrice unit1;

    /**
     * The comparator.
     */
    private ProductDataComparator comparator;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        ProductPriceRules rules = new ProductPriceRules(getArchetypeService());
        comparator = new ProductDataComparator(rules, getArchetypeService());

        product = createProduct("Product 1", "P1");
        fixed1 = createFixedPrice("1.0", "0.5", "100", "10", "2013-02-01", "2013-04-01", true);
        unit1 = createUnitPrice("1.92", "1.2", "60", "10", "2013-02-02", "2013-04-02");
        product.addProductPrice(fixed1);
        product.addProductPrice(unit1);
        save(product);
    }

    /**
     * Verifies that {@link ProductDataComparator#compare(Product, ProductData)} returns {@code null} if there are no
     * changes.
     */
    @Test
    public void testCompareNoChanges() {
        ProductData data = createProduct(product);
        assertNull(comparator.compare(product, data));
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to the printed name.
     */
    @Test
    public void testComparePrintedNameChange() {
        ProductData data = createProduct(product);
        data.setPrintedName("Foo");
        ProductData changed = comparator.compare(product, data);
        assertNotNull(changed);

        assertEquals(data.getName(), changed.getName());
        assertEquals(product.getObjectReference(), changed.getReference());
        assertEquals("Foo", changed.getPrintedName());
        assertEquals(0, changed.getFixedPrices().size());
        assertEquals(0, changed.getUnitPrices().size());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a fixed price's cost.
     */
    @Test
    public void testFixedPriceCostChange() {
        ProductData data = createProduct(product);
        data.getFixedPrices().get(0).setCost(BigDecimal.ONE);

        PriceData fixedPrice = checkFixedPriceChange(data);
        assertEquals(BigDecimal.ONE, fixedPrice.getCost());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a fixed price's price.
     */
    @Test
    public void testFixedPriceChange() {
        ProductData data = createProduct(product);
        data.getFixedPrices().get(0).setPrice(BigDecimal.TEN);

        PriceData fixedPrice = checkFixedPriceChange(data);
        assertEquals(BigDecimal.TEN, fixedPrice.getPrice());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a fixed price's from date.
     */
    @Test
    public void testFixedPriceFromDateChange() {
        ProductData data = createProduct(product);
        Date date = getDate("2013-03-01");
        data.getFixedPrices().get(0).setFrom(date);

        PriceData fixedPrice = checkFixedPriceChange(data);
        assertEquals(date, fixedPrice.getFrom());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a fixed price's to date.
     */
    @Test
    public void testFixedPriceToDateChange() {
        ProductData data = createProduct(product);
        Date date = getDate("2013-12-01");
        data.getFixedPrices().get(0).setTo(date);

        PriceData fixedPrice = checkFixedPriceChange(data);
        assertEquals(date, fixedPrice.getTo());
    }

    /**
     * Verifies that overlapping fixed prices can be added.
     */
    @Test
    public void testFixedPriceOverlap() {
        checkFixedPriceOverlap("2012-01-01", null);          // starts before the existing fixed price
        checkFixedPriceOverlap("2012-01-01", "2013-02-02");  // overlaps the start
        checkFixedPriceOverlap("2013-02-03", "2013-02-04");  // intersects
        checkFixedPriceOverlap("2013-03-31", null);          // overlaps the end
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a unit price's cost.
     */
    @Test
    public void testUnitPriceCostChange() {
        ProductData data = createProduct(product);
        data.getUnitPrices().get(0).setCost(BigDecimal.ONE);

        PriceData unitPrice = checkUnitPriceChange(data);
        assertEquals(BigDecimal.ONE, unitPrice.getCost());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a unit price's price.
     */
    @Test
    public void testUnitPriceChange() {
        ProductData data = createProduct(product);
        data.getUnitPrices().get(0).setPrice(BigDecimal.TEN);

        PriceData unitPrice = checkUnitPriceChange(data);
        assertEquals(BigDecimal.TEN, unitPrice.getPrice());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a unit price's from date.
     */
    @Test
    public void testUnitPriceFromDateChange() {
        ProductData data = createProduct(product);
        Date date = getDate("2013-03-01");
        data.getUnitPrices().get(0).setFrom(date);

        PriceData unitPrice = checkUnitPriceChange(data);
        assertEquals(date, unitPrice.getFrom());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to a unit price's to date.
     */
    @Test
    public void testUnitPriceToDateChange() {
        ProductData data = createProduct(product);
        Date date = getDate("2013-12-01");
        data.getUnitPrices().get(0).setTo(date);

        PriceData unitPrice = checkUnitPriceChange(data);
        assertEquals(date, unitPrice.getTo());
    }

    /**
     * Verifies that adding a unit price that overlaps an existing one throws an exception.
     */
    @Test
    public void testAddOverlappingUnitPrice() {
        checkUnitPriceOverlap("2012-01-01", null);          // starts before the existing unit price
        checkUnitPriceOverlap("2012-01-01", "2013-02-03");  // overlaps the start
        checkUnitPriceOverlap("2013-02-03", "2013-02-04");  // intersects
        checkUnitPriceOverlap("2013-04-01", null);          // overlaps the end
    }

    /**
     * Verifies that adding a unit price with no start date throws an exception.
     */
    @Test
    public void testAddUnitPriceWithNoStartDate() {
        ProductData data = createProduct(product);
        Set<Lookup> locations = Collections.emptySet();
        data.addUnitPrice(-1, new BigDecimal("1.0"), new BigDecimal("0.5"), BigDecimal.TEN, null, null, locations, 1);

        try {
            comparator.compare(product, data);
            fail("Expected ProductIOException");
        } catch (ProductIOException exception) {
            assertEquals("A price start date is required", exception.getMessage());
        }
    }

    /**
     * Verifies that adding a fixed price with no start date throws an exception.
     */
    @Test
    public void testAddFixedPriceWithNoStartDate() {
        ProductData data = createProduct(product);
        Set<Lookup> groups = Collections.emptySet();
        data.addFixedPrice(-1, new BigDecimal("1.0"), new BigDecimal("0.5"), BigDecimal.TEN, null, null, true,
                           groups, 1);

        try {
            comparator.compare(product, data);
            fail("Expected ProductIOException");
        } catch (ProductIOException exception) {
            assertEquals("A price start date is required", exception.getMessage());
        }
    }

    /**
     * Verifies that a unit price with start and end dates can be added before an existing price.
     */
    @Test
    public void testAddUnitPriceBeforeExisting() {
        ProductData data = createProduct(product);
        BigDecimal unitCost = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("1.0");
        Date from = getDate("2012-01-01");
        Date to = getDate("2013-02-02");
        Set<Lookup> groups = Collections.emptySet();
        data.addUnitPrice(-1, unitPrice, unitCost, BigDecimal.TEN, from, to, groups, 1);

        PriceData changed = checkUnitPriceChange(data);
        checkPrice(changed, unitCost, unitPrice, BigDecimal.TEN, from, to, groups);
    }

    /**
     * Verifies that a unit price with start and end dates can be added after an existing price.
     */
    @Test
    public void testAddUnitPriceAfterExisting() {
        ProductData data = createProduct(product);
        BigDecimal unitCost = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("1.0");
        Date from = getDate("2013-04-02");
        Date to = getDate("2013-06-01");
        Set<Lookup> groups = Collections.emptySet();
        data.addUnitPrice(-1, unitPrice, unitCost, BigDecimal.TEN, from, to, groups, 1);

        PriceData changed = checkUnitPriceChange(data);
        checkPrice(changed, unitCost, unitPrice, BigDecimal.TEN, from, to, groups);
    }

    /**
     * Verifies that adding a new unit price (i.e. id = -1) with the same dates as an existing one throws an exception.
     */
    @Test
    public void testAddNewUnitPriceWithSameDates() {
        ProductData data = createProduct(product);
        BigDecimal unitCost = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("1.0");
        Set<Lookup> groups = Collections.emptySet();
        data.addUnitPrice(-1, unitPrice, unitCost, BigDecimal.TEN, unit1.getFromDate(), unit1.getToDate(), groups, 1);

        try {
            comparator.compare(product, data);
            fail("Expected ProductIOException to be thrown");
        } catch (ProductIOException expected) {
            assertEquals("Duplicate unit price", expected.getMessage());
        }
    }

    /**
     * Verifies that a fixed price that overlaps an existing open ended fixed price closes it.
     */
    @Test
    public void testCloseExistingFixedPrice() {
        fixed1.setToDate(null);
        save(product);

        ProductData data = createProduct(product);
        BigDecimal fixedCost = new BigDecimal("1.5");
        BigDecimal fixedPrice = new BigDecimal("2.0");
        Date from = getDate("2013-04-02");
        Date to = getDate("2013-06-01");
        Set<Lookup> groups = Collections.emptySet();
        data.addFixedPrice(-1, fixedPrice, fixedCost, BigDecimal.TEN, from, to, true, groups, 1);

        ProductData changed = comparator.compare(product, data);
        assertNotNull(changed);
        assertEquals(2, changed.getFixedPrices().size());
        checkPrice(changed.getFixedPrices().get(0), new BigDecimal("0.5"), fixed1.getPrice(), BigDecimal.TEN,
                   fixed1.getFromDate(), from, groups);
        checkPrice(changed.getFixedPrices().get(1), fixedCost, fixedPrice, BigDecimal.TEN, from, to, groups);
    }

    /**
     * Verifies that a unit price that overlaps an existing open ended unit price closes it.
     */
    @Test
    public void testCloseExistingUnitPrice() {
        unit1.setToDate(null);
        save(product);

        ProductData data = createProduct(product);
        BigDecimal unitCost = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("1.0");
        Date from = getDate("2013-04-02");
        Date to = getDate("2013-06-01");
        Set<Lookup> groups = Collections.emptySet();
        data.addUnitPrice(-1, unitPrice, unitCost, BigDecimal.TEN, from, to, groups, 1);

        ProductData changed = comparator.compare(product, data);
        assertNotNull(changed);
        assertEquals(2, changed.getUnitPrices().size());
        checkPrice(changed.getUnitPrices().get(0), new BigDecimal("1.2"), unit1.getPrice(), BigDecimal.TEN,
                   unit1.getFromDate(), from, groups);
        checkPrice(changed.getUnitPrices().get(1), unitCost, unitPrice, BigDecimal.TEN, from, to, groups);
    }

    /**
     * Verifies that an exception is thrown if an attempt is made to update a fixed price linked from another product.
     */
    @Test
    public void testUpdateLinkedPrice() {
        Product template = (Product) create(ProductArchetypes.PRICE_TEMPLATE);
        template.setName("XPriceTemplate-" + System.currentTimeMillis());
        ProductPrice linkedFixedPrice = createFixedPrice(getDate("2014-01-01"), null, true);
        template.addProductPrice(linkedFixedPrice);
        IMObjectBean bean = getBean(product);
        bean.addTarget("linked", template);
        product.getProductPrices().remove(fixed1); // remove the fixed price, and use the one linked from the template
        save(product, template);

        ProductData data = createProduct(product);

        // get the linked fixed price
        assertEquals(1, data.getFixedPrices().size());
        PriceData fixedPrice = data.getFixedPrices().get(0);
        assertEquals(linkedFixedPrice.getId(), fixedPrice.getId());

        fixedPrice.setFrom(getDate("2014-02-01"));

        try {
            comparator.compare(product, data);
            fail("Expected ProductIOException to be thrown");
        } catch (ProductIOException exception) {
            assertEquals("Cannot update linked price", exception.getMessage());
        }
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to the fixed price max discount.
     */
    @Test
    public void testFixedPriceMaxDiscountChange() {
        ProductData data = createProduct(product);
        BigDecimal maxDiscount = new BigDecimal("17.5");
        data.getFixedPrices().get(0).setMaxDiscount(maxDiscount);

        PriceData fixedPrice = checkFixedPriceChange(data);
        checkEquals(maxDiscount, fixedPrice.getMaxDiscount());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects changes to the unit price max discount.
     */
    @Test
    public void testUnitPriceMaxDiscountChange() {
        ProductData data = createProduct(product);
        BigDecimal maxDiscount = new BigDecimal("17.5");
        data.getUnitPrices().get(0).setMaxDiscount(maxDiscount);

        PriceData unitPrice = checkUnitPriceChange(data);
        checkEquals(maxDiscount, unitPrice.getMaxDiscount());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects additions to fixed price pricing groups.
     */
    @Test
    public void testAddPricingGroupsToFixedPrice() {
        Lookup groupA = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "B");
        ProductData data = createProduct(product);
        Set<Lookup> groups = getSet(groupA, groupB);

        data.getFixedPrices().get(0).setPricingGroups(groups);

        PriceData fixedPrice = checkFixedPriceChange(data);
        assertEquals(groups, fixedPrice.getPricingGroups());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects additions to unit price pricing groups.
     */
    @Test
    public void testAddPricingGroupsToUnitPrice() {
        Lookup groupA = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "B");
        ProductData data = createProduct(product);
        Set<Lookup> groups = getSet(groupA, groupB);

        data.getUnitPrices().get(0).setPricingGroups(groups);

        PriceData unitPrice = checkUnitPriceChange(data);
        assertEquals(groups, unitPrice.getPricingGroups());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects deletions to fixed price pricing groups.
     */
    @Test
    public void testDeleteFixedPricePricingGroups() {
        Lookup groupA = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "B");
        fixed1.addClassification(groupA);
        fixed1.addClassification(groupB);
        ProductData data = createProduct(product);
        Set<Lookup> groups = Collections.emptySet();
        data.getFixedPrices().get(0).setPricingGroups(groups);

        PriceData fixedPrice = checkFixedPriceChange(data);
        assertEquals(groups, fixedPrice.getPricingGroups());
    }

    /**
     * Verifies that {@link ProductDataComparator} detects deletions to unit price pricing groups.
     */
    @Test
    public void testDeleteUnitPricePricingGroups() {
        Lookup groupA = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "B");
        unit1.addClassification(groupA);
        unit1.addClassification(groupB);
        ProductData data = createProduct(product);
        Set<Lookup> groups = Collections.emptySet();
        data.getUnitPrices().get(0).setPricingGroups(groups);

        PriceData unitPrice = checkUnitPriceChange(data);
        assertEquals(groups, unitPrice.getPricingGroups());
    }

    /**
     * Verifies a unit price can be added that overlaps an existing price, if it has a different pricing group.
     */
    @Test
    public void testAddUnitPriceWithDifferentPricingGroup() {
        Lookup groupA = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "A");
        ProductData data = createProduct(product);
        Set<Lookup> groups = getSet(groupA);
        BigDecimal unitPrice = new BigDecimal("1.0");
        BigDecimal unitCost = new BigDecimal("0.5");
        data.addUnitPrice(-1, unitPrice, unitCost, BigDecimal.TEN, unit1.getFromDate(), unit1.getToDate(), groups, 1);

        PriceData changed = checkUnitPriceChange(data);
        checkPrice(changed, unitCost, unitPrice, BigDecimal.TEN, unit1.getFromDate(), unit1.getToDate(), groups);
    }

    /**
     * Verifies that the pricing groups on two unit prices that have the same dates can be swapped.
     */
    @Test
    public void testUnitPriceSwapPriceGroups() {
        Lookup groupA = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "A");
        Lookup groupB = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, "B");
        ProductPrice unit2 = createUnitPrice("2.55", "1.5", "70", "10", "2013-02-02", "2013-04-02");
        unit1.addClassification(groupA);
        unit2.addClassification(groupB);
        product.addProductPrice(unit2);
        save(product);

        ProductData data = createProduct(product);

        PriceData unitPrice1 = getPrice(data.getUnitPrices(), unit1.getId());
        PriceData unitPrice2 = getPrice(data.getUnitPrices(), unit2.getId());
        unitPrice1.setPricingGroups(getSet(groupB));
        unitPrice2.setPricingGroups(getSet(groupA));

        ProductData changed = comparator.compare(product, data);
        assertNotNull(changed);
        assertEquals(2, changed.getUnitPrices().size());
        unitPrice1 = getPrice(changed.getUnitPrices(), unit1.getId());
        unitPrice2 = getPrice(changed.getUnitPrices(), unit2.getId());

        Date fromDate = unit1.getFromDate();
        Date toDate = unit1.getToDate();
        checkPrice(unitPrice1, new BigDecimal("1.2"), unit1.getPrice(), BigDecimal.TEN, fromDate, toDate,
                   getSet(groupB));
        checkPrice(unitPrice2, new BigDecimal("1.5"), unit2.getPrice(), BigDecimal.TEN, fromDate, toDate,
                   getSet(groupA));
    }

    /**
     * Verifies that prices with no cost and maxDiscount can be compared for changes.
     */
    @Test
    public void testMissingCostAndMaxDiscount() {
        IMObjectBean bean = getBean(fixed1);
        bean.setValue("cost", null);
        bean.setValue("maxDiscount", null);
        ProductData data = createProduct(product);
        PriceData price = getPrice(data.getFixedPrices(), fixed1.getId());
        checkEquals(BigDecimal.ZERO, price.getCost());
        checkEquals(BigDecimal.ZERO, price.getMaxDiscount());
        ProductData changed = comparator.compare(product, data);
        assertNull(changed);
    }

    /**
     * Finds a price by id.
     *
     * @param prices the prices to search
     * @param id     the price identifier
     * @return the corresponding price
     */
    private PriceData getPrice(List<PriceData> prices, long id) {
        for (PriceData price : prices) {
            if (price.getId() == id) {
                return price;
            }
        }
        fail("Price with id=" + id + " not found");
        return null;
    }

    /**
     * Helper to create a set of lookups.
     *
     * @param lookups the lookups
     * @return a set of the lookups
     */
    private Set<Lookup> getSet(Lookup... lookups) {
        return new HashSet<>(Arrays.asList(lookups));
    }

    /**
     * Verifies that adding a fixed price that overlaps an existing one throws an exception.
     *
     * @param from the price start date. May be {@code null}
     * @param to   the price end date. May be {@code null}
     */
    private void checkFixedPriceOverlap(String from, String to) {
        ProductData data = createProduct(product);
        BigDecimal unitCost = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("1.0");
        Date fromDate = getDate(from);
        Date toDate = getDate(to);
        Set<Lookup> locations = Collections.emptySet();
        data.addFixedPrice(-1, unitPrice, unitCost, BigDecimal.TEN, fromDate, toDate, true, locations, 1);

        PriceData changed = checkFixedPriceChange(data);
        assertEquals(fromDate, changed.getFrom());
        assertEquals(toDate, changed.getTo());
    }

    /**
     * Verifies that adding a unit price that overlaps an existing one throws an exception.
     *
     * @param from the price start date. May be {@code null}
     * @param to   the price end date. May be {@code null}
     */
    private void checkUnitPriceOverlap(String from, String to) {
        ProductData data = createProduct(product);
        BigDecimal unitCost = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("1.0");
        Set<Lookup> locations = Collections.emptySet();
        data.addUnitPrice(-1, unitPrice, unitCost, BigDecimal.TEN, getDate(from), getDate(to), locations, 1);

        try {
            comparator.compare(product, data);
            fail("Expected ProductIOException");
        } catch (ProductIOException exception) {
            assertEquals("Unit price dates overlap an existing unit price", exception.getMessage());
        }
    }

    /**
     * Changes a price.
     *
     * @param data                the price data
     * @param expectedCost        the expected cost
     * @param expectedPrice       the expected price
     * @param expectedMaxDiscount the expected maximum discount
     * @param expectedFrom        the expected from date. May be {@code null}
     * @param expectedTo          the expected to date. May be {@code null}
     * @param expectedGroups      the expected pricing groups
     */
    private void checkPrice(PriceData data, BigDecimal expectedCost, BigDecimal expectedPrice,
                            BigDecimal expectedMaxDiscount, Date expectedFrom, Date expectedTo,
                            Set<Lookup> expectedGroups) {
        checkEquals(expectedCost, data.getCost());
        checkEquals(expectedPrice, data.getPrice());
        checkEquals(expectedMaxDiscount, data.getMaxDiscount());
        assertEquals(expectedFrom, data.getFrom());
        assertEquals(expectedTo, data.getTo());
        assertEquals(expectedGroups, data.getPricingGroups());
    }

    /**
     * Verifies that a change to a unit price is detected.
     *
     * @param data the product data with the changed unit price
     * @return the changed unit price
     */
    private PriceData checkUnitPriceChange(ProductData data) {
        ProductData changed = comparator.compare(product, data);
        assertNotNull(changed);

        assertEquals(0, changed.getFixedPrices().size());
        assertEquals(1, changed.getUnitPrices().size());
        return changed.getUnitPrices().get(0);
    }

    /**
     * Verifies that a change to a fixed price is detected.
     *
     * @param data the product data with the changed fixed price
     * @return the changed fixed price
     */
    private PriceData checkFixedPriceChange(ProductData data) {
        ProductData changed = comparator.compare(product, data);
        assertNotNull(changed);

        assertEquals(0, changed.getUnitPrices().size());
        assertEquals(1, changed.getFixedPrices().size());
        return changed.getFixedPrices().get(0);
    }

}
