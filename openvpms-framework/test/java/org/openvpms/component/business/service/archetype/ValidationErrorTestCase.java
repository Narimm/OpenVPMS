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
import java.util.Date;
import java.util.Hashtable;

//openvpms-framework
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;


// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test that validation errors work correctly
 *
 * @author   <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version  $LastChangedDate$
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

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String)params.get("assertionFile");
        String dir = (String)params.get("dir");
        String extension = (String)params.get("extension");
        
        service = new ArchetypeService(dir, new String[]{extension}, assertionFile);
        assertTrue(service != null);
    }
    
    /**
     * Test that a validation exception is actually generated for an invalid
     * object
     */
    public void testSimpleValidationException()
    throws Exception {
        Person person = new Person();
        person.setArchetypeId(service.getArchetypeDescriptor("person.person")
                .getArchetypeId());
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException)exception).getErrors().size() == 3);
        }
    }
    
    /**
     * Test that no validation exception is thrown for this extended validation
     * example
     */
    public void testExtendedValidationException()
    throws Exception {
        Person person = (Person)service.createDefaultObject("person.person");
        EntityIdentity eid = (EntityIdentity)service.createDefaultObject("entityIdentity.personAlias");

        person.setTitle("Mr");
        person.setFirstName("Jim");
        person.setLastName("Alateras");
        person.addIdentity(eid);
        service.validateObject(person);
    }
    
    /**
     * Test an object going from 5 to zero validation errors
     */
    public void testDecreaseToZeroErrors()
    throws Exception {
        Person person = new Person();
        person.setArchetypeId(service.getArchetypeDescriptor("person.person")
                .getArchetypeId());
        try {
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException)exception).getErrors().size() == 3);
        }

        try {
            person.setTitle("Mr");
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException)exception).getErrors().size() == 2);
        }

        try {
            person.setFirstName("Jim");
            service.validateObject(person);
            fail("This object should not have passed validation");
        } catch (Exception exception) {
            assertTrue(exception instanceof ValidationException);
            assertTrue(((ValidationException)exception).getErrors().size() == 1);
        }

        person.setLastName("Alateras");
        service.validateObject(person);
    }
    
    /**
     * Test that the correct error is generated when a incorrect value is 
     * passed in for title
     */
   public void testIncorrectLookupValue()
   throws Exception {
       Person person = new Person();
       person.setArchetypeId(service.getArchetypeDescriptor("person.person")
               .getArchetypeId());
       try {
           person.setTitle("Mister");
           person.setFirstName("Jim");
           person.setLastName("Alateras");
           service.validateObject(person);
           fail("This object should not have passed validation");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           assertTrue(((ValidationException)exception).getErrors().size() == 1);
       }
   }

   /**
    * Test that the archetype range validation works correctly.
    */
   public void testArchetypeRangeValidation()
   throws Exception {
       Person person = (Person)service.createDefaultObject("person.person");
       EntityIdentity eid = (EntityIdentity)service.createDefaultObject("entityIdentity.animalAlias");

       try {
           person.setTitle("Mr");
           person.setFirstName("Jim");
           person.setLastName("Alateras");
           person.addIdentity(eid);
           service.validateObject(person);
           fail("This object should not have passed validation");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           
           ValidationException ve = (ValidationException)exception;
           assertTrue(ve.getErrors().size() == 1);
           assertTrue(ve.getErrors().get(0).getNodeName().equals("identities"));
       }
   }
   
   /**
    * Test that the archetype range validation works correctly for multiple
    * validation exceptions
    */
   public void testArchetypeRangeValidation2()
   throws Exception {
       Person person = (Person)service.createDefaultObject("person.person");

       try {
           person.setTitle("Mr");
           person.setFirstName("Jim");
           person.setLastName("Alateras");
           person.addIdentity((EntityIdentity)service
                   .createDefaultObject("entityIdentity.animalAlias"));
           person.addIdentity((EntityIdentity)service
                   .createDefaultObject("entityIdentity.animalAlias"));
           person.addIdentity((EntityIdentity)service
                   .createDefaultObject("entityIdentity.personAlias"));
           person.addIdentity((EntityIdentity)service
                   .createDefaultObject("entityIdentity.animalAlias"));
           service.validateObject(person);
           fail("This object should not have passed validation");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           
           ValidationException ve = (ValidationException)exception;
           assertTrue(ve.getErrors().size() == 1);
           assertTrue(ve.getErrors().get(0).getNodeName().equals("identities"));
       }
   }

   /**
    * Test a simple regex validation
    */
   public void testRegExValidation() throws Exception {
       Hashtable cparams = getTestData().getGlobalParams();
       Hashtable params = this.getTestData().getTestCaseParams(
               "testRegExValidation", "normal");

       ArchetypeService service = new ArchetypeService((String) params
               .get("file"), (String) cparams.get("assertionFile"));
       
       assertTrue(service.getArchetypeDescriptor("address.phoneNumber") != null);
       Address address = (Address)service.createDefaultObject("address.phoneNumber");
       address.getDetails().setAttribute("areaCode", "03");
       address.getDetails().setAttribute("telephoneNumber", "976767666");
       service.validateObject(address);
       
       // test for a failure
       try {
           address.getDetails().setAttribute("areaCode", "ABCD");
           service.validateObject(address);
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

       ArchetypeService service = new ArchetypeService((String) params
               .get("file"), (String) cparams.get("assertionFile"));
       
       assertTrue(service.getArchetypeDescriptor("animal.pet") != null);
       Animal pet = (Animal)service.createDefaultObject("animal.pet");
       pet.setName("bill");
       pet.setSex("male");
       pet.setDateOfBirth(new Date());

       try {
           service.validateObject(pet);
           fail("Validation should have failed since min cardinality was violated");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           assertTrue(((ValidationException)exception).getErrors().size() == 1);
       }
       
       // this should now validate
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // so should this
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // and this
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // but not this
       try {
           pet.getIdentities().add(new EntityIdentity());
           service.validateObject(pet);
           fail("Validation should have failed since max cardinality was violated");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           assertTrue(((ValidationException)exception).getErrors().size() == 1);
       }
   }
   
   /**
    * Test where only the max cardinality is specified on a collection
    */
   public void testMaxCardinalityOnCollections()
   throws Exception {
       Hashtable cparams = getTestData().getGlobalParams();
       Hashtable params = this.getTestData().getTestCaseParams(
               "testMaxCardinalityOnCollections", "normal");

       ArchetypeService service = new ArchetypeService((String) params
               .get("file"), (String) cparams.get("assertionFile"));
       
       assertTrue(service.getArchetypeDescriptor("animal.pet1") != null);
       Animal pet = (Animal)service.createDefaultObject("animal.pet1");
       pet.setName("bill");
       pet.setSex("male");
       pet.setDateOfBirth(new Date());
       service.validateObject(pet);
       
       // this should validate
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // and this
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // and this
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // but not this
       try {
           pet.getIdentities().add(new EntityIdentity());
           service.validateObject(pet);
           fail("Validation should have failed since min cardinality was violated");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           assertTrue(((ValidationException)exception).getErrors().size() == 1);
       }
   }
   
   /**
    * Test where min cardinality and unbounded is specifed on collection
    */
   public void testMinUnboundedCardinalityOnCollections()
   throws Exception {
       Hashtable cparams = getTestData().getGlobalParams();
       Hashtable params = this.getTestData().getTestCaseParams(
               "testMinUnboundedCardinalityOnCollections", "normal");

       ArchetypeService service = new ArchetypeService((String) params
               .get("file"), (String) cparams.get("assertionFile"));
       
       assertTrue(service.getArchetypeDescriptor("animal.pet2") != null);
       Animal pet = (Animal)service.createDefaultObject("animal.pet2");
       pet.setName("bill");
       pet.setSex("male");
       pet.setDateOfBirth(new Date());
       try {
           service.validateObject(pet);
           fail("Validation should have failed since min cardinality was violated");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           assertTrue(((ValidationException)exception).getErrors().size() == 1);
       }
       
       // this should not validate
       pet.getIdentities().add(new EntityIdentity());
       try {
           service.validateObject(pet);
           fail("Validation should have failed since min cardinality was violated");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           assertTrue(((ValidationException)exception).getErrors().size() == 1);
       }
       
       
       // this should not validate
       pet.getIdentities().add(new EntityIdentity());
       try {
           service.validateObject(pet);
           fail("Validation should have failed since min cardinality was violated");
       } catch (Exception exception) {
           assertTrue(exception instanceof ValidationException);
           assertTrue(((ValidationException)exception).getErrors().size() == 1);
       }

       // but this should
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // and so should this
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
   }
   
   /**
    * Tst where only unbounded cardinality is specified on collections
    */
   public void testUnboundedCardinalityOnCollections()
   throws Exception {
       Hashtable cparams = getTestData().getGlobalParams();
       Hashtable params = this.getTestData().getTestCaseParams(
               "testUnboundedCardinalityOnCollections", "normal");

       ArchetypeService service = new ArchetypeService((String) params
               .get("file"), (String) cparams.get("assertionFile"));
       
       assertTrue(service.getArchetypeDescriptor("animal.pet3") != null);
       Animal pet = (Animal)service.createDefaultObject("animal.pet3");
       pet.setName("bill");
       pet.setSex("male");
       pet.setDateOfBirth(new Date());
       service.validateObject(pet);
       
       // this should also validate
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // and this
       pet.getIdentities().add(new EntityIdentity());
       service.validateObject(pet);
       
       // and this
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
       pet.getIdentities().add(new EntityIdentity());
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
}
