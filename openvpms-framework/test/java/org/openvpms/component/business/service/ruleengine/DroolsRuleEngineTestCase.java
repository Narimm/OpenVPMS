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
 *  $Id: DroolsRuleEngineTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.ruleengine;


// log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.IArchetypeService;

// openvpms-test-component
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test the
 * {@link org.openvpms.component.business.service.ruleengine.DroolsRuleEngine}
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class DroolsRuleEngineTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(DroolsRuleEngineTestCase.class);

    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService archetype;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(DroolsRuleEngineTestCase.class);
    }

    /**
     * Default constructor
     */
    public DroolsRuleEngineTestCase() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "org/openvpms/component/business/service/ruleengine/rule-engine-appcontext.xml" };
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
    public void testRuleEngineOnSave()
    throws Exception {
        Person person = createPerson("Mr", "Jim", "Alateras");
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
}
