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

// java-core
import java.util.Hashtable;

// commons-lang
import org.apache.commons.lang.StringUtils;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;

// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the
 * {@link org.openvpms.component.business.service.archetype.IArchetypeService}
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceTestCase extends BaseTestCase {

    /**
     * Reference to the archetype service
     */
    private ArchetypeService service;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceTestCase.class);
    }

    /**
     * Constructor for ArchetypeServiceTestCase.
     * 
     * @param name
     */
    public ArchetypeServiceTestCase(String name) {
        super(name);
    }

    /**
     * Test that we can successfully call createDefaultObject on every archetype
     * loaded in the registry
     */
    public void testCreateDefaultObject() throws Exception {
        for (ArchetypeDescriptor descriptor : service
                .getArchetypeDescriptors()) {
            assertTrue("Creating " + descriptor.getName(), service
                    .create(descriptor.getType()) != null);
        }
    }

    /**
     * Test create an instance of animal.pet
     */
    public void testCreationAnimalPet() throws Exception {
        Animal animal = (Animal) service.create("animal.pet");
        assertTrue(animal != null);
    }
    
    /**
     * Test that a node value for an {@link IMObject can be retrieved from a
     * {@link NodeDescriptor}
     */
    public void testGetValueFromNodeDescriptor() throws Exception {
        Person person = createPerson(null, "Mr", "Jim", "Alateras");

        NodeDescriptor ndesc = service.getArchetypeDescriptor(
                person.getArchetypeId()).getNodeDescriptor("description");
        assertTrue(ndesc.getValue(person).equals(
                person.getArchetypeId().getConcept()));

        ndesc = service.getArchetypeDescriptor(person.getArchetypeId())
                .getNodeDescriptor("name");
        assertTrue(ndesc.getValue(person).equals("Jim Alateras"));
    }

    /**
     * Test that default vcalues are assigned for lookups
     */
    public void testDefaultValuesForLookups() throws Exception {
        Contact contact = (Contact) service.create("contact.location");
        assertTrue(contact.getDetails().getAttribute("country") != null);
        assertTrue(contact.getDetails().getAttribute("country").equals(
                "Australia"));
        assertTrue(contact.getDetails().getAttribute("state") != null);
        assertTrue(contact.getDetails().getAttribute("state")
                .equals("Victoria"));
        assertTrue(StringUtils.isEmpty((String) contact.getDetails()
                .getAttribute("suburb")));
        assertTrue(StringUtils.isEmpty((String) contact.getDetails()
                .getAttribute("postCode")));

        Person person = (Person) service.create("person.person");
        assertTrue(person != null);
        assertTrue(person.getTitle().equals("Mr"));
    }
    
    /**
     * Test the creation of an archetype which defines a BigDecimal node
     */
    public void testOVPMS174() throws Exception {
        IMObject obj = service.create("productPrice.margin");
        assertTrue(obj != null);
    }
    
    /**
     * Test the validation of an archetype which defines a wildcard expression
     * for the short name of an archetype range assertion
     */
    public void testOVPMS197()
    throws Exception {
        Person person = createPerson("person.bernief", "Ms", "Bernadette", "Feeney");
        person.addIdentity(createEntityIdentity("entityIdentity.personAlias", "special"));
        service.validateObject(person);
    }
    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Hashtable params = getTestData().getGlobalParams();
        String assertionFile = (String) params.get("assertionFile");
        String dir = (String) params.get("dir");
        String extension = (String) params.get("extension");

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(dir,
                new String[] { extension }, assertionFile);
        service = new ArchetypeService(cache);
    }

    /**
     * This will create an entity identtiy with the specified identity
     * 
     * @param shortName
     *            the archetype
     * @param identity
     *            the identity to set
     * @return EntityIdentity
     * @throws Exception                       
     */
    private EntityIdentity createEntityIdentity(String shortName, String identity)
    throws Exception {
        EntityIdentity eid = (EntityIdentity) service.create(shortName);
        eid.setIdentity(identity);
        
        return eid;
    }

    /**
     * Create a person with the specified title, first name and last name
     * 
     * @param title
     *            the title of the person
     * @param firstName
     *            the firstname
     * @param lastName
     *            the last name            
     * @return Person
     * @throws Exception                       
     */
    private Person createPerson(String shortName, String title, String firstName, String lastName)
    throws Exception {
        Person person = null;
        if (shortName == null) {
            person = (Person) service.create("person.person");
        } else {
            person = (Person) service.create(shortName);
        }
        person.setTitle(title);
        person.setFirstName(firstName);
        person.setLastName(lastName);
        
        return person;
    }
}
