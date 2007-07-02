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


package org.openvpms.component.business.dao.hibernate.im.act;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.HibernateUtil;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;

/**
 * Exercise the act entities
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PersistentActTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentActTestCase.class);
    }

    /**
     * Constructor for PersistentParticipationTestCase.
     * 
     * @param name
     */
    public PersistentActTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of an act
     */
    public void testActCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of entries in tables
            int acount = HibernateUtil.getTableRowCount(session, "act");

            // execute the test
            tx = session.beginTransaction();
            Act act1 = createAct("act1");
            Act act2 = createAct("act2");
            Act act3 = createAct("act3");
            session.save(act1);
            session.save(act2);
            session.save(act3);
            tx.commit();

            // ensure that the correct number of rows have been inserted
            int acount1 = HibernateUtil.getTableRowCount(session, "act");
            assertTrue(acount1 == acount + 3);
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
     * Test the modification of an act
     */
    public void testActModification()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateUtil.getTableRowCount(session, "act");

            // execute the test
            tx = session.beginTransaction();
            Act act1 = createAct("mact1");
            Act act2 = createAct("mact2");
            session.save(act1);
            session.save(act2);
            tx.commit();

            // ensure that the correct number of rows have been inserted
            int acount1 = HibernateUtil.getTableRowCount(session, "act");
            assertTrue(acount1 == acount + 2);
            
            // modify the act
            tx = session.beginTransaction();
            act1 = (Act)session.load(Act.class, act1.getUid());
            assertTrue(act1 != null);
            act1.setDescription("my first act");
            session.saveOrUpdate(act1);
            tx.commit();
            
            acount1 = HibernateUtil.getTableRowCount(session, "act");
            assertTrue(acount1 == acount + 2);
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
     * Test the deletion of an act
     */
    public void testActDeletion()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateUtil.getTableRowCount(session, "act");

            // execute the test
            tx = session.beginTransaction();
            Act act1 = createAct("dact1");
            Act act2 = createAct("dact2");
            Act act3 = createAct("dact3");
            session.save(act1);
            session.save(act2);
            session.save(act3);
            tx.commit();

            // ensure that the correct number of rows have been inserted
            int acount1 = HibernateUtil.getTableRowCount(session, "act");
            assertTrue(acount1 == acount + 3);
            
            // delete a couple of acts
            tx = session.beginTransaction();
            session.delete(act1);
            session.delete(act2);
            tx.commit();
            
            acount1 = HibernateUtil.getTableRowCount(session, "act");
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
     * Create a simple act
     *
     * @param name
     *          the name of the act
     * @return Act
     */
    private Act createAct(String name) {
        Act act = new Act();
        act.setArchetypeId(new ArchetypeId("act.simple.1.0"));
        act.setName(name);
        
        return act;
    }
}
