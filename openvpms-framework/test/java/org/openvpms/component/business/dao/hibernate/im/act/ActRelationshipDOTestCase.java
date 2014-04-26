/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.dao.hibernate.im.act;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Exercise the act and act relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActRelationshipDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of acts in the database
     */
    private int acts;

    /**
     * The initial no. of relationships in the database.
     */
    private int relationships;


    /**
     * Test the creation of act relationships.
     */
    @Test
    public void testCreate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ActDO source = createAct("act1");
        ActDO target = createAct("act2");
        ActRelationshipDO actRel1 = createActRelationship(source, target);
        ActRelationshipDO actRel2 = createActRelationship(target, source);
        session.save(source);
        session.save(target);
        tx.commit();

        // check the row counts
        assertEquals(acts + 2, count(ActDOImpl.class));
        assertEquals(relationships + 2, count(ActRelationshipDOImpl.class));

        // verify the relationships
        checkSource(source, actRel1);
        checkTarget(source, actRel2);
        checkSource(target, actRel2);
        checkTarget(target, actRel1);
    }

    /**
     * Test the deletion of an act relationship.
     */
    @Test
    public void testDelete() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ActDO source = createAct("act1");
        ActDO target = createAct("act2");
        session.save(source);
        session.save(target);

        ActRelationshipDO actRel1 = createActRelationship(source, target);
        ActRelationshipDO actRel2 = createActRelationship(target, source);
        session.save(actRel1);
        session.save(actRel2);
        tx.commit();

        // check the row counts
        assertEquals(acts + 2, count(ActDOImpl.class));
        assertEquals(relationships + 2, count(ActRelationshipDOImpl.class));

        // remove actRel1
        tx = session.beginTransaction();
        source.removeSourceActRelationship(actRel1);
        target.removeTargetActRelationship(actRel1);
        tx.commit();

        // check the row counts
        assertEquals(acts + 2, count(ActDOImpl.class));
        assertEquals(relationships + 1, count(ActRelationshipDOImpl.class));

        // verify the relationships
        checkSource(target, actRel2);
        checkTarget(source, actRel2);

        // remove actRel2
        tx = session.beginTransaction();
        source.removeTargetActRelationship(actRel2);
        target.removeSourceActRelationship(actRel2);
        tx.commit();

        checkTarget(source);
        checkSource(target);

        assertNull(reload(actRel1));
    }

    /**
     * Test the modification of an act relationship.
     */
    @Test
    public void testUpdate() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ActDO source = createAct("act1");
        ActDO target = createAct("act2");
        session.save(source);
        session.save(target);
        ActRelationshipDO actRel = createActRelationship(source, target);
        session.save(actRel);
        tx.commit();

        // change the relationship's description
        tx = session.beginTransaction();
        String description = "my description";
        actRel.setDescription(description);
        tx.commit();

        // reload and verify
        actRel = reload(actRel);
        assertEquals(description, actRel.getDescription());

        // check the counts
        assertEquals(acts + 2, count(ActDOImpl.class));
        assertEquals(relationships + 1, count(ActRelationshipDOImpl.class));
    }

    /**
     * Test that we can set the actrelationships without first saving the
     * acts.
     */
    @Test
    public void testActRelationshipsToActsBeforeSave() {
        Session session = getSession();

        Transaction tx = session.beginTransaction();
        ActDO source = createAct("act11");
        ActDO target = createAct("act22");
        ActRelationshipDO rel = createActRelationship(source, target);
        session.save(rel);
        session.save(source);
        session.save(target);
        tx.commit();

        // test the counts
        assertEquals(acts + 2, count(ActDOImpl.class));
        assertEquals(relationships + 1, count(ActRelationshipDOImpl.class));
    }

    /**
     * Test the OVPMS-219 bug.
     */
    @Test
    public void testOVPMS219() {
        Session session = getSession();
        Transaction tx;

        // step 1
        tx = session.beginTransaction();
        ActDO source = createAct("act11");
        session.save(source);
        tx.commit();

        // step 2
        tx = session.beginTransaction();
        ActDO target = createAct("act22");
        session.save(target);
        tx.commit();

        // step 3, 4 and 5
        tx = session.beginTransaction();
        ActRelationshipDO rel = createActRelationship(source, target);
        session.save(source);
        session.save(rel);
        tx.commit();

        // step 6
        tx = session.beginTransaction();
        session.save(target);
        tx.commit();

        // step 7
        tx = session.beginTransaction();
        session.save(source);
        tx.commit();
    }

    /**
     * Test deletion of an act when act relationships are associated with it.
     */
    @Test
    public void testDeleteActRelationshipAndAct() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        ActDO source = createAct("act1");
        ActDO target = createAct("act2");
        session.save(source);
        session.save(target);

        ActRelationshipDO actRel1 = createActRelationship(source, target);
        ActRelationshipDO actRel2 = createActRelationship(target, source);
        session.save(source);
        session.save(actRel1);
        session.save(actRel2);
        tx.commit();

        // test the counts
        assertEquals(acts + 2, count(ActDOImpl.class));
        assertEquals(relationships + 2, count(ActRelationshipDOImpl.class));

        actRel1.setSource(null);
        actRel2.setTarget(null);
        // now delete the source act
        // If the act and its relationships are already present in the
        // session, an ObjectDeletedException will be thrown with a message
        // like: "deleted object would be re-saved by cascade
        //        (remove deleted object from associations):
        //        [....ActRelationshipDO#92]"
        // The correct solution for this is to remove the relationships
        // prior to removing the act itself.

        tx = session.beginTransaction();
        source.removeSourceActRelationship(actRel1);
        source.removeTargetActRelationship(actRel2);
        target.removeSourceActRelationship(actRel2);
        target.removeTargetActRelationship(actRel1);
        session.delete(source);
        tx.commit();

        // retest the counts
        assertEquals(acts + 1, count(ActDOImpl.class));
        assertEquals(relationships, count(ActRelationshipDOImpl.class));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        acts = count(ActDOImpl.class);
        relationships = count(ActRelationshipDOImpl.class);
    }

    private void checkSource(ActDO source,
                             ActRelationshipDO... relationships) {
        source = reload(source);
        assertNotNull(source);
        Set<ActRelationshipDO> sources = source.getSourceActRelationships();
        Set<ActRelationshipDO> targets = source.getTargetActRelationships();
        assertEquals(relationships.length, sources.size());
        for (ActRelationshipDO relationship : relationships) {
            assertTrue(sources.contains(relationship));
            assertFalse(targets.contains(relationship));
        }
    }

    private void checkTarget(ActDO target,
                             ActRelationshipDO... relationships) {
        target = reload(target);
        assertNotNull(target);
        Set<ActRelationshipDO> sources = target.getSourceActRelationships();
        Set<ActRelationshipDO> targets = target.getTargetActRelationships();
        assertEquals(relationships.length, targets.size());
        for (ActRelationshipDO relationship : relationships) {
            assertFalse(sources.contains(relationships));
            assertTrue(targets.contains(relationship));
        }
    }

    /**
     * Creates an act.
     *
     * @param name the name of the act
     * @return a new act
     */
    private ActDO createAct(String name) {
        ActDO act = new ActDOImpl();
        act.setArchetypeId(new ArchetypeId("act.simple.1.0"));
        act.setName(name);
        return act;
    }

    /**
     * Creates a relationship between two acts.
     *
     * @param source the source act
     * @param target the target act
     * @return a new relationship
     */
    private ActRelationshipDO createActRelationship(ActDO source,
                                                    ActDO target) {
        ActRelationshipDO rel = new ActRelationshipDOImpl();
        rel.setArchetypeId(new ArchetypeId("act.simpleRel.1.0"));
        source.addSourceActRelationship(rel);
        target.addTargetActRelationship(rel);
        return rel;
    }
}
