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

// commons-lang
import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;

// openvpms-framework
import org.openvpms.component.business.dao.hibernate.im.entity.IMObjectDAOHibernate;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.PropertyDescriptor;
import org.openvpms.component.business.domain.im.party.Address;
import org.openvpms.component.business.domain.im.party.Animal;
import org.openvpms.component.business.domain.im.party.Person;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheDB;

// openvpms-test-component
import org.openvpms.component.system.common.test.BaseTestCase;

/**
 * Test the archetype service, which is baked by a database cache
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeServiceWithDBCacheTestCase extends BaseTestCase {

    /**
     * Reference to the archetype service
     */
    private ArchetypeService service;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ArchetypeServiceWithDBCacheTestCase.class);
    }

    /**
     * Constructor for ArchetypeServiceTestCase.
     * 
     * @param name
     */
    public ArchetypeServiceWithDBCacheTestCase(String name) {
        super(name);
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
        Person person = (Person) service.create("person.person");
        person.setTitle("Mr");
        person.setFirstName("Jim");
        person.setLastName("Alateras");

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
        Address address = (Address) service
                .create("address.location");
        assertTrue(address.getDetails().getAttribute("country") != null);
        assertTrue(address.getDetails().getAttribute("country").equals(
                "Australia"));
        assertTrue(address.getDetails().getAttribute("state") != null);
        assertTrue(address.getDetails().getAttribute("state")
                .equals("Victoria"));
        assertTrue(StringUtils.isEmpty((String) address.getDetails()
                .getAttribute("suburb")));
        assertTrue(StringUtils.isEmpty((String) address.getDetails()
                .getAttribute("postCode")));

        Person person = (Person) service.create("person.person");
        assertTrue(person != null);
        assertTrue(person.getTitle().equals("Mr"));
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(ArchetypeDescriptor.class);
        config.addClass(NodeDescriptor.class);
        config.addClass(AssertionDescriptor.class);
        config.addClass(PropertyDescriptor.class);
        config.addClass(AssertionTypeDescriptor.class);
        
        // create the session factory
        IMObjectDAOHibernate dao = new IMObjectDAOHibernate();
        dao.setSessionFactory(config.buildSessionFactory());
        
        // instantiate a DB cache
        service = new ArchetypeService(new ArchetypeDescriptorCacheDB(dao));
    }
}
