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
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.HibernateEntityUtil;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.Classification;
import org.openvpms.component.business.domain.im.Entity;
import org.openvpms.component.business.domain.im.EntityClassification;
import org.openvpms.component.business.domain.im.EntityIdentity;
import org.openvpms.component.business.domain.im.EntityRelationship;
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
            String id = role.getUid();
            session.save(role);
            tx.commit();

            // ensure that there is still one more address
            int acount1 = HibernatePartyUtil.getTableRowCount(session, "role");

            assertTrue(acount1 == acount + 1);

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            acount = HibernateEntityUtil.getTableRowCount(session,
                    "entityIdentity");
            Role role1 = (Role) session.load(Role.class, id);
            EntityIdentity eid = createEntityIdentity();
            role1.addEntityIdentity(eid);
            session.save(eid);
            tx.commit();

            // check that the row has been added
            acount1 = HibernateEntityUtil.getTableRowCount(session,
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
            int acount = HibernateEntityUtil.getTableRowCount(session,
                    "entityIdentity");

            // retrieve the entity identity to add
            int eicount = ((Integer) this.getTestData().getTestCaseParameter(
                    "testAdditionRemoveEntityIdentities", "normal",
                    "entityIdentityCount")).intValue();
            // execute the test
            tx = session.beginTransaction();

            Role role = createRole();
            session.save(role);

            for (int index = 0; index < eicount; index++) {
                EntityIdentity eid = createEntityIdentity();
                role.addEntityIdentity(eid);
                session.save(eid);
            }
            tx.commit();

            // check that row count
            int acount1 = HibernateEntityUtil.getTableRowCount(session,
                    "entityIdentity");
            assertTrue(acount1 == (acount + eicount));

            // now retrieve the role and delete a single entity identity
            tx = session.beginTransaction();
            Role role1 = (Role) session.load(Role.class, role.getUid());
            assertTrue(role1.getEntityIdentities().length == eicount);

            // delete the first identity
            EntityIdentity identity = role1.getEntityIdentities()[0];
            role1.removeEntityIdentity(identity);
            session.delete(identity);
            tx.commit();

            // now check the number of rows in the EntityIdentity table
            acount1 = HibernateEntityUtil.getTableRowCount(session,
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
            int acount = HibernateEntityUtil.getTableRowCount(session,
                    "entityRelationship");
            Role role = createRole();
            Role target = createRole();
            session.save(role);
            session.save(target);
            tx.commit();

            // now retrieve the role and add an entity identity
            tx = session.beginTransaction();
            role = (Role) session.load(Role.class, role.getUid());
            target = (Role) session.load(Role.class, target.getUid());

            EntityRelationship erel = createEntityRelationship(role, target);
            session.save(erel);
            role.addSourceEntityRelationship(erel);
            tx.commit();

            int acount1 = HibernateEntityUtil.getTableRowCount(session,
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
            int acount = HibernateEntityUtil.getTableRowCount(session,
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

            int acount1 = HibernateEntityUtil.getTableRowCount(session,
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
                    .getTargetEntity().getUid().equals(role.getUid()));
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
            int acount = HibernateEntityUtil.getTableRowCount(session,
                    "entityRelationship");
            Role role = createRole();
            session.save(role);

            EntityRelationship erel = createEntityRelationship(role, role);
            session.save(erel);
            role.addSourceEntityRelationship(erel);
            tx.commit();

            int acount1 = HibernateEntityUtil.getTableRowCount(session,
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
            acount1 = HibernateEntityUtil.getTableRowCount(session,
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
            int erelCount = HibernateEntityUtil.getTableRowCount(session,
                    "entityRelationship");
            int eclassCount = HibernateEntityUtil.getTableRowCount(session,
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

            int erelCount1 = HibernateEntityUtil.getTableRowCount(session,
                    "entityRelationship");
            int eclassCount1 = HibernateEntityUtil.getTableRowCount(session,
                    "entityClassification");
            assertTrue(erelCount1 == (erelCount + 1));
            assertTrue(eclassCount1 == (eclassCount + 1));

            // check that the role now has zero entity relationships
            session.flush();
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getClassifications().length == 1);
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
            int eclassCount = HibernateEntityUtil.getTableRowCount(session,
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

            int eclassCount1 = HibernateEntityUtil.getTableRowCount(session,
                    "entityClassification");
            assertTrue(eclassCount1 == (eclassCount + eccount));

            // retrieve the role, delete the first entity classification and
            // do some checks
            session.flush();
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getClassifications().length == eccount);

            tx = session.beginTransaction();
            EntityClassification eclass = role.getClassifications()[0];
            role.removeEntityClassification(eclass);
            session.delete(eclass);
            tx.commit();

            eclassCount1 = HibernateEntityUtil.getTableRowCount(session,
                    "entityClassification");
            assertTrue(eclassCount1 == (eclassCount + eccount - 1));
            role = (Role) session.load(Role.class, role.getUid());
            assertTrue(role.getClassifications().length == (eccount - 1));
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
        return new Role(getGenerator().nextId(),
                new ArchetypeId("openvpms", "role"), "role.simple", null, null,
                createTimeInterval(), createSimpleAttributeMap());

    }

    /**
     * Creare a simple enttiy identity
     * 
     * @return EntityIdentity
     */
    private EntityIdentity createEntityIdentity() {
        return new EntityIdentity(getGenerator().nextId(),
                new ArchetypeId("openvpms", "entityIdentity"), "eidentity", 
                "isbn1203", createSimpleAttributeMap());
    }

    /**
     * Createa single entity relationship
     * 
     * @return EntityRelationship
     */
    private EntityRelationship createEntityRelationship(Entity source,
            Entity target) {
        return new EntityRelationship(getGenerator().nextId(),
                new ArchetypeId("openvpms", "entityIdentity"), 
                "identity.simple", source, target, null);
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
        return new EntityClassification(getGenerator().nextId(),
                new ArchetypeId("openvpms", "entityClassification"),
                "classification.simple", entity, classification, 
                createTimeInterval());
    }

    /**
     * Return a default classification.
     * 
     * @return Classification
     * @thorws Exception
     */
    private Classification createClassification() throws Exception {
        return new Classification(getGenerator().nextId(),
                new ArchetypeId("openvpms", "classification"),
                "classification.base", null, null);
    }
}
