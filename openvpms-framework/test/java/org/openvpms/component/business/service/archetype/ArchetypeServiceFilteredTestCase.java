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

// java core
import java.util.List;

// spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.party.Party;

// log4j
import org.apache.log4j.Logger;

/**
 * Test that different filtered collection sets to ensure that they work
 * well together.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceFilteredTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceFilteredTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceFilteredTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceFilteredTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml" 
                };
    }

    /**
     * Test the creation process still works with the filtered set in place.
     */
    public void testSimplePersonCreation() throws Exception {
        Party person = createPersonFilter("Mr", "Jim", "Alateras");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(person.getArchetypeId());
        NodeDescriptor ndesc1 = adesc.getNodeDescriptor("staffClassifications");
        NodeDescriptor ndesc2 = adesc.getNodeDescriptor("patientClassifications");
        
        // add a single staff classification
        person.addClassification(createClassification("classification.staff", 
                "class1"));
        assertTrue(((List)ndesc1.getValue(person)).size() == 1); 
        assertTrue(((List)ndesc2.getValue(person)).size() == 0); 
        
        // add a single patient classification
        person.addClassification(createClassification("classification.patient", 
        "patient1"));
        assertTrue(((List)ndesc1.getValue(person)).size() == 1); 
        assertTrue(((List)ndesc2.getValue(person)).size() == 1);
        
        // this should also be valid
        service.validateObject(person);
        
    }
    
    /**
     * Test the modification process still works with the filtered set in place.
     */
    public void testModificationPersonCreation() throws Exception {
        Party person = createPersonFilter("Mr", "Jim", "Alateras");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(person.getArchetypeId());
        NodeDescriptor ndesc1 = adesc.getNodeDescriptor("staffClassifications");
        NodeDescriptor ndesc2 = adesc.getNodeDescriptor("patientClassifications");
        
        // add a single staff classification
        Classification class1 = createClassification("classification.staff", "class1");
        Classification class2 = createClassification("classification.staff", "class2");
        Classification class3 = createClassification("classification.patient", "patient1");
        person.addClassification(class1);
        person.addClassification(class2);
        person.addClassification(class3);
        
        assertTrue(((List)ndesc1.getValue(person)).size() == 2); 
        assertTrue(((List)ndesc2.getValue(person)).size() == 1);
        service.validateObject(person);
        
        // remove all of the classification.staff
        person.removeClassification(class1);
        person.removeClassification(class2);

        assertTrue(((List)ndesc1.getValue(person)).size() == 0); 
        assertTrue(((List)ndesc2.getValue(person)).size() == 1);
        try {
            service.validateObject(person);
            fail("Should not be valid");
        } catch (ValidationException exception) {
            // this is okay
        }
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
                "archetypeService");
    }
    
    /**
     * Create a person with the specified title, firstName and LastName
     * 
     * @param title
     * @param firstName
     * @param lastName
     * 
     * @return Party
     */
    public Party createPersonFilter(String title, String firstName, String lastName) {
        Party person = (Party)service.create("person.filter");
        person.getDetails().setAttribute("lastName", lastName);
        person.getDetails().setAttribute("firstName", firstName);
        person.getDetails().setAttribute("title", title);
        
        return person;
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
}
