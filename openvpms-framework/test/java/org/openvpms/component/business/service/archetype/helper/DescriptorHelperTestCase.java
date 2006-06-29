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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.List;


/**
 * Tests the {@link DescriptorHelper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DescriptorHelperTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptor(String)} method.
     */
    public void testGetArchetypeDescriptor() {
        ArchetypeDescriptor animal
                = DescriptorHelper.getArchetypeDescriptor("animal.pet");
        assertNotNull(animal);
        assertEquals("animal.pet", animal.getType().getShortName());

        ArchetypeDescriptor noExist
                = DescriptorHelper.getArchetypeDescriptor("fox.pet");
        assertNull(noExist);

        // verify wildcards not expanded
        ArchetypeDescriptor noWild
                = DescriptorHelper.getArchetypeDescriptor("animal.*");
        assertNull(noWild);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptor(IMObjectReference)} method.
     */
    public void testGetArchetypeDescriptorFromRef() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject pet = service.create("animal.pet");
        assertNotNull(pet);

        ArchetypeDescriptor expected;
        ArchetypeDescriptor actual;

        IMObjectReference ref = pet.getObjectReference();

        expected = DescriptorHelper.getArchetypeDescriptor("animal.pet");
        actual = DescriptorHelper.getArchetypeDescriptor(ref);
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected, actual);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptor(IMObject)} method.
     */
    public void testGetArchetypeDescriptorFromObject() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject person = service.create("party.customerperson");
        assertNotNull(person);

        ArchetypeDescriptor expected;
        ArchetypeDescriptor actual;

        expected = DescriptorHelper.getArchetypeDescriptor(
                "party.customerperson");
        actual = DescriptorHelper.getArchetypeDescriptor(person);
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected, actual);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptors(String[])} method.
     */
    public void testGetArchetypeDescriptorFromRange() {
        String[] range = {"act.customerAccountPayment",
                          "act.customerEstimation",
                          "act.customerEstimationItem"};
        List<ArchetypeDescriptor> matches
                = DescriptorHelper.getArchetypeDescriptors(range);
        checkEquals(range, matches);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptors(String[])}
     * method, with wildcards.
     */
    public void testGetAfrchetypeDescriptorFromWildcardRange() {
        String[] range = {"entityRelationship.animal*",
                          "entityRelationship.family*"};
        String[] expected = {"entityRelationship.animalCarer",
                             "entityRelationship.animalOwner",
                             "entityRelationship.familyMember"};
        List<ArchetypeDescriptor> matches
                = DescriptorHelper.getArchetypeDescriptors(range);
        checkEquals(expected, matches);
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String, String, String)}
     * method.
     */
    public void testGetShortNames() {
        String[] result;
        result = DescriptorHelper.getShortNames("party", "animal", "pet");
        checkEquals(result, "animal.pet");

        result = DescriptorHelper.getShortNames(null, "animal", "pet");
        checkEquals(result, "animal.pet");

        result = DescriptorHelper.getShortNames(null, null, "pet");
        checkEquals(result, "animal.pet", "horse.pet");

        result = DescriptorHelper.getShortNames("party", "animal", null);
        checkEquals(result, "animal.pet");

        result = DescriptorHelper.getShortNames(null, "act",
                                                "customerEstimation*");
        checkEquals(result, "act.customerEstimation",
                    "act.customerEstimationItem");
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(NodeDescriptor)} method.
     */
    public void testGetShortNamesFromNodeDescriptor() {
        ArchetypeDescriptor person = DescriptorHelper.getArchetypeDescriptor(
                "person.person");
        assertNotNull(person);

        // get a range from a collection node
        NodeDescriptor contacts = person.getNodeDescriptor("contacts");
        assertNotNull(contacts);
        String[] range = DescriptorHelper.getShortNames(contacts);
        checkEquals(range, "contact.location", "contact.phoneNumber");

        // now check a node with a filter
        ArchetypeDescriptor personFilter
                = DescriptorHelper.getArchetypeDescriptor("person.filter");
        assertNotNull(personFilter);
        NodeDescriptor patients
                = personFilter.getNodeDescriptor("staffClassifications");
        assertNotNull(patients);
        assertNotNull(patients.getFilter());
        String[] filter = DescriptorHelper.getShortNames(patients);
        checkEquals(filter, "classification.staff");
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String)} method.
     */
    public void testGetShortNamesFromSingleShortName() {
        String[] actual;

        actual = DescriptorHelper.getShortNames("animal.pet");
        checkEquals(actual, "animal.pet");

        // now check wildcards
        actual = DescriptorHelper.getShortNames("*.pet");
        checkEquals(actual, "animal.pet", "horse.pet");
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String, boolean)} method.
     */
    public void testGetShortNamesNoPrimary() {
        String[] actual;

        // verify non-primary archetypes aren't returned when primaryOnly==true
        actual = DescriptorHelper.getShortNames("contact.*", true);
        assertEquals(0, actual.length);

        // verify they are returned when primaryOnly==false
        actual = DescriptorHelper.getShortNames("contact.*", false);
        checkEquals(actual, "contact.location", "contact.phoneNumber");
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String[])} method.
     */
    public void testGetShortNamesFromRange() {
        String[] expected = {"animal.pet", "horse.pet"};
        String[] actual = DescriptorHelper.getShortNames(expected);
        checkEquals(actual, expected);

        String[] wildcards = {"animal.*", "horse.*"};
        actual = DescriptorHelper.getShortNames(wildcards);
        checkEquals(actual, expected);
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String[], boolean)} method.
     */
    public void testGetShortNamesFromRangeNoPrimary() {
        // verify non-primary archetypes aren't returned when primaryOnly==true
        String[] expected = {"contact.location", "contact.phoneNumber"};
        String[] actual = DescriptorHelper.getShortNames(expected, true);
        assertEquals(0, actual.length);

        // verify they are returned when primaryOnly==false
        actual = DescriptorHelper.getShortNames(expected, false);
        checkEquals(expected, actual);
    }

    /**
     * Tests the {@link DescriptorHelper#getDisplayName(String)} method.
     */
    public void testGetDisplayName() {
        String name = DescriptorHelper.getDisplayName("animal.pet");
        assertEquals("Patient(Pet)", name);

        // now check non-existent archetype
        String noExist = DescriptorHelper.getDisplayName("badshortname");
        assertNull(noExist);
    }

    /**
     * Tests the {@link DescriptorHelper#getDisplayName(IMObject)} method.
     */
    public void testGetDisplayNameForObject() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create("animal.pet");
        assertNotNull(object);
        String name = DescriptorHelper.getDisplayName(object);
        assertEquals("Patient(Pet)", name);
    }

    /**
     * Tests the {@link DescriptorHelper#getDisplayName(IMObject, String)}
     * method.
     */
    public void testGetDisplayNameForNode() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create("act.customerAccountPayment");
        assertNotNull(object);
        String name = DescriptorHelper.getDisplayName(object, "startTime");
        assertEquals("Date", name);
    }

    /**
     * (non-Javadoc)
     *
     * @see AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }

    /**
     * Verifies that two lists of short names match.
     */
    private void checkEquals(String[] actualShortNames,
                             String ... expectedShortNames) {
        assertEquals(expectedShortNames.length, actualShortNames.length);
        for (String expected : expectedShortNames) {
            boolean found = false;
            for (String actual : actualShortNames) {
                if (expected.equals(actual)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Shortname not found: " + expected, found);
        }
    }

    /**
     * Verifies that a list of short names are all present in a list of
     * archetype descriptors
     *
     * @param shortNames the short names to check
     * @param archetypes the archetype descriptors
     */
    private void checkEquals(String[] shortNames,
                             List<ArchetypeDescriptor> archetypes) {
        assertEquals(shortNames.length, archetypes.size());
        for (String shortName : shortNames) {
            boolean found = false;
            for (ArchetypeDescriptor archetype : archetypes) {
                if (archetype.getType().getShortName().equals(shortName)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Archetype not found for shortname: " + shortName,
                       found);
        }
    }

}
