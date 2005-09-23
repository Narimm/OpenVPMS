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
import java.util.Iterator;

import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

//openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;

/**
 * Exercise the persistent aspects of the {@link LookupRelationship} class
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PersistentLookupRelationshipTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentLookupRelationshipTestCase.class);
    }

    /**
     * Constructor for PersistentParticipationTestCase.
     * 
     * @param name
     */
    public PersistentLookupRelationshipTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of a simple lookup relationship between country and
     * state
     */
    public void testSimpleLookupRelationshipCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int lcount = HibernateLookupUtil.getTableRowCount(session, "lookup");
            int lrcount = HibernateLookupUtil.getTableRowCount(session, "lookupRelationship");

            // execute the test
            tx = session.beginTransaction();
            Lookup country = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("country"), "Australia", "AU");
            Lookup state1 = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("state"), "Victoria", "VIC");
            Lookup state2 = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("state"), "New South Wales", "NWS");
            Lookup state3 = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("state"), "Tasmania", "TAS");
            Lookup state4 = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("state"), "South Australia", "SA");
            session.save(state1);
            session.save(state2);
            session.save(state3);
            session.save(state4);
            session.save(country);
            
            // create the relationship
            LookupRelationship relationship = new LookupRelationship(country, state1);
            session.save(relationship);
            relationship = new LookupRelationship(country, state2);
            session.save(relationship);
            relationship = new LookupRelationship(country, state3);
            session.save(relationship);
            relationship = new LookupRelationship(country, state4);
            session.save(relationship);
            tx.commit();

            // ensure that the correct number of rows have been inserted
            int lcount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            int lrcount1 = HibernateLookupUtil.getTableRowCount(session, "lookupRelationship");
            assertTrue(lcount1 == lcount + 5);
            assertTrue(lrcount1 == lrcount + 4);
            
            // now and retrieve the realtionships from the source side
            Query query = session.getNamedQuery("lookupRelationship.getTargetLookups");
            query.setParameter("source", country.getUid());
            query.setParameter("type", "country.state");
            assertTrue(query.list().size() == 4);
            
            // now and retrieve the realtionships from the target side
            query = session.getNamedQuery("lookupRelationship.getSourceLookups");
            query.setParameter("target", state1.getUid());
            query.setParameter("type", "country.state");
            assertTrue(query.list().size() == 1);
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
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int lcount = HibernateLookupUtil.getTableRowCount(session, "lookup");
            int lrcount = HibernateLookupUtil.getTableRowCount(session, "lookupRelationship");

            // execute the test
            tx = session.beginTransaction();
            Lookup country = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("country"), "Greece", "GRE");
            Lookup city1 = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("city"), "Athens", null);
            Lookup city2 = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("city"), "Hellas", null);
            Lookup city3 = new Lookup(getGenerator().nextId(), 
                    createLookArchetypeId("city"), "Limnos", null);
            session.save(city1);
            session.save(city2);
            session.save(city3);
            session.save(country);
            
            // create the relationship
            LookupRelationship relationship = new LookupRelationship(country, city1);
            session.save(relationship);
            relationship = new LookupRelationship(country, city2);
            session.save(relationship);
            relationship = new LookupRelationship(country, city3);
            session.save(relationship);
            tx.commit();

            // ensure that the correct number of rows have been inserted
            int lcount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            int lrcount1 = HibernateLookupUtil.getTableRowCount(session, "lookupRelationship");
            assertTrue(lcount1 == lcount + 4);
            assertTrue(lrcount1 == lrcount + 3);
            
            // now delete the entity and all its relationships
            Lookup lookup = (Lookup)session.load(Lookup.class, country.getUid());
            tx = session.beginTransaction();
            session.delete(lookup);
            Query query = session.getNamedQuery("lookupRelationship.getAllRelationshipsWithId");
            query.setParameter("id", country.getUid());
            if (query.list().size() > 0) {
                Iterator iter = query.iterate();
                while (iter.hasNext()) {
                    session.delete(iter.next());
                }
            }
            
            // ensure that the correct number of rows have been deleted
            lcount1 = HibernateLookupUtil.getTableRowCount(session, "lookup");
            lrcount1 = HibernateLookupUtil.getTableRowCount(session, "lookupRelationship");
            assertTrue(lcount1 == lcount + 3);
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
