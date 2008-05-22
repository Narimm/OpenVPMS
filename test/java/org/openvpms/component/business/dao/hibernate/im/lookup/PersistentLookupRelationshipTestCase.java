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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.lookup.LookupUtil;

/**
 * Exercise the persistent aspects of the {@link LookupRelationship} class
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentLookupRelationshipTestCase
        extends HibernateInfoModelTestCase {

    /**
     * Test the creation of a simple lookup relationship between country and
     * state
     */
    public void testSimpleLookupRelationshipCreation()
            throws Exception {
        Session session = getSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int lcount = HibernateUtil.getTableRowCount(session, "lookup");
            int lrcount = HibernateUtil.getTableRowCount(session,
                                                         "lookupRelationship");

            // execute the test
            tx = session.beginTransaction();
            Lookup country = LookupUtil.createLookup("lookup.country", "AU",
                                                     "Australia");
            Lookup state1 = LookupUtil.createLookup("lookup.state", "VIC",
                                                                                                   "Victoria");
            Lookup state2 = LookupUtil.createLookup("lookup.state", "NSW",
                                                    "New South Wales");
            Lookup state3 = LookupUtil.createLookup("lookup.state", "TAS",
                                                    "Tasmania");
            Lookup state4 = LookupUtil.createLookup("lookup.state", "SA",
                                                    "South Australia");
            session.save(state1);
            session.save(state2);
            session.save(state3);
            session.save(state4);
            session.save(country);

            // create the relationship
            LookupRelationship relationship = createRelationship(country,
                                                                 state1);
            session.save(relationship);
            relationship = createRelationship(country, state2);
            session.save(relationship);
            relationship = createRelationship(country, state3);
            session.save(relationship);
            relationship = createRelationship(country, state4);
            session.save(relationship);
            tx.commit();

            // ensure that the correct number of rows have been inserted
            int lcount1 = HibernateUtil.getTableRowCount(session, "lookup");
            int lrcount1 = HibernateUtil.getTableRowCount(session,
                                                          "lookupRelationship");
            assertTrue(lcount1 == lcount + 5);
            assertTrue(lrcount1 == lrcount + 4);
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
     * Test the deletion of a lookup and all its relationships
     */
    public void testSimpleLookupRelationshipDeletion()
            throws Exception {
        Session session = getSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int lcount = HibernateUtil.getTableRowCount(session, "lookup");
            int lrcount = HibernateUtil.getTableRowCount(session,
                                                         "lookupRelationship");

            // execute the test
            tx = session.beginTransaction();
            Lookup country = LookupUtil.createLookup("lookup.xcountry", "GRE",
                                                     "Greece");
            Lookup city1 = LookupUtil.createLookup("lookup.xcity", "Athens",
                                                   null);
            Lookup city2 = LookupUtil.createLookup("lookup.xcity", "Hellas",
                                                   null);
            Lookup city3 = LookupUtil.createLookup("lookup.xcity", "Limnos",
                                                   null);
            session.save(city1);
            session.save(city2);
            session.save(city3);

            // create the relationship
            country.addLookupRelationship(createRelationship(country, city1));
            country.addLookupRelationship(createRelationship(country, city2));
            country.addLookupRelationship(createRelationship(country, city3));
            session.save(country);
            tx.commit();

            // ensure that the correct number of rows have been inserted
            int lcount1 = HibernateUtil.getTableRowCount(session, "lookup");
            int lrcount1 = HibernateUtil.getTableRowCount(session,
                                                          "lookupRelationship");
            assertTrue(lcount1 == lcount + 4);
            assertTrue(lrcount1 == lrcount + 3);

            // now delete the entity and all its relationships
            tx = session.beginTransaction();
            Lookup lookup = (Lookup) session.load(Lookup.class,
                                                  country.getUid());

            // iterate through all the relationships and remove them
            LookupRelationship[] rels = lookup.getSourceLookupRelationships()
                    .toArray(
                            new LookupRelationship[lookup.getSourceLookupRelationships().size()]);
            for (LookupRelationship rel : rels) {
                lookup.removeLookupRelationship(rel);
            }
            session.save(lookup);
            tx.commit();

            // ensure that the correct number of rows have been deleted
            lcount1 = HibernateUtil.getTableRowCount(session, "lookup");
            lrcount1 = HibernateUtil.getTableRowCount(session,
                                                      "lookupRelationship");
            assertTrue(lcount1 == lcount + 4);
            assertTrue(lrcount1 == lrcount);
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
     * Creates a new lookup relationship.
     *
     * @param country the country
     * @param state   the state
     * @return a new lookup relationship
     */
    private LookupRelationship createRelationship(Lookup country,
                                                  Lookup state) {

        LookupRelationship result = new LookupRelationship(country, state);
        ArchetypeId type
                = new ArchetypeId("lookupRelationship.countryState.1.0");
        result.setArchetypeId(type);
        return result;
    }

}
