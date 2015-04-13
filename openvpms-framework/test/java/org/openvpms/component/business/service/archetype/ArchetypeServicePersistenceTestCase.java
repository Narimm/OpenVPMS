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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.SecurityRole;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Test the persistence side of the archetype service.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("archetype-service-appcontext.xml")
public class ArchetypeServicePersistenceTestCase extends AbstractArchetypeServiceTest {

    /**
     * Test that we can create a {@link Party} through this service.
     */
    @Test
    public void testCreatePerson() {
        Entity entity = (Entity) create("party.person");
        assertTrue(entity instanceof Party);

        Party person = (Party) entity;
        person.getDetails().put("lastName", "Alateras");
        person.getDetails().put("firstName", "Jim");
        person.getDetails().put("title", "MR");

        save(person);
    }

    /**
     * Test create, retrieve and update person.
     */
    @Test
    public void testPersonLifecycle() {
        Entity entity = (Entity) create("party.person");
        assertTrue(entity instanceof Party);

        Party person = (Party) entity;
        person.getDetails().put("lastName", "Alateras");
        person.getDetails().put("firstName", "Jim");
        person.getDetails().put("title", "MR");

        Contact contact = createConact();

        Date start = new Date();
        Date end = new Date(start.getTime() + (7 * 24 * 60 * 60 * 1000));
        contact.setActiveStartTime(start);
        contact.setActiveEndTime(end);
        person.addContact(contact);
        assertTrue(person.getContacts().size() == 1);
        save(person);

        person = (Party) get(person.getObjectReference());
        assertTrue(person.getContacts().size() == 1);
        person.getDetails().put("firstName", "Grace");
        save(person);
    }

    /**
     * Test that we can create a {@link Party} through this service.
     */
    @Test
    public void testAnimalCreation() {
        // create and insert a new pet
        save(createPet("brutus"));
    }

    /**
     * Test that we can create a {@link Lookup} through this service.
     */
    @Test
    public void testLookupCreation() {
        // create and insert a new lookup
        Lookup lookup = createCountryLookup("South Africa");
        save(lookup);
        assertTrue(get(lookup.getObjectReference()) != null);
    }

    /**
     * Test that we can locate entities by EntityName only.
     */
    @Test
    public void testFindWithEntityName() {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", null, false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int before = get(query).size();

        // create and insert a new pet
        save(createPet("simon"));

        // now get a new count
        int after = get(query).size();
        assertEquals(before + 1, after);
    }

    /**
     * Test that we can locate entities by partial EntityName.
     */
    @Test
    public void testFindWithPartialEntityName() {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party*", null, false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int before = get(query).size();

        // create and insert a new pet
        save(createPet("testFindWithPartialEntityName"));

        // now get a new count
        List<IMObject> after = get(query);
        assertEquals(before + 1, after.size());

        for (IMObject entity : after) {
            assertTrue(
                    entity.getArchetypeId().getEntityName().matches("part.*"));
        }

        // now  test with a starts with
        ArchetypeQuery query2 = new ArchetypeQuery("*arty", null, false, false);
        after = get(query2);
        for (IMObject entity : after) {
            assertTrue(entity.getArchetypeId().getEntityName().matches(".*arty"));
        }

        // now  test with a start and ends with
        ArchetypeQuery query3 = new ArchetypeQuery("*arty*", null, false, false);
        after = get(query3);
        for (IMObject entity : after) {
            assertTrue(entity.getArchetypeId().getEntityName().matches(".*arty.*"));
        }
    }

    /**
     * Test that we can locate entities by EntityName and ConceptName.
     */
    @Test
    public void testFindWithConceptName() {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "animalpet", false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int before = get(query).size();

        // create and insert a new pet
        save(createPet("jack"));

        // now get a new count
        int after = get(query).size();
        assertEquals(before + 1, after);
    }

