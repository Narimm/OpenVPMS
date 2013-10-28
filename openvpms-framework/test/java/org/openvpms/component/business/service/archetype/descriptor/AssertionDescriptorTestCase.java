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
 */


package org.openvpms.component.business.service.archetype.descriptor;

import org.junit.Test;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptorReader;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptorWriter;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test assertion descriptors for archetypes.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
@ContextConfiguration("descriptor-test-appcontext.xml")
public class AssertionDescriptorTestCase extends AbstractJUnit4SpringContextTests {


    /**
     * Holds a reference to the entity service
     */
    @Autowired
    private ArchetypeService service;


    /**
     * Test that the assertion descriptors are returned in the order they were
     * entered.
     *
     * @throws Exception for any error
     */
    @Test
    public void testAssertionDescriptorOrdering() throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("party.personbernief");
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertNotNull(ndesc);
        assertTrue(ndesc.getAssertionDescriptors().size() == 5);
        int currIndex = 0;
        String assertName = "dummyAssertion";
        for (AssertionDescriptor desc : ndesc.getAssertionDescriptorsInIndexOrder()) {
            String name = desc.getName();
            if (name.startsWith(assertName)) {
                int index = Integer.parseInt(
                        name.substring(assertName.length()));
                if (index > currIndex) {
                    currIndex = index;
                } else {
                    fail("Assertions are not returned in the correct order currIndex: "
                         + currIndex + " index: " + index);
                }
            }
        }

