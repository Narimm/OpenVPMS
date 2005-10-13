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

//openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.party.HibernatePartyUtil;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Role;

/**
 * Exercise the persistent aspects of the {@link Participantion} class,
 * including the ability to associated an {@link Entity} to a participantion 
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
     * Test the creation of a simple participation
     */
    public void testSimpleParticiaptionCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "participation");

            // execute the test
            tx = session.beginTransaction();
            Entity entity = createEntity();
            Participation participation = createParticipation(null);
            entity.addParticipation(participation);
            session.saveOrUpdate(entity);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
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
    
    /**
     * Test the creation of a multiple participation to a single entity
     */
    public void testMultipleParticiaptionCreation()
    throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "participation");

            // retrieve the number of participation creation count
            int pcount = ((Integer) this.getTestData().getTestCaseParameter(
                    "testMultipleParticiaptionCreation", "normal",
                    "participationCreationCount")).intValue();
            
            // execute the test
            tx = session.beginTransaction();
            Entity entity = createEntity();
            session.save(entity);
            
            for (int index = 0; index < pcount; index++) {
                Participation participation = createParticipation(null);
                entity.addParticipation(participation);
                session.save(participation);
            }
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == acount + pcount);
            
            // retrieve the entity and ensure that it as the correct number
            // of participations
            entity = (Entity)session.load(Entity.class, entity.getUid());
            assertTrue(entity.getParticipations().size() == pcount);
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
            int pcount = ((Integer) this.getTestData().getTestCaseParameter(
                    "testMultipleParticiaptionCreation", "normal",
                    "participationCreationCount")).intValue();
            
            // execute the test
            tx = session.beginTransaction();
            Entity entity = createEntity();
            for (int index = 0; index < pcount; index++) {
                Participation participation = createParticipation(null);
                entity.addParticipation(participation);
            }
            session.saveOrUpdate(entity);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == (acount + pcount));
            
            // retrieve the entity and ensure that it as the correct number
            // of participations
            tx = session.beginTransaction();
            entity = (Entity)session.load(Entity.class, entity.getUid());
            assertTrue(entity.getParticipations().size() == pcount);
            
            // remove the first participation
            Participation participation = entity.getParticipations().iterator().next();
            assertTrue(entity.removeParticipation(participation));
            session.saveOrUpdate(entity);
            tx.commit();
            
            // check the number of rows now
            acount1 = HibernatePartyUtil.getTableRowCount(session, "participation");
            assertTrue(acount1 == (acount + pcount - 1));
            
            // retrieve the entity object again
            entity = (Entity)session.load(Entity.class, entity.getUid());
            assertTrue(entity.getParticipations().size() == pcount - 1);
            
            // retrieve the first participation object and then use it to 
            // navigate to the Entity object
            participation = (Participation)session.load(Participation.class, 
                    entity.getParticipations().iterator().next().getUid());
            assertTrue(participation.getEntity().getUid() == entity.getUid());
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
     * Create a simple participation
     * 
     * @param entity
     *            the entity to add to the participation
     * @return Participation
     */
    private Participation createParticipation(Entity entity) {
        return new Participation( 
                new ArchetypeId("openvpms-common-participation.participation.1.0"),
                entity, null, null);
    }
    
    /**
     * Create a simple entity
     * 
     * @return Entity
     */
    private Entity createEntity() {
        return new Role(
                new ArchetypeId("openvpms-party-role.role.1.0"), "administrator",
                null, null, createTimeInterval(), null);

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
