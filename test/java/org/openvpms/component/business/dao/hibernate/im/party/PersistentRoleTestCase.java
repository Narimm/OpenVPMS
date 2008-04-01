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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.dao.hibernate.im.HibernateUtil;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Random;


/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentRoleTestCase extends HibernateInfoModelTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PersistentRoleTestCase.class);
    }

    /**
     * Constructor for PersistentRoleTestCase.
     * 
     * @param name
     */
    public PersistentRoleTestCase(String name) {
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
    public void testSimpleRoleCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "party");

            // execute the test
            tx = session.beginTransaction();
            Party role = createRole("doctor");
            session.save(role);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "party");

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
     * Test the simple creation of a role
     */
    public void testRoleAndEntityIdentityCreation() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get initial numbr of entries in address tabel
            int acount = HibernatePartyUtil.getTableRowCount(session, "party");

            // execute the test
            tx = session.beginTransaction();
            Party role = createRole("doctor");
            session.saveOrUpdate(role);
            long id = role.getUid();
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "party");

            assertTrue(acount1 == acount + 1);

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            acount = HibernateUtil.getTableRowCount(session,
                                                    "entityIdentity");
            Party role1 = (Party) session.load(Party.class, id);
            EntityIdentity eid = createEntityIdentity();
            role1.addIdentity(eid);
            session.saveOrUpdate(role1);
            tx.commit();

            // check that the row has been added
            acount1 = HibernateUtil.getTableRowCount(session,
                                                     "entityIdentity");
            assertTrue(acount1 == (acount + 1));
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
     * Test the addition and removal of the entity identies to the role
     */
    public void testAdditionRemoveEntityIdentities() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // get the initial row count in the role table
            int acount = HibernateUtil.getTableRowCount(session,
                                                        "entityIdentity");

            // retrieve the entity identity to add
            int eicount = (Integer) this.getTestData().getTestCaseParameter(
                    "testAdditionRemoveEntityIdentities", "normal",
                    "entityIdentityCount");
            // execute the test
            tx = session.beginTransaction();

            Party role = createRole("doctor");
            for (int index = 0; index < eicount; index++) {
                EntityIdentity eid = createEntityIdentity();
                role.addIdentity(eid);
            }
            session.saveOrUpdate(role);
            tx.commit();

            // check that row count
            int acount1 = HibernateUtil.getTableRowCount(session,
                                                         "entityIdentity");
            assertTrue(acount1 == (acount + eicount));

            // now retrieve the role and delete a single entity identity
            tx = session.beginTransaction();
            Party role1 = (Party) session.load(Party.class, role.getUid());
            assertTrue(role1.getIdentities().size() == eicount);

            // delete the first identity
            EntityIdentity identity = role1.getIdentities().iterator().next();
            assertTrue(role1.removeIdentity(identity));
            session.saveOrUpdate(role1);
            tx.commit();

            // now check the number of rows in the EntityIdentity table
            acount1 = HibernateUtil.getTableRowCount(session,
                                                     "entityIdentity");
            assertTrue(acount1 == (acount + eicount - 1));

            // retrieve the role and check the entity identity count
            role1 = (Party) session.load(Party.class, role.getUid());
            assertTrue(role1.getIdentities().size() == (eicount - 1));
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
     * Test the addition of a single entity relationship
     */
    public void testAdditionEntityRelationship() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // execute the test
            tx = session.beginTransaction();

            // get the initial count
            int acount = HibernateUtil.getTableRowCount(session,
                                                        "entityRelationship");
            Party role = createRole("doctor");
            Party target = createRole("doctor");
            session.saveOrUpdate(role);
            session.saveOrUpdate(target);
            tx.commit();

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            role = (Party) session.load(Party.class, role.getUid());
            target = (Party) session.load(Party.class, target.getUid());

            EntityRelationship erel = createEntityRelationship(role, target);
            role.addEntityRelationship(erel);
            session.saveOrUpdate(role);
            tx.commit();

            int acount1 = HibernateUtil.getTableRowCount(session,
                                                         "entityRelationship");
            assertTrue(acount1 == (acount + 1));
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
     * Test the creation of roles and entities and the addition of entity
     * relationships to the created roles
     */
    public void testAdditionAndHookingUpEntityRelationships() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // execute the test
            tx = session.beginTransaction();

            // get the initial count
            int acount = HibernateUtil.getTableRowCount(session,
                                                        "entityRelationship");
            Party role = createRole("doctor");
            Party target = createRole("doctor1");
            Party source = createRole("doctor2");
            session.saveOrUpdate(role);
            session.saveOrUpdate(target);
            session.saveOrUpdate(source);
            tx.commit();

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            EntityRelationship erel = createEntityRelationship(role, target);
            role.addEntityRelationship(erel);
            session.saveOrUpdate(role);
            tx.commit();

            tx = session.beginTransaction();
            erel = createEntityRelationship(source, role);
            source.addEntityRelationship(erel);
            session.saveOrUpdate(source);
            tx.commit();

            int acount1 = HibernateUtil.getTableRowCount(session,
                                                         "entityRelationship");
            assertTrue(acount1 == (acount + 2));

            // now retrieve the role and check that there is one source and
            // one target relationship entry
            //role = (Role) session.load(Role.class, role.getUid());
            //assertTrue(role.getEntityRelationships().size() == 2);
            source = (Party) session.load(Party.class, source.getUid());
            assertTrue(source.getEntityRelationships().size() == 1);
            //target = (Role) session.load(Role.class, target.getUid());
            //assertTrue(target.getEntityRelationships().size() == 1);
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
     * Test the creation and deletion of entity relationships in roles
     */
    public void testAdditionAndRemovalEntityRelationships() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // execute the test
            tx = session.beginTransaction();

            // get the initial count
            int acount = HibernateUtil.getTableRowCount(session,
                                                        "entityRelationship");
            Party role = createRole("doctor");
            session.save(role);

            EntityRelationship erel = createEntityRelationship(role, role);
            session.save(erel);
            role.addEntityRelationship(erel);
            tx.commit();

            int acount1 = HibernateUtil.getTableRowCount(session,
                                                         "entityRelationship");
            assertTrue(acount1 == (acount + 1));

            // now retrieve the role and remove the erel
            tx = session.beginTransaction();
            role = (Party) session.load(Party.class, role.getUid());
            erel = role.getEntityRelationships().iterator().next();
            role.removeEntityRelationship(erel);
            session.delete(erel);
            tx.commit();

            // ensure that there is one less row
            acount1 = HibernateUtil.getTableRowCount(session,
                                                     "entityRelationship");
            assertTrue(acount1 == acount);

            // check that the role now has zero entity relationships
            session.flush();
            role = (Party) session.load(Party.class, role.getUid());
            assertTrue((role.getEntityRelationships() == null)
                    || (role.getEntityRelationships().size() == 0));
        } catch (Exception exception) {
            if (tx!= null) {
                tx.rollback();
            }
            throw exception;
        } finally {
            closeSession();
        }
    }

    /**
     * Test the addition of a single classification
     */
    public void testAdditionClassification() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // execute the test
            tx = session.beginTransaction();

            // get the initial count
            int erelCount = HibernateUtil.getTableRowCount(session,
                                                           "entityRelationship");
            int classCount = HibernateUtil.getTableRowCount(session, "lookup");
            Party role = createRole("doctor");
            Party role1 = createRole("patient");
            session.save(role);
            session.save(role1);

            EntityRelationship erel = createEntityRelationship(role, role1);
            Lookup class1 = createClassification();
            session.save(class1);
            role.addEntityRelationship(erel);
            role.addClassification(class1);
            tx.commit();

            int erelCount1 = HibernateUtil.getTableRowCount(session,
                                                            "entityRelationship");
            int classCount1 = HibernateUtil.getTableRowCount(session, "lookup");
            assertTrue(erelCount1 == (erelCount + 1));
            assertTrue(classCount1 == (classCount + 1));

            // check that the role now has zero entity relationships
            session.flush();
            role = (Party) session.load(Party.class, role.getUid());
            assertTrue(role.getClassifications().size() == 1);
            assertTrue(role.getEntityRelationships().size() == 1);
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
     * Test the addition and remove of classifications
     */
    public void testAdditionRemovalClassification() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // execute the test
            tx = session.beginTransaction();

            // get the initial count
            Party role = createRole("doctor");
            session.save(role);
            tx.commit();

            // retrieve the entity classification count
            int eccount = (Integer) this.getTestData().getTestCaseParameter(
                    "testAdditionRemovalClassification", "normal",
                    "classificationCount");
            int classCount = HibernateUtil.getTableRowCount(session, "lookup");

            tx = session.beginTransaction();
            role = (Party) session.load(Party.class, role.getUid());
            for (int index = 0; index < eccount; index++) {
                Lookup class1 = createClassification();
                role.addClassification(class1);
                session.save(class1);
            }
            session.save(role);
            tx.commit();

            int classCount1 = HibernateUtil.getTableRowCount(session, "lookup");
            assertEquals(classCount + eccount, classCount1);

            role = (Party) session.load(Party.class, role.getUid());
            assertEquals(eccount, role.getClassifications().size());

            tx = session.beginTransaction();
            Lookup class1 = role.getClassifications().iterator().next();
            role.removeClassification(class1);
            session.delete(class1);
            tx.commit();

            classCount1 = HibernateUtil.getTableRowCount(session,
                                                         "lookup");
            assertEquals(classCount + eccount - 1, classCount1);
            role = (Party) session.load(Party.class, role.getUid());
            assertEquals(eccount - 1, role.getClassifications().size());
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
    private Party createRole(String name) throws Exception {
        return new Party(createRoleArchetypeId(), name,  name, null,
                         createSimpleAttributeMap());
    }

    /**
     * Creare a simple enttiy identity
     * 
     * @return EntityIdentity
     */
    private EntityIdentity createEntityIdentity() {
        return new EntityIdentity(createArchetypeId(), "isbn1203",
                                  createSimpleAttributeMap());
    }

    /**
     * Createa single entity relationship
     * 
     * @return EntityRelationship
     */
    private EntityRelationship createEntityRelationship(Entity source,
                                                        Entity target) {
        return new EntityRelationship(
                new ArchetypeId("entity.basicEntityRel.1.0"),
                source.getObjectReference(),
                target.getObjectReference());
    }

    /**
     * Creates a default classification lookup.
     * 
     * @return a new lookup
     */
    private Lookup createClassification() {
        String code = "CODE_" + new Random().nextInt();
        return new Lookup(createClassificationArchetypeId(), code);
    }

    /**
     * Return a role archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createRoleArchetypeId() {
        return new ArchetypeId("role.role.1.0");
    }

    /**
     * Return a classificationarchetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createClassificationArchetypeId() {
        return new ArchetypeId("classification.classification.1.0");
    }

    /**
     * Return a entity identity archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createArchetypeId() {
        return new ArchetypeId("entityId.entityId.1.0");
    }

}