        // clone and test it again
        NodeDescriptor clone = (NodeDescriptor) ndesc.clone();
        assertNotNull(clone);
        assertTrue(clone.getAssertionDescriptors().size() == 5);
        currIndex = 0;
        for (AssertionDescriptor desc : clone.getAssertionDescriptorsInIndexOrder()) {
            String name = desc.getName();
            if (name.startsWith(assertName)) {
                int index = Integer.parseInt(
                        name.substring(assertName.length()));
                if (index > currIndex) {
                    currIndex = index;
                } else {
                    fail("Assertions are not returned in the correct order currIndex: "
                         + currIndex + " index: " + index);
                }
            }
        }
    }

    /**
     * Test that the properties within an assertion are returned in the
     * order that they are defined.
     */
    @Test
    public void testAssertionPropertyOrder() {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("party.personbernief");
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("identities");
        assertNotNull(ndesc);

        int index = 0;
        for (String shortName : ndesc.getArchetypeRange()) {
            switch (index++) {
                case 0:
                    assertTrue(shortName.equals("entityIdentity.animalAlias"));
                    break;
                case 1:
                    assertTrue(shortName.equals("entityIdentity.personAlias"));
                    break;
                case 2:
                    assertTrue(
                            shortName.equals("entityIdentity.customerAlias1"));
                    break;
                case 3:
                    assertTrue(
                            shortName.equals("entityIdentity.customerAlias"));
                    break;
                default:
                    fail("The short name " + shortName + " should not be defined");
                    break;
            }
        }
    }

    /**
     * Deleting nodes with archetypes assertions cause a validation error.
     */
    @Test
    public void testOBF10() {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("party.person");
        assertNotNull(adesc);

        // find and remove the classifications node
        assertTrue(adesc.getNodeDescriptor("classifications") != null);
        adesc.removeNodeDescriptor("classifications");
        assertTrue(adesc.getNodeDescriptor("classifications") == null);

        // remove the title node
        assertTrue(adesc.getNodeDescriptor("title") != null);
        adesc.removeNodeDescriptor("title");
        assertTrue(adesc.getNodeDescriptor("title") == null);

        // now validate the archetype
        service.validateObject(adesc);
    }

    /**
     * Tests serialisation of {@link AssertionDescriptor}s.
     * <p/>
     * This serialises:
     * <pre>{@code
     * <assertion name="lookup.local">
     *   <errorMessage>error message</errorMessage>
     *   <propertyList name="entries">
     *     <property name="IN_PROGRESS" type="java.lang.String" value="In Progress"/>
     *     <property name="COMPLETED" type="java.lang.String" value="Completed"/>
     *   </propertyList>
     * </assertion>
     * }</pre>
     */
    @Test
    public void testSerialisation() {
        AssertionDescriptor descriptor = new AssertionDescriptor();
        descriptor.setErrorMessage("error message");
        descriptor.setName("lookup.local");
        PropertyList entries = createPropertyList("entries", createProperty("IN_PROGRESS", "In Progress"),
                                                  createProperty("COMPLETED", "Completed"));
        descriptor.addProperty(entries);

        checkSerialisation(descriptor);
    }

    /**
     * Verifies that {@link AssertionDescriptor}s with nested collection properties can be serialized.
     * <p/>
     * This serialises:
     * <pre>{@code
     * <assertion name="archetypeRange">
     *   <errorMessage>error message</errorMessage>
     *   <propertyList name="archetypes">
     *     <propertyMap name="archetype">
     *       <property name="shortName" type="java.lang.String" value="entityIdentity.barcode"/>
     *     </propertyMap>
     *     <propertyMap name="archetype">
     *       <property name="shortName" type="java.lang.String" value="entityIdentity.code"/>
     *     </propertyMap>
     *   </propertyList>
     * </assertion>
     * }
     * </pre>
     */
    @Test
    public void testNestedPropertySerialisation() {
        AssertionDescriptor descriptor = new AssertionDescriptor();
        descriptor.setErrorMessage("error message");
        descriptor.setName("archetypeRange");
        PropertyList archetypes = createPropertyList(
                "archetypes",
                createPropertyMap("archetype", createProperty("shortName", "entityIdentity.barcode")),
                createPropertyMap("archetype", createProperty("shortName", "entityIdentity.code")));
        descriptor.addProperty(archetypes);

        checkSerialisation(descriptor);
    }

    /**
     * Verifies an {@link AssertionDescriptor} can be written and read, and that the written version is the same
     * as the read.
     *
     * @param descriptor the descriptor to write
     */
    private void checkSerialisation(AssertionDescriptor descriptor) {
        ArchetypeDescriptorWriter writer = new ArchetypeDescriptorWriter(false, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writer.write(descriptor, stream);
        System.out.println(stream);
        ArchetypeDescriptorReader reader = new ArchetypeDescriptorReader();
        AssertionDescriptor read = reader.read(new ByteArrayInputStream(stream.toByteArray()), AssertionDescriptor.class);
        checkEquals(descriptor, read);
    }

    /**
     * Verifies a {@link AssertionDescriptor} matches that expected.
     *
     * @param expected the expected descriptor
     * @param actual   the actual descriptor
     */
    private void checkEquals(AssertionDescriptor expected, AssertionDescriptor actual) {
        assertFalse(expected == actual); // expect a different instance
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
        NamedProperty[] expectedProperties = expected.getPropertiesAsArray();
        NamedProperty[] actualProperties = actual.getPropertiesAsArray();
        checkEquals(expectedProperties, actualProperties);
    }

    /**
     * Verifies a {@link NamedProperty} matches that expected.
     *
     * @param expected the expected property
     * @param actual   the actual property
     */
    private void checkEquals(NamedProperty expected, NamedProperty actual) {
        assertFalse(expected == actual); // expect a different instance
        assertEquals(expected.getClass(), actual.getClass());
        assertEquals(expected.getName(), actual.getName());
        if (expected instanceof PropertyList) {
            checkEquals((PropertyList) expected, (PropertyList) actual);
        } else if (expected instanceof PropertyMap) {
            checkEquals((PropertyMap) expected, (PropertyMap) actual);
        } else if (expected instanceof AssertionProperty) {
            checkEquals((AssertionProperty) expected, (AssertionProperty) actual);
        }
    }

    /**
     * Verifies a {@link PropertyList} matches that expected.
     *
     * @param expected the expected list
     * @param actual   the actual list
     */
    private void checkEquals(PropertyList expected, PropertyList actual) {
        NamedProperty[] expectedProperties = expected.getPropertiesAsArray();
        NamedProperty[] actualProperties = actual.getPropertiesAsArray();
        checkEquals(expectedProperties, actualProperties);
    }

    /**
     * Verifies a {@link PropertyMap} matches that expected.
     *
     * @param expected the expected map
     * @param actual   the actual map
     */
    private void checkEquals(PropertyMap expected, PropertyMap actual) {
        NamedProperty[] expectedProperties = expected.getPropertiesAsArray();
        NamedProperty[] actualProperties = actual.getPropertiesAsArray();
        checkEquals(expectedProperties, actualProperties);
    }

    /**
     * Verifies a list of {@link NamedProperty} instances match those expected.
     *
     * @param expected the expected properties
     * @param actual   the actual properties
     */
    private void checkEquals(NamedProperty[] expected, NamedProperty[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; ++i) {
            checkEquals(expected[i], actual[i]);
        }
    }

    /**
     * Verifies an {@link AssertionProperty} matches that expected.
     *
     * @param expected the expected property
     * @param actual   the actual property
     */
    private void checkEquals(AssertionProperty expected, AssertionProperty actual) {
        assertEquals(expected.getValue(), actual.getValue());
        assertEquals(expected.getType(), actual.getType());
    }

    /**
     * Helper to create a new {@link PropertyList}.
     *
     * @param name       the list name
     * @param properties the list properties
     * @return the new list
     */
    private PropertyList createPropertyList(String name, NamedProperty... properties) {
        PropertyList result = new PropertyList();
        result.setName(name);
        for (NamedProperty property : properties) {
            result.addProperty(property);
        }
        return result;
    }

    /**
     * Helper to create a new {@link PropertyMap}.
     *
     * @param name       the map name
     * @param properties the properties
     * @return the new map
     */
    private PropertyMap createPropertyMap(String name, NamedProperty... properties) {
        PropertyMap result = new PropertyMap(name);
        result.setName(name);
        for (NamedProperty property : properties) {
            result.addProperty(property);
        }
        return result;
    }

    /**
     * Helper to create a new {@link AssertionProperty}.
     *
     * @param name  the property name
     * @param value the property value
     * @return the new property
     */
    private AssertionProperty createProperty(String name, String value) {
        AssertionProperty result = new AssertionProperty();
        result.setName(name);
        result.setValue(value);
        return result;
    }


}
