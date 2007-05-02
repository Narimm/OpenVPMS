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

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

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
        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.person");
        Party person = createPerson("MR", "Jim", "Alateras");
        archetype.save(person);
    }

    /**
     * Test that the caller does not have the credentials to make the call
     */
    public void testInvalidAuthorizationOnSave()
    throws Exception {
        // create a security contect before executing a method
        createSecurityContext("bernief", "bernief", "archetype:archetypeService.save:party.person");
        Party person = createPerson("MR", "Peter", "Alateras");

        try {
            archetype.save(person);
            fail("The caller does not have the authority to call IArchetypeService.save");
        } catch (OpenVPMSAccessDeniedException exception) {
            if (exception.getErrorCode() != OpenVPMSAccessDeniedException.ErrorCode.AccessDenied) {
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
        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.per*");
        Party person = createPerson("MR", "Peter1", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:pers*.person");
        person = createPerson("MR", "Peter2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.*erson");
        person = createPerson("MR", "Peter3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:*son.person");
        person = createPerson("MR", "Peter4", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:*son.per*");
        person = createPerson("MR", "Peter5", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:per*.*son*");
        person = createPerson("MR", "Peter6", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:*.*");
        person = createPerson("MR", "Peter7", "Alateras");
        archetype.save(person);
    }

    /**
     * Test method wild card on save
     */
    public void testMethodWildcardAuthorizationOnSave()
    throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.person");
        Party person = createPerson("MR", "Save", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.s*:person.person");
        person = createPerson("MR", "Save2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*ave:person.person");
        person = createPerson("MR", "Save3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*:person.person");
        person = createPerson("MR", "Save4", "Alateras");
        archetype.save(person);
    }

    /**
     * Test method  and archetype wild card on save
     */
    public void testMethodAndArchetypeWildcardAuthorizationOnSave()
            throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.person");
        Party person = createPerson("MR", "Bob", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.s*:pers*.*son");
        person = createPerson("MR", "Bob2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*ave:*.*");
        person = createPerson("MR", "Bob3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*:person.*");
        person = createPerson("MR", "Bob4", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*:*.*");
        person = createPerson("MR", "Bob4", "Alateras");
        archetype.save(person);
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
        Party person = (Party)archetype.create("person.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);
        person.addContact(createPhoneContact());

        return person;
    }

    /**
     * Create a phone contact
     *
     * @return Contact
     */
    private Contact createPhoneContact() {
        Contact contact = (Contact)archetype.create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "1234567");
        contact.getDetails().put("preferred", true);

        return contact;
    }

    /**
     * Create a secure context so that we can do some authorization testing
     *
     * @param user
     *            the user name
     * @param password
     *            the password
     * @param authority
     *            the authority of the person
     *
     */
    abstract protected void createSecurityContext(String user, String password, String authority);
}
