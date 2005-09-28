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


package org.openvpms.component.business.dao.hibernate.im.lookup;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;

//openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;

/**
 * Exercise the persistent aspects of the {@link Lookup} class
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PersistentLookupTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentLookupTestCase.class);
    }

    /**
     * Constructor for PersistentParticipationTestCase.
     * 
     * @param name
     */
    public PersistentLookupTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of a simple lookup
     */
    public void testSimpleLookupCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernateLookupUtil.getTableRowCount(session, "lookup");

            // execute the test
            tx = session.beginTransaction();
            Lookup lookup = new Lookup( 
                    createLookArchetypeId("breed"), "dog", "dog");
            session.save(lookup);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            assertTrue(acount1 == acount + 1);
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
     * Test the creation, find and update of a lookup entity
     */
    public void testSimpleLookupUpdate()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in lookup table
            int acount = HibernateLookupUtil.getTableRowCount(session, "lookup");

            // execute the test
            tx = session.beginTransaction();
            Lookup lookup = new Lookup( 
                    createLookArchetypeId("breed"), "jimmy", null);
            session.save(lookup);
            tx.commit();

            // ensure that there is still one more lookup
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            assertTrue(acount1 == acount + 1);
            
            // retrieve update and save
            tx = session.beginTransaction();
            lookup = (Lookup)session.load(Lookup.class, lookup.getUid());
            lookup.setCode(lookup.getValue());
            session.update(lookup);
            tx.commit();
            
            // ensure that there is still one more lookup
            acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            assertTrue(acount1 == acount + 1);
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
     * Test the creation and deletion of a simple lookup
     */
    public void testSimpleLookupDeletion()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in lookup table
            int acount = HibernateLookupUtil.getTableRowCount(session, "lookup");

            // execute the test
            tx = session.beginTransaction();
            Lookup lookup = new Lookup( 
                    createLookArchetypeId("breed"), "johnny", null);
            session.save(lookup);
            tx.commit();

            // ensure that there is still one more lookup
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            assertTrue(acount1 == acount + 1);
            
            // retrieve update and save
            tx = session.beginTransaction();
            lookup = (Lookup)session.load(Lookup.class, lookup.getUid());
            session.delete(lookup);
            tx.commit();
            
            // ensure that there is still one more lookup
            acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            assertTrue(acount1 == acount);
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
     * Test the creation of a simple lookup
     */
    public void testCategoryLookupCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernateLookupUtil.getTableRowCount(session, "lookup");

            // execute the test
            tx = session.beginTransaction();
            Lookup lookup = new Lookup( 
                    createLookArchetypeId("species"), "dog", "dog");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("species"), "cat", "cat");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("species"), "mouse", "mouse");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("species"), "rabbit", "rabbit");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("species"), "cow", "cow");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("species"), "donkey", "donkey");
            session.save(lookup);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            assertTrue(acount1 == acount + 6);
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
     * Test the creation of a simple lookup
     */
    public void testMultipleCategoryLookupCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernateLookupUtil.getTableRowCount(session, "lookup");

            // execute the test
            tx = session.beginTransaction();
            Lookup lookup = new Lookup( 
                    createLookArchetypeId("colour"), "red", "red");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("colour"), "green", "green");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("colour"), "blue", "blue");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("colour"), "rabbit", "rabbit");
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("title"), "Mr", null);
            session.save(lookup);
            lookup = new Lookup( 
                    createLookArchetypeId("title"), "Mrs", null);
            session.save(lookup);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            assertTrue(acount1 == acount + 6);
        } catch (Exception exception) {
            if (tx != null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
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
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data for this
    }

    /**
     * Create an archetype id for the specified concept
     * 
     * @param concept
     *            the concept to create
     * @return ArchetypeId
     */
    private ArchetypeId createLookArchetypeId(String concept) {
        return new ArchetypeId("openvpms-lookup-lookup." + concept + ".1.0");
    }
}
