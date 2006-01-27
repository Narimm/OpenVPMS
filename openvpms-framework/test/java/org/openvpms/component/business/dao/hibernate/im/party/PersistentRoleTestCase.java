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
import org.openvpms.component.business.dao.hibernate.im.HibernateUtil;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Role;

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
     * @param arg0
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
            int acount = HibernatePartyUtil.getTableRowCount(session, "role");

            // execute the test
            tx = session.beginTransaction();
            Role role = createRole("doctor");
            session.save(role);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "role");

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
            int acount = HibernatePartyUtil.getTableRowCount(session, "role");

            // execute the test
            tx = session.beginTransaction();
            Role role = createRole("doctor");
            session.saveOrUpdate(role);
            long id = role.getUid();
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "role");

            assertTrue(acount1 == acount + 1);

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            acount = HibernateUtil.getTableRowCount(session,
                    "entityIdentity");
            Role role1 = (Role) session.load(Role.class, id);
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
            int eicount = ((Integer) this.getTestData().getTestCaseParameter(
                    "testAdditionRemoveEntityIdentities", "normal",
                    "entityIdentityCount")).intValue();
            // execute the test
            tx = session.beginTransaction();

            Role role = createRole("doctor");
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
            Role role1 = (Role) session.load(Role.class, role.getUid());
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
            role1 = (Role) session.load(Role.class, role.getUid());
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
            Role role = createRole("doctor");
            Role target = createRole("doctor");
            session.saveOrUpdate(role);
            session.saveOrUpdate(target);
            tx.commit();

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            role = (Role) session.load(Role.class, role.getUid());
            target = (Role) session.load(Role.class, target.getUid());

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
            Role role = createRole("doctor");
            Role target = createRole("doctor1");
            Role source = createRole("doctor2");
            session.save(role);
            session.save(target);
            session.save(source);

            EntityRelationship erel = createEntityRelationship(role, target);
            role.addEntityRelationship(erel);
            target.addEntityRelationship((EntityRelationship)erel.clone());
            
            erel = createEntityRelationship(source, role);
            source.addEntityRelationship(erel);
            role.addEntityRelationship((EntityRelationship)erel.clone());
            session.save(role);
            session.save(target);
            session.save(source);
            tx.commit();

            int acount1 = HibernateUtil.getTableRowCount(session,
                    "entityRelationship");
            assertTrue(acount1 == (acount + 4));

            // now retrieve the role and check that there is one source and
            // one target relationship entry
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getEntityRelationships().size() == 2);
            source = (Role) session.load(Role.class, source.getUid());
            assertTrue(source.getEntityRelationships().size() == 1);
            target = (Role) session.load(Role.class, target.getUid());
            assertTrue(target.getEntityRelationships().size() == 1);
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
            Role role = createRole("doctor");
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
            role = (Role) session.load(Role.class, role.getUid());
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
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue((role.getEntityRelationships() == null)
                    || (role.getEntityRelationships().size() == 0));
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
     * Test the addition of a single entity classification
     */
    public void testAdditionEntityClassification() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // execute the test
            tx = session.beginTransaction();

            // get the initial count
            int erelCount = HibernateUtil.getTableRowCount(session,
                    "entityRelationship");
            int classCount = HibernateUtil.getTableRowCount(session,
                    "classification");
            Role role = createRole("doctor");
            session.save(role);

            EntityRelationship erel = createEntityRelationship(role, role);
            Classification class1 = createClassification();
            session.save(erel);
            session.save(class1);
            role.addEntityRelationship(erel);
            role.addClassification(class1);
            tx.commit();

            int erelCount1 = HibernateUtil.getTableRowCount(session,
                    "entityRelationship");
            int classCount1 = HibernateUtil.getTableRowCount(session,
                    "classification");
            assertTrue(erelCount1 == (erelCount + 1));
            assertTrue(classCount1 == (classCount + 1));

            // check that the role now has zero entity relationships
            session.flush();
            role = (Role) session.load(Role.class, role.getUid());
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
     * Test the addition and remove of entity classifications
     */
    public void testAdditionRemovalEntityClassification() throws Exception {
        Session session = currentSession();
        Transaction tx = null;

        try {
            // execute the test
            tx = session.beginTransaction();

            // get the initial count
            Role role = createRole("doctor");
            session.save(role);
            EntityRelationship erel = createEntityRelationship(role, role);
            session.save(erel);
            role.addEntityRelationship(erel);
            tx.commit();

            // retrieve the entity classification count
            int eccount = ((Integer) this.getTestData().getTestCaseParameter(
                    "testAdditionRemovalEntityClassification", "normal",
                    "entityClassificationCount")).intValue();
            int classCount = HibernateUtil.getTableRowCount(session,
                    "classification");

            tx = session.beginTransaction();
            role = (Role) session.load(Role.class, role.getUid());
            for (int index = 0; index < eccount; index++) {
                Classification class1 = createClassification();
                role.addClassification(class1);
                session.save(class1);
            }
            tx.commit();

            int classCount1 = HibernateUtil.getTableRowCount(session,
                    "classification");
            assertTrue(classCount1 == (classCount + eccount));

            // retrieve the role, delete the first entity classification and
            // do some checks
            session.flush();
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getClassifications().size() == eccount);

            tx = session.beginTransaction();
            Classification class1 = role.getClassifications().iterator().next();
            role.removeClassification(class1);
            session.delete(class1);
            tx.commit();

            classCount1 = HibernateUtil.getTableRowCount(session,
                    "classification");
            assertTrue(classCount1 == (classCount + eccount - 1));
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getClassifications().size() == (eccount - 1));
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
                new ArchetypeId("openvpms-common-entity.basicEntityRel.1.0"), 
                new IMObjectReference(source), 
                new IMObjectReference(target), null);
    }

    /**
     * Return a default classification.
     * 
     * @return Classification
     * @thorws Exception
     */
    private Classification createClassification() throws Exception {
        return new Classification(createClassificationArchetypeId(), null, null);
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
    private ArchetypeId createClassificationArchetypeId() {
        return new ArchetypeId("openvpms-common-classification.classification.1.0");
    }

    /**
     * Return a entity identity archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createArchetypeId() {
        return new ArchetypeId("openvpms-common-entityId.entityId.1.0");
    }

}
