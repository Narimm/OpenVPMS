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

// hibernate
import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;

/**
 * Test the actor and actor-role relationship
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentProductTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentProductTestCase.class);
    }

    /**
     * Constructor for PersistentProductTestCase.
     * 
     * @param arg0
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
            product.setDetails(new DynamicAttributeMap());
            product.getDetails().setAttribute("supplier", "jima");
            product.getDetails().setAttribute("code", "1123");
            product.getDetails().setAttribute("overpriced", new Boolean(true));
            session.save(product);
            tx.commit();

            // ensure that the appropriate rows have been added to the database
            int acount1 = HibernateProductUtil.getTableRowCount(session, "product");
            assertTrue(acount1 == acount + 1); 
            
            product = (Product)session.load(Product.class, product.getUid());
            assertTrue(product != null);
            assertTrue(product.getDetails().getAttribute("supplier").equals("jima"));
            assertTrue(product.getDetails().getAttribute("code").equals("1123"));
            assertTrue(((Boolean)product.getDetails().getAttribute("overpriced")).booleanValue() == true);
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
                    assertTrue(item.isFixed() == false);
                } else {
                    assertTrue(item.isFixed() == true);
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

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }

}
