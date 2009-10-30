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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.List;


/**
 * Test that different filtered collection sets to ensure that they work
 * well together.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceFilteredTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the archetype service.
     */
    private ArchetypeService service;


    /**
     * Test the creation process still works with the filtered set in place.
     */
    public void testSimplePersonCreation() throws Exception {
        Party person = createPersonFilter("MR", "Jim", "Alateras");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                person.getArchetypeId());
        NodeDescriptor ndesc1 = adesc.getNodeDescriptor("staffClassifications");
        NodeDescriptor ndesc2 = adesc.getNodeDescriptor(
                "patientClassifications");

        // add a single staff classification
        person.addClassification(createLookup("lookup.staff", "class1"));
        assertTrue(((List) ndesc1.getValue(person)).size() == 1);
        assertTrue(((List) ndesc2.getValue(person)).size() == 0);

        // add a single patient classification
        person.addClassification(createLookup("lookup.patient", "patient1"));
        assertTrue(((List) ndesc1.getValue(person)).size() == 1);
        assertTrue(((List) ndesc2.getValue(person)).size() == 1);

        // this should also be valid
        service.validateObject(person);

    }

    /**
     * Test the modification process still works with the filtered set in place.
     */
    public void testModificationPersonCreation() throws Exception {
        Party person = createPersonFilter("MR", "Jim", "Alateras");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                person.getArchetypeId());
        NodeDescriptor ndesc1 = adesc.getNodeDescriptor("staffClassifications");
        NodeDescriptor ndesc2 = adesc.getNodeDescriptor(
                "patientClassifications");

        // add classification lookups
        Lookup class1 = createLookup("lookup.staff", "class1");
        Lookup class2 = createLookup("lookup.staff", "class2");
        Lookup class3 = createLookup("lookup.patient", "patient1");
        person.addClassification(class1);
        person.addClassification(class2);
        person.addClassification(class3);

        assertTrue(((List) ndesc1.getValue(person)).size() == 2);
        assertTrue(((List) ndesc2.getValue(person)).size() == 1);
        service.validateObject(person);

        // remove all of the classification.staff
        person.removeClassification(class1);
        person.removeClassification(class2);

        assertTrue(((List) ndesc1.getValue(person)).size() == 0);
        assertTrue(((List) ndesc2.getValue(person)).size() == 1);
        try {
            service.validateObject(person);
            fail("Should not be valid");
        } catch (ValidationException exception) {
            // this is okay
        }
    }

    /**
     * Create a person with the specified title, firstName and LastName
     *
     * @param title
     * @param firstName
     * @param lastName
     * @return Party
     */
    public Party createPersonFilter(String title, String firstName,
                                    String lastName) {
        Party person = (Party) service.create("party.personfilter");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /* (non-Javadoc)
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
    */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Create a lookup with the specified code.
     *
     * @param shortName the lookup short name
     * @param code    the code of the lookup
     * @return a new lookup
     */
    private Lookup createLookup(String shortName, String code) {
        return LookupUtil.createLookup(service, shortName, code);
    }
}
