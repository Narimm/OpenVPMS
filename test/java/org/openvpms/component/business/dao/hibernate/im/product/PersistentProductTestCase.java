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
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.party.HibernatePartyUtil;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;


/**
 * Test the actor and actor-role relationship
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class PersistentProductTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentProductTestCase.class);
    }

    /**
     * Constructor for PersistentProductTestCase.
     * 
     * @param name
     */
    public PersistentProductTestCase(String name) {
        super(name);
    }

    /*
     * @see HibernateInfoModelTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see HibernateInfoModelTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the simple creation of a product
     */
    public void testSimpleProductCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateProductUtil.getTableRowCount(session, "product");

            // execute the test
            tx = session.beginTransaction();
            Product product = createProduct("pill");
            session.save(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateProductUtil.getTableRowCount(session, "product");
            assertTrue(acount1 == acount + 1);

            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test the creation of a product with some dynamic attributes
     */
    public void testProductCreationWithDynamicAttributes() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int acount = HibernateProductUtil.getTableRowCount(session, "product");

            // execute the test
            tx = session.beginTransaction();
            Product product = createProduct("pill");
            product.setDetails(new HashMap<String, Object>());
            product.getDetails().put("supplier", "jima");
            product.getDetails().put("code", "1123");
            product.getDetails().put("overpriced", true);
            session.save(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateProductUtil.getTableRowCount(session, "product");
            assertTrue(acount1 == acount + 1);

            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
            assertTrue(product.getDetails().get("supplier").equals("jima"));
            assertTrue(product.getDetails().get("code").equals("1123"));
            assertEquals(true, product.getDetails().get("overpriced"));
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test the lifecycle of a product
     */
    public void testProductLifecycle() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int pcount = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount = HibernateProductUtil.getTableRowCount(session, "productPrice");

            // execute the test
            tx = session.beginTransaction();
            Product product = createProduct("product-lifecycle");
            product.addProductPrice(createProductPrice(1000, true));
            product.addProductPrice(createProductPrice(100, true));
            session.save(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int pcount1 = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount1 = HibernateProductUtil.getTableRowCount(session, "productPrice");
            assertTrue(pcount1 == pcount + 1);
            assertTrue(ppcount1 == ppcount + 2);

            tx = session.beginTransaction();
            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
            session.delete(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            pcount1 = HibernateProductUtil.getTableRowCount(session, "product");
            ppcount1 = HibernateProductUtil.getTableRowCount(session, "productPrice");
            assertTrue(pcount1 == pcount);
            assertTrue(ppcount1 == ppcount);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test name and description fields are being stored and retrieved
     */
    public void testOVPMS134()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int ppcount = HibernateProductUtil.getTableRowCount(session, "productPrice");

            // execute the test
            tx = session.beginTransaction();
            ProductPrice pprice = createProductPrice(1000, true);
            pprice.setName("gum");
            pprice.setDescription("this is really nice gum");
            session.save(pprice);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int ppcount1 = HibernateProductUtil.getTableRowCount(session, "productPrice");
            assertTrue(ppcount1 == ppcount + 1);

            pprice = (ProductPrice)session.load(ProductPrice.class, pprice.getUid());
            assertTrue(pprice != null);
            assertTrue(pprice.getName().equals("gum"));
            assertTrue(pprice.getDescription().equals("this is really nice gum"));
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test the create of product with product prices
     */
    public void testProductCreationWithProductPrices()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int pcount = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount = HibernateProductUtil.getTableRowCount(session, "productPrice");

            // execute the test
            tx = session.beginTransaction();
            Product product = createProduct("another-type-of-pill");
            product.addProductPrice(createProductPrice(1000, true));
            product.addProductPrice(createProductPrice(100, true));
            product.addProductPrice(createProductPrice(10, true));
            product.addProductPrice(createProductPrice(1, false));
            session.save(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int pcount1 = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount1 = HibernateProductUtil.getTableRowCount(session, "productPrice");
            assertTrue(pcount1 == pcount + 1);
            assertTrue(ppcount1 == ppcount + 4);

            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
            assertTrue(product.getProductPrices().size() == 4);

            // iterate through the product prics and ensure that all 
            // fixed indicators are true except the one with the price 
            // of 1.
            for (ProductPrice item : product.getProductPrices()) {
                if (item.getPrice().intValue() == 1) {
                    assertFalse(item.isFixed());
                } else {
                    assertTrue(item.isFixed());
                }
            }
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }


    /**
     * Test the lifecycle of product prics
     */
    public void testProductPricesLifecycle()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int pcount = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount = HibernateProductUtil.getTableRowCount(session, "productPrice");

            // execute the test
            tx = session.beginTransaction();
            Product product = createProduct("type-of-pill");
            product.addProductPrice(createProductPrice(1000, true));
            product.addProductPrice(createProductPrice(100, true));
            product.addProductPrice(createProductPrice(10, true));
            product.addProductPrice(createProductPrice(1, false));
            session.save(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int pcount1 = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount1 = HibernateProductUtil.getTableRowCount(session, "productPrice");
            assertTrue(pcount1 == pcount + 1);
            assertTrue(ppcount1 == ppcount + 4);

            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
            assertTrue(product.getProductPrices().size() == 4);

            // delete the first entry
            tx = session.beginTransaction();
            ProductPrice entry = product.getProductPrices().iterator().next();
            product.removeProductPrice(entry);
            assertTrue(product.getProductPrices().size() == 3);
            session.save(product);
            tx.commit();

            // retrieve again and check
            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
            assertTrue(product.getProductPrices().size() == 3);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test the lifecycle of product price classifications
     */
    public void testProductPricesClassificationLifecycle()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of rows
            int pcount = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount = HibernateProductUtil.getTableRowCount(session, "productPrice");
            int pppcount = HibernateProductUtil.getTableRowCount(session, "lookup");

            // Create product, price and classification
            tx = session.beginTransaction();
            Product product = createProduct("type-of-pill");
            ProductPrice price = createProductPrice(1000,true);
            Lookup tax = createClassification("gst");
            session.save(tax);
            price.addClassification(tax);
            product.addProductPrice(price);
            session.save(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int pcount1 = HibernateProductUtil.getTableRowCount(session, "product");
            int ppcount1 = HibernateProductUtil.getTableRowCount(session, "productPrice");
            int pppcount1 = HibernateProductUtil.getTableRowCount(session, "lookup");
            assertTrue(pcount1 == pcount + 1);
            assertTrue(ppcount1 == ppcount + 1);
            assertTrue(pppcount1 == pppcount + 1);

            // Retrieve the product 
            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
            assertTrue(product.getProductPrices().size() == 1);

            // Add another product price classification
            tx = session.beginTransaction();
            ProductPrice price1 = product.getProductPrices().iterator().next();
            Lookup exempt = createClassification("exempt");
            session.save(exempt);
            price1.addClassification(exempt);
            session.save(price1);
            tx.commit();

            // check that there is only one productprice and two classifications 
            ppcount1 = HibernatePartyUtil.getTableRowCount(session, "productPrice");
            pppcount1 = HibernatePartyUtil.getTableRowCount(session, "lookup");
            assertTrue(ppcount1 == ppcount + 1);
            assertTrue(pppcount1 == pppcount + 2);

            // retrieve the productprice and make sure there are 2 classifications
            price = (ProductPrice)session.get(ProductPrice.class, price1.getUid());
            assertTrue(price.getClassifications().size() == 2);

            // delete a classification
            tx = session.beginTransaction();
            Lookup entry = price.getClassifications().iterator().next();
            price.removeClassification(entry);
            assertTrue(price.getClassifications().size() == 1);
            session.save(price);
            tx.commit();

            // retrieve the productprice and make sure there are 1 classification
            price1 = (ProductPrice)session.get(ProductPrice.class, price.getUid());
            assertTrue(price1.getClassifications().size() == 1);

        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Create a simple product of the specified name
     * 
     * @param name
     *            the name of the product
     * @return Product
     */
    private Product createProduct(String name) throws Exception {
        Product product = new Product();
        product.setArchetypeIdAsString("openvpms-product-product.basic.1.0");
        product.setName(name);

        return product;
    }

    /**
     * Create a product price for the specified price
     * 
     * @param price
     *            the price of the product
     * @param fixed
     *            indicates whether it is a fied price            
     * @return ProductPrice
     */
    private ProductPrice createProductPrice(int price, boolean fixed) throws Exception {
        ProductPrice pp = new ProductPrice();
        pp.setArchetypeIdAsString("openvpms-product-productPrice.basic.1.0");
        pp.setName("price");
        pp.setFromDate(new Date());
        pp.setThruDate(new Date());
        pp.setFixed(fixed);
        pp.setPrice(new BigDecimal(price));

        return pp;
    }

    /**
     * Creates a simple classification lookup with the specified code.
     * 
     * @param code the code of the classification
     * @return a new lookup
     */
    private Lookup createClassification(String code) throws Exception {
        Lookup result = new Lookup();
        result.setArchetypeIdAsString("openvpms-productprice-classification.current.1.0");
        result.setCode(code);
        return result;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

}
