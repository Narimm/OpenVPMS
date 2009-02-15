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
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

/**
 * Test the
 * {@link org.openvpms.component.business.service.archetype.IArchetypeService}
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class ArchetypeServiceTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Creates a new <tt>ArchetypeServiceTestCase</tt>.
     */
    public ArchetypeServiceTestCase() {
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
     * Test create an instance of party.animalpet.
     */
    public void testCreationAnimalPet() throws Exception {
        Party animal = (Party) service.create("party.animalpet");
        assertNotNull(animal);
    }

    /**
     * Test that a node value for an {@link IMObject can be retrieved from a
     * {@link NodeDescriptor}
     */
    public void testGetValueFromNodeDescriptor() throws Exception {
        Party person = createPerson("party.person", "MR", "Jim", "Alateras");

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
        assertEquals("AU", contact.getDetails().get("country"));
        assertEquals("VIC", contact.getDetails().get("state"));
        assertTrue(StringUtils.isEmpty(
                (String) contact.getDetails().get("suburb")));
        assertTrue(StringUtils.isEmpty(
                (String) contact.getDetails().get("postCode")));

        Party person = createPerson("party.person", "MR", "Jim", "Alateras");
        assertEquals("MR", person.getDetails().get("title"));
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
        Party person = createPerson("party.personbernief", "MS", "Bernadette",
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
        Lookup country = (Lookup) service.create("lookup.country");
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
        Lookup country = (Lookup) service.create("lookup.country");
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
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                margin.getArchetypeId());
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

    /**
     * Verifies that the default lookup.staff is assigned to a
     * party.customerperson on its creation, via the openvpms:defaultLookup()
     * function.
     */
    public void testDefaultLookup() {
        // ensure existing lookups are non-default and create some new ones,
        // making the last one created the default.
        Collection<Lookup> lookups
                = LookupServiceHelper.getLookupService().getLookups(
                "lookup.staff");
        for (Lookup lookup : lookups) {
            if (lookup.isDefaultLookup()) {
                lookup.setDefaultLookup(false);
                service.save(lookup);
            }
        }
        Lookup lookup = null;
        for (int i = 0; i < 10; ++i) {
            lookup = (Lookup) service.create("lookup.staff");
            lookup.setCode("CODE" + System.nanoTime());
            lookup.setName(lookup.getCode());
            lookup.setDescription(lookup.getCode());
            service.save(lookup);
        }
        assertNotNull(lookup);
        lookup.setDefaultLookup(true);
        service.save(lookup);

        Party party = createPerson("party.customerperson", "MR", "T",
                                   "Anderson");
        Set<Lookup> classifications = party.getClassifications();
        assertEquals(1, classifications.size());
        assertTrue(classifications.contains(lookup));
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

    @Override
    protected void onSetUp() throws Exception {
        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
    }


    /**
     * This will create an entity identtiy with the specified identity
     *
     * @param shortName the archetype
     * @param identity  the identity to set
     * @return EntityIdentity
     * @throws Exception
     */
    private EntityIdentity createEntityIdentity(String shortName,
                                                String identity)
            throws Exception {
        EntityIdentity eid = (EntityIdentity) service.create(shortName);
        eid.setIdentity(identity);
        return eid;
    }

    /**
     * Create a person with the specified title, first name and last name
     *
     * @param title     the title of the person
     * @param firstName the firstname
     * @param lastName  the last name
     * @return a new party
     */
    private Party createPerson(String shortName, String title, String firstName,
                               String lastName) {
        Party person = (Party) service.create(shortName);
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }
}
