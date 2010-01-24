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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.product;

import org.hibernate.Session;
import org.hibernate.Transaction;
import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOHelper;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Tests the {@link ProductDOImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ProductDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of products in the database.
     */
    private int products;

    /**
     * The initial no. of product prices in the database.
     */
    private int prices;

    /**
     * The initial no. of lookups in the database.
     */
    private int lookups;


    /**
     * Test the creation of a product.
     */
    @Test
    public void testCreate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ProductDO product = createProduct("pill");
        product.addProductPrice(createProductPrice(1000, true));
        product.addProductPrice(createProductPrice(100, true));
        product.addProductPrice(createProductPrice(10, true));
        product.addProductPrice(createProductPrice(1, false));
        session.save(product);
        tx.commit();

        product = reload(product);
        assertNotNull(product);

        // iterate through the product prics and ensure that all
        // fixed indicators are true except the one with the price
        // of 1.
        for (ProductPriceDO prices : product.getProductPrices()) {
            if (prices.getPrice().intValue() == 1) {
                assertFalse(prices.isFixed());
            } else {
                assertTrue(prices.isFixed());
            }
        }

        // check row counts
        assertEquals(products + 1, count(ProductDOImpl.class));
        assertEquals(prices + 4, count(ProductPriceDOImpl.class));
    }

    /**
     * Test the creation of a product with some dynamic attributes
     */
    @Test
    public void testProductCreationWithDynamicAttributes() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ProductDO product = createProduct("pill");
        product.getDetails().put("supplier", "jima");
        product.getDetails().put("code", "1123");
        product.getDetails().put("overpriced", true);
        session.save(product);
        tx.commit();

        product = reload(product);
        assertNotNull(product);
        assertTrue(product.getDetails().get("supplier").equals("jima"));
        assertTrue(product.getDetails().get("code").equals("1123"));
        assertEquals(true, product.getDetails().get("overpriced"));

        assertEquals(products + 1, count(ProductDOImpl.class));
    }

    /**
     * Tests the deletion of a product.
     */
    @Test
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ProductDO product = createProduct("product-lifecycle");
        product.addProductPrice(createProductPrice(1000, true));
        product.addProductPrice(createProductPrice(100, true));
        session.save(product);
        tx.commit();

        // check row counts
        assertEquals(products + 1, count(ProductDOImpl.class));
        assertEquals(prices + 2, count(ProductPriceDOImpl.class));

        tx = session.beginTransaction();
        product = (ProductDO) session.load(ProductDOImpl.class, product.getId());
        assertNotNull(product);
        session.delete(product);
        tx.commit();

        // check row counts
        assertEquals(products, count(ProductDOImpl.class));
        assertEquals(prices, count(ProductPriceDOImpl.class));
    }

    /**
     * Test name and description fields are being stored and retrieved.
     */
    @Test
    public void testOVPMS134() {
        String name = "gum";
        String description = "this is really nice gum";

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ProductPriceDO price = createProductPrice(1000, true);
        price.setName(name);
        price.setDescription(description);
        session.save(price);
        tx.commit();

        // ensure that the appropriate rows have been added to the database
        assertEquals(prices + 1, count(ProductPriceDOImpl.class));
        price = (ProductPriceDO) session.load(ProductPriceDOImpl.class,
                                              price.getId());
        assertNotNull(price);
        assertEquals(name, price.getName());
        assertEquals(description, price.getDescription());
    }

    /**
     * Test the lifecycle of product prics.
     */
    @Test
    public void testProductPricesLifecycle() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ProductDO product = createProduct("type-of-pill");
        product.addProductPrice(createProductPrice(1000, true));
        product.addProductPrice(createProductPrice(100, true));
        product.addProductPrice(createProductPrice(10, true));
        product.addProductPrice(createProductPrice(1, false));
        session.save(product);
        tx.commit();

        // check row counts
        assertEquals(products + 1, count(ProductDOImpl.class));
        assertEquals(prices + 4, count(ProductPriceDOImpl.class));

        product = (ProductDO) session.load(ProductDOImpl.class, product.getId());
        assertNotNull(product);
        assertEquals(4, product.getProductPrices().size());

        // delete the first entry
        tx = session.beginTransaction();
        ProductPriceDO entry = product.getProductPrices().iterator().next();
        product.removeProductPrice(entry);
        assertEquals(3, product.getProductPrices().size());
        session.save(product);
        tx.commit();

        // retrieve again and check
        product = (ProductDO) session.load(ProductDOImpl.class, product.getId());
        assertNotNull(product);
        assertEquals(3, product.getProductPrices().size());
    }

    /**
     * Test the lifecycle of product price classifications.
     */
    @Test
    public void testProductPricesClassificationLifecycle() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ProductDO product = createProduct("type-of-pill");
        ProductPriceDO price = createProductPrice(1000, true);
        LookupDO tax = createClassification("gst");
        session.save(tax);
        price.addClassification(tax);
        product.addProductPrice(price);
        session.save(product);
        tx.commit();

        // check row counts
        assertEquals(products + 1, count(ProductDOImpl.class));
        assertEquals(prices + 1, count(ProductPriceDOImpl.class));
        assertEquals(lookups + 1, count(LookupDOImpl.class));

        // retrieve the product
        product = (ProductDO) session.load(ProductDOImpl.class,
                                           product.getId());
        assertNotNull(product);
        assertEquals(1, product.getProductPrices().size());

        // add another product price classification
        tx = session.beginTransaction();
        ProductPriceDO price1 = product.getProductPrices().iterator().next();
        LookupDO exempt = createClassification("exempt");
        session.save(exempt);
        price1.addClassification(exempt);
        session.save(price1);
        tx.commit();

        // check that there is only one productprice and 2 lookups
        assertEquals(prices + 1, count(ProductPriceDOImpl.class));
        assertEquals(lookups + 2, count(LookupDOImpl.class));

        // retrieve the price and make sure there are 2 lookups
        price = (ProductPriceDO) session.get(ProductPriceDOImpl.class,
                                             price1.getId());
        assertEquals(2, price.getClassifications().size());

        // delete a classification
        tx = session.beginTransaction();
        LookupDO entry = price.getClassifications().iterator().next();
        price.removeClassification(entry);
        assertEquals(1, price.getClassifications().size());
        session.save(price);
        tx.commit();

        // retrieve the productprice and make sure there is 1 lookup
        price1 = (ProductPriceDO) session.get(ProductPriceDOImpl.class,
                                              price.getId());
        assertEquals(1, price1.getClassifications().size());
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        products = count(ProductDOImpl.class);
        prices = count(ProductPriceDOImpl.class);
        lookups = count(LookupDOImpl.class);
    }

    /**
     * Creates a product.
     *
     * @param name the name of the product
     * @return a new product
     */
    private ProductDO createProduct(String name) {
        ProductDO product
                = new ProductDOImpl(new ArchetypeId("product.basic.1.0"));
        product.setName(name);
        return product;
    }

    /**
     * Creates a product price
     *
     * @param price the price of the product
     * @param fixed indicates whether it is a fixed price
     * @return a new price
     */
    private ProductPriceDO createProductPrice(int price, boolean fixed) {
        ProductPriceDO result = new ProductPriceDOImpl(
                new ArchetypeId("productPrice.basic.1.0"));
        result.setName("price");
        result.setFromDate(new Date());
        result.setToDate(new Date());
        result.setFixed(fixed);
        result.setPrice(new BigDecimal(price));
        return result;
    }

    /**
     * Creates a simple classification lookup with the specified code.
     *
     * @param code the code of the classification
     * @return a new lookup
     */
    private LookupDO createClassification(String code) {
        return LookupDOHelper.createLookup("lookup.classification", code);
    }

}
