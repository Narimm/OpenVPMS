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
import org.openvpms.component.business.domain.im.common.EntityClassification;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
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
            Role role = createRole();
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
            Role role = createRole();
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
            role1.addEntityIdentity(eid);
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

            Role role = createRole();
            for (int index = 0; index < eicount; index++) {
                EntityIdentity eid = createEntityIdentity();
                role.addEntityIdentity(eid);
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
            assertTrue(role1.getEntityIdentities().length == eicount);

            // delete the first identity
            EntityIdentity identity = role1.getEntityIdentities()[0];
            assertTrue(role1.removeEntityIdentity(identity));
            session.saveOrUpdate(role1);
            tx.commit();

            // now check the number of rows in the EntityIdentity table
            acount1 = HibernateUtil.getTableRowCount(session,
                    "entityIdentity");
            assertTrue(acount1 == (acount + eicount - 1));

            // retrieve the role and check the entity identity count
            role1 = (Role) session.load(Role.class, role.getUid());
            assertTrue(role1.getEntityIdentities().length == (eicount - 1));
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
            Role role = createRole();
            Role target = createRole();
            session.saveOrUpdate(role);
            session.saveOrUpdate(target);
            tx.commit();

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            role = (Role) session.load(Role.class, role.getUid());
            target = (Role) session.load(Role.class, target.getUid());

            EntityRelationship erel = createEntityRelationship(role, target);
            role.addSourceEntityRelationship(erel);
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
            Role role = createRole();
            Role target = createRole();
            Role source = createRole();
            session.save(role);
            session.save(target);
            session.save(source);

            EntityRelationship erel = createEntityRelationship(role, target);
            session.save(erel);
            role.addSourceEntityRelationship(erel);
            target.addTargetEntityRelationship(erel);
            erel = createEntityRelationship(source, role);
            session.save(erel);
            source.addSourceEntityRelationship(erel);
            role.addTargetEntityRelationship(erel);
            tx.commit();

            int acount1 = HibernateUtil.getTableRowCount(session,
                    "entityRelationship");
            assertTrue(acount1 == (acount + 2));

            // now retrieve the role and check that there is one source and
            // one target relationship entry
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getSourceEntityRelationships().length == 1);
            assertTrue(role.getTargetEntityRelationships().length == 1);
            assertTrue(source.getSourceEntityRelationships()[0]
                    .getTargetEntity().getSourceEntityRelationships().length == 1);
            assertTrue(source.getSourceEntityRelationships()[0]
                    .getTargetEntity().getUid() == role.getUid());
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
            Role role = createRole();
            session.save(role);

            EntityRelationship erel = createEntityRelationship(role, role);
            session.save(erel);
            role.addSourceEntityRelationship(erel);
            tx.commit();

            int acount1 = HibernateUtil.getTableRowCount(session,
                    "entityRelationship");
            assertTrue(acount1 == (acount + 1));

            // now retrieve the role and remove the erel
            tx = session.beginTransaction();
            role = (Role) session.load(Role.class, role.getUid());
            erel = role.getSourceEntityRelationships()[0];
            role.removeSourceEntityRelationship(erel);
            session.delete(erel);
            tx.commit();

            // ensure that there is one less row
            acount1 = HibernateUtil.getTableRowCount(session,
                    "entityRelationship");
            assertTrue(acount1 == acount);

            // check that the role now has zero entity relationships
            session.flush();
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue((role.getSourceEntityRelationships() == null)
                    || (role.getSourceEntityRelationships().length == 0));
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
            int eclassCount = HibernateUtil.getTableRowCount(session,
                    "entityClassification");
            Role role = createRole();
            session.save(role);

            EntityRelationship erel = createEntityRelationship(role, role);
            EntityClassification eclass = createEntityClassification(role,
                    createClassification());
            session.save(erel);
            session.save(eclass);
            role.addSourceEntityRelationship(erel);
            role.addEntityClassification(eclass);
            tx.commit();

            int erelCount1 = HibernateUtil.getTableRowCount(session,
                    "entityRelationship");
            int eclassCount1 = HibernateUtil.getTableRowCount(session,
                    "entityClassification");
            assertTrue(erelCount1 == (erelCount + 1));
            assertTrue(eclassCount1 == (eclassCount + 1));

            // check that the role now has zero entity relationships
            session.flush();
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getClassifications().size() == 1);
            assertTrue(role.getSourceEntityRelationships().length == 1);
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
            Role role = createRole();
            session.save(role);
            EntityRelationship erel = createEntityRelationship(role, role);
            session.save(erel);
            role.addSourceEntityRelationship(erel);
            tx.commit();

            // retrieve the entity classification count
            int eccount = ((Integer) this.getTestData().getTestCaseParameter(
                    "testAdditionRemovalEntityClassification", "normal",
                    "entityClassificationCount")).intValue();
            int eclassCount = HibernateUtil.getTableRowCount(session,
                    "entityClassification");

            tx = session.beginTransaction();
            role = (Role) session.load(Role.class, role.getUid());
            for (int index = 0; index < eccount; index++) {
                EntityClassification eclass = createEntityClassification(role,
                        createClassification());
                role.addEntityClassification(eclass);
                session.save(eclass);
            }
            tx.commit();

            int eclassCount1 = HibernateUtil.getTableRowCount(session,
                    "entityClassification");
            assertTrue(eclassCount1 == (eclassCount + eccount));

            // retrieve the role, delete the first entity classification and
            // do some checks
            session.flush();
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getClassifications().size() == eccount);

            tx = session.beginTransaction();
            EntityClassification eclass = role.getClassifications().iterator().next();
            role.removeEntityClassification(eclass);
            session.delete(eclass);
            tx.commit();

            eclassCount1 = HibernateUtil.getTableRowCount(session,
                    "entityClassification");
            assertTrue(eclassCount1 == (eclassCount + eccount - 1));
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
     * @return Role
     */
    private Role createRole() throws Exception {
        return new Role(createRoleArchetypeId(), "doctor",  null, null, 
                createTimeInterval(), createSimpleAttributeMap());

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
        return new EntityRelationship(createArchetypeId(), source, target, null);
    }

    /**
     * Create an {@link EntityClassification}.
     * 
     * @param entity
     *            the entity
     * @parm classification the classification to associate with it
     * @return EntityClassification
     */
    private EntityClassification createEntityClassification(Entity entity,
            Classification classification) {
        return new EntityClassification( createEntityArchetypeId(), entity, 
                classification, createTimeInterval());
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
     * Return a entity identity archetype Id
     * 
     * @return ArchetypeId
     */
    private ArchetypeId createEntityArchetypeId() {
        return new ArchetypeId("openvpms-common-entity.entity.1.0");
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
