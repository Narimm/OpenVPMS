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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Lookup test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
@SuppressWarnings("HardCodedStringLiteral")
public class LookupTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private ArchetypeService service;


    /**
     * Test that we can create an object through this service.
     */
    public void testLookupObjectCreation()
            throws Exception {
        for (int index = 0; index < 5; index++) {
            Lookup lookup = (Lookup) service.create("lookup.country");
            assertNotNull(lookup);

            // make sure the code is unique
            lookup.setCode("AU-" + System.currentTimeMillis());
            lookup.setName("Australia");

            // insert the lookup object
            service.save(lookup);
        }
    }

    /**
     * Tests that the lookup name is derived from the code if unset.
     */
    public void testDefaultName() {
        Lookup lookup = (Lookup) service.create("lookup.country");
        assertNull(lookup.getName());

        lookup.setCode("AUSTRALIA");
        assertEquals("Australia", lookup.getName());

        lookup.setCode("UNITED_KINGDOM");
        assertEquals("United Kingdom", lookup.getName());
    }

    /**
     * Test the target lookup retrievals given a source
     */
    public void testGetTargetLookups() {
        // create the country and states and relationships
        Lookup cty = createCountryLookup("AU");
        Lookup state1 = createStateLookup("VIC");
        service.save(state1);
        Lookup state2 = createStateLookup("NSW");
        service.save(state2);
        Lookup state3 = createStateLookup("TAS");
        service.save(state3);
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state1));
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state2));
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state3));
        service.save(cty);

        // retrieve the 
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                cty.getArchetypeId());
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
    public void testLookupRetrievalFromNodeDescriptor()
            throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "party.person");
        assertNotNull(descriptor.getNodeDescriptor("title"));
        assertTrue(descriptor.getNodeDescriptor("title").isLookup());
        assertEquals(7, LookupHelper.get(service, descriptor.getNodeDescriptor(
                "title")).size());
    }

    /**
     * Test for that the default indicator in the lookup model is working
     */
    public void testOBF43()
            throws Exception {
        // the case where no default value is specified
        Party person = (Party) service.create("party.personfootballer");
        assertFalse(StringUtils.isEmpty(
                (String) person.getDetails().get("team")));
        String team = (String) person.getDetails().get("team");
        assertTrue(team.equals("ST_KILDA"));

        // the case where a default value is specified
        person = (Party) service.create("party.personnewfootballer");
        assertFalse(StringUtils.isEmpty(
                (String) person.getDetails().get("team")));
        team = (String) person.getDetails().get("team");
        assertTrue(team.equals("RICHMOND"));
    }

    /**
     * Test the lookup using a concept name using the country node defined in
     * the address.location archetype
     */
    public void testDatabaseLookupRetrievalFromNodeDescriptor()
            throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "contact.location");
        assertNotNull(descriptor.getNodeDescriptor("country"));
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor(
                "country")).size() > 0);
    }

    /**
     * Test the lookup using the same a differentr call
     */
    public void testDatabaseLookupRetrievalFromNodeDescriptor2()
            throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "contact.location");
        assertNotNull(descriptor.getNodeDescriptor("country"));
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(LookupHelper.get(service,
                                    descriptor.getNodeDescriptor("country"),
                                    (IMObject) null).size() > 0);
    }

    /**
     * Test the target lookup up or constrained lookups for the address.location
     * archetype for country Australia
     */
    public void testConstrainedLookupRetrievalFromNodeDescriptor()
            throws Exception {
        Lookup cty = createCountryLookup("AU");
        Lookup state = createStateLookup("VIC");
        cty.addLookupRelationship(createLookupRelationship(
                "lookupRelationship.countryState", cty, state));
        service.save(Arrays.asList(state, cty));

        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "contact.location");
        Contact contact = (Contact) service.create(descriptor.getType());
        contact.getDetails().put("country", cty.getCode());
        assertTrue(LookupHelper.get(service,
                                    descriptor.getNodeDescriptor("state"),
                                    contact).size() > 0);
        contact.getDetails().put("country", "TAS");
        assertTrue(LookupHelper.get(service,
                                    descriptor.getNodeDescriptor("state"),
                                    contact).size() == 0);
    }

    /**
     * Test for OVPMS-195
     */
    public void testOVPMS195()
            throws Exception {
        ArchetypeQuery query = new ArchetypeQuery(
                new String[]{"lookupRelationship.common"}, true, true)
                .setFirstResult(0)
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List<IMObject> objects = service.get(query).getResults();
        assertNotNull(objects);
    }

    /**
     * Test OBF-46 bug report.
     */
    public void testOBF46()
            throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "party.horsepet");
        assertNotNull(descriptor);
        Party animal = (Party) service.create(descriptor.getType());
        assertNotNull(animal);
        assertNotNull(descriptor.getNodeDescriptor("breed"));
        assertTrue(descriptor.getNodeDescriptor("breed").isLookup());
        assertTrue(LookupHelper.get(service,
                                    descriptor.getNodeDescriptor("breed"),
                                    animal).size() > 0);
    }

    /**
     * Test that OBF-15 has been resolved
     */
    public void testOBF15()
            throws Exception {
        ArchetypeDescriptor adesc = service.getArchetypeDescriptor(
                "contact.location");
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

        service.save(objects);

        // verify the id's have updated
        for (IMObject object : objects) {
            assertFalse(object.getId() == -1);
        }
    }

    /* (non-Javadoc)
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
    */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/lookup/lookup-service-appcontext.xml"
        };
    }

    /**
     * Creates a country lookup.
     *
     * @param code the country code
     * @return a new lookup
     */
    private Lookup createCountryLookup(String code) {
        return LookupUtil.createLookup(service, "lookup.country", code);
    }

    /**
     * Creates a state lookup.
     *
     * @param code the state code
     * @return a new lookup
     */
    private Lookup createStateLookup(String code) {
        return LookupUtil.createLookup(service, "lookup.state", code);
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
        LookupRelationship rel = (LookupRelationship) service.create(type);
        rel.setSource(source.getObjectReference());
        rel.setTarget(target.getObjectReference());

        return rel;
    }
}
