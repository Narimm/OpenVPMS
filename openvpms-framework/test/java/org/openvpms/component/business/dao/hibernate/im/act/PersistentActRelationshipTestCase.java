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

//openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.lookup.HibernateLookupUtil;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Exercise the act and act relationships
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PersistentActRelationshipTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentActRelationshipTestCase.class);
    }

    /**
     * Constructor for PersistentParticipationTestCase.
     * 
     * @param name
     */
    public PersistentActRelationshipTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of an act relationship
     */
    public void testActRelationshipCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act1");
            Act tar = createAct("act2");
            session.save(src);
            session.save(tar);
            
            ActRelationship actRel = createActRelationship("dummy", src, tar);
            session.save(actRel);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
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
     * Test the deletion of an act relationship
     */
    public void testActRelationshipDeletion()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act1");
            Act tar = createAct("act2");
            session.save(src);
            session.save(tar);
            
            ActRelationship actRel1 = createActRelationship("dummy", src, tar);
            ActRelationship actRel2 = createActRelationship("dummy1", tar, src);
            session.save(actRel1);
            session.save(actRel2);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 2);
            
            tx = session.beginTransaction();
            session.delete(actRel1);
            tx.commit();
            
            // retest the counts
            acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
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
     * Test the modification of an act relationship
     */
    public void testActRelationshipModification()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act1");
            Act tar = createAct("act2");
            session.save(src);
            session.save(tar);
            
            ActRelationship actRel1 = createActRelationship("dummy", src, tar);
            session.save(actRel1);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
            
            tx = session.beginTransaction();
            actRel1 = (ActRelationship)session.load(ActRelationship.class, actRel1.getUid());
            assertTrue(actRel1 != null);
            actRel1.setSource(new IMObjectReference(tar));
            actRel1.setTarget(new IMObjectReference(src));
            session.saveOrUpdate(actRel1);
            tx.commit();
            
            // retest the counts
            acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
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
     * Test adding an act relationship to an act
     */
    public void testModActRelationshipToAct()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act1");
            Act tar = createAct("act2");
            session.save(src);
            session.save(tar);

            ActRelationship actRel1 = createActRelationship("dummy", src, tar);
            src.addActRelationship(actRel1);
            src.addActRelationship(createActRelationship("dummy1", tar, src));
            session.save(src);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 2);
            
            session.clear();
            tx = session.beginTransaction();
            src = (Act)session.load(Act.class, src.getUid());
            assertTrue(src != null);
            assertTrue(src.getActRelationships().size() == 2);
            src.removeActRelationship(actRel1);
            tx.commit();
            
            // retest the counts;
            acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
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
     * Test adding an act relationship to an act
     */
    public void testAddingActRelationshipToAct()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act1");
            Act tar = createAct("act2");
            session.save(src);
            session.save(tar);
            src.addActRelationship(createActRelationship("dummy", src, tar));
            session.save(src);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
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
     * Test that we can set the actrelationships without first saving the 
     * acts
     */
    public void testActRelationshipsToActsBeforeSave()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act11");
            Act tar = createAct("act22");
            src.addActRelationship(createActRelationship("dummy", src, tar));
            session.save(src);
            session.save(tar);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
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
     * Test that OVPMS-219 bug
     */
    public void testOVPMS219()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // step 1
            tx = session.beginTransaction();
            Act src = createAct("act11");
            session.save(src);
            tx.commit();
            
            // step 2
            tx = session.beginTransaction();
            Act tar = createAct("act22");
            session.save(tar);
            tx.commit();
            
            // step 3, 4 and 5
            tx = session.beginTransaction();
            src.addActRelationship(createActRelationship("dummy", src, tar));
            session.save(src);
            tx.commit();
            
            // step 6
            tx = session.beginTransaction();
            session.save(tar);
            tx.commit();
            
            // step 7
            tx = session.beginTransaction();
            session.save(src);
            tx.commit();
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
     * Test adding an act relationship to an act
     */
    public void testMod2ActRelationshipToAct()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act1");
            Act tar = createAct("act2");
            session.save(src);
            session.save(tar);

            ActRelationship actRel1 = createActRelationship("dummy", src, tar);
            src.addActRelationship(actRel1);
            src.addActRelationship(createActRelationship("dummy1", tar, src));
            session.save(src);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 2);
            
            session.clear();
            tx = session.beginTransaction();
            tar = (Act)session.load(Act.class, tar.getUid());
            assertTrue(tar != null);
            assertTrue(tar.getActRelationships().size() == 2);
            tar.removeActRelationship(actRel1);
            tx.commit();
            
            // retest the counts;
            acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 1);
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
     * Test deletion of an act when act relationships are associated with it.
     */
    public void testDeleteActRelationshipAndAct()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in tables
            int acount = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount = HibernateLookupUtil.getTableRowCount(session, "actRelationship");

            // execute the test
            tx = session.beginTransaction();
            Act src = createAct("act1");
            Act tar = createAct("act2");
            session.save(src);
            session.save(tar);

            ActRelationship actRel1 = createActRelationship("dummy", src, tar);
            src.addActRelationship(actRel1);
            src.addActRelationship(createActRelationship("dummy1", tar, src));
            session.save(src);
            tx.commit();
            
            // test the counts
            int acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            int arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 2);
            assertTrue(arcount1 == arcount + 2);
            
            // now delete the src act. We are hoping that one act remains but 
            // that both relationships are also deleted
            session.clear();
            tx = session.beginTransaction();
            src = (Act)session.load(Act.class, src.getUid());
            session.delete(src);
            tx.commit();
            
            // retest the counts;
            acount1 = HibernateLookupUtil.getTableRowCount(session, "act");
            arcount1 = HibernateLookupUtil.getTableRowCount(session, "actRelationship");
            assertTrue(acount1 == acount + 1);
            assertTrue(arcount1 == arcount);
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
     * Create a simple actrelation
     *
     * @param name
     *          the name of the act
     * @return Act
     */
    private Act createAct(String name) {
        Act act = new Act();
        act.setArchetypeId(new ArchetypeId("openvpms-party-act.simple.1.0"));
        act.setName(name);
        
        return act;
    }
    
    /**
     * Create an act relationship between the source and target acts
     * 
     * @param source
     * @param target
     * @return ActRelationship
     */
    private ActRelationship createActRelationship(String name, Act source, Act target) {
        ActRelationship rel = new ActRelationship();
        rel.setArchetypeId(new ArchetypeId("openvpms-party-act.simpleRel.1.0"));
        rel.setName(name);
        rel.setSource(new IMObjectReference(source));
        rel.setTarget(new IMObjectReference(target));
        
        return rel;
    }
}
