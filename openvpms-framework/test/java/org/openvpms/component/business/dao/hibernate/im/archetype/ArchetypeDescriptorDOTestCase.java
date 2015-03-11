/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.archetype;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.hibernate.im.HibernateInfoModelTestCase;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyMap;
import org.openvpms.component.business.domain.im.party.Contact;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link ArchetypeDescriptorDOImpl} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeDescriptorDOTestCase
        extends HibernateInfoModelTestCase {

    /**
     * The initial no. of archetype descriptors in the database.
     */
    private int archetypes;

    /**
     * The initial no. of node descriptors in the database.
     */
    private int nodes;

    /**
     * The initial no. of assertion descriptors in the database.
     */
    private int assertions;


    /**
     * Tests the creation of an archetype descriptor.
     */
    @Test
    public void testCreate() {
        ArchetypeDescriptorDO desc = createArchetypeDescriptor("animal.mypet");
        int size = desc.getNodeDescriptors().size();
        assertFalse(size == 0);

        String name = desc.getName();

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(desc);
        tx.commit();

        desc = reload(desc);
        assertNotNull(desc);
        assertEquals(name, desc.getName());

        // check row counts
        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + size, count(NodeDescriptorDOImpl.class));
    }

    /**
     * Test the deletion of an archetype descriptor.
     */
    @Test
    public void testDelete() {
        ArchetypeDescriptorDO desc = createArchetypeDescriptor("animal.mypet");
        int size = desc.getNodeDescriptors().size();
        assertFalse(size == 0);

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(desc);
        tx.commit();

        // check row counts
        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + size, count(NodeDescriptorDOImpl.class));

        // now delete the object and check again
        tx = session.beginTransaction();
        session.delete(desc);
        tx.commit();

        // check row count
        assertEquals(archetypes, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes, count(NodeDescriptorDOImpl.class));
    }

    /**
     * Test nested node descriptors.
     */
    @Test
    public void testNestedNodeDescriptors() {
        ArchetypeDescriptorDO desc = createArchetypeDescriptor(
                "contact.location");
        int size = desc.getNodeDescriptors().size();

        desc.setClassName(Contact.class.getName());
        NodeDescriptorDO details = createNodeDescriptor("details", "/details",
                                                        Map.class);
        desc.addNodeDescriptor(details);
        details.addNodeDescriptor(createNodeDescriptor("address",
                                                       "/details/address",
                                                       String.class));
        details.addNodeDescriptor(createNodeDescriptor("postCode",
                                                       "/details/postCode",
                                                       String.class));
        size += 3;

        Session session = getSession();
        Transaction tx = session.beginTransaction();

        session.save(desc);
        tx.commit();

        // check row counts
        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + size, count(NodeDescriptorDOImpl.class));

        // retrieve the object and ensure that the correct number of
        // node descriptors are present
        ArchetypeDescriptorDO adesc = (ArchetypeDescriptorDO) session.load(
                ArchetypeDescriptorDOImpl.class, desc.getId());
        assertEquals(size, adesc.getAllNodeDescriptors().size());
        assertNotNull(adesc.getNodeDescriptor("id"));
        assertNotNull(adesc.getNodeDescriptor("name"));

        details = adesc.getNodeDescriptor("details");
        assertNotNull(details);
        assertEquals(2, details.getNodeDescriptors().size());
        assertNotNull(adesc.getNodeDescriptor("address"));
        assertNotNull(adesc.getNodeDescriptor("postCode"));

        // delete the archetype descriptor and check results
        tx = session.beginTransaction();
        session.delete(adesc);
        tx.commit();
        // check row counts
        assertEquals(archetypes, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes, count(NodeDescriptorDOImpl.class));
    }

    /**
     * Tests adding assertion descriptors.
     */
    @Test
    public void testAddAssertionDescriptors() {
        ArchetypeDescriptorDO desc = createArchetypeDescriptor("animal.breed");
        int size = desc.getNodeDescriptors().size();

        // set up the descriptor
        NodeDescriptorDO name = desc.getNodeDescriptor("name");
        assertNotNull(name);
        AssertionDescriptorDO regexp
                = createAssertionDescriptor("regularExpression");
        regexp.addProperty(createAssertionProperty("expression", String.class,
                                                   ".*"));
        name.addAssertionDescriptor(regexp);

        AssertionDescriptorDO maxLength
                = createAssertionDescriptor("maxLength");
        maxLength.addProperty(createAssertionProperty("length", Integer.class,
                                                      "20"));
        name.addAssertionDescriptor(maxLength);

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(desc);
        tx.commit();

        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + size, count(NodeDescriptorDOImpl.class));
        assertEquals(assertions + 2, count(AssertionDescriptorDOImpl.class));
    }

    /**
     * Tests removal of node descriptors from an archetype descriptor.
     */
    @Test
    public void testDeleteNodeDescriptor() {
        Session session = getSession();

        ArchetypeDescriptorDO desc = createArchetypeDescriptor("animal.mypet");
        int size = desc.getNodeDescriptors().size();
        Transaction tx = session.beginTransaction();
        session.save(desc);
        tx.commit();

        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + size, count(NodeDescriptorDOImpl.class));

        // retrieve the object and ensure that the correct number of
        // node descriptors are present
        tx = session.beginTransaction();
        desc = (ArchetypeDescriptorDO) session.load(
                ArchetypeDescriptorDOImpl.class,
                desc.getId());

        NodeDescriptorDO description = desc.getNodeDescriptor("description");
        assertNotNull(description);
        desc.removeNodeDescriptor(description);
        session.save(desc);
        tx.commit();

        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + size - 1, count(NodeDescriptorDOImpl.class));
        desc = (ArchetypeDescriptorDO) session.load(
                ArchetypeDescriptorDOImpl.class,
                desc.getId());
        assertNull(desc.getNodeDescriptor("description"));
    }

    /**
     * Test archetype with the updsting of assertion descriptors.
     */
    @Test
    public void testUpdateAssertionDescriptors() {
        ArchetypeDescriptorDO desc = createArchetypeDescriptor("animal.pet");
        int size = desc.getNodeDescriptors().size();

        NodeDescriptorDO name = desc.getNodeDescriptor("name");
        name.addAssertionDescriptor(
                createAssertionDescriptor("regularExpression"));

        AssertionDescriptorDO regexp
                = createAssertionDescriptor("regularExpression");
        regexp.addProperty(createAssertionProperty("expression", String.class,
                                                   ".*"));
        NodeDescriptorDO description = desc.getNodeDescriptor("description");
        description.addAssertionDescriptor(regexp);

        AssertionDescriptorDO maxLength
                = createAssertionDescriptor("maxLength");
        maxLength.addProperty(createAssertionProperty("length", Integer.class,
                                                      "20"));
        maxLength.addProperty(createAssertionProperty("length2",
                                                      Integer.class,
                                                      "20"));
        description.addAssertionDescriptor(maxLength);

        NodeDescriptorDO breed = createNodeDescriptor("breed", "/breed",
                                                      String.class);
        desc.addNodeDescriptor(breed);
        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(desc);
        tx.commit();

        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + size + 1, count(NodeDescriptorDOImpl.class));
        assertEquals(assertions + 3, count(AssertionDescriptorDOImpl.class));

        // retrieve the object and delete an assertion and a property
        tx = session.beginTransaction();
        desc = (ArchetypeDescriptorDO) session.load(
                ArchetypeDescriptorDOImpl.class, desc.getId());
        assertNotNull(desc);
        desc.removeNodeDescriptor(breed);
        description = desc.getNodeDescriptor("description");
        description.removeAssertionDescriptor("regularExpression");
        maxLength = description.getAssertionDescriptor("maxLength");
        maxLength.removeProperty("length2");
        session.save(desc);
        tx.commit();

        assertEquals(archetypes + 1, count(ArchetypeDescriptorDOImpl.class));
        assertEquals(nodes + 3, count(NodeDescriptorDOImpl.class));
        assertEquals(assertions + 2, count(AssertionDescriptorDOImpl.class));

        desc = (ArchetypeDescriptorDO) session.load(
                ArchetypeDescriptorDOImpl.class, desc.getId());
        assertEquals(size, desc.getNodeDescriptors().size());
        description = desc.getNodeDescriptor("description");
        assertNotNull(description);
        assertEquals(1, description.getAssertionDescriptors().size());
    }

    /**
     * Verifies that an {@link AssertionDescriptorDOImpl}'s ProperyMap is correctly
     * loaded when its other attributes are null.
     */
    @Test
    public void testOBF112() {
        AssertionDescriptorDO assertion = new AssertionDescriptorDOImpl();
        assertion.setName("assertionOBF112");
        assertion.addProperty(createAssertionProperty("expression",
                                                      String.class,
                                                      ".*"));
        assertNotNull(assertion.getPropertyMap());

        Session session = getSession();
        Transaction tx = session.beginTransaction();
        session.save(assertion);
        tx.commit();
        session.evict(assertion); // evict the assertion to force load from db

        // reload and verify the property map was loaded
        AssertionDescriptorDO loaded = (AssertionDescriptorDO) session.load(
                AssertionDescriptorDOImpl.class, assertion.getId());
        PropertyMap map = loaded.getPropertyMap();
        assertNotNull(map);
        assertNotNull(loaded.getProperty("expression"));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        archetypes = count(ArchetypeDescriptorDOImpl.class);
        nodes = count(NodeDescriptorDOImpl.class);
        assertions = count(AssertionDescriptorDOImpl.class);
    }

    /**
     * Creates a new archetype descriptor with id, name and description
     * node descriptors.
     *
     * @param shortName the archetype short name
     * @return a new archetype descriptor
     */
    private ArchetypeDescriptorDO createArchetypeDescriptor(String shortName) {
        ArchetypeDescriptorDO desc = new ArchetypeDescriptorDOImpl();
        desc.setName(shortName + System.currentTimeMillis() + ".1.0");
        desc.setLatest(true);
        desc.setClassName(this.getClass().getName());

        desc.addNodeDescriptor(
                createNodeDescriptor("id", "/id", Long.class));
        desc.addNodeDescriptor(
                createNodeDescriptor("name", "/name", String.class));
        desc.addNodeDescriptor(
                createNodeDescriptor("description", "/description",
                                     String.class));
        return desc;
    }

    /**
     * Creates a new node descriptor.
     *
     * @param name the node name
     * @param path the node path
     * @param type the node type
     * @return a new node descriptor.
     */
    private NodeDescriptorDO createNodeDescriptor(String name, String path,
                                                  Class type) {
        NodeDescriptorDO desc = new NodeDescriptorDOImpl();
        desc.setName(name);
        desc.setPath(path);
        desc.setType(type.getName());
        desc.setMinCardinality(1);
        desc.setMaxCardinality(1);
        return desc;
    }

    /**
     * Create an {@link AssertionDescriptor} with the specified parameters.
     *
     * @param type the assertion descriptor type
     * @return AssertionDescriptor
     */
    private AssertionDescriptorDO createAssertionDescriptor(String type) {
        AssertionDescriptorDO desc = new AssertionDescriptorDOImpl();
        desc.setName(type);
        desc.setErrorMessage("An error message");

        return desc;
    }

    /**
     * Create a {@link AssertionProperty} with the specified parameters.
     *
     * @param name  the property name
     * @param type  the property type
     * @param value the property value
     * @return AssertionProperty
     */
    private AssertionProperty createAssertionProperty(String name, Class type, String value) {
        AssertionProperty prop = new AssertionProperty();
        prop.setName(name);
        prop.setType(type.getName());
        prop.setValue(value);

        return prop;
    }
}
