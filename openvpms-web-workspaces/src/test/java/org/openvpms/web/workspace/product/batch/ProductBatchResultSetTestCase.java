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

package org.openvpms.web.workspace.product.batch;

import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.product.ProductBatchResultSet;
import org.openvpms.web.component.im.query.QueryTestHelper;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.im.util.VirtualNodeSortConstraint;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.component.system.common.query.Constraints.shortName;

/**
 * Tests the {@link ProductBatchResultSet}.
 *
 * @author Tim Anderson
 */
public class ProductBatchResultSetTestCase extends ArchetypeServiceTest {

    /**
     * Verifies that batches can be filtered on expiry.
     */
    @Test
    public void testQueryOnExpiry() {
        Product product = TestHelper.createProduct();
        Date tomorrow = DateRules.getTomorrow();
        Date today = DateRules.getToday();
        Entity batch1 = createBatch("batch1", product, today);
        Entity batch2 = createBatch("batch2", product, tomorrow);
        Entity batch3 = createBatch("batch3", product, null);

        // no filter on expiry
        checkExpiry(null, null, true, batch1, batch2, batch3);

        // expiring after tomorrow
        checkExpiry(tomorrow, null, false, batch1);
        checkExpiry(tomorrow, null, true, batch2, batch3);

        // expiring before tomorrow
        checkExpiry(null, tomorrow, true, batch1);
        checkExpiry(null, tomorrow, false, batch2, batch3);

        // expiring today or tomorrow
        checkExpiry(today, DateRules.getNextDate(tomorrow), true, batch1, batch2);
        checkExpiry(today, DateRules.getNextDate(tomorrow), false, batch3);
    }

    /**
     * Verifies that batches can be filtered on stock location.
     */
    @Test
    public void testQueryOnStockLocation() {
        Product product = TestHelper.createProduct();
        Party location1 = ProductTestHelper.createStockLocation();
        Party location2 = ProductTestHelper.createStockLocation();

        Entity batch1 = createBatch("batch1", product, null, location1);
        Entity batch2 = createBatch("batch2", product, null, location2);
        Entity batch3 = createBatch("batch3", product, null);
        Entity batch4 = createBatch("batch4", product, null, location1, location2);

        checkStockLocation(location1, null, true, batch1, batch3, batch4);
        checkStockLocation(location1, null, false, batch2);

        checkStockLocation(location2, null, true, batch2, batch3, batch4);
        checkStockLocation(location2, null, false, batch1);

        checkStockLocation(null, null, true, batch1, batch2, batch3, batch4);
    }

    /**
     * Verifies that batches can be filtered on stock location with inactive relationships.
     */
    @Test
    public void testQueryOnStockLocationForInactiveRelationships() {
        Product product = TestHelper.createProduct();
        Party location = ProductTestHelper.createStockLocation();

        Date date1 = DateRules.getToday();
        Date date2 = DateRules.getYesterday();
        Date date3 = DateRules.getPreviousDate(date2);
        Entity batch1 = createBatch("batch1", product, null);
        addStockLocation(batch1, location, date2);  // relationship inactive after date2

        checkStockLocation(location, null, true, batch1);
        checkStockLocation(location, date1, false, batch1);
        checkStockLocation(location, date2, true, batch1);  // <=
        checkStockLocation(location, date3, true, batch1);
    }

    /**
     * Verifies that batches can be filtered by manufacturer.
     */
    @Test
    public void testQueryOnManufacturer() {
        Product product = TestHelper.createProduct();
        Entity batch1 = createBatch("batch1", product, null);
        Entity batch2 = createBatch("batch2", product, null);
        Party man1 = addManufacturer(batch1);
        addManufacturer(batch2);

        checkManufacturer(man1, true, batch1);
        checkManufacturer(man1, false, batch2);
    }

    /**
     * Tests sorting on batch name.
     */
    @Test
    public void testSortOnName() {
        Product product = TestHelper.createProduct();
        Date expiry = DateRules.getTomorrow();
        Entity batch1 = createBatch("batch1", product, expiry);
        Entity batch2 = createBatch("batch2", product, expiry);

        SortConstraint[] sort = {Constraints.sort("name")};
        ProductBatchResultSet set = new ProductBatchResultSet(shortName(ProductArchetypes.PRODUCT_BATCH),
                                                              null, null, null, null, null, null, null, null, sort, 20);
        checkOrder(set, batch1, batch2);
    }

    /**
     * Tests sorting on expiry date.
     */
    @Test
    public void testSortOnExpiryDate() {
        Product product = TestHelper.createProduct();
        Entity batch1 = createBatch("batch1", product, DateRules.getTomorrow());
        Entity batch2 = createBatch("batch2", product, DateRules.getToday());

        SortConstraint[] sort = {new VirtualNodeSortConstraint("expiryDate", true)};
        ProductBatchResultSet set = new ProductBatchResultSet(shortName(ProductArchetypes.PRODUCT_BATCH),
                                                              null, null, null, null, null, null, null, null, sort, 20);
        checkOrder(set, batch2, batch1);
    }

