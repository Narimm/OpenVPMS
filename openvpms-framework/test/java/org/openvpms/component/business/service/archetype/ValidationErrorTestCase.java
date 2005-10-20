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
import java.util.Hashtable;

//openvpms-framework
import org.openvpms.component.business.domain.im.common.EntityIdentity;
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
