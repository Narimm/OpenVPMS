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

package org.openvpms.component.business.service.archetype.rule;

// java core
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;

// openvpms-test-component
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Exercise the IArchetypeService.executeRule method
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceRuleEngineInvocationTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServiceRuleEngineInvocationTestCase.class);

    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService archetype;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceRuleEngineInvocationTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceRuleEngineInvocationTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "org/openvpms/component/business/service/archetype/rule/rule-engine-appcontext.xml" };
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
    }
    
    /**
     * Test that rule engine is called when this object is being saved
     */
    public void testSimpleInvocation()
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        List<Object> facts = new ArrayList<Object>();
        facts.add(person);
        
        List<Object> results = archetype.executeRule("person.uppercaseName", null, facts);
        assertTrue(results.size() == 1);
        assertTrue(results.get(0) instanceof Party);
        
        // now set the name
        person.setName("jim alateras");
        results = archetype.executeRule("person.uppercaseName", null, facts);
        assertTrue(results.size() == 1);
        assertTrue(results.get(0) instanceof Party);
        assertTrue(((Party)results.get(0)).getName().equals("jim alateras".toUpperCase()));
    }
    
    /**
     * Test a more complicated invocation with more facts
     */
    public void testMultipleFactInvocation()
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        person.setName("Jim Alateras");
        StringBuffer result = new StringBuffer();
        List<Object> facts = new ArrayList<Object>();
        facts.add(person);
        facts.add(result);
        
        List<Object> results = archetype.executeRule("person.copyName", null, facts);
        assertTrue(results.size() == 2);
        assertTrue(person.getName().equals(result.toString()));
    }
    
    /**
     * Test invocation with application data
     */
    public void testInvocationWithApplicationData()
    throws Exception {
        Party person = createPerson("Mr", "Jim", "Alateras");
        person.setName("Jim Alateras");
        StringBuffer result = new StringBuffer();
        
        // facts
        List<Object> facts = new ArrayList<Object>();
        facts.add(person);
        facts.add(result);
        
        // application data
        Map<String, Object> appData = new HashMap<String, Object>();
        appData.put("prefix", "Mr ");
        
        List<Object> results = archetype.executeRule("person.copyName", appData, facts);
        assertTrue(results.size() == 2);
        assertTrue(result.toString().equals("Mr Jim Alateras"));
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
        contact.getDetails().setAttribute("areaCode", "03");
        contact.getDetails().setAttribute("telephoneNumber", "1234567");
        contact.getDetails().setAttribute("preferred", new Boolean(true));
        
        return contact;
    }
}
