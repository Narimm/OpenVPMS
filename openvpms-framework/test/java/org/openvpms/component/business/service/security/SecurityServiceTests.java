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

// acegi security
import org.acegisecurity.AccessDeniedException;

//log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.IArchetypeService;

// openvpms-test-component
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * This base class contains all the security test cases
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public abstract class SecurityServiceTests extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(SecurityServiceTests.class);

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
        Person person = createPerson("Mr", "Jim", "Alateras");
        archetype.save(person);
    }
    
    /**
     * Test that the caller does not have the credentials to make the call
     */
    public void testInvalidAuthorizationOnSave()
    throws Exception {
        // create a security contect before executing a method
        createSecurityContext("bernief", "bernief", "archetype:archetypeService.save:party.person");
        Person person = createPerson("Mr", "Peter", "Alateras");
        
        try {
            archetype.save(person);
            fail("The caller does not have the authority to call IArchetypeService.save");
        } catch (AccessDeniedException exception) {
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
        Person person = createPerson("Mr", "Peter1", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:pers*.person");
        person = createPerson("Mr", "Peter2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.*erson");
        person = createPerson("Mr", "Peter3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:*son.person");
        person = createPerson("Mr", "Peter4", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:*son.per*");
        person = createPerson("Mr", "Peter5", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:per*.*son*");
        person = createPerson("Mr", "Peter6", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.save:*.*");
        person = createPerson("Mr", "Peter7", "Alateras");
        archetype.save(person);
    }
    
    /**
     * Test method wild card on save
     */
    public void testMethodWildcardAuthorizationOnSave()
    throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.person");
        Person person = createPerson("Mr", "Save", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.s*:person.person");
        person = createPerson("Mr", "Save2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*ave:person.person");
        person = createPerson("Mr", "Save3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*:person.person");
        person = createPerson("Mr", "Save4", "Alateras");
        archetype.save(person);
    }

    /**
     * Test method  and archetype wild card on save
     */
    public void testMethodAndArchetypeWildcardAuthorizationOnSave()
    throws Exception {
        // create a security contect before executing a method
        createSecurityContext("jima", "jima", "archetype:archetypeService.save:person.person");
        Person person = createPerson("Mr", "Bob", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.s*:pers*.*son");
        person = createPerson("Mr", "Bob2", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*ave:*.*");
        person = createPerson("Mr", "Bob3", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*:person.*");
        person = createPerson("Mr", "Bob4", "Alateras");
        archetype.save(person);

        createSecurityContext("jima", "jima", "archetype:archetypeService.*:*.*");
        person = createPerson("Mr", "Bob4", "Alateras");
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
    private Person createPerson(String title, String firstName, String lastName) {
        Person person = (Person)archetype.create("person.person");
        person.setTitle(title);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.addAddress(createPhoneAddress());

        return person;
    }
    
    /**
     * Create a phone address
     * 
     * @return Address
     */
    private Address createPhoneAddress() {
        Address address = (Address)archetype.create("address.phoneNumber");
        address.getDetails().setAttribute("areaCode", "03");
        address.getDetails().setAttribute("telephoneNumber", "1234567");
        address.getDetails().setAttribute("preferred", new Boolean(true));
        
        return address;
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
