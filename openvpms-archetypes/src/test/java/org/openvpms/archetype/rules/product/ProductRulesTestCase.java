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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.product.ProductPrice;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.math.MathRules.ONE_HUNDRED;
import static org.openvpms.archetype.rules.product.ProductTestHelper.createDose;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link ProductRules} class.
 *
 * @author Tim Anderson
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
        StockRules stockRules = new StockRules(getArchetypeService());
        BigDecimal price = new BigDecimal("2.00");
        Party supplier = TestHelper.createSupplier();
        Product product = TestHelper.createProduct();
        ProductPrice unitPrice = addUnitPrice(product, price);
        ProductSupplier ps = rules.createProductSupplier(product, supplier);
        save(Arrays.asList(product, supplier));

        // add a dose. This should be copied.
        Lookup species = TestHelper.getLookup(PatientArchetypes.SPECIES, "CANINE");
        Entity dose = createDose(species, ZERO, TEN, ONE, ONE);
        ProductTestHelper.addDose(product, dose);

        Party stockLocation = ProductTestHelper.createStockLocation();
        stockRules.updateStock(product, stockLocation, TEN);

        // add a linked product. This should *not* be copied
        Product linked = TestHelper.createProduct(ProductArchetypes.PRICE_TEMPLATE, null);
        IMObjectBean bean = getBean(product);
        bean.addTarget("linked", linked);
        save(product, linked);

        // now perform the copy
        String name = "Copy";
        Product copy = rules.copy(product, name);

        // verify it is a copy
        assertTrue(product.getId() != copy.getId());
        assertEquals(product.getArchetypeId(), copy.getArchetypeId());
        assertEquals(name, copy.getName());

        // verify the copy has a different dose
        IMObjectBean copyBean = getBean(copy);
        List<Entity> doses = copyBean.getTargets("doses", Entity.class);
        assertEquals(1, doses.size());
        assertNotEquals(doses.get(0), dose);

        // verify the dose species is identical
        Lookup species2 = doses.get(0).getClassifications().iterator().next();
        assertEquals(species.getId(), species2.getId());

        // verify the copy refers to the same stock location
        assertEquals(copyBean.getTarget("stockLocations"), stockLocation);

        // verify the product price has been copied
        Set<ProductPrice> productPrices = copy.getProductPrices();
        assertEquals(1, productPrices.size());
        ProductPrice priceCopy = productPrices.toArray(new ProductPrice[0])[0];
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
        List<Reference> linkedRefs = copyBean.getTargetRefs("linked");
        assertEquals(1, linkedRefs.size());
        assertEquals(linked.getObjectReference(), linkedRefs.get(0));

        // stock quantity should not be copied
        checkEquals(ZERO, stockRules.getStock(copy, stockLocation));
    }

    /**
     * Copies a service product with a linked location.
     */
    @Test
    public void testCopyProductWithLocation() {
        Product product = ProductTestHelper.createService();

        // add a location exclusion. This should not be copied.
        IMObjectBean bean = getBean(product);
        Party location = TestHelper.createLocation();
        bean.addTarget("locations", location);
        bean.save();

        // now perform the copy
        String name = "Copy";
        Product copy = rules.copy(product, name);

        // verify it is a copy
        assertTrue(product.getId() != copy.getId());
        assertEquals(product.getArchetypeId(), copy.getArchetypeId());
        assertEquals(name, copy.getName());

        // verify the copy refers to the same location
        EntityBean copyBean = new EntityBean(copy);
        assertEquals(copyBean.getNodeTargetEntity("locations"), location);
    }

    /**
     * Tests the {@link ProductRules#getDose(Product, Weight, String)} method.
     */
    @Test
    public void testGetDose() {
        Product product = ProductTestHelper.createProductWithConcentration(BigDecimal.valueOf(2));

        Lookup canine = TestHelper.getLookup(PatientArchetypes.SPECIES, "CANINE");
        Lookup feline = TestHelper.getLookup(PatientArchetypes.SPECIES, "FELINE");

        Entity dose1 = createDose(canine, ZERO, TEN, ONE, ONE);                                      // canine 0-10kg
        Entity dose2 = createDose(feline, ZERO, TEN, BigDecimal.valueOf(2), BigDecimal.valueOf(2));  // feline 0-10kg
        Entity dose3 = createDose(null, TEN, BigDecimal.valueOf(20), BigDecimal.valueOf(4), ONE); // all species 10-20kg

        ProductTestHelper.addDose(product, dose1);
        ProductTestHelper.addDose(product, dose2);
        ProductTestHelper.addDose(product, dose3);

        checkEquals(new BigDecimal("0.5"), rules.getDose(product, new Weight(1), "CANINE"));
        checkEquals(2, rules.getDose(product, new Weight(1), "FELINE"));

        checkEquals(new BigDecimal(20), rules.getDose(product, new Weight(10), "CANINE")); // picks up all species dose
        checkEquals(ZERO, rules.getDose(product, new Weight(20), "FELINE"));               // no dose for any species

        // check null species
        checkEquals(ZERO, rules.getDose(product, new Weight(1), null));
        checkEquals(new BigDecimal(20), rules.getDose(product, new Weight(10), null));
        checkEquals(ZERO, rules.getDose(product, new Weight(20), null));
    }

    /**
     * Verifies that {@link ProductRules#getDose(Product, Weight, String)} rounds doses correctly.
     */
    @Test
    public void testGetDoseRounding() {
        BigDecimal concentration = BigDecimal.valueOf(50);
        Product product1 = ProductTestHelper.createProductWithConcentration(concentration);
        Product product2 = ProductTestHelper.createProductWithConcentration(concentration);
        Product product3 = ProductTestHelper.createProductWithConcentration(concentration);

        BigDecimal rate = BigDecimal.valueOf(4);

        // use the same concentration and date, but round to different no. of places for each weight range
        ProductTestHelper.addDose(product1, createDose(null, ZERO, ONE_HUNDRED, rate, ONE, 0));
        ProductTestHelper.addDose(product2, createDose(null, ZERO, ONE_HUNDRED, rate, ONE, 1));
        ProductTestHelper.addDose(product3, createDose(null, ZERO, ONE_HUNDRED, rate, ONE, 2));

        Weight weight = new Weight(new BigDecimal("15.5"));
        checkEquals(1, rules.getDose(product1, weight, "CANINE"));
        checkEquals(new BigDecimal("1.2"), rules.getDose(product2, weight, "CANINE"));
        checkEquals(new BigDecimal("1.24"), rules.getDose(product3, weight, "CANINE"));
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
     * Tests the {@link ProductRules#getBatches(Product, String, Date, Party)} method.
     */
    @Test
    public void testGetBatches() {
        Product product = TestHelper.createProduct();
        Party manufacturer1 = createManufacturer();
        Party manufacturer2 = createManufacturer();
        Party manufacturer3 = createManufacturer();
        List<Entity> batches = rules.getBatches(product, null, null, null);
        assertEquals(0, batches.size());

        Entity batch1 = rules.createBatch(product, "aa", getDatetime("2014-06-01 10:00:00"), manufacturer1);
        Entity batch2 = rules.createBatch(product, "ab", getDatetime("2014-07-01 07:00:00"), manufacturer2);
        Entity batch3 = rules.createBatch(product, "ac", getDatetime("2014-08-01 15:00:00"), manufacturer3);
        save(batch1, batch2, batch3);

        checkBatches(rules.getBatches(product, "a*", null, null), batch1, batch2, batch3);

        checkBatches(rules.getBatches(product, "a*", getDate("2014-06-01"), null), batch1);
        checkBatches(rules.getBatches(product, "a*", null, manufacturer1), batch1);

        checkBatches(rules.getBatches(product, null, null, manufacturer2), batch2);

        checkBatches(rules.getBatches(product, "ac", null, manufacturer3), batch3);
    }

    /**
     * Tests the {@link ProductRules#createBatch(Product, String, Date, Party)} and
     * {@link ProductRules#getBatchExpiry(Entity)} method.
     */
    @Test
    public void testCreateBatch() {
        Product product = TestHelper.createProduct();
        Date expiry = TestHelper.getDate("2014-06-14");
        Party manufacturer = createManufacturer();
        String batchNumber = "12345";
        Entity batch = rules.createBatch(product, batchNumber, expiry, manufacturer);
        assertTrue(batch.isNew());
        save(batch);

        batch = get(batch);
        EntityBean bean = new EntityBean(batch);
        assertEquals(batchNumber, batch.getName());
        assertEquals(product.getObjectReference(), bean.getNodeTargetObjectRef("product", false));
        assertEquals(expiry, rules.getBatchExpiry(batch));
        assertEquals(manufacturer.getObjectReference(), bean.getNodeTargetObjectRef("manufacturer"));
    }

    /**
     * Tests the {@link ProductRules#canUseProductAtLocation(Product, Party)} method.
     */
    @Test
    public void testCanUseProductAtLocation() {
        Product medication = ProductTestHelper.createMedication();
        Product merchandise = ProductTestHelper.createMerchandise();
        Product service = ProductTestHelper.createService();
        Product template = ProductTestHelper.createTemplate();
        Party location = TestHelper.createLocation();

        // will always return true for medication and merchandise products
        assertTrue(rules.canUseProductAtLocation(medication, location));
        assertTrue(rules.canUseProductAtLocation(merchandise, location));
        assertTrue(rules.canUseProductAtLocation(service, location));
        assertTrue(rules.canUseProductAtLocation(template, location));

        // exclude the location for the service and template and verify they can no longer by used
        ProductTestHelper.addLocationExclusion(service, location);
        ProductTestHelper.addLocationExclusion(template, location);

        assertFalse(rules.canUseProductAtLocation(service, location));
        assertFalse(rules.canUseProductAtLocation(template, location));
    }

    /**
     * Tests the {@link ProductRules#isRestricted(Product)} method.
     */
    @Test
    public void testIsRestricted() {
        Product medication1 = ProductTestHelper.createMedication(false);
        Product medication2 = ProductTestHelper.createMedication(true);
        Product medication3 = ProductTestHelper.createMedication(); // no schedule
        Product merchandise = ProductTestHelper.createMerchandise();
        Product service = ProductTestHelper.createService();
        Product template = ProductTestHelper.createTemplate();
        assertFalse(rules.isRestricted(medication1));
        assertTrue(rules.isRestricted(medication2));
        assertFalse(rules.isRestricted(medication3));
        assertFalse(rules.isRestricted(merchandise));
        assertFalse(rules.isRestricted(service));
        assertFalse(rules.isRestricted(template));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new ProductRules(getArchetypeService(), getLookupService());
    }

    /**
     * Verifies batches match those expected.
     *
     * @param matches the matches to check
     * @param batches the expected batches
     */
    private void checkBatches(List<Entity> matches, Entity... batches) {
        assertEquals(batches.length, matches.size());
        for (int i = 0; i < batches.length; ++i) {
            assertEquals(batches[i], matches.get(i));
        }
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

    /**
     * Helper to create a party.supplierManufacturer.
     *
     * @return a new manufacturer
     */
    private Party createManufacturer() {
        Party manufacturer = (Party) create(SupplierArchetypes.MANUFACTURER);
        manufacturer.setName("Z-Manufacturer" + System.currentTimeMillis());
        save(manufacturer);
        return manufacturer;
    }

}
