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

// java-core
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.test.BaseTestCase;

import java.util.Date;
import java.util.Hashtable;

/**
 * Test that validation errors work correctly
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ValidationErrorTestCase extends BaseTestCase {
    /**
     * cache the archetype service
     */
    private ArchetypeService service;

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ValidationErrorTestCase.class);
    }

    /**
     * Constructor for ArchetypeServiceTestCase.
     *
     * @param name
     */
    public ValidationErrorTestCase(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String) params.get("assertionFile");
        String dir = (String) params.get("dir");
        String extension = (String) params.get("extension");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(dir,
                new String[] { extension }, assertionFile);
        service = new ArchetypeService(cache);
        assertTrue(service != null);
    }

    /**
     * Test that a validation exception is actually generated for an invalid
     * object
     */
    public void testSimpleValidationException() throws Exception {
        Party person = new Party();
        person.setArchetypeId(service.getArchetypeDescriptor("person.person")
                .getType());
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            ValidationException ve = (ValidationException)exception;
            assertTrue("The size should be " + ve.getErrors().size(), ve.getErrors().size() == 2);
        }
    }

    /**
     * Test that no validation exception is thrown for this extended validation
     * example
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
     * Test an object going from 5 to zero validation errors
     */
    public void testDecreaseToZeroErrors() throws Exception {
        Party person = (Party)service.create("person.person");
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }

        person.getDetails().setAttribute("lastName", "Alateras");
        service.validateObject(person);
    }

    /**
     * Test that the correct error is generated when a incorrect value is passed
     * in for title
     */
    public void testIncorrectLookupValue() throws Exception {
        Party person = createPerson("Mister", "Jim", "Alateras");
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }
    }

    /**
     * Test the min cardinality attached to the archetypeRange assertion being
     * satisfied
     */
    public void testValidMinCardinalityOnArchetypeRange() throws Exception {
        Party person = createPerson("MR", "Jim", "Alateras");

        person.addClassification(createClassification("classification.staff",
                "class1"));
        service.validateObject(person);
    }

    /**
     * Test the max cardinality attached to the archetypeRange
     * assertion being satisfied
     */
    public void testValidMaxCardinalityOnArchetypeRange()
    throws Exception {
        Party person = (Party)service.create("person.jima");

        person.getDetails().setAttribute("title", "MR");
        person.getDetails().setAttribute("firstName", "Jim");
        person.getDetails().setAttribute("lastName", "Alateras");
        person.addClassification(createClassification("classification.staff", "class1"));
        assertTrue(person.getClassifications().size() == 1);
        service.validateObject(person);
        person.addClassification(createClassification("classification.staff", "class2"));
        assertTrue(person.getClassifications().size() == 2);
        service.validateObject(person);
        person.addClassification(createClassification("classification.staff", "class3"));
        service.validateObject(person);
        person.addClassification(createClassification("classification.patient", "classs1"));
        service.validateObject(person);

        // now try and add anotehr .staff
        Classification classification = null;
        try {
            classification = createClassification("classification.staff", "class4");
            person.addClassification(classification);
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            ValidationException ve = (ValidationException) exception;
            assertTrue(ve.getErrors().size() == 1);
            assertTrue(ve.getErrors().get(0).getNodeName().equals(
                    "classifications"));
            person.removeClassification(classification);
        }

        // now try and add another .patient
        try {
            classification = createClassification("classification.patient", "classs2");
            person.addClassification(classification);
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            ValidationException ve = (ValidationException) exception;
            assertTrue(ve.getErrors().size() == 1);
            assertTrue(ve.getErrors().get(0).getNodeName().equals(
                    "classifications"));
        }
    }

    /**
     * Test a simple regex validation
     */
    public void testRegExValidation() throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testRegExValidation", "normal");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                (String) params.get("file"), (String) cparams
                        .get("assertionFile"));
        ArchetypeService service = new ArchetypeService(cache);

        assertTrue(service.getArchetypeDescriptor("contact.phoneNumber") != null);
        Contact contact = (Contact) service.create("contact.phoneNumber");
        contact.getDetails().setAttribute("areaCode", "03");
        contact.getDetails().setAttribute("telephoneNumber", "976767666");
        service.validateObject(contact);

        // test for a failure
        try {
            contact.getDetails().setAttribute("areaCode", "ABCD");
            service.validateObject(contact);
            fail("Validation should have failed");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
        }
    }

    /**
     * Test that min and max cardinalities also work for collection classes
     */
    public void testMinMaxCardinalityOnCollections() throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testMinMaxCardinalityOnCollections", "normal");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                (String) params.get("file"), (String) cparams
                        .get("assertionFile"));
        ArchetypeService service = new ArchetypeService(cache);

        assertTrue(service.getArchetypeDescriptor("animal.pet") != null);
        Party pet = (Party) service.create("animal.pet");
        pet.setName("bill");
        pet.getDetails().setAttribute("sex", "male");
        pet.getDetails().setAttribute("dateOfBirth", new Date());

        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }

        // this should now validate
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // so should this
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus1"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus2"));
        service.validateObject(pet);

        // but not this
        try {
            pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus3"));
            service.validateObject(pet);
            fail("Validation should have failed since max cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }
    }

    /**
     * Test where only the max cardinality is specified on a collection
     */
    public void testMaxCardinalityOnCollections() throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testMaxCardinalityOnCollections", "normal");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                (String) params.get("file"), (String) cparams
                        .get("assertionFile"));
        ArchetypeService service = new ArchetypeService(cache);

        assertTrue(service.getArchetypeDescriptor("animal.pet1") != null);
        Party pet = (Party) service.create("animal.pet1");
        pet.setName("bill");
        pet.getDetails().setAttribute("sex", "male");
        pet.getDetails().setAttribute("dateOfBirth", new Date());
        service.validateObject(pet);

        // this should validate
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus1"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus2"));
        service.validateObject(pet);

        // but not this
        try {
            pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus3"));
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }
    }

    /**
     * Test where min cardinality and unbounded is specifed on collection
     */
    public void testMinUnboundedCardinalityOnCollections() throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testMinUnboundedCardinalityOnCollections", "normal");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                (String) params.get("file"), (String) cparams
                        .get("assertionFile"));
        ArchetypeService service = new ArchetypeService(cache);

        assertTrue(service.getArchetypeDescriptor("animal.pet") != null);
        Party pet = (Party) service.create("animal.pet2");
        pet.setName("bill");
        pet.getDetails().setAttribute("sex", "male");
        pet.getDetails().setAttribute("dateOfBirth", new Date());
        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }

        // this should not validate
        EntityIdentity eid = (EntityIdentity)service.create("entityIdentity.animalAlias");
        eid.setIdentity("animal1");
        pet.getIdentities().add(eid);
        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }

        // this should not validate
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        try {
            service.validateObject(pet);
            fail("Validation should have failed since min cardinality was violated");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException) exception).getErrors().size() == 1);
        }

        // but this should
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and so should this
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);
    }

    /**
     * Tst where only unbounded cardinality is specified on collections
     */
    public void testUnboundedCardinalityOnCollections() throws Exception {
        Hashtable cparams = getTestData().getGlobalParams();
        Hashtable params = this.getTestData().getTestCaseParams(
                "testUnboundedCardinalityOnCollections", "normal");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                (String) params.get("file"), (String) cparams
                        .get("assertionFile"));
        ArchetypeService service = new ArchetypeService(cache);

        assertTrue(service.getArchetypeDescriptor("animal.pet3") != null);
        Party pet = (Party) service.create("animal.pet3");
        pet.setName("bill");
        pet.getDetails().setAttribute("sex", "male");
        pet.getDetails().setAttribute("dateOfBirth", new Date());
        service.validateObject(pet);

        // this should also validate
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);

        // and this
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        pet.getIdentities().add(createEntityIdentity("entityIdentity.animalAlias", "brutus"));
        service.validateObject(pet);
    }

    /**
     * Dump the errors in the validation exception.
     *
     * @param exception
     *            the validation exception
     */
    protected void dumpErrors(ValidationException exception) {
        for (ValidationError error : exception.getErrors()) {
            error(error);
        }
    }

    /**
     * Create a classification with the specified name.
     *
     * @param shortName
     *            the archetype short name to create
     * @param name
     *            the name of the classification
     * @return Classification
     */
    private Classification createClassification(String shortName, String name)
            throws Exception {
        Classification classification = (Classification) service
                .create(shortName);
        classification.setName(name);
        classification.setDescription(name);

        return classification;
    }

    /**
     * This will create an entity identtiy with the specified identity
     *
     * @param shortName
     *            the archetype
     * @param identity
     *            the identity to set
     * @return EntityIdentity
     * @throws Exception
     */
    private EntityIdentity createEntityIdentity(String shortName, String identity)
    throws Exception {
        EntityIdentity eid = (EntityIdentity) service.create(shortName);
        eid.setIdentity(identity);

        return eid;
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

}
