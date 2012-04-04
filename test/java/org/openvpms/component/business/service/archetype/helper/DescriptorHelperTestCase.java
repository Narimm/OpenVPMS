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

import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DescriptorHelper} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class DescriptorHelperTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptor(String)} method.
     */
    @Test
    public void testGetArchetypeDescriptor() {
        ArchetypeDescriptor animal = DescriptorHelper.getArchetypeDescriptor("party.animalpet");
        assertNotNull(animal);
        assertEquals("party.animalpet", animal.getType().getShortName());

        ArchetypeDescriptor noExist = DescriptorHelper.getArchetypeDescriptor("fox.pet");
        assertNull(noExist);

        // verify wildcards not expanded
        ArchetypeDescriptor noWild = DescriptorHelper.getArchetypeDescriptor("party.animal*");
        assertNull(noWild);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptor(IMObjectReference)} method.
     */
    @Test
    public void testGetArchetypeDescriptorFromRef() {
        IMObject pet = create("party.animalpet");

        ArchetypeDescriptor expected;
        ArchetypeDescriptor actual;

        IMObjectReference ref = pet.getObjectReference();

        expected = DescriptorHelper.getArchetypeDescriptor("party.animalpet");
        actual = DescriptorHelper.getArchetypeDescriptor(ref);
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected, actual);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptor(IMObject)} method.
     */
    @Test
    public void testGetArchetypeDescriptorFromObject() {
        IMObject person = create("party.customerperson");

        ArchetypeDescriptor expected;
        ArchetypeDescriptor actual;

        expected = DescriptorHelper.getArchetypeDescriptor("party.customerperson");
        actual = DescriptorHelper.getArchetypeDescriptor(person);
        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals(expected, actual);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptors(String[])} method.
     */
    @Test
    public void testGetArchetypeDescriptorFromRange() {
        String[] range = {"act.customerAccountPayment",
                          "act.customerEstimation",
                          "act.customerEstimationItem"};
        List<ArchetypeDescriptor> matches = DescriptorHelper.getArchetypeDescriptors(range);
        checkEquals(range, matches);
    }

    /**
     * Tests the {@link DescriptorHelper#getArchetypeDescriptors(String[])}
     * method, with wildcards.
     */
    @Test
    public void testGetAfrchetypeDescriptorFromWildcardRange() {
        String[] range = {"entityRelationship.animal*",
                          "entityRelationship.family*"};
        String[] expected = {"entityRelationship.animalCarer",
                             "entityRelationship.animalOwner",
                             "entityRelationship.familyMember"};
        List<ArchetypeDescriptor> matches = DescriptorHelper.getArchetypeDescriptors(range);
        checkEquals(expected, matches);
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String, String)}
     * method.
     */
    @Test
    public void testGetShortNames() {
        String[] result;
        result = DescriptorHelper.getShortNames("party", "animalpet");
        checkEquals(result, "party.animalpet");

        result = DescriptorHelper.getShortNames(null, "animalpet");
        checkEquals(result, "party.animalpet");

        result = DescriptorHelper.getShortNames(null, "*pet");
        checkEquals(result, "party.patientpet", "party.animalpet", "party.horsepet");

        result = DescriptorHelper.getShortNames("party", "animal*");
        checkEquals(result, "party.animalpet");

        result = DescriptorHelper.getShortNames("act", "customerEstimation*");
        checkEquals(result, "act.customerEstimation", "act.customerEstimationItem");
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(NodeDescriptor)} method.
     */
    @Test
    public void testGetShortNamesFromNodeDescriptor() {
        ArchetypeDescriptor person = DescriptorHelper.getArchetypeDescriptor("party.person");
        assertNotNull(person);

        // get a range from a collection node
        NodeDescriptor contacts = person.getNodeDescriptor("contacts");
        assertNotNull(contacts);
        String[] range = DescriptorHelper.getShortNames(contacts);
        checkEquals(range, "contact.location", "contact.phoneNumber");

        // now check a node with a filter
        ArchetypeDescriptor personFilter = DescriptorHelper.getArchetypeDescriptor("party.personfilter");
        assertNotNull(personFilter);
        NodeDescriptor staff = personFilter.getNodeDescriptor("staffClassifications");
        assertNotNull(staff);
        assertNotNull(staff.getFilter());
        String[] staffShortNames = DescriptorHelper.getShortNames(staff);
        checkEquals(staffShortNames, "lookup.staff");

        // now check a node with a filter and multiple archetypes, and verify
        // order is preserved
        NodeDescriptor patients = person.getNodeDescriptor("patients");
        assertNotNull(patients);
        assertNotNull(patients.getFilter());
        String[] relShortNames = DescriptorHelper.getShortNames(patients);
        checkEquals(relShortNames, true, "entityRelationship.animalOwner", "entityRelationship.animalCarer");
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String)} method.
     */
    @Test
    public void testGetShortNamesFromSingleShortName() {
        String[] actual;

        actual = DescriptorHelper.getShortNames("party.animalpet");
        checkEquals(actual, "party.animalpet");

        // now check wildcards
        actual = DescriptorHelper.getShortNames("*pet");
        checkEquals(actual, "party.patientpet", "party.animalpet", "party.horsepet");
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String, boolean)} method.
     */
    @Test
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
    @Test
    public void testGetShortNamesFromRange() {
        String[] expected = {"party.animalpet", "party.horsepet"};
        String[] actual = DescriptorHelper.getShortNames(expected);
        checkEquals(actual, expected);

        String[] wildcards = {"party.animal*", "party.horse*"};
        actual = DescriptorHelper.getShortNames(wildcards);
        checkEquals(actual, expected);
    }

    /**
     * Tests the {@link DescriptorHelper#getShortNames(String[], boolean)} method.
     */
    @Test
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
    @Test
    public void testGetDisplayName() {
        String name = DescriptorHelper.getDisplayName("party.animalpet");
        assertEquals("Patient(Pet)", name);

        // now check non-existent archetype
        String noExist = DescriptorHelper.getDisplayName("badshortname");
        assertNull(noExist);
    }

    /**
     * Tests the {@link DescriptorHelper#getDisplayName(IMObject)} method.
     */
    @Test
    public void testGetDisplayNameForObject() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        IMObject object = service.create("party.animalpet");
        assertNotNull(object);
        String name = DescriptorHelper.getDisplayName(object);
        assertEquals("Patient(Pet)", name);
    }

    /**
     * Tests the {@link DescriptorHelper#getDisplayName(IMObject, String)}
     * method.
     */
    @Test
    public void testGetDisplayNameForObjectNode() {
        IMObject object = create("act.customerAccountPayment");
        assertNotNull(object);
        String name = DescriptorHelper.getDisplayName(object, "startTime");
        assertEquals("Date", name);

        // check a non existent node
        assertNull(DescriptorHelper.getDisplayName(object, "foo"));
    }

    /**
     * Tests the {@link DescriptorHelper#getDisplayName(String, String)} method.
     */
    @Test
    public void testGetDisplayNameForArchetypeNode() {
        String name = DescriptorHelper.getDisplayName(
                "act.customerAccountPayment", "startTime");
        assertEquals("Date", name);

        // check a non existent node
        assertNull(DescriptorHelper.getDisplayName("act.customerAccountPayment",
                                                   "foo"));

        // check a non-existent archetype
        assertNull(DescriptorHelper.getDisplayName("foo", "foo"));
    }

    /**
     * Tests the {@link DescriptorHelper#getNodeShortNames(String[], String)}
     * method.
     */
    @Test
    public void testGetNodeShortNames() {
        String[] shortNames = {"entityRelationship.animal*"};
        String[] nodeShortNames = DescriptorHelper.getNodeShortNames(
                shortNames, "target");
        assertEquals(1, nodeShortNames.length);
        assertEquals("party.animalpet", nodeShortNames[0]);

        nodeShortNames = DescriptorHelper.getNodeShortNames(shortNames,
                                                            "source");
        assertEquals(2, nodeShortNames.length);
        List<String> list = Arrays.asList(nodeShortNames);
        assertTrue(list.contains("party.person"));
        assertTrue(list.contains("organization.organization"));
    }

    /**
     * Tests the {@link DescriptorHelper#getCommonNodeNames(String[], IArchetypeService)} method.
     */
    @Test
    public void testGetCommonNodeNames() {
        String[] shortNames = {"party.customerperson", "party.patientpet"};
        String[] names = DescriptorHelper.getCommonNodeNames(shortNames, getArchetypeService());
        assertEquals(4, names.length);
        assertArrayEquals(new String[]{"id", "name", "description", "identities"}, names);
    }

    /**
     * Tests the {@link DescriptorHelper#getCommonNodeNames(String[], String[], IArchetypeService)} method.
     */
    @Test
    public void testGetCommonNodeNamesForNodes() {
        String[] shortNames = {"party.customerperson", "party.patientpet"};
        String[] nodes = {"id", "name", "species"};
        String[] names = DescriptorHelper.getCommonNodeNames(shortNames, nodes, getArchetypeService());
        assertEquals(2, names.length);
        assertArrayEquals(new String[]{"id", "name",}, names);
    }

    /**
     * Verifies that two lists of short names match.
     *
     * @param actualShortNames   the actual short names
     * @param expectedShortNames the expected short names
     */
    private void checkEquals(String[] actualShortNames, String... expectedShortNames) {
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
     * Verifies that two lists of short names match.
     *
     * @param actualShortNames   the actual short names
     * @param preserveOrder      if <tt>true</tt> the actual and expected shortnames must be in the same order
     * @param expectedShortNames the expected short names
     */
    private void checkEquals(String[] actualShortNames, boolean preserveOrder,
                             String... expectedShortNames) {
        if (preserveOrder) {
            assertEquals(expectedShortNames.length, actualShortNames.length);
            for (int i = 0; i < actualShortNames.length; ++i) {
                assertEquals(expectedShortNames[i], actualShortNames[i]);
            }
        } else {
            checkEquals(actualShortNames, expectedShortNames);
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
