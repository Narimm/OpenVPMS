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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;


/**
 * Test that different filtered collection sets to ensure that they work
 * well together.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("archetype-service-appcontext.xml")
public class ArchetypeServiceFilteredTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test the creation process still works with the filtered set in place.
     */
    @Test
    public void testSimplePersonCreation() {
        Party person = createPersonFilter("MR", "Jim", "Alateras");
        ArchetypeDescriptor adesc = getArchetypeService().getArchetypeDescriptor(person.getArchetypeId());
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
        validateObject(person);

    }

    /**
     * Test the modification process still works with the filtered set in place.
     */
    @Test
    public void testModificationPersonCreation() {
        Party person = createPersonFilter("MR", "Jim", "Alateras");
        ArchetypeDescriptor adesc = getArchetypeService().getArchetypeDescriptor(person.getArchetypeId());
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
        validateObject(person);

        // remove all of the classification.staff
        person.removeClassification(class1);
        person.removeClassification(class2);

        assertTrue(((List) ndesc1.getValue(person)).size() == 0);
        assertTrue(((List) ndesc2.getValue(person)).size() == 1);
        try {
            validateObject(person);
            fail("Should not be valid");
        } catch (ValidationException exception) {
            // this is okay
        }
    }

    /**
     * Create a person with the specified title, firstName and LastName
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return a new party
     */
    public Party createPersonFilter(String title, String firstName,
                                    String lastName) {
        Party person = (Party) create("party.personfilter");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /**
     * Create a lookup with the specified code.
     *
     * @param shortName the lookup short name
     * @param code      the code of the lookup
     * @return a new lookup
     */
    private Lookup createLookup(String shortName, String code) {
        return LookupUtil.createLookup(getArchetypeService(), shortName, code);
    }
}
