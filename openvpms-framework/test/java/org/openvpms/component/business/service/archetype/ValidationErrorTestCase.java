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

package org.openvpms.component.business.service.archetype;

import junit.framework.TestCase;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;

import java.util.Date;


/**
 * Test that validation errors work correctly.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ValidationErrorTestCase extends TestCase {

    /**
     * The archetype service.
     */
    private ArchetypeService service;


    /**
     * Test that a validation exception is actually generated for an invalid
     * object.
     */
    public void testSimpleValidationException() throws Exception {
        Party person = new Party();
        person.setArchetypeId(service.getArchetypeDescriptor("party.person")
                .getType());
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            ValidationException ve = (ValidationException) exception;
            assertEquals(1, ve.getErrors().size());
        }
    }

    /**
     * Test that no validation exception is thrown for this extended validation
     * example.
     */
    public void testExtendedValidationException() throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");
        EntityIdentity eid = (EntityIdentity) service
                .create("entityIdentity.personAlias");
        eid.setIdentity("jimmy");

        person.addIdentity(eid);
        service.validateObject(person);
    }

    /**
     * Test an object going from 5 to zero validation errors.
     */
    public void testDecreaseToZeroErrors() throws Exception {
        Party person = (Party) service.create("party.person");
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }

        person.getDetails().put("lastName", "Alateras");
        service.validateObject(person);
    }

    /**
     * Test that the correct error is generated when a incorrect value is passed
     * in for title.
     */
    public void testIncorrectLookupValue() throws Exception {
        Party person = createPerson("Mister", "Jim", "Alateras");
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }
    }

    /**
     * Test the min cardinality attached to the archetypeRange assertion being
     * satisfied.
     */
    public void testValidMinCardinalityOnArchetypeRange() throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");

        person.addClassification(createLookup("lookup.staff", "CLASS1"));
        service.validateObject(person);
    }

    /**
     * Test the max cardinality attached to the archetypeRange
     * assertion being satisfied.
     */
    public void testValidMaxCardinalityOnArchetypeRange()
            throws Exception {
        Party person = (Party) service.create("party.personjima");

        person.getDetails().put("title", "MR");
        person.getDetails().put("firstName", "Jim");
        person.getDetails().put("lastName", "Alateras");
        person.addClassification(createLookup("lookup.staff", "CLASS1"));
        assertTrue(person.getClassifications().size() == 1);
        service.validateObject(person);
        person.addClassification(createLookup("lookup.staff", "CLASS2"));
        assertTrue(person.getClassifications().size() == 2);
        service.validateObject(person);
        person.addClassification(createLookup("lookup.staff", "CLASS3"));
        service.validateObject(person);
        person.addClassification(createLookup("lookup.patient", "CLASS1"));
        service.validateObject(person);

        // now try and add another .staff
        Lookup classification = null;
        try {
            classification = createLookup("lookup.staff", "class4");
            person.addClassification(classification);
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            ValidationException ve = (ValidationException) exception;
            assertTrue(ve.getErrors().size() == 1);
            assertTrue(ve.getErrors().get(0).getNode().equals(
                    "classifications"));
            person.removeClassification(classification);
        }

        // now try and add another .patient
        try {
            classification = createLookup("lookup.patient", "classs2");
            person.addClassification(classification);
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            ValidationException ve = (ValidationException) exception;
            assertTrue(ve.getErrors().size() == 1);
            assertTrue(ve.getErrors().get(0).getNode().equals(
                    "classifications"));
        }
    }

    /**
     * Test a simple regex validation.
     */
    public void testRegExValidation() throws Exception {
        assertNotNull(service.getArchetypeDescriptor("contact.phoneNumber"));
        Contact contact = (Contact) service.create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "976767666");
        service.validateObject(contact);

        // test for a failure
        try {
            contact.getDetails().put("areaCode", "ABCD");
            service.validateObject(contact);
            fail("Validation should have failed");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
        }
    }

    /**
     * Test that min and max cardinalities also work for collection classes.
     */
    public void testMinMaxCardinalityOnCollections() throws Exception {
        assertTrue(service.getArchetypeDescriptor("party.animalpet") != null);
        Party pet = (Party) service.create("party.animalpet");
        pet.setName("bill");
        pet.getDetails().put("sex", "MALE");
        pet.getDetails().put("dateOfBirth", new Date());

        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }

        // this should now validate
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // so should this
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus1"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus2"));
        service.validateObject(pet);

        // but not this
        try {
            pet.getIdentities().add(createEntityIdentity(
                    "entityIdentity.animalAlias", "brutus3"));
            service.validateObject(pet);
            fail("Validation should have failed since max cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }
    }

    /**
     * Test where only the max cardinality is specified on a collection.
     */
    public void testMaxCardinalityOnCollections() throws Exception {
        assertNotNull(service.getArchetypeDescriptor("party.animalpet1"));
        Party pet = (Party) service.create("party.animalpet1");
        pet.setName("bill");
        pet.getDetails().put("sex", "male");
        pet.getDetails().put("dateOfBirth", new Date());
        service.validateObject(pet);

        // this should validate
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus1"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus2"));
        service.validateObject(pet);

        // but not this
        try {
            pet.getIdentities().add(createEntityIdentity(
                    "entityIdentity.animalAlias", "brutus3"));
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }
    }

    /**
     * Test where min cardinality and unbounded is specifed on collection.
     */
    public void testMinUnboundedCardinalityOnCollections() throws Exception {
        assertTrue(service.getArchetypeDescriptor("party.animalpet2") != null);
        Party pet = (Party) service.create("party.animalpet2");
        pet.setName("bill");
        pet.getDetails().put("sex", "male");
        pet.getDetails().put("dateOfBirth", new Date());
        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }

        // this should not validate
        EntityIdentity eid = (EntityIdentity) service.create(
                "entityIdentity.animalAlias");
        eid.setIdentity("animal1");
        pet.getIdentities().add(eid);
        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }

        // this should not validate
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(
                    ((ValidationException) exception).getErrors().size() == 1);
        }

        // but this should
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and so should this
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);
    }

    /**
     * Test where only unbounded cardinality is specified on collections.
     */
    public void testUnboundedCardinalityOnCollections() throws Exception {
        assertNotNull(service.getArchetypeDescriptor("party.animalpet3"));
        Party pet = (Party) service.create("party.animalpet3");
        pet.setName("bill");
        pet.getDetails().put("sex", "male");
        pet.getDetails().put("dateOfBirth", new Date());
        service.validateObject(pet);

        // this should also validate
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(
                createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);
    }

    /**
     * Verfies that validation fails if any control characters
     * (except '\n', '\r' and '\t') are present in a string.
     *
     * @throws Exception for any error
     */
    public void testInvalidChars() throws Exception {
        assertNotNull(service.getArchetypeDescriptor("party.animalpet"));
        Party pet = (Party) service.create("party.animalpet");

        pet.getDetails().put("sex", "male");
        pet.getDetails().put("dateOfBirth", new Date());

        EntityIdentity eid = (EntityIdentity) service.create(
                "entityIdentity.animalAlias");
        eid.setIdentity("animal1");
        pet.getIdentities().add(eid);

        // check control chars <= 31. \r, \n, and \t should valid
        for (char ch = 0; ch < 32; ++ch) {
            boolean valid = ch == '\r' || ch == '\n' || ch == '\t';
            checkCharacter(ch, valid, pet, service);
        }
        // check remaining chars up to 255. 0x7F (delete) should be invalid.
        for (char ch = 32; ch <= 255; ++ch) {
            boolean valid = ch != '\u007F';  // delete char
            checkCharacter(ch, valid, pet, service);
        }
    }

    /**
     * Sets up the test case.
     */
    @Override
    protected void setUp() {
        String archFile = "org/openvpms/component/business/service/archetype/ValidationErrorArchetypes.xml";
        String assertionFile = "org/openvpms/component/business/service/archetype/assertionTypes.xml";

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                archFile, assertionFile);
        service = new ArchetypeService(cache);
    }

    /**
     * Verifies that a validation exception is thrown for invalid characters
     * present in a string.
     *
     * @param ch      the character to check
     * @param valid   determines if the character is valid or not
     * @param pet     an object to populate
     * @param service the archetype service
     */
    private void checkCharacter(char ch, boolean valid, Party pet,
                                ArchetypeService service) {
        String name = "abc" + ch + "def";
        pet.setName(name);
        try {
            service.validateObject(pet);
            if (!valid) {
                fail("Expected validation error to be thrown for char "
                        + "0x" + Integer.toHexString(ch));
            }
        } catch (ValidationException exception) {
            if (valid) {
                fail("Expected no validation error to be thrown for char "
                        + "0x" + Integer.toHexString(ch));
            }
        }
    }

    /**
     * Create a lookup with the specified code.
     *
     * @param shortName the archetype short name to create
     * @param code      the lookup cocde
     * @return a new lookup
     */
    private Lookup createLookup(String shortName, String code) {
        Lookup lookup = (Lookup) service.create(shortName);
        assertNotNull(lookup);
        lookup.setCode(code);
        return lookup;
    }

    /**
     * Helper to create a new entity identity.
     *
     * @param shortName the archetype
     * @param identity  the identity
     * @return a new entity identity
     */
    private EntityIdentity createEntityIdentity(String shortName,
                                                String identity) {
        EntityIdentity result = (EntityIdentity) service.create(shortName);
        assertNotNull(result);
        result.setIdentity(identity);
        return result;
    }

    /**
     * Create a person.
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party) service.create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

}
