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


//openvpms-framework
import org.openvpms.component.business.service.ServiceBaseTestCase;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.ArchetypeService;

/**
 * Test the entity service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EntityIdentityTestCase extends
    ServiceBaseTestCase {

    /**
     * Holds a reference to the archetype service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(EntityIdentityTestCase.class);
    }

    /**
     * Default constructor
     */
    public EntityIdentityTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "org/openvpms/component/business/service/entity/entity-service-appcontext.xml" };
    }

    /**
     * Test the creation of EntityIdentities on an entity object.
     */
    public void testEntityIdentityCreation() throws Exception {
        Party person = createPerson("Mr", "EntityIdentity", "Test");
        EntityIdentity eidentity = createEntityIdentity("jimbo");

        person.addIdentity(eidentity);
        service.save(person);

        // retrieve the person and check that there is a single
        // entity identity
        person = (Party) ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getIdentities().size() == 1);
        assertTrue(((EntityIdentity)person.getIdentities().iterator().next()).getUid() != -1);
    }

    /**
     * Test the creation of multiple entity identity objects to the single
     * person object.
     * 
     * @throws Exception
     */
    public void testEntityIdentityDeletion() throws Exception {
        Party person = createPerson("Mr", "EntityIdentity", "Test");
        EntityIdentity ident1 = createEntityIdentity("jimbo");
        EntityIdentity ident2 = createEntityIdentity("jimmya");
        person.addIdentity(ident1);
        person.addIdentity(ident2);
        
        service.save(person);
        
        // retrieve the entity, check it and then remove an entity identity
        person = (Party) ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getIdentities().size() == 2);
        
        ident1 = person.getIdentities().iterator().next();
        person.removeIdentity(ident1);
        assertTrue(person.getIdentities().size() == 1);
        service.save(person);

        assertTrue(getObjectById("entityIdentity.getById", ident1.getUid()) == null);
        person = (Party) ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getIdentities().size() == 1);
    }

    /**
     * Test the update of an entity identity object, which is attached to a
     * person object
     */
    public void testEntityIdentityUpdate() throws Exception {
        Party person = createPerson("Mr", "EntityIdentity", "Test");
        EntityIdentity ident1 = createEntityIdentity("jimbo");
        person.addIdentity(ident1);
        service.save(person);
        
        // retrieve the entity, check it and then update an entity identity
        person = (Party) ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getIdentities().size() == 1);
        ident1 = person.getIdentities().iterator().next();
        assertTrue(ident1.getIdentity().equals("jimbo"));
        ident1.setIdentity("jimmya");
        service.save(person);
        
        // make sure the update happened
        person = (Party) ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person != null);
        assertTrue(person.getIdentities().size() == 1);
        ident1 = person.getIdentities().iterator().next();
        assertTrue(ident1.getIdentity().equals("jimmya"));
    }

    /**
     * Test that we can clone anEntityIdentities object.
     */
    public void testEntityIdentityClone() throws Exception {
        EntityIdentity eidentity = createEntityIdentity("jimbo");
        EntityIdentity copy = (EntityIdentity)eidentity.clone();
        copy.setIdentity("jimmya");
        assertTrue(copy.getIdentity().equals(eidentity.getIdentity()) == false);
    }

    /**
     * Create a person
     * 
     * @param title
     *            the person's title
     * @param firstName
     *            the person's first name
     * @param lastName
     *            the person's last name
     * @return Person
     */
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party)service.create("person.person");
        person.getDetails().setAttribute("lastName", lastName);
        person.getDetails().setAttribute("firstName", firstName);
        person.getDetails().setAttribute("title", title);
        
        return person;
    }

    /**
     * Create an entity identity with the specified identity
     * 
     * @param identity
     *            the identity to assign
     * @return EntityIdentity
     */
    private EntityIdentity createEntityIdentity(String identity) {
        EntityIdentity eidentity = (EntityIdentity) service
                .create("entityIdentity.personAlias");
        assertTrue(eidentity != null);

        eidentity.setIdentity(identity);
        return eidentity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (ArchetypeService) applicationContext
                .getBean("archetypeService");
        assertTrue(service != null);
    }
}
