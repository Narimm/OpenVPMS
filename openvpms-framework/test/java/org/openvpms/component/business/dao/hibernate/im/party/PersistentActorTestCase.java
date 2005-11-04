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

package org.openvpms.component.business.dao.hibernate.im.party;

// hibernate
import org.hibernate.Session;
import org.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.party.Actor;
import org.openvpms.component.business.domain.im.party.Role;

/**
 * Test the actor and actor-role relationship
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentActorTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentActorTestCase.class);
    }

    /**
     * Constructor for PersistentActorTestCase.
     * 
     * @param arg0
     */
    public PersistentActorTestCase(String name) {
        super(name);
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

    /**
     * Test the simple creation of a role
     */
    public void testSimpleActorCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "actor");
            int rcount = HibernatePartyUtil.getTableRowCount(session, "role");

            // execute the test
            tx = session.beginTransaction();
            Actor actor = createActor("jima");
            Role role = createRole("doctor");
            actor.addRole(role);
            session.save(actor);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "actor");
            int rcount1 = HibernatePartyUtil.getTableRowCount(session, "role");

            assertTrue(acount1 == acount + 1);
            assertTrue(rcount1 == rcount + 1);
            
            actor = (Actor)session.load(Actor.class, actor.getUid());
            assertTrue(actor != null);
            assertTrue(actor.getRoles().size() == 1);
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
     * Test the creation of an actor with multiple roles
     */
    public void testActorCreationWithMultipleRoles() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "actor");
            int rcount = HibernatePartyUtil.getTableRowCount(session, "role");

            // execute the test
            tx = session.beginTransaction();
            Actor actor = createActor("jima");
            Role role = createRole("doctor");
            Role role1 = createRole("doctor1");
            Role role2 = createRole("doctor2");
            actor.addRole(role);
            actor.addRole(role1);
            actor.addRole(role2);
            session.save(actor);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "actor");
            int rcount1 = HibernatePartyUtil.getTableRowCount(session, "role");

            assertTrue(acount1 == acount + 1);
            assertTrue(rcount1 == rcount + 3);
            
            actor = (Actor)session.load(Actor.class, actor.getUid());
            assertTrue(actor != null);
            assertTrue(actor.getRoles().size() == 3);
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
     * Test the creation of an actor with multiple roles
     */
    public void testActorRoleDeletion() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "actor");
            int rcount = HibernatePartyUtil.getTableRowCount(session, "role");

            // execute the test
            tx = session.beginTransaction();
            Actor actor = createActor("jima");
            Role role = createRole("doctor");
            Role role1 = createRole("doctor1");
            Role role2 = createRole("doctor2");
            actor.addRole(role);
            actor.addRole(role1);
            actor.addRole(role2);
            session.save(actor);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "actor");
            int rcount1 = HibernatePartyUtil.getTableRowCount(session, "role");

            assertTrue(acount1 == acount + 1);
            assertTrue(rcount1 == rcount + 3);
            
            actor = (Actor)session.load(Actor.class, actor.getUid());
            assertTrue(actor != null);
            assertTrue(actor.getRoles().size() == 3);
            
            // delete the first role and resave
            tx = session.beginTransaction();
            actor.removeRole(role);
            session.save(actor);
            tx.commit();
            rcount1 = HibernatePartyUtil.getTableRowCount(session, "role");
            assertTrue(rcount1 == rcount + 2);
            
            // delete another role
            tx = session.beginTransaction();
            actor = (Actor)session.load(Actor.class, actor.getUid());
            assertTrue(actor != null);
            assertTrue(actor.getRoles().size() == 2);
            actor.removeRole(role1);
            session.save(actor);
            tx.commit();
            rcount1 = HibernatePartyUtil.getTableRowCount(session, "role");
            assertTrue(rcount1 == rcount + 1);
            
            // delete the last role
            tx = session.beginTransaction();
            actor = (Actor)session.load(Actor.class, actor.getUid());
            assertTrue(actor != null);
            assertTrue(actor.getRoles().size() == 1);
            actor.removeRole(role2);
            session.save(actor);
            tx.commit();
            rcount1 = HibernatePartyUtil.getTableRowCount(session, "role");
            assertTrue(rcount1 == rcount);
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
     * Create a simple role
     * 
     * @param name
     *            the name of the role
     * @return Role
     */
    private Role createRole(String name) throws Exception {
        return new Role(createRoleArchetypeId(), name,  name, null, 
                createSimpleAttributeMap());

    }

    /**
     * Creare a simple actor
     * 
     * @param name
     *            the name of the actor
     * @return Actor
     */
    private Actor createActor(String name) {
        return new Actor(createActorArchetypeId(), name, name);
    }

    /**
     * Return a role archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createRoleArchetypeId() {
        return new ArchetypeId("openvpms-party-role.role.1.0");
    }

    /**
     * Return a classificationarchetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createActorArchetypeId() {
        return new ArchetypeId("openvpms-party-actor.actor.1.0");
    }
}
