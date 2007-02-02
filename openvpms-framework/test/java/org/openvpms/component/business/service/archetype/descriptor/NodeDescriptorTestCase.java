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


package org.openvpms.component.business.service.archetype.descriptor;

import junit.log4j.LoggedTestCase;
import org.apache.commons.lang.StringUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.xml.sax.InputSource;

import java.io.InputStreamReader;
import java.util.List;


/**
 * Tests the {@link NodeDescriptor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class NodeDescriptorTestCase extends LoggedTestCase {

    /**
     * The archetype descriptors.
     */
    private ArchetypeDescriptors _archetypes;

    /**
     * The mapping path.
     */
    private static final String MAPPING
            = "org/openvpms/component/business/domain/im/archetype/descriptor/archetype-mapping-file.xml";

    /**
     * The archetype descriptors path.
     */
    private static final String ARCHETYPES
            = "org/openvpms/component/business/service/archetype/descriptor/archetypes.xml";


    /**
     * Tests the {@link NodeDescriptor#getDisplayName()} method.
     */
    public void testDisplayName() {
        ArchetypeDescriptor archetype = getArchetype("person.person");

        // node with display name specified
        NodeDescriptor uid = archetype.getNodeDescriptor("uid");
        assertNotNull(uid);
        assertEquals("id", uid.getDisplayName());

        // node with no display name specified
        NodeDescriptor firstName = archetype.getNodeDescriptor("firstName");
        assertNotNull(firstName);
        assertEquals("First Name", firstName.getDisplayName());

        // iterate through the top level nodes and enusre that the
        // display name is not null
        for (NodeDescriptor node : archetype.getNodeDescriptorsAsArray()) {
            assertFalse(StringUtils.isEmpty(node.getDisplayName()));
        }
    }

    /**
     * Tests the {@link NodeDescriptor#getMaxLength()} method.
     */
    public void testDefaultMaxLength() {
        ArchetypeDescriptor archetype = getArchetype("person.person");

        // no max length specified, should be default
        NodeDescriptor title = archetype.getNodeDescriptor("title");
        assertNotNull(title);
        assertEquals(NodeDescriptor.DEFAULT_MAX_LENGTH, title.getMaxLength());

        // max length specified
        NodeDescriptor firstName = archetype.getNodeDescriptor("firstName");
        assertNotNull(firstName);
        assertEquals(30, firstName.getMaxLength());
    }

    /**
     * Tests the {@link NodeDescriptor#isLookup()} method.
     */
    public void testIsLookup() {
        ArchetypeDescriptor archetype = getArchetype("person.person");

        // lookup node
        NodeDescriptor title = archetype.getNodeDescriptor("title");
        assertNotNull(title);
        assertTrue(title.isLookup());

        // non-lookup node
        NodeDescriptor lastName = archetype.getNodeDescriptor("lastName");
        assertNotNull(lastName);
        assertFalse(lastName.isLookup());
    }

    /**
     * Tests the {@link NodeDescriptor#isLookup()} method.
     */
    public void testIsHidden() {
        ArchetypeDescriptor archetype = getArchetype("person.person");

        // hidden node
        NodeDescriptor details = archetype.getNodeDescriptor("details");
        assertNotNull(details);
        assertTrue(details.isHidden());

        // non-hidden node
        NodeDescriptor firstName = archetype.getNodeDescriptor("firstName");
        assertNotNull(firstName);
        assertFalse(firstName.isHidden());
    }

    /**
     * Tests the {@link NodeDescriptor#getArchetypeRange()} method.
     */
    public void testArchetypeRange() {
        ArchetypeDescriptor archetype = getArchetype("person.person");
        NodeDescriptor node = archetype.getNodeDescriptor("classifications");
        assertTrue(node != null);
        String[] range = node.getArchetypeRange();
        assertNotNull(range);
        assertTrue(range.length == 2);
        assertEquals("classification.personType", range[0]);
        assertEquals("classification.staff", range[1]);

    }

    /**
     * Tests the {@link NodeDescriptor#getMinCardinality()} and
     * {@link NodeDescriptor#getMaxCardinality()} methods.
     */
    public void testCardinality() {
        ArchetypeDescriptor archetype = getArchetype("person.person");

        // simple node with unspecified cardinality
        NodeDescriptor initials = archetype.getNodeDescriptor("initials");
        assertNotNull(initials);
        assertEquals(0, initials.getMinCardinality());
        assertEquals(1, initials.getMaxCardinality());

        // simple node with specified cardinality
        NodeDescriptor firstName = archetype.getNodeDescriptor("firstName");
        assertNotNull(firstName);
        assertEquals(1, firstName.getMinCardinality());
        assertEquals(1, firstName.getMaxCardinality());

        // collection node with unspecified cardinality
        NodeDescriptor classifications = archetype.getNodeDescriptor(
                "classifications");
        assertNotNull(classifications);
        assertEquals(0, classifications.getMinCardinality());
        assertEquals(1, classifications.getMaxCardinality());

        // collection node with specified cardinality
        NodeDescriptor contacts = archetype.getNodeDescriptor("contacts");
        assertNotNull(contacts);
        assertEquals(1, contacts.getMinCardinality());
        assertEquals(NodeDescriptor.UNBOUNDED, contacts.getMaxCardinality());
    }

    /**
     * Tests the {@link NodeDescriptor#isParentChild()} method.
     */
    public void testParentChildAttribute() {
        ArchetypeDescriptor archetype = getArchetype("person.person");

        // parent-child node
        NodeDescriptor contacts = archetype.getNodeDescriptor("contacts");
        assertTrue(contacts != null);
        assertTrue(contacts.isParentChild());

        // non parent-child node
        NodeDescriptor identities = archetype.getNodeDescriptor("identities");
        assertTrue(identities != null);
        assertFalse(identities.isParentChild());
    }

    /**
     * Tests the {@link NodeDescriptor#addChildToCollection} and
     * {@link NodeDescriptor#removeChildFromCollection} method, when
     * a baseName is specified on the descriptor.
     */
    public void testCollectionAddRemove() {
        ArchetypeDescriptor archetype = getArchetype("person.person");
        Party party = new Party();
        NodeDescriptor node = archetype.getNodeDescriptor("contacts");
        assertNotNull(node);
        assertNotNull(node.getBaseName());

        // add a contact
        Contact contact = new Contact();
        node.addChildToCollection(party, contact);

        // verify it was added
        List<IMObject> values = node.getChildren(party);
        assertNotNull(values);
        assertEquals(1, values.size());
        IMObject value = values.get(0);
        assertEquals(contact, value);

        // remove the contact
        node.removeChildFromCollection(party, contact);

        // verify it was removed
        values = node.getChildren(party);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    /**
     * Tests the {@link NodeDescriptor#addChildToCollection} and
     * {@link NodeDescriptor#removeChildFromCollection} method, when
     * no baseName is specified on the descriptor.
     */
    public void testCollectionAddRemoveNoBaseName() {
        ArchetypeDescriptor archetype = getArchetype("person.person");
        Party party = new Party();
        NodeDescriptor node = archetype.getNodeDescriptor("classifications");
        assertNotNull(node);
        assertNull(node.getBaseName());

        // add a classification lookup
        Lookup classification = new Lookup();
        node.addChildToCollection(party, classification);

        // verify it was added
        List<IMObject> values = node.getChildren(party);
        assertNotNull(values);
        assertEquals(1, values.size());
        IMObject value = values.get(0);
        assertEquals(classification, value);

        // remove the classification
        node.removeChildFromCollection(party, classification);

        // verify it was removed
        values = node.getChildren(party);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    /**
     * Tests the {@link NodeDescriptor#removeChildFromCollection} method, when
     * no baseName is specified on the descriptor.
     */
    public void testRemoveChildFromCollectionNoBaseName() {
        ArchetypeDescriptor archetype = getArchetype("person.person");
        Party party = new Party();

        Lookup classification = new Lookup();
        NodeDescriptor node = archetype.getNodeDescriptor("classifications");
        assertNotNull(node);
        assertNull(node.getBaseName());
        node.addChildToCollection(party, classification);

        // verify it was added
        List<IMObject> values = node.getChildren(party);
        assertNotNull(values);
        assertEquals(1, values.size());
        IMObject value = values.get(0);
        assertEquals(classification, value);
    }


    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        _archetypes = getArchetypeDescriptors();
    }

    /**
     * Returns the named archetype descriptor.
     *
     * @param shortName the archetype short name
     * @return the archetype descriptor corresponding to <code>shortName</code>
     */
    private ArchetypeDescriptor getArchetype(String shortName) {
        ArchetypeDescriptor archetype
                = _archetypes.getArchetypeDescriptors().get(shortName);
        assertNotNull(archetype);
        return archetype;
    }

    /**
     * Loads the archetype descriptors.
     *
     * @return the archetype descriptors
     * @throws Exception if the descriptors can't be loaded
     */
    private ArchetypeDescriptors getArchetypeDescriptors()
            throws Exception {
        Mapping mapping = new Mapping();
        mapping.loadMapping(new InputSource(new InputStreamReader(
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(MAPPING))));

        // set up the unmarshaller
        Unmarshaller unmarshaller = new Unmarshaller(mapping);
        return (ArchetypeDescriptors) unmarshaller.unmarshal(
                new InputSource(new InputStreamReader(
                        Thread.currentThread().getContextClassLoader()
                                .getResourceAsStream(ARCHETYPES))));
    }
}
