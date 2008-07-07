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
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityIdentityDO;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDOHelper;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.util.Random;


/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PartyDOTestCase extends AbstractPartyDOTest {

    /**
     * The initial no. of parties in the database.
     */
    private int parties;

    /**
     * The initial no. of relationships in the database.
     */
    private int relationships;

    /**
     * The initial no. of party details in the database.
     */
    private int details;

    /**
     * The initial no. of lookups in the database.
     */
    private int lookups;


    /**
     * Entity identity archetype identifier.
     */

    private static final ArchetypeId ENTITY_IDENTITY_ID = new ArchetypeId(
            "EntityIdentityDO.dummy.1.0");

    /**
     * Entity relationship archetype identifier.
     */
    private static final ArchetypeId RELATIONSHIP_ID = new ArchetypeId(
            "entityeRelationship.dummy.1.0");


    /**
     * Tests that a simple PartyDO can be made persistent.
     * <p/>
     * Verifies that properties in the associated 'details' table is removed
     * when the PartyDO is deleted.
     */
    public void testPartySaveDelete() {
        Session session = getSession();

        // get initial count of parties and corresponding details

        PartyDO person = createPerson();
        int size = person.getDetails().size();
        assertTrue(size != 0);

        Transaction tx = session.beginTransaction();
        session.save(person);
        tx.commit();

        // reload
        session.evict(person);
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        assertEquals(size, person.getDetails().size());

        // check row count
        assertEquals(parties + 1, count(PartyDO.class));
        assertEquals(details + size, countDetails(PartyDO.class));

        // delete it and verify it no long exists
        tx = session.beginTransaction();
        session.delete(person);
        tx.commit();

        assertNull(session.get(PartyDO.class, person.getId()));
        assertEquals(parties, count(PartyDO.class));
        assertEquals(details, countDetails(PartyDO.class));
    }

    /**
     * Tests that a simple PartyDO can be made persistent with an entity identity,
     * and that the identity is removed along with its 'details' properties
     * when the PartyDO is deleted.
     */
    public void testPartySaveDeleteWithEntityIdentityDO() {
        Session session = getSession();

        // get initial count of parties
        int parties = count(PartyDO.class);
        Transaction tx = session.beginTransaction();
        PartyDO person = createPerson();
        session.saveOrUpdate(person);
        tx.commit();

        assertEquals(parties + 1, count(PartyDO.class));

        // now retrieve the PartyDO and add an entity identity
        tx = session.beginTransaction();
        int identities = count(EntityIdentityDO.class);
        int details = countDetails(EntityIdentityDO.class);

        person = (PartyDO) session.load(PartyDO.class, person.getId());
        EntityIdentityDO identity = createEntityIdentityDO();
        int size = identity.getDetails().size();
        assertTrue(size != 0);

        person.addIdentity(identity);
        session.saveOrUpdate(person);
        tx.commit();

        // check that the row has been added
        assertEquals(identities + 1, count(EntityIdentityDO.class));
        assertEquals(details + size, countDetails(EntityIdentityDO.class));

        // now delete the person, and verify the identity is also deleted
        tx = session.beginTransaction();
        session.delete(person);
        tx.commit();

        assertNull(session.get(EntityIdentityDO.class, identity.getId()));

        // check that the rows have been removed
        assertEquals(identities, count(EntityIdentityDO.class));
        assertEquals(details, countDetails(EntityIdentityDO.class));
    }

    /**
     * Test the addition and removal of the entity identies from a party.
     */
    public void testAddRemoveEntityIdentities() {
        Session session = getSession();

        // get the initial row count in the role table
        int initial = count(EntityIdentityDO.class);
        int toAdd = 10;
        Transaction tx = session.beginTransaction();

        PartyDO person = createPerson();
        for (int index = 0; index < toAdd; index++) {
            EntityIdentityDO eid = createEntityIdentityDO();
            person.addIdentity(eid);
        }
        session.saveOrUpdate(person);
        tx.commit();

        // check the row count
        assertEquals(initial + toAdd, count(EntityIdentityDO.class));

        // now retrieve the role and delete a single entity identity
        tx = session.beginTransaction();
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        assertEquals(toAdd, person.getIdentities().size());

        // delete the first identity
        EntityIdentityDO identity = person.getIdentities().iterator().next();
        assertTrue(person.removeIdentity(identity));
        session.saveOrUpdate(person);
        tx.commit();

        // now check the number of rows in the EntityIdentityDO table
        assertEquals(initial + toAdd - 1, count(EntityIdentityDO.class));

        // retrieve the PartyDO and check the entity identity count
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        assertEquals(toAdd - 1, person.getIdentities().size());
    }

    /**
     * Test the addition of a single entity relationship.
     */
    public void testEntityRelationshipAddition() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // get the initial count
        int relationships = count(EntityRelationshipDO.class);
        PartyDO source = createPerson();
        PartyDO target = createPerson();
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);
        tx.commit();

        // now retrieve the source and add an entity relationship
        tx = session.beginTransaction();
        source = (PartyDO) session.load(PartyDO.class, source.getId());
        target = (PartyDO) session.load(PartyDO.class, target.getId());

        EntityRelationshipDO rel = createEntityRelationship(source, target);
        source.addSourceEntityRelationship(rel);
        session.saveOrUpdate(source);
        tx.commit();

        assertEquals(relationships + 1, count(EntityRelationshipDO.class));
    }

    /**
     * Test the removal of parties with entity relationships.
     */
    public void testEntityRelationshipRemoval() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // create two parties, and add two relationships between each
        PartyDO source = createPerson();
        PartyDO target = createPerson();
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);
        tx.commit();

        tx = session.beginTransaction();
        EntityRelationshipDO rel1 = createEntityRelationship(source, target);
        EntityRelationshipDO rel2 = createEntityRelationship(source, target);
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);

        tx.commit();

        // check the relationship count
        assertEquals(relationships + 2, count(EntityRelationshipDO.class));

        // now remove the first relationship
        tx = session.beginTransaction();

        source.removeSourceEntityRelationship(rel1);
        target.removeTargetEntityRelationship(rel1);
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);

        tx.commit();

        // check the count
        assertEquals(relationships + 1, count(EntityRelationshipDO.class));
        assertEquals(1, target.getTargetEntityRelationships().size());

        // now delete the target. The remaining entity relationship should
        // also be deleted
        session.evict(source);
        tx = session.beginTransaction();
        session.delete(target);
        tx.commit();

        assertNull(session.get(EntityRelationshipDO.class, rel2.getId()));

        // check the count
        assertEquals(relationships, count(EntityRelationshipDO.class));
    }

    /**
     * Test the creation and deletion of entity relationships.
     */
    public void testAddRemoveEntityRelationships() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        PartyDO person = createPerson();
        session.save(person);

        EntityRelationshipDO rel = createEntityRelationship(person, person);
        session.save(rel);
        tx.commit();

        assertEquals(relationships + 1, count(EntityRelationshipDO.class));

        // now retrieve the role and remove the relationship
        tx = session.beginTransaction();
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        rel = person.getSourceEntityRelationships().iterator().next();
        person.removeSourceEntityRelationship(rel);
        person.removeTargetEntityRelationship(rel);
        session.delete(rel);
        tx.commit();

        // ensure that there is one less row
        assertEquals(relationships, count(EntityRelationshipDO.class));

        // check that the role now has zero entity relationships
        session.flush();
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        assertEquals(0, person.getEntityRelationships().size());
    }

    /**
     * Test the addition of a single classification
     */
    public void testAdditionClassification() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // get the initial count
        PartyDO person = createPerson();
        PartyDO patient = createPatient();
        session.save(person);
        session.save(patient);

        EntityRelationshipDO rel = createEntityRelationship(person, patient);
        LookupDO class1 = createClassification();
        session.save(class1);
        person.addClassification(class1);
        tx.commit();

        assertEquals(relationships + 1, count(EntityRelationshipDO.class));
        assertEquals(lookups + 1, count(LookupDO.class));

        // check the party
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        assertEquals(1, person.getClassifications().size());
        assertEquals(1, person.getEntityRelationships().size());
    }

    /**
     * Test the addition and remove of classifications
     */
    public void testAdditionRemovalClassification() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        PartyDO person = createPerson();
        session.save(person);
        tx.commit();

        int count = 10; // no. of classifications to add

        tx = session.beginTransaction();
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        for (int index = 0; index < count; index++) {
            LookupDO class1 = createClassification();
            person.addClassification(class1);
            session.save(class1);
        }
        session.save(person);
        tx.commit();

        assertEquals(lookups + count, count(LookupDO.class));

        person = (PartyDO) session.load(PartyDO.class, person.getId());
        assertEquals(count, person.getClassifications().size());

        tx = session.beginTransaction();
        LookupDO class1 = person.getClassifications().iterator().next();
        person.removeClassification(class1);
        session.delete(class1);
        tx.commit();

        assertEquals(lookups + count - 1, count(LookupDO.class));
        person = (PartyDO) session.load(PartyDO.class, person.getId());
        assertEquals(count - 1, person.getClassifications().size());
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parties = count(PartyDO.class);
        relationships = count(EntityRelationshipDO.class);
        details = countDetails(PartyDO.class);
        lookups = count(LookupDO.class);
    }

    /**
     * Creates a new identity.
     *
     * @return the new identity
     */
    private EntityIdentityDO createEntityIdentityDO() {
        EntityIdentityDO identity = new EntityIdentityDO(ENTITY_IDENTITY_ID);
        identity.setIdentity("isbn1203");
        identity.setDetails(createSimpleAttributeMap());
        return identity;
    }

    /**
     * Creates a relationship between two entities.
     *
     * @param source the source of the relationship
     * @param target the target of the relationship
     * @return the new relationship
     */
    private EntityRelationshipDO createEntityRelationship(EntityDO source,
                                                          EntityDO target) {
        EntityRelationshipDO result = new EntityRelationshipDO(RELATIONSHIP_ID);
        source.addSourceEntityRelationship(result);
        target.addTargetEntityRelationship(result);
        return result;
    }

    /**
     * Creates a default classification lookup.
     *
     * @return a new lookup
     */
    private LookupDO createClassification() {
        String code = "CODE_" + new Random().nextInt();
        return LookupDOHelper.createLookup("lookup.dummy.1.0", code);
    }

}
