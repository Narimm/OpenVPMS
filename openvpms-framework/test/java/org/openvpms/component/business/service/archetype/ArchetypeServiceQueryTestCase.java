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

// spring-context

import org.openvpms.component.business.domain.im.common.Classification;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.query.NodeSet;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeProperty;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeShortNameConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.CollectionNodeConstraint.JoinType;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Test that ability to create and query on acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceQueryTestCase extends
                                           AbstractDependencyInjectionSpringContextTests {
    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService service;


    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceQueryTestCase.class);
    }

    /**
     * Default constructor
     */
    public ArchetypeServiceQueryTestCase() {
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

        this.service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Test the query by code in the lookup entity. This will support
     * OVPMS-35
     */
    public void testOVPMS35()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Belarus"));

        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }

    /**
     * Test query by code with wildcard
     */
    public void testGetByCodeWithWildcard()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Bel*"));

        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }

    /**
     * Test query by code with wild in short name
     */
    public void testGetCodeWithWildCardShortName()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Bel*"));

        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }

    /**
     * Test query by code with wild in short name and an order clause
     */
    public void testGetCodeWithWildCardShortNameAndOrdered()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false, true)
                .add(new NodeConstraint("name", RelationalOp.EQ, "Bel*"))
                .add(new NodeSortConstraint("name", true));

        int acount = service.get(query).getRows().size();
        Lookup lookup = (Lookup) service.create("lookup.country");
        lookup.setCode("Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getRows().size();
        assertTrue(acount1 == acount + 1);
    }

    /**
     * Test OVPMS245
     */
    public void testOVPMS245()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery(
                new ArchetypeShortNameConstraint("product.product", false,
                                                 true))
                .add(new CollectionNodeConstraint("classifications", true)
                        .setJoinType(JoinType.LeftOuterJoin)
                        .add(new OrConstraint()
                        .add(new ArchetypeNodeConstraint(
                                ArchetypeProperty.ConceptName,
                                RelationalOp.IsNULL))
                        .add(new AndConstraint()
                        .add(new ArchetypeNodeConstraint(
                                ArchetypeProperty.ConceptName, RelationalOp.EQ,
                                "species"))
                        .add(new NodeConstraint("name", RelationalOp.EQ,
                                                "Canine"))
                        .add(new NodeSortConstraint("name", true)))));

        IPage<IMObject> page = service.get(query);
        assertTrue(page != null);
    }

    /**
     * Tests the NodeSet get method. This verifies that subcollections are
     * loaded correctly, avoiding LazyInitializationException.
     */
    public void testGetNodeSet() {
        // set up a party with a single contact and contact purpose
        Contact contact = (Contact) service.create("contact.phoneNumber");
        contact.getDetails().setAttribute("areaCode", "03");
        contact.getDetails().setAttribute("telephoneNumber", "0123456789");
        Classification purpose = (Classification) service.create(
                "classification.contactPurpose");
        purpose.setName("Home");
        service.save(purpose);

        contact.addClassification(purpose);

        Party person = (Party) service.create("person.person");
        person.getDetails().setAttribute("lastName", "Anderson");
        person.getDetails().setAttribute("firstName", "Tim");
        person.getDetails().setAttribute("title", "MR");
        person.addContact(contact);
        service.save(person);

        // query the firstName, lastName and contacts nodes of the person
        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());
        List<String> names = Arrays.asList("firstName", "lastName", "contacts");
        IPage<NodeSet> page = service.getNodes(query, names);
        assertNotNull(page);

        // verify that the page only has a single element, and that the node
        // set has the expected nodes
        assertEquals(1, page.getRows().size());
        NodeSet nodes = page.getRows().get(0);
        assertEquals(3, nodes.getNames().size());
        assertTrue(nodes.getNames().contains("firstName"));
        assertTrue(nodes.getNames().contains("lastName"));
        assertTrue(nodes.getNames().contains("contacts"));

        // verify the values of the simple nodes
        assertEquals(person.getObjectReference(), nodes.getObjectReference());
        assertEquals("Tim", nodes.get("firstName"));
        assertEquals("Anderson", nodes.get("lastName"));

        // verify the values of the contact node. If the classification hasn't
        // been loaded, a LazyInitializationException will be raised by
        // hibernate
        Collection<Contact> contacts
                = (Collection<Contact>) nodes.get("contacts");
        assertEquals(1, contacts.size());
        contact = contacts.toArray(new Contact[0])[0];
        assertEquals("03", contact.getDetails().getAttribute("areaCode"));
        assertEquals("0123456789",
                     contact.getDetails().getAttribute("telephoneNumber"));
        assertEquals(1, contact.getClassificationsAsArray().length);
        purpose = contact.getClassificationsAsArray()[0];
        assertEquals("Home", purpose.getName());
    }

    /**
     * Tests the partial get method. This verifies that specified subcollections
     * are loaded correctly, avoiding LazyInitializationException.
     */
    public void testGetPartialObject() {
        // set up a party with a single contact and contact purpose
        Contact contact = (Contact) service.create("contact.phoneNumber");
        contact.getDetails().setAttribute("areaCode", "03");
        contact.getDetails().setAttribute("telephoneNumber", "0123456789");
        Classification purpose = (Classification) service.create(
                "classification.contactPurpose");
        purpose.setName("Home");
        service.save(purpose);

        contact.addClassification(purpose);

        Party person = (Party) service.create("person.person");
        person.getDetails().setAttribute("lastName", "Anderson");
        person.getDetails().setAttribute("firstName", "Tim");
        person.getDetails().setAttribute("title", "MR");
        person.addContact(contact);
        service.save(person);

        // query the firstName, lastName and contacts nodes of the person
        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());
        List<String> names = Arrays.asList("details", "contacts");
        IPage<IMObject> page = service.get(query, names);
        assertNotNull(page);

        // verify that the page only has a single element, and that the
        // contacts node has been loaded.
        assertEquals(1, page.getRows().size());
        Party person2 = (Party) page.getRows().get(0);
        Set<Contact> contacts = person2.getContacts();
        assertEquals(1, contacts.size());

        // verify the values of the simple nodes. Note that although details
        // is a collection, it is treated as a simple node by hibernate as it
        // maps to a single column. We specify it to load anyway
        assertEquals(person.getObjectReference(), person2.getObjectReference());
        assertEquals(3, person2.getDetails().getAttributes().size());
        assertEquals("Tim", person.getDetails().getAttribute("firstName"));
        assertEquals("Anderson", person.getDetails().getAttribute("lastName"));
        assertEquals("MR", person2.getDetails().getAttribute("title"));

        // verify the values of the contact node. If the classification hasn't
        // been loaded, a LazyInitializationException will be raised by
        // hibernate
        Contact contact2 = contacts.toArray(new Contact[0])[0];
        assertEquals("03", contact2.getDetails().getAttribute("areaCode"));
        assertEquals("0123456789",
                     contact2.getDetails().getAttribute("telephoneNumber"));
        assertEquals(1, contact2.getClassificationsAsArray().length);
        Classification purpose2 = contact2.getClassificationsAsArray()[0];
        assertEquals("Home", purpose2.getName());
    }

}
