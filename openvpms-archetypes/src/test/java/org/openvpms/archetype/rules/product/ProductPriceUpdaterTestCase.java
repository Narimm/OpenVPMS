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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.product;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Set;


/**
 * Tests the {@link ProductPriceUpdater} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
        Product product = TestHelper.createProduct(
                ProductArchetypes.MERCHANDISE, null);
        checkUpdateFromProduct(product);
    }

    /**
     * Tests the {@link ProductPriceUpdater} when invoked via
     * the <em>archetypeService.save.party.supplierperson.before</em> rule.
     */
    @Test
    public void testUpdateFromSupplierPerson() {
        Party party = (Party) create(SupplierArchetypes.SUPPLIER_PERSON);
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("firstName", "Foo");
        bean.setValue("lastName", "XSupplier" + party.hashCode());
        bean.setValue("title", "MR");
        bean.save();
        checkUpdateFromSupplier(party);
    }

    /**
     * Tests the {@link ProductPriceUpdater} when invoked via
     * the <em>archetypeService.save.party.supplierorganisation.before</em>
     * rule.
     */
    @Test
    public void testUpdateFromSupplierOrganisation() {
        Party party = (Party) create(SupplierArchetypes.SUPPLIER_ORGANISATION);
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("name", "XSupplier" + party.hashCode());
        bean.save();
        checkUpdateFromSupplier(party);
    }

    /**
     * Verifies that a product can be saved with a custom unit price
     * i.e the unit price doesn't get overwritten when the product is saved
     * despite having an auto-update product-supplier relationship.
     */
    @Test
    public void testCustomUnitPrice() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION,
                                                   null);
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

    /**
     * Verifies that prices are updated correctly when relationships are
     * created between products and suppliers.
     *
     * @param newProduct       if <tt>true</tt> the product is not saved prior
     *                         to adding the relationship
     * @param newSupplier      if <tt>true</tt> the supplier is not saved prior
     *                         to adding the relationship
     * @param saveProductFirst if <tt>true</tt> the product is saved first
     *                         in the transaction, otherwise the supplier is.
     *                         This affects the order in which rules are fired
     */
    private void checkSaveProductAndSupplier(boolean newProduct,
                                             boolean newSupplier,
                                             boolean saveProductFirst) {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION,
                                                   null, !newProduct);
        Party supplier = TestHelper.createSupplier(!newSupplier);

        // add a new price
        addUnitPrice(product, BigDecimal.ZERO, BigDecimal.ZERO, false);

        // create a product-supplier relationship.
        int packageSize = 30;
        ProductRules rules = new ProductRules();
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

        // verify that the price has updated
        checkPrice(product, new BigDecimal("0.67"), new BigDecimal("1.34"));
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
     * Verifies that:
     * <ul>
     * <li>product prices update when the associated supplier is saved.
     * <li>product prices aren't updated when the supplier is inactive.
     * </ul>
     *
     * @param supplier the supplier
     */
    private void checkUpdateFromSupplier(Party supplier) {
        Product product = TestHelper.createProduct();
        BigDecimal initialCost = BigDecimal.ZERO;
        BigDecimal initialPrice = BigDecimal.ONE;

        // add a new price
        addUnitPrice(product, initialCost, initialPrice);

        // create a product-supplier relationship.
        // It should not trigger auto price updates
        int packageSize = 20;
        ProductSupplier ps = createProductSupplier(product, supplier);
        ps.setPackageSize(packageSize);
        assertFalse(ps.isAutoPriceUpdate());
        supplier.addEntityRelationship(ps.getRelationship());
        save(supplier);

        checkPrice(product, initialCost, initialPrice);

        // reload product-supplier relationship and set to auto update prices
        product = get(product);
        ps = getProductSupplier(product, supplier, packageSize);
        assertNotNull(ps);
        ps.setNettPrice(new BigDecimal("10.00"));
        ps.setListPrice(new BigDecimal("20.00"));
        ps.setAutoPriceUpdate(true);

        // now trigger the rule to update prices
        // NOTE: remove and add as the ps.getRelationship() returns the
        // relationship from the product
        supplier.removeEntityRelationship(ps.getRelationship());
        supplier.addEntityRelationship(ps.getRelationship());
        save(supplier);

        // verify that the price has updated
        checkPrice(product, new BigDecimal("1.00"), new BigDecimal("2.00"));

        // now update the net, list price but disable the supplier. Prices shouldn't update
        ps.setNettPrice(new BigDecimal("20.00"));
        ps.setListPrice(new BigDecimal("40.00"));

        supplier = get(supplier);
        supplier.setActive(false);
        save(supplier);

        // verify that the price hasn't updated
        checkPrice(product, new BigDecimal("1.00"), new BigDecimal("2.00"));
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
        ProductRules rules = new ProductRules();
        return rules.getProductSupplier(product, supplier, packageSize, PACKAGE_UNITS);
    }

    /**
     * Helper to create a new product supplier relationship.
     *
     * @param product  the product
     * @param supplier the supplier
     * @return the new relationship
     */
    private ProductSupplier createProductSupplier(Product product, Party supplier) {
        ProductRules rules = new ProductRules();
        supplier = get(supplier);         // make sure using the latest
        product = get(product);           // instance of each
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        ps.setPackageUnits(PACKAGE_UNITS);
        return ps;
    }

}
