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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.LookupUtil;

import java.util.Random;


/**
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PersistentPartyTestCase extends AbstractPersistentPartyTest {

    /**
     * Entity identity archetype identifier.
     */

    private static final ArchetypeId ENTITY_IDENTITY_ID = new ArchetypeId(
            "entityIdentity.dummy.1.0");

    /**
     * Entity relationship archetype identifier.
     */
    private static final ArchetypeId RELATIONSHIP_ID = new ArchetypeId(
            "entityeRelationship.dummy.1.0");


    /**
     * Tests that a simple party can be made persistent.
     * <p/>
     * Verifies that properties in the associated 'details' table is removed
     * when the party is deleted.
     */
    public void testPartySaveDelete() {
        Session session = getSession();

        // get initial count of parties and corresponding details
        int parties = count(Party.class);
        int details = countDetails(Party.class);

        Party person = createPerson();
        int size = person.getDetails().size();
        assertTrue(size != 0);

        Transaction tx = session.beginTransaction();
        session.save(person);
        tx.commit();

        // reload
        session.evict(person);
        person = (Party) session.load(Party.class, person.getUid());
        assertEquals(size, person.getDetails().size());

        // check row count
        assertEquals(parties + 1, count(Party.class));
        assertEquals(details + size, countDetails(Party.class));

        // delete it and verify it no long exists
        tx = session.beginTransaction();
        session.delete(person);
        tx.commit();

        assertNull(session.get(Party.class, person.getUid()));
        assertEquals(parties, count(Party.class));
        assertEquals(details, countDetails(Party.class));
    }

    /**
     * Tests that a simple party can be made persistent with an entity identity,
     * and that the identity is removed along with its 'details' properties
     * when the party is deleted.
     */
    public void testPartySaveDeleteWithEntityIdentity() {
        Session session = getSession();

        // get initial count of parties
        int parties = count(Party.class);
        Transaction tx = session.beginTransaction();
        Party person = createPerson();
        session.saveOrUpdate(person);
        tx.commit();

        assertEquals(parties + 1, count(Party.class));

        // now retrieve the party and add an entity identity
        tx = session.beginTransaction();
        int identities = count(EntityIdentity.class);
        int details = countDetails(EntityIdentity.class);

        person = (Party) session.load(Party.class, person.getUid());
        EntityIdentity identity = createEntityIdentity();
        int size = identity.getDetails().size();
        assertTrue(size != 0);

        person.addIdentity(identity);
        session.saveOrUpdate(person);
        tx.commit();

        // check that the row has been added
        assertEquals(identities + 1, count(EntityIdentity.class));
        assertEquals(details + size, countDetails(EntityIdentity.class)) ;

        // now delete the person, and verify the identity is also deleted
        tx = session.beginTransaction();
        session.delete(person);
        tx.commit();

        assertNull(session.get(EntityIdentity.class, identity.getUid()));

        // check that the rows have been removed
        assertEquals(identities, count(EntityIdentity.class));
        assertEquals(details, countDetails(EntityIdentity.class));
    }

    /**
     * Test the addition and removal of the entity identies from a party.
     */
    public void testAddRemoveEntityIdentities() {
        Session session = getSession();

        // get the initial row count in the role table
        int initial = count(EntityIdentity.class);
        int toAdd = 10;
        Transaction tx = session.beginTransaction();

        Party person = createPerson();
        for (int index = 0; index < toAdd; index++) {
            EntityIdentity eid = createEntityIdentity();
            person.addIdentity(eid);
        }
        session.saveOrUpdate(person);
        tx.commit();

        // check the row count
        assertEquals(initial + toAdd, count(EntityIdentity.class));

        // now retrieve the role and delete a single entity identity
        tx = session.beginTransaction();
        person = (Party) session.load(Party.class, person.getUid());
        assertEquals(toAdd, person.getIdentities().size());

        // delete the first identity
        EntityIdentity identity = person.getIdentities().iterator().next();
        assertTrue(person.removeIdentity(identity));
        session.saveOrUpdate(person);
        tx.commit();

        // now check the number of rows in the EntityIdentity table
        assertEquals(initial + toAdd - 1, count(EntityIdentity.class));

        // retrieve the party and check the entity identity count
        person = (Party) session.load(Party.class, person.getUid());
        assertEquals(toAdd - 1, person.getIdentities().size());
    }

    /**
     * Test the addition of a single entity relationship.
     */
    public void testEntityRelationshipAddition() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // get the initial count
        int relationships = count(EntityRelationship.class);
        Party source = createPerson();
        Party target = createPerson();
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);
        tx.commit();

        // now retrieve the source and add an entity relationship
        tx = session.beginTransaction();
        source = (Party) session.load(Party.class, source.getUid());
        target = (Party) session.load(Party.class, target.getUid());

        EntityRelationship rel = createEntityRelationship(source, target);
        source.addEntityRelationship(rel);
        session.saveOrUpdate(source);
        tx.commit();

        assertEquals(relationships + 1, count(EntityRelationship.class));
    }

    /**
     * Test the removal of parties with entity relationships.
     */
    public void testEntityRelationshipRemoval() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // get the initial count
        int relationships = count(EntityRelationship.class);

        // create two parties, and add two relationships between each
        Party source = createPerson();
        Party target = createPerson();
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);
        tx.commit();

        tx = session.beginTransaction();
        EntityRelationship rel1 = createEntityRelationship(source, target);
        EntityRelationship rel2 = createEntityRelationship(source, target);
        source.addEntityRelationship(rel1);
        target.addEntityRelationship(rel1);

        source.addEntityRelationship(rel2);
        target.addEntityRelationship(rel2);
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);

        tx.commit();

        // check the relationship count
        assertEquals(relationships + 2, count(EntityRelationship.class));

        // now remove the first relationship
        tx = session.beginTransaction();

        source.removeEntityRelationship(rel1);
        target.removeEntityRelationship(rel1);
        session.saveOrUpdate(source);
        session.saveOrUpdate(target);

        tx.commit();

        // check the count
        assertEquals(relationships + 1, count(EntityRelationship.class));
        assertEquals(1, target.getEntityRelationships().size());

        // now delete the target. The remaining entity relationship should
        // also be deleted
        session.evict(source);
        tx = session.beginTransaction();
        session.delete(target);
        tx.commit();

        assertNull(session.get(EntityRelationship.class, rel2.getUid()));

        // check the count
        assertEquals(relationships, count(EntityRelationship.class));
    }

    /**
     * Test the creation and deletion of entity relationships.
     */
    public void testAddRemoveEntityRelationships() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // get the initial count
        int relationships = count(EntityRelationship.class);
        Party role = createPerson();
        session.save(role);

        EntityRelationship erel = createEntityRelationship(role, role);
        session.save(erel);
        role.addEntityRelationship(erel);
        tx.commit();

        assertEquals(relationships + 1, count(EntityRelationship.class));

        // now retrieve the role and remove the erel
        tx = session.beginTransaction();
        role = (Party) session.load(Party.class, role.getUid());
        erel = role.getEntityRelationships().iterator().next();
        role.removeEntityRelationship(erel);
        session.delete(erel);
        tx.commit();

        // ensure that there is one less row
        assertEquals(relationships, count(EntityRelationship.class));

        // check that the role now has zero entity relationships
        session.flush();
        role = (Party) session.load(Party.class, role.getUid());
        assertTrue((role.getEntityRelationships() == null)
                || (role.getEntityRelationships().size() == 0));
    }

    /**
     * Test the addition of a single classification
     */
    public void testAdditionClassification() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // get the initial count
        int relationships = count(EntityRelationship.class);
        int lookups = count(Lookup.class);
        Party person = createPerson();
        Party patient = createPatient();
        session.save(person);
        session.save(patient);

        EntityRelationship rel = createEntityRelationship(person, patient);
        Lookup class1 = createClassification();
        session.save(class1);
        person.addEntityRelationship(rel);
        person.addClassification(class1);
        tx.commit();

        assertEquals(relationships + 1, count(EntityRelationship.class));
        assertEquals(lookups + 1, count(Lookup.class));

        session.flush();

        // check the party
        person = (Party) session.load(Party.class, person.getUid());
        assertEquals(1, person.getClassifications().size());
        assertEquals(1, person.getEntityRelationships().size());
    }

    /**
     * Test the addition and remove of classifications
     */
    public void testAdditionRemovalClassification() {
        Session session = getSession();
        Transaction tx = session.beginTransaction();

        // get the initial count
        Party role = createPerson();
        session.save(role);
        tx.commit();

        // retrieve the entity classification count
        int eccount = 10;
        int classCount = count(Lookup.class);

        tx = session.beginTransaction();
        role = (Party) session.load(Party.class, role.getUid());
        for (int index = 0; index < eccount; index++) {
            Lookup class1 = createClassification();
            role.addClassification(class1);
            session.save(class1);
        }
        session.save(role);
        tx.commit();

        int classCount1 = count(Lookup.class);
        assertEquals(classCount + eccount, classCount1);

        role = (Party) session.load(Party.class, role.getUid());
        assertEquals(eccount, role.getClassifications().size());

        tx = session.beginTransaction();
        Lookup class1 = role.getClassifications().iterator().next();
        role.removeClassification(class1);
        session.delete(class1);
        tx.commit();

        classCount1 = count(Lookup.class);
        assertEquals(classCount + eccount - 1, classCount1);
        role = (Party) session.load(Party.class, role.getUid());
        assertEquals(eccount - 1, role.getClassifications().size());
    }

    /**
     * Creare a simple enttiy identity
     *
     * @return EntityIdentity
     */
    private EntityIdentity createEntityIdentity() {
        return new EntityIdentity(ENTITY_IDENTITY_ID, "isbn1203",
                                  createSimpleAttributeMap());
    }

    /**
     * Createa single entity relationship
     *
     * @return EntityRelationship
     */
    private EntityRelationship createEntityRelationship(Entity source,
                                                        Entity target) {
        return new EntityRelationship(RELATIONSHIP_ID,
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
        return LookupUtil.createLookup("lookup.dummy.1.0", code);
    }

}
