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
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDOImpl;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Tests the {@link ParticipationDOImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ParticipationDOTestCase extends HibernateInfoModelTestCase {

    /**
     * The initial no. of participations in the database.
     */
    private int participations;


    /**
     * Test the creation of a simple participation from the act end
     */
    @Test
    public void testCreateParticipationFromActEnd() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        EntityDO entity = createEntity("jima-entity");
        session.saveOrUpdate(entity);
        ActDO act = createAct("jima-act");
        session.saveOrUpdate(act);
        ParticipationDO participation = createParticipation(entity, act);
        act.addParticipation(participation);
        session.saveOrUpdate(act);
        tx.commit();

        // check the no. of participations
        assertEquals(participations + 1, count(ParticipationDOImpl.class));

        // clear the session otherwise it does not go back to the database
        // and retrieve the right act.
        act = reload(act);
        assertNotNull(act);
        assertEquals(1, act.getParticipations().size());
    }

    /**
     * Create an entity, act and particpation and ensure that the participation
     * can be removed from the act end.
     */
    @Test
    public void testDeleteParticipantFromActEnd() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        EntityDO entity = createEntity("jima-entity");
        session.saveOrUpdate(entity);
        ActDO act = createAct("jima-act");
        ParticipationDO participation = createParticipation(entity, act);
        act.addParticipation(participation);
        session.saveOrUpdate(act);
        tx.commit();

        // ensure that there is still one more add participation
        assertEquals(participations + 1, count(ParticipationDOImpl.class));

        session.clear();
        tx = session.beginTransaction();
        act = reload(act);
        assertNotNull(act);
        assertEquals(1, act.getParticipations().size());

        act.removeParticipation(act.getParticipations().iterator().next());
        session.saveOrUpdate(act);
        tx.commit();

        // ensure that the participation has been removed
        assertEquals(participations, count(ParticipationDOImpl.class));
        act = reload(act);
        assertNotNull(act);
        assertEquals(0, act.getParticipations().size());
    }

    /**
     * Test the creation of a multiple participation to a single entity
     */
    @Test
    public void testMultipleParticipationCreation() {
        Session session = getSession();

        // the number of participation creation count
        int pcount = 5;

        Transaction tx = session.beginTransaction();
        EntityDO entity = createEntity("jima1-entity");
        session.saveOrUpdate(entity);

        for (int index = 0; index < pcount; index++) {
            ActDO act = createAct("act-" + index);
            ParticipationDO participation = createParticipation(entity, act);
            act.addParticipation(participation);
            session.saveOrUpdate(act);
        }
        session.saveOrUpdate(entity);
        tx.commit();

        // check the no. of participations
        assertEquals(participations + pcount, count(ParticipationDOImpl.class));
    }

    /**
     * Test the creation and deletion of participations to a single entity
     */
    @Test
    public void testParticiaptionCreationAndDeletion() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // the participation creation count
        int pcount = 5;

        EntityDO entity = createEntity("jima2-entity");
        session.saveOrUpdate(entity);
        for (int index = 0; index < pcount; index++) {
            ActDO act = createAct("act2-" + index);
            ParticipationDO participation = createParticipation(entity, act);
            act.addParticipation(participation);
            session.saveOrUpdate(act);
        }
        session.saveOrUpdate(entity);
        tx.commit();

        // check the no. of rows
        assertEquals(participations + pcount, count(ParticipationDOImpl.class));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        participations = count(ParticipationDOImpl.class);
    }

    /**
     * Create a simple participation netween the entity and the act
     *
     * @param entity the entity
     * @param act    the act
     * @return Participation
     */
    private ParticipationDO createParticipation(EntityDO entity, ActDO act) {
        ArchetypeId archetypeId
                = new ArchetypeId("participation.participation.1.0");
        ParticipationDO result = new ParticipationDOImpl();
        result.setArchetypeId(archetypeId);
        result.setEntity(entity);
        result.setAct(act);
        return result;
    }

    /**
     * Create a simple entity
     *
     * @param name the name of the entity to create
     * @return Entity
     */
    private EntityDO createEntity(String name) {
        ArchetypeId archetypeId = new ArchetypeId("role.role.1.0");
        EntityDO result = new EntityDOImpl();
        result.setArchetypeId(archetypeId);
        result.setName(name);
        return result;
    }

    /**
     * Creates a simple act.
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

}
