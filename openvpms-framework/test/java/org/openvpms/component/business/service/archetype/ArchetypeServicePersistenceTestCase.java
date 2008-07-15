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
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.lookup.LookupUtil;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

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
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServicePersistenceTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService service;


    /**
     * Default constructor
     */
    public ArchetypeServicePersistenceTestCase() {
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

    /**
     * Test that we can create a {@link Party} through this service.
     */
    public void testCreatePerson() throws Exception {
        Entity entity = (Entity) service.create("party.person");
        assertTrue(entity instanceof Party);

        Party person = (Party) entity;
        person.getDetails().put("lastName", "Alateras");
        person.getDetails().put("firstName", "Jim");
        person.getDetails().put("title", "MR");

        try {
            service.save(person);
        } catch (ValidationException exception) {
            dump(exception);
        }
    }

    /**
     * Test create, retrieve and update person.
     */
    public void testPersonLifecycle() throws Exception {
        Entity entity = (Entity) service.create("party.person");
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
        service.save(person);

        person = (Party) service.get(person.getObjectReference());
        assertTrue(person.getContacts().size() == 1);
        person.getDetails().put("firstName", "Grace");
        service.save(person);
    }

    /**
     * Test that we can create a {@link Party} through this service.
     */
    public void testAnimalCreation() throws Exception {
        // create and insert a new pet
        service.save(createPet("brutus"));
    }

    /**
     * Test that we can create a {@link Lookup} through this service.
     */
    public void testLookupCreation() throws Exception {
        // create and insert a new lookup
        Lookup lookup = createCountryLookup("South Africa");
        service.save(lookup);
        assertTrue(service.get(lookup.getObjectReference()) != null);
    }

    /**
     * Test that we can locate entities by EntityName only.
     */
    public void testFindWithEntityName() throws Exception {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", null, false, false);
        query.setCountResults(true);
        query.setMaxResults(1);
        int before = service.get(query).getTotalResults();

        // create and insert a new pet
        service.save(createPet("simon"));

        // now get a new count
        int after = service.get(query).getTotalResults();
        assertEquals(before + 1, after);
    }

    /**
     * Test that we can locate entities by partial EntityName.
     */
    public void testFindWithPartialEntityName() throws Exception {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party*", null, false, false);
        query.setCountResults(true);
        query.setMaxResults(1);
        int before = service.get(query).getTotalResults();

        // create and insert a new pet
        service.save(createPet("testFindWithPartialEntityName"));

        // now get a new count
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> after = service.get(query).getResults();
        assertEquals(before + 1, after.size());

        for (IMObject entity : after) {
            assertTrue(
                    entity.getArchetypeId().getEntityName().matches("part.*"));
        }

        // now  test with a starts with
        ArchetypeQuery query2 = new ArchetypeQuery("*arty", null, false, false);
        after = service.get(query2).getResults();
        for (IMObject entity : after) {
            assertTrue(
                    entity.getArchetypeId().getEntityName().matches(".*arty"));
        }

        // now  test with a start and ends with
        ArchetypeQuery query3 = new ArchetypeQuery("*arty*", null, false,
                                                   false);
        after = service.get(query3).getResults();
        for (IMObject entity : after) {
            assertTrue(
                    entity.getArchetypeId().getEntityName().matches(
                            ".*arty.*"));
        }
    }

    /**
     * Test that we can locate entities by EntityName and ConceptName.
     */
    public void testFindWithConceptName() throws Exception {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "animalpet",
                                                  false, false);
        query.setMaxResults(1);
        query.setCountResults(true);
        int before = service.get(query).getTotalResults();

        // create and insert a new pet
        service.save(createPet("jack"));

        // now get a new count
        int after = service.get(query).getTotalResults();
        assertEquals(before + 1, after);
    }

    /**
     * Test that we can locate entities by EntityName and partial
     * conceptName
     */
    public void testFindWithPartialConceptName() throws Exception {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "anim*",
                                                  false, false);
        query.setMaxResults(1);
        query.setCountResults(true);
        int before = service.get(query).getTotalResults();

        // create and insert a new pet
        service.save(createPet("testFindWithPartialConceptName"));

        // now get a new count
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> after = service.get(query).getResults();
        assertEquals(before + 1, after.size());
        for (IMObject entity : after) {
            assertTrue(entity.getArchetypeId().getConcept().matches("anim.*"));
        }

        // now test with a starts with
        ArchetypeQuery query2 = new ArchetypeQuery("party", "*nimalpet", false,
                                                   false);
        after = service.get(query2).getResults();
        for (IMObject entity : after) {
            assertTrue(
                    entity.getArchetypeId().getConcept().matches(".*nimalpet"));
        }

        // now test with a start and ends with
        ArchetypeQuery query3 = new ArchetypeQuery("party", "*nimalpe*", false,
                                                   false);
        after = service.get(query3).getResults();
        for (IMObject entity : after) {
            assertTrue(entity.getArchetypeId().getConcept().matches(
                    ".*nimalpe.*"));
        }
    }

    /**
     * Test that we can locate entities by EntityName, ConceptName
     * and instance name
     */
    public void testFindWithInstanceName()
            throws Exception {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "animalpet", false,
                                                  false);
        query.add(new NodeConstraint("name", RelationalOp.EQ, "brutus"));
        query.setCountResults(true);
        query.setMaxResults(1);
        int before = service.get(query).getTotalResults();

        // create and insert a new pet
        service.save(createPet("brutus"));

        // now get a new count
        int after = service.get(query).getTotalResults();
        assertEquals(before + 1, after);
    }


    /**
     * Test that we can locate entities by EntityName, ConceptName
     * and partial instance name
     */
    public void testFindWithPartialInstanceName()
            throws Exception {

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery("party", "animalpet", false,
                                                  false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "br*"));
        query.setCountResults(true);
        query.setMaxResults(1);
        int before = service.get(query).getTotalResults();

        // create and insert a new pet
        service.save(createPet("brutus"));

        // now get a new count
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> after = service.get(query).getResults();
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
        after = service.get(query2).getResults();
        for (IMObject entity : after) {
            assertTrue(entity.getName().matches(".*tus"));
        }

        // now test with a start and ends with
        ArchetypeQuery query3 = new ArchetypeQuery("party", "animalpet", false,
                                                   false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "*utu*"));
        query3.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        after = service.get(query3).getResults();
        for (IMObject entity : after) {
            assertTrue(entity.getName().matches(".*utu.*"));
        }
    }

    /**
     * Test for OVPMS-147, where I pass in a set of archetypes short names and
     * return a list of objects
     */
    public void testOVPMS147() throws Exception {
        // setup a list of archetype short names
        String[] shortNames = {"party.animalp*", "party.person"};

        // get the initial count
        ArchetypeQuery query = new ArchetypeQuery(shortNames,
                                                  false,
                                                  false);
        query.setMaxResults(1);
        query.setCountResults(true);
        int before = service.get(query).getTotalResults();

        // create a new person and check the count 
        service.save(createPerson("MS", "Bernadette", "Feeney"));
        service.save(createPerson("MS", "Rose", "Feeney Alateras"));
        int after = service.get(query).getTotalResults();
        assertEquals(before + 2, after);

        // create a new lookup
        service.save(createCountryLookup("Algeria"));
        after = service.get(query).getTotalResults();
        assertEquals(before + 2, after);
    }

    /**
     * Test that that we can retrieve short names using a combination
     * of entityName and conceptName.
     */
    public void testGetArchetypeShortNames() throws Exception {
        List<String> shortNames = service.getArchetypeShortNames("jimaparty",
                                                                 null, true);
        assertTrue(shortNames.size() == 0);

        // check primary short names
        shortNames = service.getArchetypeShortNames("party", null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(
                    shortName);
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
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(
                    shortName);
            ArchetypeId archId = desc.getType();
            assertEquals("contact", archId.getEntityName());
            assertFalse(desc.isPrimary());
        }

        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("part*", null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(
                    shortName);
            ArchetypeId archId = desc.getType();
            assertTrue(archId.getEntityName().matches("part.*"));
            assertTrue(desc.isPrimary());
        }

        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("*arty", null, false);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(
                    shortName);
            ArchetypeId archId = desc.getType();
            assertTrue(archId.getEntityName().matches(".*arty"));
        }
    }

    /**
     * Test that we can retrieve all the object for a particular
     * classification.
     */
    public void testGetCandidates() throws Exception {
        Lookup cf = LookupUtil.createLookup(service, "lookup.staff", "VET");
        cf.setDescription("vet");
        service.save(cf);

        cf = LookupUtil.createLookup(service, "lookup.staff", "VET1");
        cf.setDescription("vet1");
        service.save(cf);

        cf = LookupUtil.createLookup(service, "lookup.patient", "PREMIUM");
        cf.setDescription("premium");
        service.save(cf);

        cf = LookupUtil.createLookup(service, "lookup.patient", "STANDARD");
        cf.setDescription("standard");
        service.save(cf);

        Party person = (Party) service.create("party.person");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                person.getArchetypeId());
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
                fail(candidate.getArchetypeId() + " does not match "
                        + ndesc.getArchetypeRange());

            }
        }
    }

    /**
     * Test active flag on pet object.
     */
    public void testActiveFlagOnAnimal()
            throws Exception {
        Party pet = createPet("jimbo");
        service.save(pet);

        ArchetypeId aid = pet.getArchetypeId();
        IPage<IMObject> pageOfPet = service.get(
                new ArchetypeQuery(aid.getEntityName(), aid.getConcept(), false,
                                   true)
                        .add(new NodeConstraint("name", RelationalOp.EQ,
                                                pet.getName()))
                        .setMaxResults(1));
        assertTrue(pageOfPet.getResults().size() == 1);
        Party retrievePet = (Party) pageOfPet.getResults().get(0);
        assertNotNull(retrievePet);
        retrievePet.setActive(false);
        service.save(retrievePet);

        retrievePet = (Party) service.get(retrievePet.getObjectReference());
        assertNotNull(retrievePet);
        assertFalse(retrievePet.isActive());
    }

    /**
     * Test the we can retrieve values for each NodeDescriptor specified
     * for the Archetype
     */
    public void testGetValueForEachNodeDescriptor() throws Exception {
        Party party = (Party) service.create("party.customerperson");
        assertTrue(party != null);
        assertTrue(party.getDetails() != null);

        party.getDetails().put("title", "MR");
        party.getDetails().put("firstName", "Jim");
        party.getDetails().put("lastName", "Alateras");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                party.getArchetypeId());
        for (NodeDescriptor ndesc : adesc.getAllNodeDescriptors()) {
            ndesc.getValue(party);
        }

        service.save(party);
        party = (Party) service.get(party.getObjectReference());
        assertTrue(party != null);
        assertTrue(party.getDetails() != null);
        assertTrue(party.getDetails().get("title").equals("MR"));
    }

    /**
     * Test the creation of a SecurityRole
     */
    public void testOVPMS115() throws Exception {

        // retrieve the initial count
        ArchetypeQuery query = new ArchetypeQuery("security", "role", false,
                                                  true);
        query.setCountResults(true);
        query.setMaxResults(1);
        int count = service.get(query).getTotalResults();
        SecurityRole role = (SecurityRole) service.create("security.role");
        role.setName("administrator");
        service.save(role);

        // retrieve by id
        role = (SecurityRole) service.get(role.getObjectReference());
        assertNotNull(role);
        assertEquals("administrator", role.getName());

        // retrieve the count after the add
        int count1 = service.get(query).getTotalResults();
        assertEquals(count + 1, count1);
    }

    /**
     * Test OVPMS-135 bug.
     */
    public void testOVPMS135()
            throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "lookup.staff");
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("alias");
        assertTrue(ndesc != null);

        Lookup object = LookupUtil.createLookup(service, "lookup.staff",
                                                "jima");
        object.setDescription("head chef");
        ndesc.setValue(object, "jimmya");
        assertTrue(ndesc.getValue(object) != null);
        service.save(object);

        object = (Lookup) service.get(object.getObjectReference());
        assertTrue(object != null);
        assertTrue(ndesc.getValue(object) != null);
        assertTrue(ndesc.getValue(object).equals("jimmya"));
    }

    /**
     * Test that we can suppor OVPMS-146
     */
    public void testOVPMS146()
            throws Exception {
        Entity entity = (Entity) service.create("party.person");
        assertTrue(entity instanceof Party);

        Party person = (Party) entity;
        person.getDetails().put("lastName", "Alateras");
        person.getDetails().put("firstName", "Jim");
        person.getDetails().put("title", "MR");
        service.save(person);

        IMObjectReference ref = person.getObjectReference();
        person = (Party) service.get(ref);
        assertTrue(person.getLinkId().equals(ref.getLinkId()));
        assertTrue(person.getDetails().get("lastName").equals("Alateras"));
    }


    /**
     * Test that we can sup[port OVPMS-176
     */
    public void testOVPMS176()
            throws Exception {
        List<IMObject> objs = service.get(
                new ArchetypeQuery("product.productP*",
                                   true, true)).getResults();
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
        service.save(createPriceMargin(new BigDecimal("1.0")));
        int acount1 = service.get(new ArchetypeQuery("product.productP*",
                                                     true,
                                                     true)).getResults().size();
        assertTrue(acount == acount1);
    }

    /**
     * Test that we can sup[port OVPMS-177
     */
    public void testOVPMS177()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery("product.product", true,
                                                  true);
        query.setCountResults(true);
        query.setMaxResults(1);
        int acount = service.get(query).getTotalResults();

        // create some products
        for (int index = 0; index < 5; index++) {
            service.save(createProduct("product-type-" + index));
        }

        int acount1 = service.get(query).getTotalResults();
        assertEquals(acount + 5, acount1);
    }

    /**
     * Create a pet with the specified name
     *
     * @param name the pet's name
     * @return Animal
     */
    private Party createPet(String name) {
        Entity entity = (Entity) service.create("party.animalpet");
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
        Contact contact = (Contact) service.create("contact.location");

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
        Party person = (Party) service.create("party.person");
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
        return LookupUtil.createLookup(service, "lookup.country", code);
    }

    /**
     * Creates a product.
     *
     * @param name the product name
     * @return a new product
     */
    private Product createProduct(String name) {
        Product product = (Product) service.create("product.product");
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
        ProductPrice price = (ProductPrice) service.create(
                "productPrice.margin");
        price.setPrice(margin);

        return price;
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
     * Display the validation errors.
     *
     * @param exception the validation exception
     */
    private void dump(ValidationException exception) {
        for (ValidationError error : exception.getErrors()) {
            logger.error("node: " + error.getNode()
                    + " message: " + error.getMessage());
        }
    }
}
