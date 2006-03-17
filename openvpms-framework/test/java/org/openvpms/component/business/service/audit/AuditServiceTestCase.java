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
 *  $Id: AuditServiceTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.audit;


// commons-lang
import java.util.List;

import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.im.audit.AuditRecord;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;

// openvpms-test-component
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test the
 * {@link org.openvpms.component.business.service.audit.AuditService}
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class AuditServiceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(AuditServiceTestCase.class);

    /**
     * Holds a reference to the archetectype service
     */
    private IArchetypeService archetype;
    
    /**
     * Holder a reference to the audit service
     */
    private IAuditService audit;
    
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(AuditServiceTestCase.class);
    }

    /**
     * Default constructor
     */
    public AuditServiceTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "org/openvpms/component/business/service/audit/audit-service-appcontext.xml" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        this.archetype = (IArchetypeService)applicationContext
            .getBean("archetypeService");
        this.audit = (IAuditService)applicationContext
            .getBean("auditService");
    }
    
    /**
     * Test that audit recrods are successfully created on save
     */
    public void testAuditOnSave()
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        archetype.save(person);
        List<AuditRecord> records = audit.getByObjectId(
                person.getArchetypeIdAsString(), person.getUid());

        assertTrue("The size " + records.size() + " for "  + person.getUid(), (records.size() == 1));
    }
    
    /**
     * Test that audit records are successfully created on update
     */
    public void testAuditOnUpdate()
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        archetype.save(person);
        assertTrue(audit.getByObjectId(person.getArchetypeIdAsString(), 
                person.getUid()).size() == 1);
        
        person.getDetails().setAttribute("firstName", "James");
        archetype.save(person);
        assertTrue(audit.getByObjectId(person.getArchetypeIdAsString(), 
                person.getUid()).size() == 2);
    }
    
    /**
     * Test that audit records are successfull created on multiple updates
     */
    public void testAuditOnMultipleUpdates()
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        archetype.save(person);
        
        for (int index = 0; index < 5; index++) {
            person.getDetails().setAttribute("firstName", 
                    (String)person.getDetails().getAttribute("firstName") + index);
            archetype.save(person);
        }
        assertTrue(audit.getByObjectId(person.getArchetypeIdAsString(), 
                person.getUid()).size() == 6);
    }
    
    /**
     * Test that we can retrieve audit records by id
     */
     public void testRetrievalById()
     throws Exception {
         Party person = createPerson("Mr", "Jim", "Alateras");
         archetype.save(person);
         archetype.save(person);
         archetype.save(person);
         List<AuditRecord> records = audit.getByObjectId(
                 person.getArchetypeIdAsString(), person.getUid());
         assertTrue(records.size() == 3);
         
         for (AuditRecord record : records) {
             assertTrue(audit.getById(record.getUid()) != null);
         }
     }
     
     /**
      * Test that an audit record is generated for a delete
      */
     public void testAuditOnDelete()
     throws Exception {
         Party person = createPerson("Mr", "Jim", "Alateras");
         archetype.save(person);
         archetype.remove(person);
         List<AuditRecord> records = audit.getByObjectId(
                 person.getArchetypeIdAsString(), person.getUid());
         assertTrue(records.size() == 2);
         for (AuditRecord record : records) {
             if (record.getOperation().equals("save") ||
                 record.getOperation().equals("remove")) {
                 // no opn
             } else {
                 fail("Unexpected audit record. Operation must either be save or remove");
             }
         }
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
         person.getDetails().setAttribute("lastName", lastName);
         person.getDetails().setAttribute("firstName", firstName);
         person.getDetails().setAttribute("title", title);
         
         return person;
     }
}
