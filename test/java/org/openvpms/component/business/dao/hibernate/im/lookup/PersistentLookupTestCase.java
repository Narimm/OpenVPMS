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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.HibernateUtil;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.LookupUtil;


/**
 * Exercise the persistent aspects of the {@link Lookup} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentLookupTestCase extends HibernateInfoModelTestCase {

    /**
     * Test the creation of a simple lookup.
     */
    public void testSimpleLookupCreation() throws Exception {
        Session session = getSession();
        Transaction tx;

        // get initial count of lookups.
        int acount = HibernateUtil.getTableRowCount(session, "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = LookupUtil.createLookup("lookup.breed", "DOG", "dog");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        int acount1 = HibernateUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test the creation, find and update of a lookup entity
     */
    public void testSimpleLookupUpdate() throws Exception {
        Session session = getSession();
        Transaction tx;

        // get initial number of entries in lookup table
        int acount = HibernateUtil.getTableRowCount(session,
                                                    "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = LookupUtil.createLookup("lookup.breed", "JIMMY");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        int acount1 = HibernateUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);

        // retrieve update and save
        tx = session.beginTransaction();
        lookup = (Lookup) session.load(Lookup.class, lookup.getUid());
        lookup.setCode(lookup.getCode());
        session.update(lookup);
        tx.commit();

        // ensure that there is one more lookup
        acount1 = HibernateUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test the creation and deletion of a simple lookup
     */
    public void testSimpleLookupDeletion()
            throws Exception {
        Session session = getSession();
        Transaction tx;

        // get initial number of entries in lookup table
        int acount = HibernateUtil.getTableRowCount(session,
                                                    "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = LookupUtil.createLookup("lookup.breed", "JOHHNY");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        int acount1 = HibernateUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);

        // retrieve update and save
        tx = session.beginTransaction();
        lookup = (Lookup) session.load(Lookup.class, lookup.getUid());
        session.delete(lookup);
        tx.commit();

        // check lookup count
        acount1 = HibernateUtil.getTableRowCount(session, "lookup");
        assertEquals(acount, acount1);
    }

    /**
     * Test the creation of a simple lookup.
     */
    public void testCategoryLookupCreation() throws Exception {
        Session session = getSession();
        Transaction tx;

        // get initial number of lookups
        int acount = HibernateUtil.getTableRowCount(session,
                                                    "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = LookupUtil.createLookup("species", "DOG", "dog");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.species", "CAT", "cat");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.species", "MOUSE", "mouse");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.species", "RABBIT", "rabbit");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.species", "COW", "cow");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.species", "DONKEY", "donkey");
        session.save(lookup);
        tx.commit();

        // ensure that there is correct no. of lookups
        int acount1 = HibernateUtil.getTableRowCount(session,
                                                     "lookup");
        assertEquals(acount + 6, acount1);
    }

    /**
     * Test lookup equality.
     */
    public void testOVPMS84() throws Exception {
        Lookup lookup = LookupUtil.createLookup("lookup.species", "DOG");
        Lookup copy = (Lookup) lookup.clone();
        assertTrue(lookup.equals(copy));

        copy.setCode("doggy");
        assertFalse(lookup.equals(copy));
    }

    /**
     * Test persistent lookup equality.
     */
    public void testPersistentLookupEquality() throws Exception {
        Session session = getSession();
        Transaction tx;
        tx = session.beginTransaction();
        Lookup lookup = LookupUtil.createLookup("lookup.equality", "FOO");
        session.save(lookup);
        tx.commit();
        closeSession();
        session = getSession();
        Lookup lookup2 = (Lookup) session.load(Lookup.class, lookup.getUid());
        assertEquals(lookup, lookup2);
    }

    /**
     * Test the creation of a simple lookup
     */
    public void testMultipleCategoryLookupCreation()
            throws Exception {
        Session session = getSession();
        Transaction tx;

        // get initial no. of lookups
        int acount = HibernateUtil.getTableRowCount(session, "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = LookupUtil.createLookup("lookup.colour", "RED", "red");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.colour", "GREEN", "green");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.colour", "BLUE", "blue");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.species", "RABBIT", "rabbit");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.title", "MR");
        session.save(lookup);
        lookup = LookupUtil.createLookup("lookup.title", "MRS");
        session.save(lookup);
        tx.commit();

        // check expected count of lookups
        int acount1 = HibernateUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 6, acount1);
    }

    /* (non-Javadoc)
    * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
    */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data for this
    }

}
