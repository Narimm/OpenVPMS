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

import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.openvpms.component.system.common.query.ArchetypeNodeConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;
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
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServiceQueryTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Test the query by name in the lookup entity. This will support
     * OVPMS-35.
     */
    public void testOVPMS35() {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Belarus"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = LookupUtil.createLookup(service, "lookup.country",
                                                "Belarus", "Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test query by name with wildcard.
     */
    public void testGetByCodeWithWildcard() {
        ArchetypeQuery query = new ArchetypeQuery("lookup.country", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Bel*"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = LookupUtil.createLookup(service, "lookup.country",
                                                "Belarus", "Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test query by name with wild in short name.
     */
    public void testGetCodeWithWildCardShortName() {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false,
                                                  true).add(
                new NodeConstraint("name", RelationalOp.EQ, "Bel*"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = LookupUtil.createLookup(service, "lookup.country",
                                                "Belarus", "Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Test query by name with wild in short name and an order clause.
     */
    public void testGetCodeWithWildCardShortNameAndOrdered() {
        ArchetypeQuery query = new ArchetypeQuery("lookup.cou*", false, true)
                .add(new NodeConstraint("name", RelationalOp.EQ, "Bel*"))
                .add(new NodeSortConstraint("name", true));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        int acount = service.get(query).getResults().size();
        Lookup lookup = LookupUtil.createLookup(service, "lookup.country",
                                                "Belarus", "Belarus");
        service.save(lookup);
        int acount1 = service.get(query).getResults().size();
        assertEquals(acount + 1, acount1);
    }

    /**
     * Tests product queries constrained by a species classification.
     * <p/>
     * This should return all products that have a canine species classification, or
     * no species classification.
     * <p/>
     * This tests the fix for OBF-20 (was OVPMS-245)
     */
    public void testGetProductBySpecies() {
        Lookup canine = LookupUtil.createLookup(service, "lookup.species", "CANINE");
        Lookup feline = LookupUtil.createLookup(service, "lookup.species", "FELINE");
        service.save(canine);
        service.save(feline);

        Product canineProduct = createProduct(); // a product for canines only
        canineProduct.addClassification(canine);
        service.save(canineProduct);

        Product felineProduct = createProduct(); // a product for felines only
        felineProduct.addClassification(feline);
        service.save(felineProduct);

        Product bothProduct = createProduct();  // a product for both canines and felines
        bothProduct.addClassification(canine);
        bothProduct.addClassification(feline);
        service.save(bothProduct);

        Product genericProduct = createProduct(); // a product foro all pets
        service.save(genericProduct);

        ArchetypeQuery query = new ArchetypeQuery("product.product", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(new CollectionNodeConstraint("classifications")
                        .setJoinType(JoinConstraint.JoinType.LeftOuterJoin)
                        .add(new ArchetypeNodeConstraint(RelationalOp.EQ, "lookup.species")))
                .add(new OrConstraint()
                        .add(new NodeConstraint("classifications.code", RelationalOp.EQ, canine.getCode()))
                        .add(new NodeConstraint("classifications.code", RelationalOp.IS_NULL)));

        List<IMObject> objects = service.get(query).getResults();
        assertTrue(objects.contains(canineProduct));
        assertFalse(objects.contains(felineProduct));
        assertTrue(objects.contains(bothProduct));
        assertTrue(objects.contains(genericProduct));
    }

    /**
     * Test party queries where the parties may have a particular identity (i.e identities node) or ID.
     */
    public void testQueryEntityByClassificationAndId() {
        Party person1 = createPerson();
        person1.addIdentity(createIdentity("IDENT1"));
        service.save(person1);

        Party person2 = createPerson();
        person1.addIdentity(createIdentity("IDENT2"));
        service.save(person2);

        Party person3 = createPerson();
        person3.addIdentity(createIdentity("IDENT12"));
        service.save(person3);

        ArchetypeQuery query = new ArchetypeQuery("party.person", false, false)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS)
                .add(Constraints.leftJoin("identities", Constraints.shortName("entityIdentity.personAlias"))
                        .add(Constraints.eq("identity", "IDENT1*")))
                .add(Constraints.or(Constraints.eq("id", person1.getId()),
                                    Constraints.notNull("identities.identity")));
        List<IMObject> objects = service.get(query).getResults();
        assertTrue(objects.contains(person1));
        assertFalse(objects.contains(person2));
        assertTrue(objects.contains(person3));
    }

    private EntityIdentity createIdentity(String identity) {
        EntityIdentity result = (EntityIdentity) service.create("entityIdentity.personAlias");
        result.setIdentity(identity);
        return result;
    }

    /**
     * Tests the NodeSet get method. This verifies that subcollections are
     * loaded correctly, avoiding LazyInitializationExceptionnException.
     */
    public void testGetNodeSet() {
        // set up a party with a single contact and contact purpose
        Contact contact = (Contact) service.create("contact.phoneNumber");
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "0123456789");
        Lookup purpose = LookupUtil.createLookup(service,
                                                 "lookup.contactPurpose",
                                                 "Home", "Home");
        service.save(purpose);

        contact.addClassification(purpose);

        Party person = createPerson();
        person.addContact(contact);
        service.save(person);

        // query the firstName, lastName and contacts nodes of the person
        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());
        List<String> names = Arrays.asList("firstName", "lastName", "contacts");
        IPage<NodeSet> page = service.getNodes(query, names);
        assertNotNull(page);

        // verify that the page only has a single element, and that the node
        // set has the expected nodes
        assertEquals(1, page.getResults().size());
        NodeSet nodes = page.getResults().get(0);
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
        @SuppressWarnings("unchecked")
        Collection<Contact> contacts
                = (Collection<Contact>) nodes.get("contacts");
        assertEquals(1, contacts.size());
        contact = contacts.toArray(new Contact[contacts.size()])[0];
        assertEquals("03", contact.getDetails().get("areaCode"));
        assertEquals("0123456789",
                     contact.getDetails().get("telephoneNumber"));
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
        contact.getDetails().put("areaCode", "03");
        contact.getDetails().put("telephoneNumber", "0123456789");
        Lookup purpose = LookupUtil.createLookup(service,
                                                 "lookup.contactPurpose",
                                                 "HOME");
        service.save(purpose);

        contact.addClassification(purpose);

        Party person = createPerson();
        person.addContact(contact);
        service.save(person);

        // query the firstName, lastName and contacts nodes of the person
        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());
        List<String> names = Arrays.asList("firstName", "lastName", "title",
                                           "contacts");
        IPage<IMObject> page = service.get(query, names);
        assertNotNull(page);

        // verify that the page only has a single element, and that the
        // contacts node has been loaded.
        assertEquals(1, page.getResults().size());
        Party person2 = (Party) page.getResults().get(0);
        Set<Contact> contacts = person2.getContacts();
        assertEquals(1, contacts.size());

        // verify the values of the simple nodes. Note that although details
        // is a collection, it is treated as a simple node by hibernate as it
        // maps to a single column. We specify it to load anyway
        assertEquals(person.getObjectReference(), person2.getObjectReference());
        assertEquals(3, person2.getDetails().size());
        assertEquals("Tim", person.getDetails().get("firstName"));
        assertEquals("Anderson", person.getDetails().get("lastName"));
        assertEquals("MR", person2.getDetails().get("title"));

        // verify the values of the contact node. If the classification hasn't
        // been loaded, a LazyInitializationException will be raised by
        // hibernate
        Contact contact2 = contacts.toArray(new Contact[contacts.size()])[0];
        assertEquals("03", contact2.getDetails().get("areaCode"));
        assertEquals("0123456789",
                     contact2.getDetails().get("telephoneNumber"));
        assertEquals(1, contact2.getClassificationsAsArray().length);
        Lookup purpose2 = contact2.getClassificationsAsArray()[0];
        assertEquals(purpose.getCode(), purpose2.getCode());
    }

    /**
     * Verifies that additional constraints can be use with
     * {@link ObjectRefConstraint}.
     */
    public void testOBF155() {
        Party person = createPerson();
        service.save(person);

        ArchetypeQuery query = new ArchetypeQuery(person.getObjectReference());

        // verify that the page only has a single element
        IPage<IMObject> page = service.get(query);
        assertEquals(1, page.getResults().size());

        // constrain the query, and verify the page is empty
        query.add(new NodeConstraint("name", "Mr Foo"));
        page = service.get(query);
        assertEquals(0, page.getResults().size());
    }

    /**
     * Verfies that relationships between entities can be queried.
     */
    public void testOBF178() {
        Party person = createPerson();
        EntityBean bean = new EntityBean(person);

        Party pet = (Party) service.create("party.animalpet");
        IMObjectBean petBean = new IMObjectBean(pet);
        String petName = "Mutt-" + System.currentTimeMillis();
        petBean.setValue("name", petName);
        petBean.setValue("species", "CANINE");
        petBean.setValue("breed", "Australian Terrier");
        bean.addRelationship("entityRelationship.animalOwner", pet);

        service.save(Arrays.asList(person, pet));

        ShortNameConstraint partyPerson
                = new ShortNameConstraint("person", "party.person");
        ShortNameConstraint animalPet
                = new ShortNameConstraint("pet", "party.animalpet");
        ShortNameConstraint relationship
                = new ShortNameConstraint("rel",
                                          "entityRelationship.animalOwner*");

        ArchetypeQuery sourceQuery = new ArchetypeQuery(partyPerson);
        sourceQuery.add(new CollectionNodeConstraint("patients", relationship));
        sourceQuery.add(animalPet);
        sourceQuery.add(new IdConstraint("rel.source", "person"));
        sourceQuery.add(new IdConstraint("rel.target", "pet"));
        sourceQuery.add(new NodeConstraint("pet.name", petName));

        // verify that the page only has a single element
        IPage<IMObject> page = service.get(sourceQuery);
        assertEquals(1, page.getResults().size());

        ArchetypeQuery targetQuery = new ArchetypeQuery(animalPet);
        targetQuery.add(
                new CollectionNodeConstraint("customers", relationship));
        targetQuery.add(partyPerson);
        targetQuery.add(new IdConstraint("rel.source", "person"));
        targetQuery.add(new IdConstraint("rel.target", "pet"));
        targetQuery.add(new NodeConstraint("pet.name", petName));

        // verify that the page only has a single element
        page = service.get(targetQuery);
        assertEquals(1, page.getResults().size());
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
     * Helper to create a party of type <em>party.person</em>.
     *
     * @return a new party
     */
    private Party createPerson() {
        Party person = (Party) service.create("party.person");
        IMObjectBean bean = new IMObjectBean(person);
        bean.setValue("firstName", "Tim");
        bean.setValue("lastName", "Anderson");
        bean.setValue("title", "MR");
        return person;
    }

    /**
     * Helper to create a product of type <em>product.product</em>.
     *
     * @return a new product
     */
    private Product createProduct() {
        Product product = (Product) service.create("product.product");
        product.setName("XProduct-" + System.currentTimeMillis());
        return product;
    }

}
