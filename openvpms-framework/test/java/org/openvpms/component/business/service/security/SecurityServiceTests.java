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
 *  $Id: MemorySecurityServiceTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.security;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This base class contains all the security test cases
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
@SuppressWarnings("HardCodedStringLiteral")
public abstract class SecurityServiceTests extends
                                           AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the archetectype service
     */
    protected IArchetypeService archetype;


    /**
     * Test that the caller has the credentials to make the call
     */
    public void testValidAuthorizationOnSave()
            throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:party.person");
        Party person = createPerson("MR", "Jim", "Alateras");
        archetype.save(person);
    }

    /**
     * Test that the caller does not have the credentials to make the call
     */
    public void testInvalidAuthorizationOnSave()
            throws Exception {
        // create a security contect before executing a method
        createSecurityContext("bernief", "bernief",
                              "archetype:archetypeService.save:person.person");
        Party person = createPerson("MR", "Peter", "Alateras");

        try {
            archetype.save(person);
            fail("The caller does not have the authority to call IArchetypeService.save");
        } catch (OpenVPMSAccessDeniedException exception) {
            if (exception.getErrorCode() != OpenVPMSAccessDeniedException.ErrorCode.AccessDenied)
            {
                fail("Incorrect error code was specified during the exception");
            }
            exception.printStackTrace();
            // this is the correct action
        }
    }

    /**
     * Test archetype wild card authorization
     */
    public void testArchetypeWildcardAuthorizationOnSave()
            throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:party.per*");
        Party person = createPerson("MR", "Peter1", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:part*.person");
        person = createPerson("MR", "Peter2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:party.*erson");
        person = createPerson("MR", "Peter3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:*rty.person");
        person = createPerson("MR", "Peter4", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:*rty.per*");
        person = createPerson("MR", "Peter5", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:par*.*son*");
        person = createPerson("MR", "Peter6", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:*.*");
        person = createPerson("MR", "Peter7", "Alateras");
        archetype.save(person);
    }

    /**
     * Test method wild card on save
     */
    public void testMethodWildcardAuthorizationOnSave()
            throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:party.person");
        Party person = createPerson("MR", "Save", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.s*:party.person");
        person = createPerson("MR", "Save2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.*ave:party.person");
        person = createPerson("MR", "Save3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.*:party.person");
        person = createPerson("MR", "Save4", "Alateras");
        archetype.save(person);
    }

    /**
     * Test method  and archetype wild card on save
     */
    public void testMethodAndArchetypeWildcardAuthorizationOnSave()
            throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.save:party.person");
        Party person = createPerson("MR", "Bob", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.s*:part*.*son");
        person = createPerson("MR", "Bob2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.*ave:*.*");
        person = createPerson("MR", "Bob3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.*:party.*");
        person = createPerson("MR", "Bob4", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima",
                              "archetype:archetypeService.*:*.*");
        person = createPerson("MR", "Bob4", "Alateras");
        archetype.save(person);
    }

    /**
     * Verifies that authorities are checked when collections of objects are
     * saved via {@link IArchetypeService#save(Collection<IMObject>)}.
     */
    public void testSaveCollection() {
        Party party1 = createPerson("MR", "Jim", "Alateras");
        Party party2 = createPet("Fido");
        List<IMObject> objects = new ArrayList<IMObject>();
        objects.add(party1);
        objects.add(party2);

        objects = checkSave(objects, false,
                            "archetype:archetypeService.save:*.*");
        objects = checkSave(objects, false,
                            "archetype:archetypeService.save:party.*");
        objects = checkSave(objects, false,
                            "archetype:archetypeService.save:party.person",
                            "archetype:archetypeService.save:party.animalpet");
        checkSave(objects, true,
                  "archetype:archetypeService.save:party.person");
    }

    /**
     * Create a secure context for authorization testing.
     *
     * @param user        the user name
     * @param password    the password
     * @param authorities the authorities of the person
     */
    protected abstract void createSecurityContext(String user, String password,
                                                  String ... authorities);

    /**
     * Create a person
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        Party person = (Party) archetype.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);
        person.addContact(createPhoneContact());

        return person;
    }

    /**
     * Creates a pet.
     *
     * @param name the pet's name
     * @return a new pet
     */
    private Party createPet(String name) {
        Party pet = (Party) archetype.create("party.animalpet");
        pet.setName(name);
        pet.getDetails().put("species", "CANINE");
        return pet;
    }

    /**
     * Create a phone contact
     *
     * @return Contact
     */
    private Contact createPhoneContact() {
        Contact contact = (Contact) archetype.create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "1234567");
        contact.getDetails().put("preferred", true);

        return contact;
    }

    /**
     * Checks if a collection can be saved, given a set of authorities.
     *
     * @param objects     the collection to save
     * @param fail        if <tt>true</tt> the save is expected to fail
     * @param authorities the user's authorities
     * @return the saved objects
     */
    private List<IMObject> checkSave(Collection<IMObject> objects,
                                     boolean fail, String ... authorities) {
        List<IMObject> result = null;
        createSecurityContext("jima", "jima", authorities);
        try {
            result = archetype.save(objects);
            if (fail) {
                fail("Save of collection should have failed");
            }
        } catch (OpenVPMSAccessDeniedException exception) {
            if (!fail) {
                fail("Save of collection should not have failed: " + exception);
            }
            if (exception.getErrorCode()
                    != OpenVPMSAccessDeniedException.ErrorCode.AccessDenied) {
                fail("Incorrect error code was specified during the exception");
            }
        }
        return result;
    }
}