    /**
     * Test that we can locate entities by EntityName and partial
     * conceptName
     */
    @Test
    public void testFindWithPartialConceptName() {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "anim*", false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int before = get(query).size();

        // create and insert a new pet
        save(createPet("testFindWithPartialConceptName"));

        // now get a new count
        List<IMObject> after = get(query);
        assertEquals(before + 1, after.size());
        for (IMObject entity : after) {
            assertTrue(entity.getArchetypeId().getConcept().matches("anim.*"));
        }

        // now test with a starts with
        ArchetypeQuery query2 = new ArchetypeQuery("party", "*nimalpet", false, false);
        after = get(query2);
        for (IMObject entity : after) {
            assertTrue(entity.getArchetypeId().getConcept().matches(".*nimalpet"));
        }

        // now test with a start and ends with
        ArchetypeQuery query3 = new ArchetypeQuery("party", "*nimalpe*", false, false);
        after = get(query3);
        for (IMObject entity : after) {
            assertTrue(entity.getArchetypeId().getConcept().matches(".*nimalpe.*"));
        }
    }

    /**
     * Test that we can locate entities by EntityName, ConceptName
     * and instance name
     */
    @Test
    public void testFindWithInstanceName() {
        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "animalpet", false, false);
        query.add(new NodeConstraint("name", RelationalOp.EQ, "brutus"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int before = get(query).size();

        // create and insert a new pet
        save(createPet("brutus"));

        // now get a new count
        int after = get(query).size();
        assertEquals(before + 1, after);
    }


    /**
     * Test that we can locate entities by EntityName, ConceptName
     * and partial instance name
     */
    @Test
    public void testFindWithPartialInstanceName() {
        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "animalpet", false, false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "br*"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int before = get(query).size();

        // create and insert a new pet
        save(createPet("brutus"));

        // now get a new count
        List<IMObject> after = get(query);
        assertEquals(before + 1, after.size());
        for (IMObject entity : after) {
            assertTrue(entity.getName(), entity.getName().matches("br.*"));
        }

        // now test with a starts with
        ArchetypeQuery query2 = new ArchetypeQuery("party", "animalpet", false,
                                                   false)
                .add(new NodeConstraint("name", RelationalOp.EQ,
                                        "*tus"));
        query2.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        after = get(query2);
        for (IMObject entity : after) {
            assertTrue(entity.getName().matches(".*tus"));
        }

        // now test with a start and ends with
        ArchetypeQuery query3 = new ArchetypeQuery("party", "animalpet", false,
                                                   false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "*utu*"));
        query3.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        after = get(query3);
        for (IMObject entity : after) {
            assertTrue(entity.getName().matches(".*utu.*"));
        }
    }

    /**
     * Test for OVPMS-147, where I pass in a set of archetypes short names and
     * return a list of objects
     */
    @Test
    public void testOVPMS147() {
        // setup a list of archetype short names
        String[] shortNames = {"party.animalp*", "party.person"};

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, false);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int before = get(query).size();

        // create a new person and check the count 
        save(createPerson("MS", "Bernadette", "Feeney"));
        save(createPerson("MS", "Rose", "Feeney Alateras"));
        int after = get(query).size();
        assertEquals(before + 2, after);

