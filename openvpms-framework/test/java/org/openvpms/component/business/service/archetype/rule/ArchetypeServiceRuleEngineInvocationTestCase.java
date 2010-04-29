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

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Exercise the IArchetypeService.executeRule method.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("rule-engine-appcontext.xml")
public class ArchetypeServiceRuleEngineInvocationTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test that rule engine is called when this object is being saved.
     */
    @Test
    public void testSimpleInvocation() {
        IArchetypeService service = getArchetypeService();
        Party person = createPerson("Mr", "Jim", "Alateras");
        List<Object> facts = new ArrayList<Object>();
        facts.add(person);

        List<Object> results = service.executeRule("person.uppercaseName", null, facts);
        assertTrue(results.size() == 1);
        assertTrue(results.get(0) instanceof Party);

        // now set the name
        person.setName("jim alateras");
        results = service.executeRule("person.uppercaseName", null, facts);
        assertTrue(results.size() == 1);
        assertTrue(results.get(0) instanceof Party);
        assertTrue(((Party) results.get(0)).getName().equals(
                "jim alateras".toUpperCase()));
    }

    /**
     * Test a more complicated invocation with more facts.
     */
    @Test
    public void testMultipleFactInvocation() {
        IArchetypeService service = getArchetypeService();

        Party person = createPerson("Mr", "Jim", "Alateras");
        person.setName("Jim Alateras");
        StringBuffer result = new StringBuffer();
        List<Object> facts = new ArrayList<Object>();
        facts.add(person);
        facts.add(result);

        List<Object> results = service.executeRule("person.copyName", null, facts);
        assertTrue(results.size() == 2);
        assertTrue(person.getName().equals(result.toString()));
    }

    /**
     * Test invocation with application data
     */
    @Test
    public void testInvocationWithApplicationData() {
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

        List<Object> results = getArchetypeService().executeRule("person.copyName", appData, facts);
        assertTrue(results.size() == 2);
        assertTrue(result.toString().equals("Mr Jim Alateras"));
    }

    /**
     * Create a person
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party) create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);
        person.addContact(createPhoneContact());

        return person;
    }

    /**
     * Creates a phone contact.
     *
     * @return a new phone contact
     */
    private Contact createPhoneContact() {
        Contact contact = (Contact) create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "1234567");
        contact.getDetails().put("preferred", true);

        return contact;
    }
}