    /**
     * Tests sorting on product.
     */
    @Test
    public void testSortOnProduct() {
        Product product1 = createProduct("Z Test Product B");
        Product product2 = createProduct("Z Test Product A");
        Entity batch1 = createBatch("batch1", product1, DateRules.getTomorrow());
        Entity batch2 = createBatch("batch2", product2, DateRules.getToday());

        SortConstraint[] sort = {Constraints.sort("product", true)};
        ProductBatchResultSet set = new ProductBatchResultSet(shortName(ProductArchetypes.PRODUCT_BATCH),
                                                              null, null, null, null, null, null, null, null, sort, 20);
        checkOrder(set, batch2, batch1);
    }

    /**
     * Verifies batches are returned in the correct order.
     *
     * @param set     the set
     * @param batches the batches
     */
    private void checkOrder(ProductBatchResultSet set, Entity... batches) {
        int index = 0;
        ResultSetIterator<Entity> iterator = new ResultSetIterator<Entity>(set);
        while (iterator.hasNext()) {
            Entity next = iterator.next();
            if (next.getName().equals(batches[index].getName())) {
                index++;
                if (index == batches.length) {
                    break;
                }
            }
        }
        assertEquals(batches.length, index);
    }

    /**
     * Creates a new batch.
     *
     * @param batchNumber    the batch number
     * @param product        the product
     * @param expiryDate     the expiry date. May be {@code null}
     * @param stockLocations the stock locations
     * @return a new batch
     */
    private Entity createBatch(String batchNumber, Product product, Date expiryDate, Party... stockLocations) {
        Entity batch = (Entity) create(ProductArchetypes.PRODUCT_BATCH);
        EntityBean bean = new EntityBean(batch);
        bean.setValue("name", batchNumber);
        IMObjectRelationship relationship = bean.addNodeTarget("product", product);
        IMObjectBean relBean = new IMObjectBean(relationship);
        relBean.setValue("activeEndTime", expiryDate);
        for (Party stockLocation : stockLocations) {
            bean.addNodeTarget("stockLocations", stockLocation);
        }
        save(batch, product);
        return batch;
    }

    private void addStockLocation(Entity batch, Party stockLocation, Date endTime) {
        EntityBean bean = new EntityBean(batch);
        EntityLink relationship = (EntityLink) bean.addNodeTarget("stockLocations", stockLocation);
        relationship.setActiveEndTime(endTime);
        if (endTime != null) {
            // make sure the start time is valid
            relationship.setActiveStartTime(DateRules.getPreviousDate(endTime));
        }
        bean.save();
    }

    /**
     * Creates a product.
     *
     * @param name the product name
     * @return a new product
     */
    private Product createProduct(String name) {
        Product product = TestHelper.createProduct();
        product.setName(name);
        save(product);
        return product;
    }

    /**
     * Adds a manufacturer to a batch.
     *
     * @param batch the batch
     * @return the manufacturer
     */
    private Party addManufacturer(Entity batch) {
        Party result = (Party) create(SupplierArchetypes.MANUFACTURER);
        result.setName("ZManufacturer-" + System.currentTimeMillis());
        EntityBean bean = new EntityBean(batch);
        bean.addNodeTarget("manufacturer", result);
        save(batch, result);
        return result;
    }

    /**
     * Verifies that batches are included/excluded as expected for an expiry date.
     *
     * @param from    the expiry date start range. May be {@code null}
     * @param to      the expiry date to range. May be {@code null}
     * @param include if {@code true} batches should be included, {@code false} if they should be excluded
     * @param batches the batches to check
     */
    private void checkExpiry(Date from, Date to, boolean include, Entity... batches) {
        ProductBatchResultSet set = new ProductBatchResultSet(
                shortName(ProductArchetypes.PRODUCT_BATCH), null, null, null, from, to, null, null, null, null, 20);
        checkBatches(set, include, batches);
    }

    /**
     * Verifies that batches are included/excluded as expected for a stock location.
     *
     * @param stockLocation the stock location. May be {@code null}
     * @param active        the from date. May be {@code null}
     * @param include       if {@code true} batches should be included, {@code false} if they should be excluded
     * @param batches       the batches to check
     */
    private void checkStockLocation(Party stockLocation, Date active, boolean include, Entity... batches) {
        IMObjectReference location = (stockLocation != null) ? stockLocation.getObjectReference() : null;
        ProductBatchResultSet set = new ProductBatchResultSet(shortName(ProductArchetypes.PRODUCT_BATCH),
                                                              null, null, null, null, null, location, active,
                                                              null, null, 20);
        checkBatches(set, include, batches);
    }

    /**
     * Verifies that batches are included/excluded as expected for a manufacturer.
     *
     * @param manufacturer the manufacturer. May be {@code null}
     * @param include      if {@code true} batches should be included, {@code false} if they should be excluded
     * @param batches      the batches to check
     */
    private void checkManufacturer(Party manufacturer, boolean include, Entity... batches) {
        ProductBatchResultSet set = new ProductBatchResultSet(shortName(ProductArchetypes.PRODUCT_BATCH),
                                                              null, null, null, null, null, null, null,
                                                              manufacturer, null, 20);
        checkBatches(set, include, batches);
    }

    private void checkBatches(ProductBatchResultSet set, boolean include, Entity[] batches) {
        List<IMObjectReference> objectRefs = QueryTestHelper.getObjectRefs(set);
        for (IMObject object : batches) {
            if (include) {
                assertTrue(object.getName() + " should be included", objectRefs.contains(object.getObjectReference()));
            } else {
                assertFalse(object.getName() + " should be excluded", objectRefs.contains(object.getObjectReference()));
            }
        }
    }

}
