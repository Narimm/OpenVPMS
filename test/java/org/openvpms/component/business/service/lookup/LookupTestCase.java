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

package org.openvpms.component.business.service.lookup;

import org.apache.commons.lang.StringUtils;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Lookup test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@ContextConfiguration("lookup-service-appcontext.xml")
public class LookupTestCase extends AbstractArchetypeServiceTest {


    /**
     * Test that we can create an object through this service.
     */
    @Test
    public void testLookupObjectCreation() {
        for (int index = 0; index < 5; index++) {
            Lookup lookup = (Lookup) create("lookup.country");

            // make sure the code is unique
            lookup.setCode("AU-" + System.nanoTime());
            lookup.setName("Australia");

            // insert the lookup object
            save(lookup);
        }
    }

    /**
     * Tests that the lookup name is derived from the code if unset.
     */
    @Test
    public void testDefaultName() {
        Lookup lookup = (Lookup) create("lookup.country");
        assertNull(lookup.getName());

        lookup.setCode("AUSTRALIA");
        assertEquals("Australia", lookup.getName());

        lookup.setCode("UNITED_KINGDOM");
        assertEquals("United Kingdom", lookup.getName());
    }

    /**
     * Test the target lookup retrievals given a source
     */
    @Test
    public void testGetTargetLookups() {
        // create the country and states and relationships
        Lookup cty = createCountryLookup("AU");
        Lookup state1 = createStateLookup("VIC");
        save(state1);
        Lookup state2 = createStateLookup("NSW");
        save(state2);
        Lookup state3 = createStateLookup("TAS");
        save(state3);
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state1));
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state2));
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state3));
        save(cty);

        // retrieve the 
        ArchetypeDescriptor adesc = getArchetypeDescriptor(cty.getArchetypeId().getShortName());
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("target");
        assertNotNull(ndesc);
        Collection<Lookup> lookups
                = LookupServiceHelper.getLookupService().getTargetLookups(cty);
        assertEquals(3, lookups.size());
    }

    /**
     * The the retrieval of look ups given a node descriptor
     */
    @Test
    public void testLookupRetrievalFromNodeDescriptor() {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor("party.person");
        assertNotNull(descriptor.getNodeDescriptor("title"));
        assertTrue(descriptor.getNodeDescriptor("title").isLookup());
        assertEquals(7, LookupHelper.get(getArchetypeService(), descriptor.getNodeDescriptor(
                "title")).size());
    }

    /**
     * Test for that the default indicator in the lookup model is working
     */
    @Test
    public void testOBF43() {
        // the case where no default value is specified on the node, but
        // a default lookup is
        Lookup lookup = LookupUtil.getLookup(getArchetypeService(), "lookup.afl", "ST_KILDA");
        lookup.setDefaultLookup(true);
        save(lookup);
        Party person = (Party) create("party.personfootballer");
        assertEquals("ST_KILDA", person.getDetails().get("team"));

        // the case where a default value is specified
        person = (Party) create("party.personnewfootballer");
        assertEquals("RICHMOND", person.getDetails().get("team"));
    }

    /**
     * Test the lookup using a concept name using the country node defined in
     * the address.location archetype
     */
    @Test
    public void testDatabaseLookupRetrievalFromNodeDescriptor() {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor("contact.location");
        assertNotNull(descriptor.getNodeDescriptor("country"));
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(LookupHelper.get(getArchetypeService(), descriptor.getNodeDescriptor(
                "country")).size() > 0);
    }

    /**
     * Test the lookup using the same a differentr call
     */
    @Test
    public void testDatabaseLookupRetrievalFromNodeDescriptor2() {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor("contact.location");
        assertNotNull(descriptor.getNodeDescriptor("country"));
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(LookupHelper.get(getArchetypeService(),
                                    descriptor.getNodeDescriptor("country"),
                                    (IMObject) null).size() > 0);
    }

    /**
     * Test the target lookup up or constrained lookups for the address.location
     * archetype for country Australia
     */
    @Test
    public void testConstrainedLookupRetrievalFromNodeDescriptor() {
        Lookup cty = createCountryLookup("AU");
        Lookup state = createStateLookup("VIC");
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state));
        save(Arrays.asList(state, cty));

        ArchetypeDescriptor descriptor = getArchetypeDescriptor(
                "contact.location");
        Contact contact = (Contact) create(descriptor.getType().getShortName());
        contact.getDetails().put("country", cty.getCode());
        IArchetypeService service = getArchetypeService();
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("state"), contact).size() > 0);
        contact.getDetails().put("country", "TAS");
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("state"), contact).size() == 0);
    }

    /**
     * Test for OVPMS-195.
     */
    @Test
    public void testOVPMS195() {
        ArchetypeQuery query = new ArchetypeQuery(
                new String[]{"lookupRelationship.common"}, true, true)
                .setFirstResult(0)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> objects = getArchetypeService().get(query).getResults();
        assertNotNull(objects);
    }

    /**
     * Test OBF-46 bug report.
     */
    @Test
    public void testOBF46() {
        ArchetypeDescriptor descriptor = getArchetypeDescriptor("party.horsepet");
        assertNotNull(descriptor);

        Lookup equine = LookupUtil.getLookup(getArchetypeService(), "lookup.species", "EQUINE");

        // make sure there is at least 1 equine breed
        IArchetypeService service = getArchetypeService();
        LookupUtil.getLookup(service, "lookup.breed", "ARAB", equine, "lookupRelationship.speciesBreed");

        Party animal = (Party) create(descriptor.getType().getShortName());
        assertNotNull(animal);
        assertNotNull(descriptor.getNodeDescriptor("breed"));
        assertTrue(descriptor.getNodeDescriptor("breed").isLookup());

        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("breed"), animal).size() > 0);
    }

    /**
     * Test that OBF-15 has been resolved
     */
    @Test
    public void testOBF15() {
        ArchetypeDescriptor adesc = getArchetypeDescriptor("contact.location");
        assertNotNull(adesc);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("country");
        assertNotNull(ndesc);
        assertTrue(
                StringUtils.isEmpty(LookupHelper.getUnspecifiedValue(ndesc)));
        ndesc = adesc.getNodeDescriptor("state");
        assertNotNull(ndesc);
        assertEquals("other", LookupHelper.getUnspecifiedValue(ndesc));
    }

    /**
     * Verifies that multiple lookups and their relationships can be saved via
     * the {@link IArchetypeService#save(Collection<IMObject>)} method.
     */
    @Test
    public void testSaveCollection() {
        // create the country and states and relationships
        Lookup cty = createCountryLookup("AU");
        Lookup state1 = createStateLookup("VIC");
        Lookup state2 = createStateLookup("NSW");
        Lookup state3 = createStateLookup("TAS");
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state1));
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state2));
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state3));
        List<IMObject> objects = Arrays.asList((IMObject) cty, state1, state2,
                                               state3);

        // verify the initial id
        for (IMObject object : objects) {
            assertEquals(-1, object.getId());
        }

        save(objects);

        // verify the id's have updated
        for (IMObject object : objects) {
            assertFalse(object.getId() == -1);
        }
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        LookupService lookups = new LookupService(getArchetypeService(),
                                                  (IMObjectDAO) applicationContext.getBean("imObjectDao"));
        new LookupServiceHelper(lookups);
    }

    /**
     * Creates a country lookup.
     *
     * @param code the country code
     * @return a new lookup
     */
    private Lookup createCountryLookup(String code) {
        return LookupUtil.createLookup(getArchetypeService(), "lookup.country", code);
    }

    /**
     * Creates a state lookup.
     *
     * @param code the state code
     * @return a new lookup
     */
    private Lookup createStateLookup(String code) {
        return LookupUtil.createLookup(getArchetypeService(), "lookup.state", code);
    }

    /**
     * Creates a lookup relationship.
     *
     * @param type   the type of relationship
     * @param source the source relationship
     * @param target the target relationship
     * @return a new lookup relationship
     */
    private LookupRelationship createLookupRelationship(String type,
                                                        Lookup source,
                                                        Lookup target) {
        LookupRelationship rel = (LookupRelationship) create(type);
        rel.setSource(source.getObjectReference());
        rel.setTarget(target.getObjectReference());

        return rel;
    }
}