        // create a new lookup
        save(createCountryLookup("Algeria"));
        after = get(query).size();
        assertEquals(before + 2, after);
    }

    /**
     * Test that that we can retrieve short names using a combination
     * of entityName and conceptName.
     */
    @Test
    public void testGetArchetypeShortNames() {
        IArchetypeService service = getArchetypeService();
        List<String> shortNames = service.getArchetypeShortNames("jimaparty", null, true);
        assertTrue(shortNames.isEmpty());

        // check primary short names
        shortNames = service.getArchetypeShortNames("party", null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            assertEquals("party", archId.getEntityName());
            assertTrue(desc.isPrimary());
        }

        // now check primary is set to false. Should be identical as
        // parties are primary.
        int count = shortNames.size();
        shortNames = service.getArchetypeShortNames("party", null, false);
        assertEquals(count, shortNames.size());

        // now check contacts. Test data has all contacts with primary=false
        // check primary short names
        shortNames = service.getArchetypeShortNames("contact", null, true);
        assertEquals(0, shortNames.size());

        // check non-primary contacts
        shortNames = service.getArchetypeShortNames("contact", null, false);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            assertEquals("contact", archId.getEntityName());
            assertFalse(desc.isPrimary());
        }

        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("part*", null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            assertTrue(archId.getEntityName().matches("part.*"));
            assertTrue(desc.isPrimary());
        }

        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("*arty", null, false);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            assertTrue(archId.getEntityName().matches(".*arty"));
        }
    }

    /**
     * Test that we can retrieve all the object for a particular
     * classification.
     */
    @Test
    public void testGetCandidates() {
        IArchetypeService service = getArchetypeService();
        Lookup cf = LookupUtil.createLookup(service, "lookup.staff", "VET");
        cf.setDescription("vet");
        save(cf);

        cf = LookupUtil.createLookup(service, "lookup.staff", "VET1");
        cf.setDescription("vet1");
        save(cf);

        cf = LookupUtil.createLookup(service, "lookup.patient", "PREMIUM");
        cf.setDescription("premium");
        save(cf);

        cf = LookupUtil.createLookup(service, "lookup.patient", "STANDARD");
        cf.setDescription("standard");
        save(cf);

        Party person = (Party) create("party.person");
        ArchetypeDescriptor adesc = getArchetypeService().getArchetypeDescriptor(person.getArchetypeId());
        NodeDescriptor ndesc = adesc.getNodeDescriptor("classifications");
        List<IMObject> candidates =
                ArchetypeQueryHelper.getCandidates(service, ndesc);
        assertTrue(candidates.size() > 0);
        for (IMObject candidate : candidates) {
            boolean matchFound = false;
            for (String shortName : ndesc.getArchetypeRange()) {
                if (candidate.getArchetypeId().getShortName().equals(
                        shortName)) {
                    matchFound = true;
                    break;
                }
            }

            // if a match cannot be found then signal an error
            if (!matchFound) {
                fail(candidate.getArchetypeId() + " does not match archetype range");
            }
        }
    }

    /**
     * Test active flag on pet object.
     */
    @Test
    public void testActiveFlagOnAnimal() {
        Party pet = createPet("jimbo");
        save(pet);

        Party retrievePet = get(pet);
        assertNotNull(retrievePet);
        retrievePet.setActive(false);
        save(retrievePet);

        retrievePet = get(retrievePet);
        assertNotNull(retrievePet);
        assertFalse(retrievePet.isActive());
    }

    /**
     * Test the we can retrieve values for each NodeDescriptor specified
     * for the Archetype
     */
    @Test
    public void testGetValueForEachNodeDescriptor() {
        Party party = (Party) create("party.customerperson");
        assertNotNull(party);
        assertNotNull(party.getDetails());

        party.getDetails().put("title", "MR");
        party.getDetails().put("firstName", "Jim");
        party.getDetails().put("lastName", "Alateras");
        ArchetypeDescriptor adesc = getArchetypeService().getArchetypeDescriptor(party.getArchetypeId());
        for (NodeDescriptor ndesc : adesc.getAllNodeDescriptors()) {
            ndesc.getValue(party);
        }

        save(party);
        party = get(party);
        assertNotNull(party);
        assertNotNull(party.getDetails());
        assertTrue(party.getDetails().get("title").equals("MR"));
    }

    /**
     * Test the creation of a SecurityRole
     */
    @Test
    public void testOVPMS115() {
        // retrieve the initial count
        ArchetypeQuery query = new ArchetypeQuery("security", "role", false, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int count = get(query).size();
        SecurityRole role = (SecurityRole) create("security.role");
        role.setName("administrator");
        save(role);

        // retrieve by id
        role = (SecurityRole) get(role.getObjectReference());
        assertNotNull(role);
        assertEquals("administrator", role.getName());

        // retrieve the count after the add
        int count1 = get(query).size();
        assertEquals(count + 1, count1);
    }

    /**
     * Test OVPMS-135 bug.
     */
    @Test
    public void testOVPMS135() {
        IArchetypeService service = getArchetypeService();
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("lookup.staff");
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("alias");
        assertNotNull(ndesc);

        Lookup object = LookupUtil.createLookup(service, "lookup.staff", "jima");
        object.setDescription("head chef");
        ndesc.setValue(object, "jimmya");
        assertTrue(ndesc.getValue(object) != null);
        save(object);

        object = get(object);
        assertNotNull(object);
        assertNotNull(ndesc.getValue(object));
        assertEquals("jimmya", ndesc.getValue(object));
    }

    /**
     * Test that we can support OVPMS-146.
     */
    @Test
    public void testOVPMS146() {
        Entity entity = (Entity) create("party.person");
        assertTrue(entity instanceof Party);

        Party person = (Party) entity;
        person.getDetails().put("lastName", "Alateras");
        person.getDetails().put("firstName", "Jim");
        person.getDetails().put("title", "MR");
        save(person);

        IMObjectReference ref = person.getObjectReference();
        person = (Party) get(ref);
        assertTrue(person.getLinkId().equals(ref.getLinkId()));
        assertTrue(person.getDetails().get("lastName").equals("Alateras"));
    }


    /**
     * Test that we can support OVPMS-176.
     */
    @Test
    public void testOVPMS176() {
        List<IMObject> objs = get(new ArchetypeQuery("product.productP*", true, true));
        for (IMObject obj : objs) {
            String shortName = obj.getArchetypeId().getShortName();
            String entity = new StringTokenizer(shortName, ".").nextToken();
            if (!entity.equals("product")) {
                fail("Failed in testOVPMS176: Retrieved unexpected archetyped "
                     + shortName + " when criteria was product.*");
            }
        }

        // add a productPrice and ensure that it behaves correctly
        int acount = objs.size();
        save(createPriceMargin(new BigDecimal("1.0")));
        int acount1 = get(new ArchetypeQuery("product.productP*", true, true)).size();
        assertEquals(acount, acount1);
    }

    /**
     * Test that we can support OVPMS-177.
     */
    @Test
    public void testOVPMS177() {
        ArchetypeQuery query = new ArchetypeQuery("product.product", true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        int acount = get(query).size();

        // create some products
        for (int index = 0; index < 5; index++) {
            save(createProduct("product-type-" + index));
        }

        int acount1 = get(query).size();
        assertEquals(acount + 5, acount1);
    }

    /**
     * Create a pet with the specified name
     *
     * @param name the pet's name
     * @return Animal
     */
    private Party createPet(String name) {
        Entity entity = (Entity) create("party.animalpet");
        assertTrue(entity instanceof Party);

        Party pet = (Party) entity;
        pet.setName(name);
        pet.getDetails().put("breed", "dog");
        pet.getDetails().put("colour", "brown");
        pet.getDetails().put("sex", "UNSPECIFIED");
        pet.getDetails().put("species", "k9");
        pet.setDescription("A dog");

        Calendar date = Calendar.getInstance();
        date.set(1963, 12, 20);
        pet.getDetails().put("dateOfBirth", date.getTime());

        return pet;
    }

    /**
     * Creates a contact.
     *
     * @return a new contact
     */
    private Contact createConact() {
        Contact contact = (Contact) create("contact.location");

        contact.getDetails().put("address", "kalulu rd");
        contact.getDetails().put("suburb", "Belgrave");
        contact.getDetails().put("postcode", "3160");
        contact.getDetails().put("state", "Victoria");
        contact.getDetails().put("country", "Australia");

        return contact;
    }

    /**
     * Creates a person with the specified title, firstName and lastName.
     *
     * @param title     the title
     * @param firstName the first name
     * @param lastName  the last name
     * @return a new person
     */
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party) create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

    /**
     * Creates a country lookup.
     *
     * @param code the lookup code
     * @return a new lookup
     */
    private Lookup createCountryLookup(String code) {
        return LookupUtil.createLookup(getArchetypeService(), "lookup.country", code);
    }

    /**
     * Creates a product.
     *
     * @param name the product name
     * @return a new product
     */
    private Product createProduct(String name) {
        Product product = (Product) create("product.product");
        product.setName(name);

        return product;
    }

    /**
     * Creates a product price margin with the specified value.
     *
     * @param margin the product price margin
     * @return a new product price
     */
    private ProductPrice createPriceMargin(BigDecimal margin) {
        ProductPrice price = (ProductPrice) create(
                "productPrice.margin");
        price.setPrice(margin);

        return price;
    }

}
