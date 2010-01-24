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

package org.openvpms.archetype.rules.stock;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.math.BigDecimal;


/**
 * Tests the {@link StockRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockRulesTestCase extends AbstractStockTest {

    /**
     * The rules.
     */
    private StockRules rules;


    /**
     * Tests the {@link StockRules#getStockLocation(Product, Party)} method.
     */
    @Test
    public void testGetStockLocation() {
        // set up a location with 2 stock locations
        Party location = TestHelper.createLocation();
        Party stockLocation1 = createStockLocation();
        Party stockLocation2 = createStockLocation();

        EntityBean locationBean = new EntityBean(location);
        locationBean.addRelationship("entityRelationship.locationStockLocation",
                                     stockLocation1);
        locationBean.addRelationship("entityRelationship.locationStockLocation",
                                     stockLocation2);
        save(location);
        save(stockLocation1);
        save(stockLocation2);

        Product product = TestHelper.createProduct();

        // by default, stock control is disabled, so no stock location
        // should be returned for the product
        assertFalse(locationBean.getBoolean("stockControl"));
        assertNull(rules.getStockLocation(product, location));

        // now enable stock control
        locationBean.setValue("stockControl", true);

        // should return either stock location
        assertNotNull(rules.getStockLocation(product, location));

        // now create a relationship with stockLocation2
        rules.updateStock(product, stockLocation2, BigDecimal.ONE);

        // verify stockLocation2 now returned for the product
        assertEquals(stockLocation2, rules.getStockLocation(product, location));
    }

    /**
     * Tests the {@link StockRules#getStock(Product, Party)} and
     * {@link StockRules#updateStock(Product, Party, BigDecimal)} methods.
     */
    @Test
    public void testGetAndUpdateStock() {
        BigDecimal quantity = new BigDecimal("10.00");

        Party stockLocation = createStockLocation();
        Product product = TestHelper.createProduct();

        // no product-stock location relationship to begin with
        assertEquals(BigDecimal.ZERO, rules.getStock(product, stockLocation));

        // add stock and verify it is added
        rules.updateStock(product, stockLocation, quantity);
        assertEquals(quantity, rules.getStock(product, stockLocation));

        // remove stock and verify it is removed
        rules.updateStock(product, stockLocation, quantity.negate());
        assertEquals(BigDecimal.ZERO, rules.getStock(product, stockLocation));
    }

    /**
     * Tests the {@link StockRules#transferStock} method.
     */
    @Test
    public void testTransferStock() {
        BigDecimal quantity = new BigDecimal("10.00");
        Party from = createStockLocation();
        Party to = createStockLocation();
        Product product = TestHelper.createProduct();

        rules.transfer(product, from, to, quantity);
        assertEquals(quantity.negate(), rules.getStock(product, from));
        assertEquals(quantity, rules.getStock(product, to));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new StockRules();
    }
}
