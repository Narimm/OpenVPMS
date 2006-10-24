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

// java core

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.List;


/**
 * Lookup test cases.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupTestCase extends
                            AbstractDependencyInjectionSpringContextTests {

    /**
     * Holds a reference to the archetype service
     */
    private ArchetypeService service;


    /**
     * Default constructor
     */
    public LookupTestCase() {
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
     * Test that we can create an object through this service
     */
    public void testLookupObjectCreation()
            throws Exception {
        for (int index = 0; index < 5; index++) {
            Lookup lookup = (Lookup) service.create("lookup.country");
            assertTrue(lookup != null);

            // set to meet the archetype requirements
            lookup.setCode("AU-" + index);
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
        Lookup cty = createCountryLookup("Australia");
        Lookup state1 = createStateLookup("Victoria");
        service.save(state1);
        Lookup state2 = createStateLookup("NSW");
        service.save(state2);
        Lookup state3 = createStateLookup("Tasmania");
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
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("target");
        assertTrue(ndesc != null);
        List<Lookup> page = LookupHelper.getTargetLookups(service, cty,
                                                          new String[]{"lookup.state"});
        assertTrue(page.size() == 3);
    }

    /**
     * The the retrieval of look ups given a node descriptor
     */
    public void testLookupRetrievalFromNodeDescriptor()
            throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "person.person");
        assertTrue(descriptor.getNodeDescriptor("title") != null);
        assertTrue(descriptor.getNodeDescriptor("title").isLookup());
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor(
                "title")).size() == 7);
    }

    /**
     * Test for that the default indicator in the lookup model is working
     */
    public void testOBF43()
            throws Exception {
        // the case where no default value is specified
        Party person = (Party) service.create("person.footballer");
        assertFalse(StringUtils.isEmpty(
                (String) person.getDetails().getAttribute("team")));
        String team = (String) person.getDetails().getAttribute("team");
        assertTrue(team.equals("ST_KILDA"));

        // the case where a default value is specified
        person = (Party) service.create("person.newfootballer");
        assertFalse(StringUtils.isEmpty(
                (String) person.getDetails().getAttribute("team")));
        team = (String) person.getDetails().getAttribute("team");
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
        assertTrue(descriptor.getNodeDescriptor("country") != null);
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
        assertTrue(descriptor.getNodeDescriptor("country") != null);
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(LookupHelper.get(service,
                                    descriptor.getNodeDescriptor("country"),
                                    null).size() > 0);
    }

    /**
     * Test the target lookup up or constrained lookups for the address.location
     * archetype for country Australia
     */
    public void testConstrainedLookupRetrievalFromNodeDescriptor()
            throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "contact.location");
        Contact contact = (Contact) service.create(descriptor.getType());
        contact.getDetails().setAttribute("country", "Australia");
        assertTrue(LookupHelper.get(service,
                                    descriptor.getNodeDescriptor("state"),
                                    contact).size() > 0);
        contact.getDetails().setAttribute("country", "Tasmania");
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
                .setFirstRow(0)
                .setNumOfRows(ArchetypeQuery.ALL_ROWS);
        List<IMObject> objects = service.get(query).getRows();
        assertTrue(objects != null);
    }

    /**
     * Test OBF-46 bug report
     */
    public void testOBF46()
            throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(
                "horse.pet");
        assertTrue(descriptor != null);
        Party animal = (Party) service.create(descriptor.getType());
        assertTrue(animal != null);
        assertTrue(descriptor.getNodeDescriptor("breed") != null);
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
        assertTrue(adesc != null);
        NodeDescriptor ndesc = adesc.getNodeDescriptor("country");
        assertTrue(ndesc != null);
        assertTrue(
                StringUtils.isEmpty(LookupHelper.getUnspecifiedValue(ndesc)));
        ndesc = adesc.getNodeDescriptor("state");
        assertTrue(ndesc != null);
        assertTrue(LookupHelper.getUnspecifiedValue(ndesc).equals("other"));
    }

    /* (non-Javadoc)
    * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
    */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        this.service = (ArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * This will create a country lookup
     *
     * @param name the name of the country
     * @return Lookup
     */
    private Lookup createCountryLookup(String name) {
        Lookup country = (Lookup) service.create("lookup.country");
        country.setCode(name);

        return country;
    }

    /**
     * This will create a state lookup
     *
     * @param name the name of the state
     * @return Lookup
     */
    private Lookup createStateLookup(String name) {
        Lookup state = (Lookup) service.create("lookup.state");
        state.setCode(name);

        return state;
    }

    /**
     * Create an lookup relationship
     *
     * @param type   the type of relationship
     * @param source the source relationship
     * @param target the target relationship
     * @return LookupRelationship
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
