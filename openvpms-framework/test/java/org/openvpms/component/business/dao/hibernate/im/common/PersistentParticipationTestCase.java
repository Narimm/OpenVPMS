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


package org.openvpms.component.business.dao.hibernate.im.common;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.party.HibernatePartyUtil;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;


/**
 * Exercise the persistent aspects of the {@link Participation} class,
 * including the ability to associate an {@link Act} to a participation.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class PersistentParticipationTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentParticipationTestCase.class);
    }

    /**
     * Constructor for PersistentParticipationTestCase.
     *
     * @param name
     */
    public PersistentParticipationTestCase(String name) {
        super(name);
    }

    /**
     * Test the creation of a simple participation from the act end
     */
    public void testCreateParticipationFromActEnd()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial number of entries in participations table
            int acount = HibernatePartyUtil.getTableRowCount(session, "participation");

            // execute the test
            tx = session.beginTransaction();
            Entity entity = createEntity("jima-entity");
            session.saveOrUpdate(entity);
            Act act = createAct("jima-act");
            session.saveOrUpdate(act);
            Participation participation = createParticipation(entity ,act);
            act.addParticipation(participation);
            session.saveOrUpdate(act);
            tx.commit();

            // check the no. of participations
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == acount + 1);

            // clear the session otherwise it does not go back to the database
            // and retrieve the right act.
            act = (Act)session.load(Act.class, act.getUid());
            assertTrue(act != null);
            assertTrue(act.getParticipations().size() == 1);
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
     * Create an entity, act and particpation and ensure that the participation
     * can be removed from the act end.
     */
    public void testDeleteParticipantFromActEnd()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "participation");

            // execute the test
            tx = session.beginTransaction();
            Entity entity = createEntity("jima-entity");
            session.saveOrUpdate(entity);
            Act act = createAct("jima-act");
            Participation participation = createParticipation(entity ,act);
            act.addParticipation(participation);
            session.saveOrUpdate(act);
            tx.commit();

            // ensure that there is still one more add participation
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == acount + 1);

            session.clear();
            tx = session.beginTransaction();
            act = (Act)session.load(Act.class, act.getUid());
            assertTrue(act != null);
            assertTrue(act.getParticipations().size() == 1);

            act.removeParticipation(act.getParticipations().iterator().next());
            session.saveOrUpdate(act);
            tx.commit();

            // ensure that the participation has been removed
            acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == acount);
            act = (Act)session.load(Act.class, act.getUid());
            assertTrue(act != null);
            assertTrue(act.getParticipations().size() == 0);
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
     * Test the creation of a multiple participation to a single entity
     */
    public void testMultipleParticipationCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "participation");

            // retrieve the number of participation creation count
            int pcount = (Integer) this.getTestData().getTestCaseParameter(
                    "testMultipleParticiaptionCreation", "normal",
                    "participationCreationCount");

            // execute the test
            tx = session.beginTransaction();
            Entity entity = createEntity("jima1-entity");
            session.saveOrUpdate(entity);

            for (int index = 0; index < pcount; index++) {
                Act act = createAct("act-" + index);
                Participation participation = createParticipation(entity, act);
                act.addParticipation(participation);
                session.saveOrUpdate(act);
            }
            session.saveOrUpdate(entity);
            tx.commit();

            // check the no. of participations
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == acount + pcount);
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
     * Test the creation and deletion of participations to a single entity
     */
    public void testParticiaptionCreationAndDeletion()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "participation");

            // retrieve the number of participation creation count
            int pcount = (Integer) this.getTestData().getTestCaseParameter(
                    "testMultipleParticiaptionCreation", "normal",
                    "participationCreationCount");

            // execute the test
            tx = session.beginTransaction();
            Entity entity = createEntity("jima2-entity");
            session.saveOrUpdate(entity);
            for (int index = 0; index < pcount; index++) {
                Act act = createAct("act2-" + index);
                Participation participation = createParticipation(entity, act);
                act.addParticipation(participation);
                session.saveOrUpdate(act);
            }
            session.saveOrUpdate(entity);
            tx.commit();

            // check the no. of rows
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == (acount + pcount));
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
     * Create a simple participation netween the entity and the act
     *
     * @param entity
     *            the entity
     * @param act
     *            the act
     * @return Participation
     */
    private Participation createParticipation(Entity entity, Act act) {
        return new Participation(
                new ArchetypeId("openvpms-common-participation.participation.1.0"),
                new IMObjectReference(entity), new IMObjectReference(act), null);
    }

    /**
     * Create a simple entity
     *
     * @param name
     *            the name of the entity to create
     *
     * @return Entity
     */
    private Entity createEntity(String name) {
        return new Entity(new ArchetypeId("openvpms-party-role.role.1.0"), name,
                          null, null);
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
        act.setArchetypeId(new ArchetypeId("openvpms-party-act.simple.1.0"));
        act.setName(name);

        return act;
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

}
