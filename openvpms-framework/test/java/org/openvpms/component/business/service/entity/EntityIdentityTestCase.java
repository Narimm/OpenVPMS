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

package org.openvpms.component.business.service.entity;

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.springframework.test.context.ContextConfiguration;


/**
 * Tests the {@link EntityIdentity} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("entity-service-appcontext.xml")
public class EntityIdentityTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test the creation of EntityIdentities on an entity object.
     */
    @Test
    public void testEntityIdentityCreation() {
        Party person = createPerson("MR", "EntityIdentity", "Test");
        EntityIdentity eidentity = createEntityIdentity("jimbo");

        person.addIdentity(eidentity);
        save(person);

        // retrieve the person and check that there is a single
        // entity identity
        person = get(person);
        assertNotNull(person);
        assertEquals(1, person.getIdentities().size());
        assertTrue((person.getIdentities().iterator().next()).getId() != -1);
    }

    /**
     * Tests the removal of an entity identity associated with a party.
     */
    @Test
    public void testEntityIdentityDeletion() {
        Party person = createPerson("MR", "EntityIdentity", "Test");
        EntityIdentity ident1 = createEntityIdentity("jimbo");
        EntityIdentity ident2 = createEntityIdentity("jimmya");
        person.addIdentity(ident1);
        person.addIdentity(ident2);

        save(person);

        // retrieve the entity, check it and then remove an entity identity
        person = (Party) get(person.getObjectReference());
        assertNotNull(person);
        assertEquals(2, person.getIdentities().size());

        ident1 = person.getIdentities().iterator().next();
        person.removeIdentity(ident1);
        assertEquals(1, person.getIdentities().size());
        save(person);

        assertNull(get(ident1.getObjectReference()));

        person = (Party) get(person.getObjectReference());
        assertNotNull(person);
        assertEquals(1, person.getIdentities().size());
    }

    /**
     * Test the update of an entity identity object, which is attached to a
     * person object.
     */
    @Test
    public void testEntityIdentityUpdate() {
        Party person = createPerson("MR", "EntityIdentity", "Test");
        EntityIdentity ident1 = createEntityIdentity("jimbo");
        person.addIdentity(ident1);
        save(person);

        // retrieve the entity, check it and then update an entity identity
        person = (Party) get(person.getObjectReference());
        assertNotNull(person);
        assertEquals(1, person.getIdentities().size());
        ident1 = person.getIdentities().iterator().next();
        assertTrue(ident1.getIdentity().equals("jimbo"));
        ident1.setIdentity("jimmya");
        save(person);

        // make sure the update happened
        person = (Party) get(person.getObjectReference());
        assertNotNull(person);
        assertEquals(1, person.getIdentities().size());
        ident1 = person.getIdentities().iterator().next();
        assertTrue(ident1.getIdentity().equals("jimmya"));
    }

    /**
     * Test that we can clone an EntityIdentity object.
     *
     * @throws Exception for any error
     */
    @Test
    public void testEntityIdentityClone() throws Exception {
        EntityIdentity eidentity = createEntityIdentity("jimbo");
        EntityIdentity copy = (EntityIdentity) eidentity.clone();
        copy.setIdentity("jimmya");
        assertFalse(copy.getIdentity().equals(eidentity.getIdentity()));
    }

    /**
     * Tests the fix for OBF-173.
     */
    @Test
    public void testOBF173() {
        Party person = createPerson("MR", "EntityIdentity", "Test");
        EntityIdentity identity = createEntityIdentity("foo");
        person.addIdentity(identity);
        save(person);
        save(identity);
    }

    /**
     * Create a person.
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        Party person = (Party) create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /**
     * Create an entity identity with the specified identity.
     *
     * @param identity the identity to assign
     * @return EntityIdentity
     */
    private EntityIdentity createEntityIdentity(String identity) {
        EntityIdentity eidentity = (EntityIdentity) create("entityIdentity.personAlias");
        eidentity.setIdentity(identity);
        return eidentity;
    }
}
