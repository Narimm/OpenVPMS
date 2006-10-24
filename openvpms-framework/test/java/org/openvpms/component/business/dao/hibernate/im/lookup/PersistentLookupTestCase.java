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
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;


/**
 * Exercise the persistent aspects of the {@link Lookup} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentLookupTestCase extends HibernateInfoModelTestCase {

    /**
     * The current session.
     */
    private Session session;

    /**
     * Constructor for PersistentParticipationTestCase.
     *
     * @param name
     */
    public PersistentLookupTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of a simple lookup.
     */
    public void testSimpleLookupCreation() throws Exception {
        Transaction tx;

        // get initial count of lookups.
        int acount = HibernateLookupUtil.getTableRowCount(session, "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = new Lookup(
                createLookArchetypeId("breed"), "DOG", "dog");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test the creation, find and update of a lookup entity
     */
    public void testSimpleLookupUpdate() throws Exception {
        Transaction tx;

        // get initial number of entries in lookup table
        int acount = HibernateLookupUtil.getTableRowCount(session,
                                                          "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = new Lookup(createLookArchetypeId("breed"), "JIMMY");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);

        // retrieve update and save
        tx = session.beginTransaction();
        lookup = (Lookup) session.load(Lookup.class, lookup.getUid());
        lookup.setCode(lookup.getCode());
        session.update(lookup);
        tx.commit();

        // ensure that there is one more lookup
        acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test the creation and deletion of a simple lookup
     */
    public void testSimpleLookupDeletion()
            throws Exception {
        Session session = currentSession();
        Transaction tx;

        // get initial number of entries in lookup table
        int acount = HibernateLookupUtil.getTableRowCount(session,
                                                          "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = new Lookup(createLookArchetypeId("breed"), "JOHHNY");
        session.save(lookup);
        tx.commit();

        // ensure that there is one more lookup
        int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 1, acount1);

        // retrieve update and save
        tx = session.beginTransaction();
        lookup = (Lookup) session.load(Lookup.class, lookup.getUid());
        session.delete(lookup);
        tx.commit();

        // check lookup count
        acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
        assertEquals(acount, acount1);
    }

    /**
     * Test the creation of a simple lookup.
     */
    public void testCategoryLookupCreation() throws Exception {
        Transaction tx;

        // get initial number of lookups
        int acount = HibernateLookupUtil.getTableRowCount(session,
                                                          "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = new Lookup(
                createLookArchetypeId("species"), "DOG", "dog");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("species"), "CAT", "cat");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("species"), "MOUSE", "mouse");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("species"), "RABBIT", "rabbit");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("species"), "COW", "cow");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("species"), "DONKEY", "donkey");
        session.save(lookup);
        tx.commit();

        // ensure that there is correct no. of lookups
        int acount1 = HibernateLookupUtil.getTableRowCount(session,
                                                           "lookup");
        assertEquals(acount + 6, acount1);
    }

    /**
     * Test lookup equality.
     */
    public void testOVPMS84() throws Exception {
        Lookup lookup = new Lookup(createLookArchetypeId("species"), "DOG");
        Lookup copy = (Lookup) lookup.clone();
        assertTrue(lookup.equals(copy));

        copy.setCode("doggy");
        assertFalse(lookup.equals(copy));
    }

    /**
     * Test the creation of a simple lookup
     */
    public void testMultipleCategoryLookupCreation()
            throws Exception {
        Session session = currentSession();
        Transaction tx;

        // get initial no. of lookups
        int acount = HibernateLookupUtil.getTableRowCount(session, "lookup");

        // execute the test
        tx = session.beginTransaction();
        Lookup lookup = new Lookup(
                createLookArchetypeId("colour"), "RED", "red");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("colour"), "GREEN", "green");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("colour"), "BLUE", "blue");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("species"), "RABBIT", "rabbit");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("title"), "MR");
        session.save(lookup);
        lookup = new Lookup(
                createLookArchetypeId("title"), "MRS");
        session.save(lookup);
        tx.commit();

        // check expected count of lookups
        int acount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
        assertEquals(acount + 6, acount1);
    }

    /*
    * @see HibernateInfoModelTestCase#setUp()
    */
    protected void setUp() throws Exception {
        super.setUp();
        session = currentSession();
    }

    /*
     * @see HibernateInfoModelTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        closeSession();
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
     * @param concept the concept to create
     * @return ArchetypeId
     */
    private ArchetypeId createLookArchetypeId(String concept) {
        return new ArchetypeId("openvpms-lookup-lookup." + concept + ".1.0");
    }
}
