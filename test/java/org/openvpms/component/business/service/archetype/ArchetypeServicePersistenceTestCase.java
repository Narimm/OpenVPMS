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

// JAV CORE
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

//spring-context 
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// log4j
import org.apache.log4j.Logger;

// opevpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Classification;
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
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

/**
 * Test the persistence side of the archetype service
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServicePersistenceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeServicePersistenceTestCase.class);
    
    /**
     * Holds a reference to the entity service
     */
    private IArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServicePersistenceTestCase.class);
    }

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
        return new String[] { 
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml" 
                };
    }

    /**
     * Test that we can create a {@link Party} through this service
     */
    public void  testCreatePerson()
    throws Exception {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Party);
        
        Party person = (Party)entity;
        person.getDetails().setAttribute("lastName", "Alateras");
        person.getDetails().setAttribute("firstName", "Jim");
        person.getDetails().setAttribute("title", "Mr");
        
        try {
            service.save(person);
        } catch (ValidationException exception) {
            dump(exception);
        }
    }

    /**
     * Test create, retrieve and update person
     */
    public void  testPersonLifecycle()
    throws Exception {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Party);
        
        Party person = (Party)entity;
        person.getDetails().setAttribute("lastName", "Alateras");
        person.getDetails().setAttribute("firstName", "Jim");
        person.getDetails().setAttribute("title", "Mr");
        
        Contact contact = createConact();
        
        Date start = new Date();
        Date end = new Date(start.getTime() + (7*24*60*60*1000));
        contact.setActiveStartTime(start);
        contact.setActiveEndTime(end);
        person.addContact(contact);
        assertTrue(person.getContacts().size() == 1);
        service.save(person);
        
        person = (Party)ArchetypeQueryHelper.getByUid(service,
                person.getArchetypeId(), person.getUid());
        assertTrue(person.getContacts().size() == 1);
        person.getDetails().setAttribute("firstName", "Grace");
        service.save(person);
    }

    /**
     * Test that we can create a {@link Party} through this service
     */
    public void  testAnimalCreation()
    throws Exception {
        // create and insert a new pet
        service.save(createPet("brutus"));
    }

    /**
     * Test that we can create a {@link Lookup} through this service
     */
    public void  testLookupCreation()
    throws Exception {
        // create and insert a new lookup
        Lookup lookup = createCountryLookup("South Africa");
        service.save(lookup);
        assertTrue(ArchetypeQueryHelper.getByUid(service,
                lookup.getArchetypeId(), lookup.getUid()) != null);
    }

    /**
     * Test that we can locate entities by RmName and EntityName only
     */
    public void  testFindWithEntityName()
    throws Exception {
        
        // get the initial count
        List before = service.get(new ArchetypeQuery("party", "animal", null, false,
                false)).getRows();
        
        // create and insert a new pet
        service.save(createPet("simon"));
        
        // now get a new count
        List after = service.get(new ArchetypeQuery("party", "animal", null, false,
                false)).getRows();
        assertTrue(after.size() == before.size() + 1);
    }

    /**
     * Test that we can locate entities by RmName and partial EntityName 
     */
    public void  testFindWithPartialEntityName()
    throws Exception {
        
        // get the initial count
        List before = service.get(new ArchetypeQuery("party", "ani*", null, false, 
                false)).getRows();
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialEntityName"));
        
        // now get a new count
        List after = service.get(new ArchetypeQuery("party", "ani*", null, false, 
                false)).getRows();
        assertTrue(after.size() == before.size() + 1);
        
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches("ani.*"));
        }

        // now  test with a starts with
        after = service.get(new ArchetypeQuery("party", "*mal", null, false, 
                false)).getRows();
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches(".*mal"));
        }

        // now  test with a start and ends with
        after = service.get(new ArchetypeQuery("party", "*nim*", null, false, 
                false)).getRows();
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getEntityName().matches(".*nim.*"));
        }
    }

    /**
     * Test that we can locate entities by RmName, EntityName and ConceptName
     */
    public void  testFindWithConceptName()
    throws Exception {
        
        // get the initial count
        List before = service.get(new ArchetypeQuery("party", "animal", "pet", 
                false, false)).getRows();
        
        // create and insert a new pet
        service.save(createPet("jack"));
        
        // now get a new count
        List after = service.get(new ArchetypeQuery("party", "animal", "pet", 
                false, false)).getRows();
        assertTrue(after.size() == before.size() + 1);
    }
    
    /**
     * Test that we can locate entities by RmName, EntityName and partial 
     * conceptName
     */
    public void  testFindWithPartialConceptName()
    throws Exception {
        
        // get the initial count
        List before = service.get(new ArchetypeQuery("party", "animal", "p*", false,
                false)).getRows();
        
        // create and insert a new pet
        service.save(createPet("testFindWithPartialConceptName"));
        
        // now get a new count
        List after = service.get(new ArchetypeQuery("party", "animal", "p*", false,
                false)).getRows();
        assertTrue(after.size() == before.size() + 1);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches("p.*"));
        }

        // now test with a starts with
        after = service.get(new ArchetypeQuery("party", "animal", "*et", false,
                false)).getRows();
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches(".*et"));
        }

        // now test with a start and ends with
        after = service.get(new ArchetypeQuery("party", "animal", "*e*", false,
                false)).getRows();
        for (Object entity : after) {
            assertTrue(((Entity)entity).getArchetypeId().getConcept().matches(".*et.*"));
        }
    }

    /**
     * Test that we can locate entities by RmName, EntityName, ConceptName
     * and instance name
     */
    public void  testFindWithInstanceName()
    throws Exception {
        
        // get the initial count
        List before = service.get(new ArchetypeQuery("party", "animal", "pet", false, false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "brutus"))).getRows();
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get(new ArchetypeQuery("party", "animal", "pet", false, false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "brutus"))).getRows();
        assertTrue(after.size() == before.size() + 1);
    }


    /**
     * Test that we can locate entities by RmName, EntityName, ConceptName
     * and partial instance name
     */
    public void  testFindWithPartialInstanceName()
    throws Exception {
        
        // get the initial count
        List before = service.get(new ArchetypeQuery("party", "animal", "pet", false, false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "br*"))).getRows();
        
        // create and insert a new pet
        service.save(createPet("brutus"));
        
        // now get a new count
        List after = service.get(new ArchetypeQuery("party", "animal", "pet", false, false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "br*"))).getRows();
        assertTrue(after.size() == before.size() + 1);
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName(), ((Entity)entity).getName().matches("br.*"));
        }

        // now test with a starts with
        after = service.get(new ArchetypeQuery("party", "animal", "pet", false, false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "*tus"))).getRows();
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches(".*tus"));
        }

        // now test with a start and ends with
        after = service.get(new ArchetypeQuery("party", "animal", "pet", false, false)
                .add(new NodeConstraint("name", RelationalOp.EQ, "*utu*"))).getRows();
        for (Object entity : after) {
            assertTrue(((Entity)entity).getName().matches(".*utu.*"));
        }
    }
    
    /**
     * Test for OVPMS-147, where I pass in a set of archetypes short names and 
     * return a list of objects
     */
    public void testOVPMS147()
    throws Exception {
        // setup a list of archetype short names
        String[] shortNames = {"animal.p*", "person.person"};
        
        // get the initial count
        List<IMObject> before = service.get(new ArchetypeQuery(shortNames, 
                false, false)).getRows();
        
        // create a new person and check the count 
        service.save(createPerson("Ms", "Bernadette", "Feeney"));
        service.save(createPerson("Ms", "Rose", "Feeney Alateras"));
        List<IMObject> after = service.get(new ArchetypeQuery(shortNames, 
                false, false)).getRows();
        assertTrue(after.size() == before.size() + 2);
        
        // create a new lookup
        service.save(createCountryLookup("Algeria"));
        after = service.get(new ArchetypeQuery(shortNames, 
                false, false)).getRows();
        assertTrue(after.size() == before.size() + 2);
    }
    
    /**
     * Test that that we can retrieve short names using a combination
     * of rmName, entityName and conceptName
     */
    public void  testGetArchetypeShortNames()
    throws Exception {
        List<String> shortNames = service.getArchetypeShortNames("jimaparty", null, 
                null, true);
        assertTrue(shortNames.size() == 0);
        
        shortNames = service.getArchetypeShortNames("party", null, 
                null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            if ((archId.getRmName().matches("party")) &&
                (desc.isPrimary())) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party");
        }

        // check when primaryOnly is set to false
        int count = shortNames.size();
        shortNames = service.getArchetypeShortNames("party", null, 
                null, false);
        assertTrue(shortNames.size() > count);
        
        
        shortNames = service.getArchetypeShortNames("party", "person", 
                null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            if ((archId.getRmName().matches("party")) &&
                    (archId.getEntityName().matches("person")) &&
                (desc.isPrimary())) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=person");
        }
        
        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("party", "per*", 
                null, true);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            if ((archId.getRmName().matches("party")) &&
                (archId.getEntityName().matches("per.*")) &&
                (desc.isPrimary())) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=per*");
        }
        
        // test with a partial entity name
        shortNames = service.getArchetypeShortNames("party", "*tact", 
                null, false);
        assertTrue(shortNames.size() > 0);
        for (String shortName : shortNames) {
            ArchetypeDescriptor desc = service.getArchetypeDescriptor(shortName);
            ArchetypeId archId = desc.getType();
            if ((archId.getRmName().matches("party")) &&
                (archId.getEntityName().matches(".*tact"))) {
                continue;
            }
            fail("shortName: " + shortName + " in invalid for rmName=party,entityName=*dress");
        }
    }
    
    /**
     * Test that we can retrieve all the object for a particular 
     * classification.
     */
    public void testGetCandidateChildren()
    throws Exception {
        Classification cf = (Classification)service.create("classification.staff");
        cf.setName("vet");
        cf.setDescription("vet");
        service.save(cf);
        
        cf = (Classification)service.create("classification.staff");
        cf.setName("vet1");
        cf.setDescription("vet1");
        service.save(cf);
        
        cf = (Classification)service.create("classification.patient");
        cf.setName("premium");
        cf.setDescription("premium");
        service.save(cf);
        
        cf = (Classification)service.create("classification.patient");
        cf.setName("standard");
        cf.setDescription("standard");
        service.save(cf);
        
        Party person = (Party)service.create("person.person");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                person.getArchetypeId());
        NodeDescriptor ndesc = adesc.getNodeDescriptor("classifications");
        List<IMObject> children = 
            ArchetypeQueryHelper.getCandidateChildren(service, ndesc, person);
        assertTrue(children.size() > 0);
        for (IMObject child : children) {
            boolean matchFound = false;
            for (String shortName : ndesc.getArchetypeRange()) {
                if (child.getArchetypeId().getShortName().equals(shortName)) {
                    matchFound = true;
                    break;
                }
            }
            
            // if a match cannot be found then signal an error
            if (!matchFound) {
                fail(child.getArchetypeId() + " does not match " + 
                        ndesc.getArchetypeRange().toString());
                
            }
        }
    }
    
    /**)
     * Test active flag on pet object
     */
    public void testActiveFlagOnAnimal()
    throws Exception {
        Party pet = createPet("jimbo");
        service.save(pet);
        
        ArchetypeId aid = pet.getArchetypeId();
        IPage<IMObject> pageOfPet = service.get(new ArchetypeQuery(aid.getRmName(),  
                aid.getEntityName(), aid.getConcept(), false, true)
                .add(new NodeConstraint("name", RelationalOp.EQ, pet.getName()))
                .setNumOfRows(1));
        assertTrue(pageOfPet.getRows().size() == 1);
        Party retrievePet = (Party)pageOfPet.getRows().get(0);
        assertTrue(retrievePet != null);
        retrievePet.setActive(false);
        service.save(retrievePet);
        
        retrievePet = (Party)ArchetypeQueryHelper.getByUid(service, 
                retrievePet.getArchetypeId(), retrievePet.getUid());
        assertTrue(retrievePet != null);
        assertTrue(retrievePet.isActive() == false);
    }
    
    /**
     * Test the we can retrieve values for each NodeDescriptor specified 
     * for the Archetype
     */
    public void testGetValueForEachNodeDescriptor() throws Exception {
        Party party = (Party)service.create("party.customerperson");
        assertTrue(party != null);
        assertTrue(party.getDetails() != null);
        
        party.getDetails().setAttribute("title", "Mr");
        party.getDetails().setAttribute("firstName", "Jim");
        party.getDetails().setAttribute("lastName", "Alateras");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(party.getArchetypeId());
        for (NodeDescriptor ndesc : adesc.getAllNodeDescriptors()) {
            //party.pathToObject(ndesc.getPath());
            //error("Value for " + ndesc.getName() + " is " + party.pathToObject(ndesc.getName()) == null ? null : party.pathToObject(ndesc.getName()).getValue());
            ndesc.getValue(party);
        }
        
        
        service.save(party);
        party = (Party)ArchetypeQueryHelper.getByUid(service, 
                party.getArchetypeId(), party.getUid());
        assertTrue(party != null);
        assertTrue(party.getDetails() != null);
        assertTrue(party != null);
        assertTrue(party.getDetails().getAttribute("title").equals("Mr"));
    }

    /**
     * Test the creation of a SecurityRole
     */
    public void testOVPMS115()
    throws Exception {
        
        // retrieve the initial count
        int count = service.get(new ArchetypeQuery("system", "security", 
                "role", false, true)).getRows().size();
        SecurityRole role = (SecurityRole)service.create("security.role");
        role.setName("administrator");
        service.save(role);
        
        // retrieve by id
        role = (SecurityRole)ArchetypeQueryHelper.getByUid(service,
                role.getArchetypeId(), role.getUid());
        assertTrue(role != null);
        assertTrue(role.getName().equals("administrator"));
        
        // retrieve the count after the add
        int count1 = service.get(new ArchetypeQuery("system", "security", 
                "role", false, true)).getRows().size();
        assertTrue(count1 == count + 1);
    }
    
    /**
     * Test OVPMS-135 bug
     */
    public void testOVPMS135()
    throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor("classification.staff");
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("alias");
        assertTrue(ndesc != null);
        
        IMObject object = service.create("classification.staff");
        object.setName("jima");
        object.setDescription("head chef");
        ndesc.setValue(object, "jimmya");
        assertTrue(ndesc.getValue(object) != null);
        service.save(object);
        
        object = ArchetypeQueryHelper.getByUid(service,
                object.getArchetypeId(), object.getUid());
        assertTrue(object != null);
        assertTrue(ndesc.getValue(object) != null);
        assertTrue(ndesc.getValue(object).equals("jimmya"));
    }
    
    /**
     * Test that we can suppor OVPMS-146
     */
    public void testOVPMS146() 
    throws Exception {
        Entity entity = (Entity)service.create("person.person");
        assertTrue(entity instanceof Party);
        
        Party person = (Party)entity;
        person.getDetails().setAttribute("lastName", "Alateras");
        person.getDetails().setAttribute("firstName", "Jim");
        person.getDetails().setAttribute("title", "Mr");
        service.save(person);
        
        IMObjectReference ref = new IMObjectReference(person);
        person = (Party)ArchetypeQueryHelper.getByObjectReference(service, ref);
        assertTrue(person.getLinkId().equals(ref.getLinkId()));
        assertTrue(person.getDetails().getAttribute("lastName").equals("Alateras"));
    }
    
    
    /**
     * Test that we can sup[port OVPMS-176
     */
    public void testOVPMS176()
    throws Exception {
        List<IMObject> objs = service.get(new ArchetypeQuery("product.productP*", 
                true, true)).getRows();
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
                true, true)).getRows().size();
        assertTrue(acount == acount1);
    }

    /**
     * Test that we can sup[port OVPMS-177
     */
    public void testOVPMS177()
    throws Exception {
        int acount = service.get(new ArchetypeQuery("product.product", 
                true, true)).getRows().size();

        // create some products
        for (int index = 0; index < 5; index++) {
            service.save(createProduct("product-type-" + index));
        }
        
        int acount1 = service.get(new ArchetypeQuery("product.product", 
                true, true)).getRows().size();
        assertTrue(acount1 == acount + 5);
    }

    /**
     * Create a pet with the specified name
     * 
     * @param name
     *            the pet's name
     * @return Animal                  
     */
    private Party createPet(String name) {
        Entity entity = (Entity)service.create("animal.pet");
        assertTrue(entity instanceof Party);
        
        Party pet = (Party)entity;
        pet.setName("brutus");
        pet.getDetails().setAttribute("breed", "dog");
        pet.getDetails().setAttribute("colour", "brown");
        pet.getDetails().setAttribute("sex", "unspecified");
        pet.getDetails().setAttribute("species", "k9");
        pet.setDescription("A dog");
        
        Calendar date = Calendar.getInstance();
        date.set(1963, 12, 20);
        pet.getDetails().setAttribute("dateOfBirth", new Date());
        
        return pet;
    }
    
    /**
     * Create a contact
     * 
     * @return Contact                  
     */
    private Contact createConact() {
        Contact contact = (Contact)service.create("contact.location");
        
        contact.getDetails().setAttribute("address", "kalulu rd");
        contact.getDetails().setAttribute("suburb", "Belgrave");
        contact.getDetails().setAttribute("postcode", "3160");
        contact.getDetails().setAttribute("state", "Victoria");
        contact.getDetails().setAttribute("country", "Australia");
        
        return contact;
    }
    
    /**
     * Create a person with the specified title, firstName and LastName
     * 
     * @param title
     * @param firstName
     * @param lastName
     * 
     * @return Person
     */
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party)service.create("person.person");
        person.getDetails().setAttribute("lastName", lastName);
        person.getDetails().setAttribute("firstName", firstName);
        person.getDetails().setAttribute("title", title);
        
        return person;
    }
    
    /**
     * Create a country lookup
     * 
     * @param name
     *            the role name
     * @return Lookup                  
     */
    private Lookup createCountryLookup(String name) {
        Lookup lookup = (Lookup)service.create("lookup.country");
        assertTrue(lookup instanceof Lookup);
        
        lookup.setCode(name);
        lookup.setValue(name);
        
        return lookup;
    }
    
    /**
     * Create a product
     * 
     * @param name
     *            the product name
     * @return Product            
     */
    private Product createProduct(String name) {
        Product product = (Product)service.create("product.product");
        product.setName(name);
        
        return product;
    }
    
    /**
     * Create a product price margin with the specified value
     * 
     * @param margin
     *            the product price margin
     * @return ProductPrice            
     */
    private ProductPrice createPriceMargin(BigDecimal margin) {
        ProductPrice price = (ProductPrice)service.create("productPrice.margin");
        price.setPrice(margin);
        
        return price;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (IArchetypeService)applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Display the validation errors
     * 
     * @param exception
     *            the validation exception 
     */
    private void dump(ValidationException exception) {
        for (ValidationError error : exception.getErrors()) {
            logger.error("node: " + error.getNodeName()
                    + " message: " + error.getErrorMessage());
        }
    }
}
