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

package org.openvpms.archetype.rules.product;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.product.ProductPriceTestHelper.createUnitPrice;
import static org.openvpms.archetype.rules.util.DateRules.getToday;
import static org.openvpms.archetype.rules.util.DateRules.getTomorrow;
import static org.openvpms.archetype.rules.util.DateRules.getYesterday;


/**
 * Tests the {@link ProductPriceUpdater} class.
 *
 * @author Tim Anderson
 */
public class ProductPriceUpdaterTestCase extends AbstractProductTest {

    /**
     * Package units.
     */
    private static final String PACKAGE_UNITS = "BOX";


    /**
     * Tests the {@link ProductPriceUpdater} when invoked via
     * the <em>archetypeService.save.product.medication.before</em> rule.
     */
    @Test
    public void testUpdateFromMedication() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION,
                                                   null);
        checkUpdateFromProduct(product);
    }

    /**
     * Tests the {@link ProductPriceUpdater} when invoked via
     * the <em>archetypeService.save.product.merchandise.before</em> rule.
     */
    @Test
    public void testUpdateFromMerchandise() {
        Product product = TestHelper.createProduct(ProductArchetypes.MERCHANDISE, null);
        checkUpdateFromProduct(product);
    }

    /**
     * Verifies that a product can be saved with a custom unit price
     * i.e the unit price doesn't get overwritten when the product is saved
     * despite having an auto-update product-supplier relationship.
     */
    @Test
    public void testCustomUnitPrice() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null);
        Party supplier = TestHelper.createSupplier();
        BigDecimal initialCost = BigDecimal.ZERO;
        BigDecimal initialPrice = BigDecimal.ONE;

        // add a new price
        addUnitPrice(product, initialCost, initialPrice);

        // create a product-supplier relationship to trigger auto price updates
        int packageSize = 30;
        ProductSupplier ps = createProductSupplier(product, supplier);
        ps.setPackageSize(packageSize);
        ps.setNettPrice(new BigDecimal("10.00"));
        ps.setListPrice(new BigDecimal("20.00"));
        ps.setAutoPriceUpdate(true);
        product.addEntityRelationship(ps.getRelationship());
        save(product);

        checkPrice(product, new BigDecimal("0.67"), new BigDecimal("1.34"));

        // verify that the price has updated
        product = get(product);

        // now save with a custom unit price, and verify it doesn't get
        // overwritten when the product saves.
        Set<ProductPrice> prices = product.getProductPrices();
        ProductPrice unit = prices.toArray(new ProductPrice[prices.size()])[0];
        unit.setPrice(new BigDecimal("1.35"));
        save(product);
        checkPrice(product, new BigDecimal("0.67"), new BigDecimal("1.35"));
    }

    /**
     * Tests update when a newly created product is saved with a relationship
     * to an existing supplier.
     */
    @Test
    public void testSaveNewProduct() {
        checkSaveProductAndSupplier(true, false, true);
        checkSaveProductAndSupplier(true, false, false);
    }


    /**
     * Tests update when an existing product is saved with a relationship
     * to a new supplier.
     */
    @Test
    public void testSaveNewSupplier() {
        checkSaveProductAndSupplier(false, true, true);
        checkSaveProductAndSupplier(false, true, false);
    }

    /**
     * Tests update when a newly created product is saved with a relationship
     * to a new supplier.
     */
    @Test
    public void testSaveNewProductAndSupplier() {
        checkSaveProductAndSupplier(true, true, true);
        checkSaveProductAndSupplier(true, true, false);
    }

    /**
     * Verifies that when a product has multiple active unit prices with different pricing groups,
     * each is updated when the product-supplier relationship is saved.
     */
    @Test
    public void testPricingGroups() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null);

        // add prices
        ProductPrice price1 = addUnitPrice(product, "1", "100", "2", "GROUP1");
        ProductPrice price2 = addUnitPrice(product, "1", "200", "3", "GROUP2");
        ProductPrice price3 = addUnitPrice(product, "1", "300", "4", null);
        ProductPrice price4 = addUnitPrice(product, "1", "400", "5", null);
        price4.setToDate(DateRules.getYesterday()); // now inactive

        Party supplier = TestHelper.createSupplier();
        int packageSize = 30;
        ProductSupplier ps = createProductSupplier(product, supplier);
        ps.setPackageSize(packageSize);
        ps.setNettPrice(new BigDecimal("10.00"));
        ps.setListPrice(new BigDecimal("20.00"));
        ps.setAutoPriceUpdate(true);
        product.addEntityRelationship(ps.getRelationship());
        save(product);

        price1 = get(price1);
        price2 = get(price2);
        price3 = get(price3);
        price4 = get(price4);

        checkPrice(price1, new BigDecimal("0.67"), new BigDecimal("1.34"));
        checkPrice(price2, new BigDecimal("0.67"), new BigDecimal("2.01"));
        checkPrice(price3, new BigDecimal("0.67"), new BigDecimal("2.68"));
        checkPrice(price4, new BigDecimal("1"), new BigDecimal("5"));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        initPractice();

        TestHelper.getLookup("lookup.uom", PACKAGE_UNITS);
    }

    /**
     * Helper to ensure the singleton <em>party.organisationPractice</em>
     * exists and has no taxes.
     *
     * @return the practice
     */
    private Party initPractice() {
        return TestHelper.getPractice();
    }

    private ProductPrice addUnitPrice(Product product, String cost, String markup, String price, String pricingGroup) {
        ProductPrice unit = ProductPriceTestHelper.createUnitPrice(new BigDecimal(price), new BigDecimal(cost),
                                                                   new BigDecimal(markup), BigDecimal.valueOf(100),
                                                                   (Date) null, null);
        if (pricingGroup != null) {
            Lookup lookup = TestHelper.getLookup(ProductArchetypes.PRICING_GROUP, pricingGroup);
            unit.addClassification(lookup);
        }
        product.addProductPrice(unit);
        return unit;
    }

    /**
     * Verifies that prices are updated correctly when relationships are
     * created between products and suppliers.
     *
     * @param newProduct       if {@code true} the product is not saved prior to adding the relationship
     * @param newSupplier      if {@code true} the supplier is not saved prior to adding the relationship
     * @param saveProductFirst if {@code true} the product is saved first in the transaction, otherwise the supplier is.
     *                         This affects the order in which rules are fired
     */
    private void checkSaveProductAndSupplier(boolean newProduct, boolean newSupplier, boolean saveProductFirst) {
        BigDecimal cost = BigDecimal.ONE;
        BigDecimal markup = BigDecimal.valueOf(100);
        BigDecimal price = BigDecimal.valueOf(2);
        BigDecimal maxDiscount = BigDecimal.valueOf(100);

        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, null, !newProduct);
        Party supplier = TestHelper.createSupplier(!newSupplier);

        // add some prices
        ProductPrice unit1 = createUnitPrice(price, cost, markup, maxDiscount, null, getYesterday()); // inactive
        ProductPrice unit2 = createUnitPrice(price, cost, markup, maxDiscount, getToday(), null);     // active
        ProductPrice unit3 = createUnitPrice(price, cost, markup, maxDiscount, (Date) null, null);    // active
        ProductPrice unit4 = createUnitPrice(price, cost, markup, maxDiscount, getTomorrow(), null);  // inactive
        product.addProductPrice(unit1);
        product.addProductPrice(unit2);
        product.addProductPrice(unit3);
        product.addProductPrice(unit4);

        // create a product-supplier relationship.
        int packageSize = 30;
        ProductRules rules = new ProductRules(getArchetypeService());
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        ps.setPackageUnits(PACKAGE_UNITS);
        ps.setPackageSize(packageSize);
        ps.setAutoPriceUpdate(true);
        ps.setNettPrice(new BigDecimal("10.00"));
        ps.setListPrice(new BigDecimal("20.00"));
        product.addEntityRelationship(ps.getRelationship());
        supplier.addEntityRelationship(ps.getRelationship());

        if (newProduct) {
            assertTrue(product.isNew());
        } else {
            assertFalse(product.isNew());
        }
        if (newSupplier) {
            assertTrue(supplier.isNew());
        } else {
            assertFalse(supplier.isNew());
        }
        if (saveProductFirst) {
            save(product, supplier);
        } else {
            save(supplier, product);
        }

        // verify that the expected prices have updated
        BigDecimal newCost = new BigDecimal("0.67");
        BigDecimal newPrice = new BigDecimal("1.34");
        checkPrice(unit1, cost, price);              // inactive, so shouldn't update
        checkPrice(unit2, newCost, newPrice);
        checkPrice(unit3, newCost, newPrice);
        checkPrice(unit4, cost, price);              // inactive, so shouldn't update
    }

    /**
     * Verifies that product prices update when the associated product is
     * saved and the supplier is active.
     *
     * @param product the product
     */
    private void checkUpdateFromProduct(Product product) {
        Party supplier = TestHelper.createSupplier();
        BigDecimal initialCost = BigDecimal.ZERO;
        BigDecimal initialPrice = BigDecimal.ONE;

        // add a new price
        addUnitPrice(product, initialCost, initialPrice);

        // create a product-supplier relationship.
        // It should not trigger auto price updates
        int packageSize = 30;
        ProductSupplier ps = createProductSupplier(product, supplier);
        ps.setPackageSize(packageSize);
        assertFalse(ps.isAutoPriceUpdate());
        product.addEntityRelationship(ps.getRelationship());
        save(product);

        checkPrice(product, initialCost, initialPrice);

        // reload product-supplier relationship and set to auto update prices
        ps = getProductSupplier(product, supplier, packageSize);
        assertNotNull(ps);
        ps.setNettPrice(new BigDecimal("10.00"));
        ps.setListPrice(new BigDecimal("20.00"));
        ps.setAutoPriceUpdate(true);

        // now trigger the rule to update prices
        save(product);

        // verify that the price has updated
        checkPrice(product, new BigDecimal("0.67"), new BigDecimal("1.34"));

        // now deactivate the supplier to prevent price updates
        supplier = get(supplier);
        supplier.setActive(false);
        save(supplier);

        // now change the net and list price
        ps.setNettPrice(new BigDecimal("15.00"));
        ps.setListPrice(new BigDecimal("30.00"));

        // now trigger the rule
        save(product);

        // verify that the price haven't updated
        checkPrice(product, new BigDecimal("0.67"), new BigDecimal("1.34"));
    }

    /**
     * Verifies that a product has an <em>productPrice.unitPrice</em> with
     * the specified cost and price.
     *
     * @param product the product
     * @param cost    the expected cost
     * @param price   the expected price
     */
    private void checkPrice(Product product, BigDecimal cost,
                            BigDecimal price) {
        product = get(product); // reload product
        Set<ProductPrice> prices = product.getProductPrices();
        assertEquals(1, prices.size());
        ProductPrice p = prices.toArray(new ProductPrice[prices.size()])[0];
        IMObjectBean bean = new IMObjectBean(p);
        checkEquals(cost, bean.getBigDecimal("cost"));
        checkEquals(price, bean.getBigDecimal("price"));
    }

    /**
     * Returns product supplier for the specified product and package size.
     *
     * @param product     the product
     * @param supplier    the supplier
     * @param packageSize the package size
     * @return the corresponding product supplier, or <tt>null</tt> if none is found
     */
    private ProductSupplier getProductSupplier(Product product, Party supplier, int packageSize) {
        ProductRules rules = new ProductRules(getArchetypeService());
        return rules.getProductSupplier(product, supplier, null, packageSize, PACKAGE_UNITS);
    }

    /**
     * Helper to create a new product supplier relationship.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the new relationship
     */
    private ProductSupplier createProductSupplier(Product product, Party supplier) {
        ProductRules rules = new ProductRules(getArchetypeService());
        supplier = get(supplier);         // make sure using the latest
        product = get(product);           // instance of each
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        ps.setPackageUnits(PACKAGE_UNITS);
        return ps;
    }

}
