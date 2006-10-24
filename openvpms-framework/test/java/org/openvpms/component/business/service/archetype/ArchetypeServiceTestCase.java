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
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.FailedToDeriveValueException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheFS;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.test.BaseTestCase;

import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.Properties;

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
        Party animal = (Party)service.create("animal.pet");
        assertTrue(animal != null);
    }

    /**
     * Test that a node value for an {@link IMObject can be retrieved from a
     * {@link NodeDescriptor}
     */
    public void testGetValueFromNodeDescriptor() throws Exception {
        Party person = createPerson(null, "MR", "Jim", "Alateras");

        NodeDescriptor ndesc = service.getArchetypeDescriptor(
                person.getArchetypeId()).getNodeDescriptor("description");
        assertTrue(ndesc.getValue(person).equals("Mr Jim  Alateras"));

        ndesc = service.getArchetypeDescriptor(person.getArchetypeId())
                .getNodeDescriptor("name");
        assertTrue(ndesc.getValue(person).equals("Alateras,Jim"));
    }

    /**
     * Test that default vcalues are assigned for lookups
     */
    public void testDefaultValuesForLookups() throws Exception {
        Contact contact = (Contact) service.create("contact.location");
        assertEquals("AU", contact.getDetails().getAttribute("country"));
        assertEquals("VIC", contact.getDetails().getAttribute("state"));
        assertTrue(StringUtils.isEmpty((String) contact.getDetails()
                .getAttribute("suburb")));
        assertTrue(StringUtils.isEmpty((String) contact.getDetails()
                .getAttribute("postCode")));

        Party person = createPerson("person.person", "MR", "Jim", "Alateras");
        assertEquals("MR", person.getDetails().getAttribute("title"));
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
        Party person = createPerson("person.bernief", "MS", "Bernadette",
                                    "Feeney");
        person.addIdentity(
                createEntityIdentity("entityIdentity.personAlias", "special"));
        service.validateObject(person);
    }

    /**
     * Test that a service call to deriveValues works as expected
     */
    public void testDeriveValues()
    throws Exception {
        Lookup country = (Lookup)service.create("lookup.country");
        country.setCode("AU");
        country.setName("Australia");
        assertTrue(StringUtils.isEmpty(country.getDescription()));

        service.deriveValues(country);
        assertFalse(StringUtils.isEmpty(country.getDescription()));
    }

    /**
     * Test that the use of derive values through the node descriptor and
     * the archetype service.
     */
    public void testDeriveValue()
            throws Exception {
        Lookup country = (Lookup) service.create("lookup.country");
        country.setCode("AU");
        country.setName("Australia");
        assertTrue(StringUtils.isEmpty(country.getDescription()));

        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                country.getArchetypeId());
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("description");
        assertNotNull(ndesc);

        // through node descriptor
        ndesc.deriveValue(country);
        assertEquals("Australia AU", country.getDescription());

        // through archetype service
        country.setDescription(null);
        service.deriveValue(country, "description");
        assertFalse(StringUtils.isEmpty(country.getDescription()));
    }

    /**
     * Test that deriveValue throws exception when an invalid node
     * is specified (i.e. a node that does not support derive value}.
     */
    public void testFailedToDeriveValue()
    throws Exception {
        Lookup country = (Lookup)service.create("lookup.country");
        country.setCode("AU");
        assertTrue(StringUtils.isEmpty(country.getDescription()));

        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                country.getArchetypeId());
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("name");
        assertNotNull(ndesc);

        // through node descriptor
        try {
            ndesc.deriveValue(country);
            fail("The node [name] does not support derived value");
        } catch (FailedToDeriveValueException exception) {
            // this is expected
        }

        // through archetype service
        try {
            service.deriveValue(country, "name");
            fail("The node [name] does not support derived value");
        } catch (FailedToDeriveValueException exception) {
            // this is expected
        }
    }

    /**
     * Test that we can only set the value of a node if it is of the same type
     * or if the supplied value can be co-erced into the same type
     */
    public void testOBF49()
    throws Exception {
        IMObject margin = service.create("productPrice.margin");
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(margin.getArchetypeId());
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("margin");
        assertTrue(ndesc != null);

        // set with incorrect type
        ndesc.setValue(margin, 10);
        assertTrue(ndesc.getValue(margin) instanceof BigDecimal);

        // set with correct type
        ndesc.setValue(margin, new BigDecimal(10));
        assertTrue(ndesc.getValue(margin) instanceof BigDecimal);
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

        IArchetypeDescriptorCache cache = new ArchetypeDescriptorCacheFS(
                dir,new String[] { extension }, assertionFile);
        service = new ArchetypeService(cache);

        // set up the jxpath functions
        Properties properties = new Properties();
        properties.put("openvpms", ArchetypeServiceFunctions.class.getName());
        new JXPathHelper(properties);

        // set up the archetype service helper
        new ArchetypeServiceHelper(service);
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
    private Party createPerson(String shortName, String title, String firstName, String lastName)
    throws Exception {
        Party person;
        if (shortName == null) {
            person = (Party) service.create("person.person");
        } else {
            person = (Party) service.create(shortName);
        }
        person.getDetails().setAttribute("lastName", lastName);
        person.getDetails().setAttribute("firstName", firstName);
        person.getDetails().setAttribute("title", title);

        return person;
    }
}
