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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This base class contains all the security test cases.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
@SuppressWarnings("HardCodedStringLiteral")
public abstract class SecurityServiceTests extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    protected IArchetypeService archetype;

    /**
     * User name seed.
     */
    private int seed;

    /**
     * Verifies that authentication is checked when creating objects.
     */
    public void testCreate() {
        final String shortName = "party.person";
        Runnable createByShortName = new Runnable() {
            public void run() {
                archetype.create(shortName);
            }
        };
        Runnable createByArchetypeId = new Runnable() {
            public void run() {
                ArchetypeId id = new ArchetypeId(shortName);
                assertNotNull(archetype.create(id));
            }
        };

        // createSecurityContext("jima", "jima", "archetype:archetypeService.create:party.noauth");
        checkOperation(createByShortName, true);
        checkOperation(createByArchetypeId, true);

        String auth = "archetype:archetypeService.create:party.person";
        checkOperation(createByShortName, false, auth);
        checkOperation(createByArchetypeId, false, auth);
    }

    /**
     * Verifies that authentication is checked when saving objects.
     */
    public void testSave() {
        final Party person = createPerson("MR", "Jim", "Alateras");
        Runnable save = new Runnable() {
            public void run() {
                archetype.save(person);
            }
        };

        // verify that the save fails when no save authority is granted
        checkOperation(save, true);
        checkOperation(save, true, "archetype:archetypeService.save:party.invalid");

        // ... and succeeds when it is
        checkOperation(save, false, "archetype:archetypeService.save:party.person");

        // now check with authorities with wildcarded archetypes
        checkOperation(save, true, "archetype:archetypeService.save:person.*");
        checkOperation(save, true, "archetype:archetypeService.*:person.*");
        checkOperation(save, false, "archetype:archetypeService.save:party.*");
        checkOperation(save, false, "archetype:archetypeService.save:*.person");
        checkOperation(save, false, "archetype:archetypeService.save:party.per*");
        checkOperation(save, false, "archetype:archetypeService.save:part*.person");
        checkOperation(save, false, "archetype:archetypeService.save:party.*erson");
        checkOperation(save, false, "archetype:archetypeService.save:*rty.person");
        checkOperation(save, false, "archetype:archetypeService.save:*rty.per*");
        checkOperation(save, false, "archetype:archetypeService.save:par*.*son*");
        checkOperation(save, false, "archetype:archetypeService.save:*.*");

        // now check with authorities with wildcarded methods
        checkOperation(save, true, "archetype:archetypeService.*:party.invalid");
        checkOperation(save, true, "archetype:archetypeService.s*:party.invalid");
        checkOperation(save, false, "archetype:archetypeService.s*:party.person");
        checkOperation(save, false, "archetype:archetypeService.*ave:party.person");
        checkOperation(save, false, "archetype:archetypeService.*:party.person");

        // now check with authorities with both wildcarded methods and archetypes
        checkOperation(save, true, "archetype:archetypeService.s*:part*.invalid");
        checkOperation(save, false, "archetype:archetypeService.s*:part*.*son");
        checkOperation(save, false, "archetype:archetypeService.*ave:*.*");
        checkOperation(save, false, "archetype:archetypeService.*:party.*");
        checkOperation(save, false, "archetype:archetypeService.*:*.*");
    }

    /**
     * Verifies that authorities are checked when collections of objects are
     * saved via {@link IArchetypeService#save(Collection<IMObject>)}.
     */
    public void testSaveCollection() {
        Party party1 = createPerson("MR", "Jim", "Alateras");
        Party party2 = createPet("Fido");
        final List<IMObject> objects = new ArrayList<IMObject>();
        objects.add(party1);
        objects.add(party2);

        Runnable save = new Runnable() {
            public void run() {
                archetype.save(objects);
            }
        };
        checkOperation(save, false, "archetype:archetypeService.save:party.*");
        checkOperation(save, false, "archetype:archetypeService.save:party.person",
                       "archetype:archetypeService.save:party.animalpet");
        checkOperation(save, false, "archetype:archetypeService.save:*.*");
        checkOperation(save, true, "archetype:archetypeService.save:party.person");
    }

    /**
     * Verifies that authentication is checked when removing objects.
     */
    public void testRemove() {
        final Party person = createPerson("MR", "Jim", "Alateras");
        createSecurityContext("jima", "jima", "archetype:archetypeService.save:*");
        archetype.save(person);

        Runnable remove = new Runnable() {
            public void run() {
                archetype.remove(person);
            }
        };

        checkOperation(remove, true);
        checkOperation(remove, true, "archetype:archetypeService.*:party.invalid");
        checkOperation(remove, false, "archetype:archetypeService.remove:party.person");
    }

    /**
     * Create a secure context for authorization testing.
     *
     * @param user        the user name
     * @param password    the password
     * @param authorities the authorities of the person
     */
    protected abstract void createSecurityContext(String user, String password,
                                                  String... authorities);

    /**
     * Creates a person.
     * <p/>
     * Note that this method grants authorities in order to perform the creation.
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    private Party createPerson(String title, String firstName,
                               String lastName) {
        createSecurityContext("jima", "jima", "archetype:archetypeService.create:*");
        Party person = (Party) archetype.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);
        person.addContact(createPhoneContact());

        return person;
    }

    /**
     * Creates a pet.
     * <p/>
     * Note that this method grants authorities in order to perform the creation.
     *
     * @param name the pet's name
     * @return a new pet
     */
    private Party createPet(String name) {
        createSecurityContext("jima", "jima", "archetype:archetypeService.create:*");
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
     * Executes an operation by a user with the specified authorities.
     *
     * @param operation   the operation to execute
     * @param fail        if <tt>true</tt>, expect the operation to fail, otherwise expect it to succeed
     * @param authorities the user's authorities
     */
    private void checkOperation(Runnable operation, boolean fail,
                                String... authorities) {
        createSecurityContext("jima" + seed, "jima", authorities);
        ++seed;
        try {
            operation.run();
            if (fail) {
                fail("Expected operation to fail");
            }
        } catch (OpenVPMSAccessDeniedException exception) {
            if (!fail) {
                fail("Didn't expect operation to fail");
            }
            if (exception.getErrorCode() != OpenVPMSAccessDeniedException.ErrorCode.AccessDenied) {
                fail("Incorrect error code was specified during the exception");
            }
        }
    }
}
