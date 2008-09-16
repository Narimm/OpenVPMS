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
    public void testUpdateFromMedication() {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION,
                                                   null);
        checkUpdateFromProduct(product);
    }

    /**
     * Tests the {@link ProductPriceUpdater} when invoked via
     * the <em>archetypeService.save.product.merchandise.before</em> rule.
     */
    public void testUpdateFromMerchandise() {
        Product product = TestHelper.createProduct(
                ProductArchetypes.MERCHANDISE, null);
        checkUpdateFromProduct(product);
    }

    /**
     * Tests the {@link ProductPriceUpdater} when invoked via
     * the <em>archetypeService.save.party.supplierperson.before</em> rule.
     */
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
    public void testUpdateFromSupplierOrganisation() {
        Party party = (Party) create(SupplierArchetypes.SUPPLIER_ORGANISATION);
        IMObjectBean bean = new IMObjectBean(party);
        bean.setValue("name", "XSupplier" + party.hashCode());
        bean.save();
        checkUpdateFromSupplier(party);
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
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
     * Verifies that product prices update when the associated product is
     * saved.
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
        int packageSize = 20;
        ProductSupplier ps = createProductSupplier(product, supplier);
        ps.setPackageSize(packageSize);
        assertFalse(ps.isAutoPriceUpdate());
        ps.save();
        product = get(product);

        checkPrice(product, initialCost, initialPrice);

        // reload product-supplier relationship and set to auto update prices
        ps = getProductSupplier(product, supplier, packageSize);
        assertNotNull(ps);
        ps.setNettPrice(new BigDecimal("10.00"));
        ps.setListPrice(new BigDecimal("20.00"));
        ps.setAutoPriceUpdate(true);
        ps.save();
        product = get(product);

        // now trigger the rule to update prices
        save(product);

        // verify that the price has updated
        checkPrice(product, new BigDecimal("1.00"), new BigDecimal("2.00"));
    }

    /**
     * Verifies that product prices update when the associated supplier is
     * saved.
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
        ps.save();

        checkPrice(product, initialCost, initialPrice);

        // reload product-supplier relationship and set to auto update prices
        ps = getProductSupplier(product, supplier, packageSize);
        assertNotNull(ps);
        ps.setNettPrice(new BigDecimal("10.00"));
        ps.setListPrice(new BigDecimal("20.00"));
        ps.setAutoPriceUpdate(true);
        ps.save();
        supplier = get(supplier);

        // now trigger the rule to update prices
        save(supplier);

        // verify that the price has updated
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
        ProductPrice p = prices.toArray(new ProductPrice[0])[0];
        IMObjectBean bean = new IMObjectBean(p);
        assertEquals(cost, bean.getBigDecimal("cost"));
        assertEquals(price, bean.getBigDecimal("price"));
    }

    /**
     * Returns product supplier for the specified product and package size.
     *
     * @param packageSize the package size
     */
    private ProductSupplier getProductSupplier(Product product,
                                               Party supplier,
                                               int packageSize) {
        ProductRules rules = new ProductRules();
        supplier = get(supplier); // make sure using the latest
        product = get(product);   // instance of each
        return rules.getProductSupplier(product, supplier, packageSize,
                                        PACKAGE_UNITS);
    }

    /**
     * Helper to create a new product supplier relationship.
     *
     * @param product the product
     * @return the new relationship
     */
    private ProductSupplier createProductSupplier(Product product,
                                                  Party supplier) {
        ProductRules rules = new ProductRules();
        supplier = get(supplier);         // make sure using the latest
        product = get(product);           // instance of each
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        ps.setPackageUnits(PACKAGE_UNITS);
        return ps;
    }

}
