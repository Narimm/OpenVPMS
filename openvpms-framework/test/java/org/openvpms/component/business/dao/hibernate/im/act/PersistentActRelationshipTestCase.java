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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;


/**
 * Exercise the act and act relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentActRelationshipTestCase
        extends HibernateInfoModelTestCase {

    /**
     * Test the creation of an act relationship.
     */
    public void testActRelationshipCreation() {
        Session session = getSession();

        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        // execute the test
        Transaction tx = session.beginTransaction();
        Act src = createAct("act1");
        Act tar = createAct("act2");
        session.save(src);
        session.save(tar);

        ActRelationship actRel = createActRelationship("dummy", src, tar);
        session.save(actRel);
        tx.commit();

        // retest the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));
    }

    /**
     * Test the deletion of an act relationship.
     */
    public void testActRelationshipDeletion() {
        Session session = getSession();

        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        // execute the test
        Transaction tx = session.beginTransaction();
        Act src = createAct("act1");
        Act tar = createAct("act2");
        session.save(src);
        session.save(tar);

        ActRelationship actRel1 = createActRelationship("dummy", src, tar);
        ActRelationship actRel2 = createActRelationship("dummy1", tar, src);
        session.save(actRel1);
        session.save(actRel2);
        tx.commit();

        // retest the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 2, count(ActRelationship.class));

        tx = session.beginTransaction();
        session.delete(actRel1);
        tx.commit();

        // retest the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));
    }

    /**
     * Test the modification of an act relationship.
     */
    public void testActRelationshipModification() {
        Session session = getSession();
        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        // execute the test
        Transaction tx = session.beginTransaction();
        Act src = createAct("act1");
        Act tar = createAct("act2");
        session.save(src);
        session.save(tar);

        ActRelationship actRel1 = createActRelationship("dummy", src, tar);
        session.save(actRel1);
        tx.commit();

        // test the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));

        tx = session.beginTransaction();
        actRel1 = (ActRelationship) session.load(ActRelationship.class,
                                                 actRel1.getUid());
        assertNotNull(actRel1);
        actRel1.setSource(tar.getObjectReference());
        actRel1.setTarget(src.getObjectReference());
        session.saveOrUpdate(actRel1);
        tx.commit();

        // retest the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));
    }

    /**
     * Test adding an act relationship to an act.
     */
    public void testModActRelationshipToAct() {
        Session session = getSession();

        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        Transaction tx = session.beginTransaction();
        Act src = createAct("act1");
        Act tar = createAct("act2");
        session.save(src);
        session.save(tar);

        ActRelationship actRel1 = createActRelationship("dummy", src, tar);
        ActRelationship actRel2 = createActRelationship("dummy1", tar, src);
        src.addActRelationship(actRel1);
        src.addActRelationship(actRel2);
        session.save(src);
        session.save(actRel1);
        session.save(actRel2);
        tx.commit();

        // test the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 2, count(ActRelationship.class));

        session.clear();
        tx = session.beginTransaction();
        src = (Act) session.load(Act.class, src.getUid());
        assertNotNull(src);
        assertEquals(2, src.getActRelationships().size());
        src.removeActRelationship(actRel1);
        tx.commit();

        // retest the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));
    }

    /**
     * Test adding an act relationship to an act.
     */
    public void testAddingActRelationshipToAct() {
        Session session = getSession();

        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        Transaction tx = session.beginTransaction();

        Act src = createAct("act1");
        Act tar = createAct("act2");
        session.save(src);
        session.save(tar);
        ActRelationship rel = createActRelationship("dummy", src, tar);
        src.addActRelationship(rel);
        session.save(src);
        session.save(rel);
        tx.commit();

        // test the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));
    }

    /**
     * Test that we can set the actrelationships without first saving the
     * acts.
     */
    public void testActRelationshipsToActsBeforeSave() {
        Session session = getSession();

        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        Transaction tx = session.beginTransaction();
        Act src = createAct("act11");
        Act tar = createAct("act22");
        ActRelationship rel = createActRelationship("dummy", src, tar);
        src.addActRelationship(rel);
        session.save(rel);
        session.save(src);
        session.save(tar);
        tx.commit();

        // test the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));
    }

    /**
     * Test the OVPMS-219 bug.
     */
    public void testOVPMS219() {
        Session session = getSession();
        Transaction tx;

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
        ActRelationship rel = createActRelationship("dummy", src, tar);
        src.addActRelationship(rel);
        session.save(src);
        session.save(rel);
        tx.commit();

        // step 6
        tx = session.beginTransaction();
        session.save(tar);
        tx.commit();

        // step 7
        tx = session.beginTransaction();
        session.save(src);
        tx.commit();
    }

    /**
     * Test adding an act relationship to an act.
     */
    public void testMod2ActRelationshipToAct() {
        Session session = getSession();

        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        Transaction tx = session.beginTransaction();
        Act src = createAct("act1");
        Act tar = createAct("act2");
        session.save(src);
        session.save(tar);

        ActRelationship actRel1 = createActRelationship("dummy", src, tar);
        ActRelationship actRel2 = createActRelationship("dummy1", tar, src);
        src.addActRelationship(actRel1);
        src.addActRelationship(actRel2);
        session.save(src);
        session.save(actRel1);
        session.save(actRel2);
        tx.commit();

        // test the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 2, count(ActRelationship.class));

        session.clear();
        tx = session.beginTransaction();
        tar = (Act) session.load(Act.class, tar.getUid());
        assertNotNull(tar);
        assertEquals(2, tar.getActRelationships().size());
        tar.removeActRelationship(actRel1);
        tx.commit();

        // retest the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 1, count(ActRelationship.class));
    }


    /**
     * Test deletion of an act when act relationships are associated with it.
     */
    public void testDeleteActRelationshipAndAct() {
        Session session = getSession();

        // get initial number of entries in tables
        int acount = count(Act.class);
        int arcount = count(ActRelationship.class);

        Transaction tx = session.beginTransaction();
        Act src = createAct("act1");
        Act tar = createAct("act2");
        session.save(src);
        session.save(tar);

        ActRelationship actRel1 = createActRelationship("dummy", src, tar);
        ActRelationship actRel2 = createActRelationship("dummy1", tar, src);
        src.addActRelationship(actRel1);
        src.addActRelationship(actRel2);
        session.save(src);
        session.save(actRel1);
        session.save(actRel2);
        tx.commit();

        // test the counts
        assertEquals(acount + 2, count(Act.class));
        assertEquals(arcount + 2, count(ActRelationship.class));

        // now delete the src act. We are hoping that one act remains but
        // that both relationships are also deleted
        session.clear();
        tx = session.beginTransaction();
        src = (Act) session.load(Act.class, src.getUid());
        session.delete(src);
        tx.commit();

        // retest the counts
        assertEquals(acount + 1, count(Act.class));
        assertEquals(arcount, count(ActRelationship.class));
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
     * @param name the name of the act
     * @return Act
     */
    private Act createAct(String name) {
        Act act = new Act();
        act.setArchetypeId(new ArchetypeId("act.simple.1.0"));
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
    private ActRelationship createActRelationship(String name, Act source,
                                                  Act target) {
        ActRelationship rel = new ActRelationship();
        rel.setArchetypeId(new ArchetypeId("act.simpleRel.1.0"));
        rel.setName(name);
        rel.setSource(source.getObjectReference());
        rel.setTarget(target.getObjectReference());

        return rel;
    }
}
