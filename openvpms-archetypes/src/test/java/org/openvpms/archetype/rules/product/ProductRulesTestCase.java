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
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link ProductRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductRulesTestCase extends AbstractProductTest {

    /**
     * The rules.
     */
    private ProductRules rules;


    /**
     * Tests the {@link ProductRules#copy(Product)} method.
     */
    @Test
    public void testCopy() {
        StockRules stockRules = new StockRules();
        BigDecimal price = new BigDecimal("2.00");
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        ProductPrice unitPrice = addUnitPrice(product, price);
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        save(Arrays.asList(product, supplier));

        Party stockLocation = (Party) create(StockArchetypes.STOCK_LOCATION);
        stockLocation.setName("STOCK-LOCATION-" + stockLocation.hashCode());
        stockRules.updateStock(product, stockLocation, BigDecimal.ONE);

        // add a linked product. This should *not* be copied
        Product linked = TestHelper.createProduct(ProductArchetypes.PRICE_TEMPLATE, null);
        EntityBean bean = new EntityBean(product);
        bean.addNodeRelationship("linked", linked);
        save(product, linked);

        // now perform the copy
        String name = "Copy";
        Product copy = rules.copy(product, name);

        // verify it is a copy
        assertTrue(product.getId() != copy.getId());
        assertEquals(product.getArchetypeId(), copy.getArchetypeId());
        assertEquals(name, copy.getName());

        // verify the copy refers to the same stock location
        EntityBean copyBean = new EntityBean(copy);
        assertEquals(copyBean.getNodeTargetEntity("stockLocations"), stockLocation);

        // verify the product price has been copied
        Set<ProductPrice> productPrices = copy.getProductPrices();
        assertEquals(1, productPrices.size());
        ProductPrice priceCopy = productPrices.toArray(new ProductPrice[productPrices.size()])[0];
        assertTrue(unitPrice.getId() != priceCopy.getId());
        checkEquals(unitPrice.getPrice(), priceCopy.getPrice());

        // verify the product supplier relationship has been copied
        ProductSupplier psCopy = rules.getProductSupplier(copy, supplier, null, ps.getPackageSize(),
                                                          ps.getPackageUnits());
        assertNotNull(psCopy);
        assertTrue(psCopy.getRelationship().getId() != ps.getRelationship().getId());

        // verify the supplier is the same
        assertEquals(supplier.getObjectReference(), psCopy.getSupplierRef());

        // verify the linked product is the same
        List<IMObjectReference> linkedRefs = copyBean.getNodeTargetEntityRefs("linked");
        assertEquals(1, linkedRefs.size());
        assertEquals(linked.getObjectReference(), linkedRefs.get(0));
    }

    /**
     * Tests the {@link ProductRules#getProductSuppliers} method.
     */
    @Test
    public void testGetProductSuppliersForSupplierAndProduct() {
        Party supplier = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();

        ProductSupplier p1rel1 = rules.createProductSupplier(product1, supplier);
        ProductSupplier p1rel2 = rules.createProductSupplier(product1, supplier);
        ProductSupplier p2rel1 = rules.createProductSupplier(product2, supplier);

        List<ProductSupplier> relationships = rules.getProductSuppliers(product1, supplier);
        assertEquals(2, relationships.size());
        assertTrue(relationships.contains(p1rel1));
        assertTrue(relationships.contains(p1rel2));
        assertFalse(relationships.contains(p2rel1));

        // deactivate one of the relationships, and verify it is no longer
        // returned. Need to sleep to allow for system clock granularity
        deactivateRelationship(p1rel1);

        relationships = rules.getProductSuppliers(product1, supplier);
        assertEquals(1, relationships.size());
        assertFalse(relationships.contains(p1rel1));
        assertTrue(relationships.contains(p1rel2));
    }

    /**
     * Tests the {@link ProductRules#getProductSupplier(Product, Party, String, int, String)} method.
     */
    @Test
    public void testGetProductSupplier() {
        Party supplier = TestHelper.createSupplier();
        Product product1 = TestHelper.createProduct();
        Product product2 = TestHelper.createProduct();
        Product product3 = TestHelper.createProduct();

        // create some relationships
        ProductSupplier p1rel1 = rules.createProductSupplier(product1, supplier);
        ProductSupplier p1rel2 = rules.createProductSupplier(product1, supplier);
        ProductSupplier p2rel = rules.createProductSupplier(product2, supplier);
        ProductSupplier p3rel = rules.createProductSupplier(product3, supplier);

        assertEquals(0, p1rel1.getPackageSize()); // default value
        p1rel2.setPackageSize(3);
        p1rel2.setPackageUnits("AMPOULE");
        p2rel.setPackageSize(4);
        p2rel.setPackageUnits("PACKET");
        p3rel.setReorderCode("p3");
        p3rel.setPackageSize(4);
        p3rel.setPackageUnits("PACKET");

        // verify that p1rel is returned if there is no corresponding
        // relationship, as its package size isn't set
        ProductSupplier test1 = rules.getProductSupplier(product1, supplier, null, 4, "BOX");
        assertEquals(p1rel1, test1);

        p1rel1.setPackageSize(4);
        p1rel1.setPackageUnits("BOX");

        // verify that the correct relationship is returned for exact matches
        assertEquals(p1rel1, rules.getProductSupplier(product1, supplier, null, 4, "BOX"));
        assertEquals(p1rel2, rules.getProductSupplier(product1, supplier, null, 3, "AMPOULE"));
        assertEquals(p2rel, rules.getProductSupplier(product2, supplier, null, 4, "PACKET"));
        assertEquals(p3rel, rules.getProductSupplier(product3, supplier, "p3", 4, "PACKET"));

        // verify that the correct relationship is returned for a match on reorder code
        assertEquals(p3rel, rules.getProductSupplier(product3, supplier, "p3", -1, null));

        // verify that the correct relationship is returned for a match on package size and units
        assertEquals(p1rel1, rules.getProductSupplier(product1, supplier, "foo", 4, "BOX"));
        assertEquals(p1rel2, rules.getProductSupplier(product1, supplier, "bar", 3, "AMPOULE"));
        assertEquals(p2rel, rules.getProductSupplier(product2, supplier, "zoo", 4, "PACKET"));
        assertEquals(p3rel, rules.getProductSupplier(product3, supplier, "p?", 4, "PACKET"));

        // verify that the correct relationship is returned for a match on package size
        assertEquals(p1rel1, rules.getProductSupplier(product1, supplier, "foo", 4, null));
        assertEquals(p1rel2, rules.getProductSupplier(product1, supplier, "bar", 3, null));
        assertEquals(p2rel, rules.getProductSupplier(product2, supplier, "zoo", 4, null));
        assertEquals(p3rel, rules.getProductSupplier(product3, supplier, "p?", 4, null));

        // verify that nothing is returned if there is no direct match
        assertNull(rules.getProductSupplier(product1, supplier, "foo", 5, "BOX"));
        assertNull(rules.getProductSupplier(product1, supplier, "bar", 5, "PACKET"));
        assertNull(rules.getProductSupplier(product2, supplier, "zoo", 5, "PACKET"));
        assertNull(rules.getProductSupplier(product2, supplier, null, 5, null));
    }

    /**
     * Tests the {@link ProductRules#getProductSuppliers(Product)} method.
     */
    @Test
    public void testGetProductSuppliersForProduct() {
        Party supplier1 = TestHelper.createSupplier();
        Party supplier2 = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();

        ProductSupplier rel1 = rules.createProductSupplier(product, supplier1);
        ProductSupplier rel2 = rules.createProductSupplier(product, supplier2);

        List<ProductSupplier> relationships
                = rules.getProductSuppliers(product);
        assertEquals(2, relationships.size());
        assertTrue(relationships.contains(rel1));
        assertTrue(relationships.contains(rel2));

        // deactivate one of the relationships, and verify it is no longer
        // returned
        deactivateRelationship(rel1);

        relationships = rules.getProductSuppliers(product);
        assertEquals(1, relationships.size());
        assertFalse(relationships.contains(rel1));
        assertTrue(relationships.contains(rel2));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new ProductRules(getArchetypeService());
    }

    /**
     * Helper to deactivate a relationship and sleep for a second, so that
     * subsequent isActive() checks return false. The sleep in between
     * deactivating a relationship and calling isActive() is required due to
     * system time granularity.
     *
     * @param relationship the relationship to deactivate
     */
    private void deactivateRelationship(ProductSupplier relationship) {
        relationship.getRelationship().setActive(false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
            // do nothing
        }
    }

}
