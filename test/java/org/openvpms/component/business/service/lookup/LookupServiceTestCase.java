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
import java.util.List;

// spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupServiceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(LookupServiceTestCase.class);
    
    
    /**
     * Holds a reference to the archetype service
     */
    private ArchetypeService service;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LookupServiceTestCase.class);
    }

    /**
     * Default constructor
     */
    public LookupServiceTestCase() {
    }

    /**
     * @param archetypeService The archetypeService to set.
     */
    public void setArchetypeService(ArchetypeService archetypeService) {
        this.service = archetypeService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/lookup/lookup-service-appcontext.xml" 
                };
    }

    /**
     * Test that we can create an object through this service
     */
    public void testLookupObjectCreation()
    throws Exception {
        for (int index = 0; index < 5; index++) {
            Lookup lookup = (Lookup)service.create("lookup.country");
            assertTrue(lookup != null);
            
            // set to meet the archetype requirements
            lookup.setValue("Autralia-" + index);
            lookup.setCode("AU-" + index);
            
            // insert the lookup object
            service.save(lookup);
        }
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
        cty.addLookupRelationship(createLookupRelationship(cty, state1));
        cty.addLookupRelationship(createLookupRelationship(cty, state2));
        cty.addLookupRelationship(createLookupRelationship(cty, state3));
        service.save(cty);
        
        // retrieve all the lookups
        IPage<IMObject> page = ArchetypeQueryHelper.getTagetLookups(service, 
                cty, "lookup.state", 0, -1);
        assertTrue(page.getRows().size() == 3);

        // retrueve all the source lookups
        page = ArchetypeQueryHelper.getSourceLookups(service, state1, 
                "lookup.country", 0, 1);
        assertTrue(page.getTotalNumOfRows() == 1);
        assertTrue(page.getRows().iterator().next().getLinkId().equals(cty.getLinkId()));
    }
    
    /**
     * The the retrieval of look ups given a node descriptor 
     */
    public void testLookupRetrievalFromNodeDescriptor()
    throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor("person.person");
        assertTrue(descriptor.getNodeDescriptor("title") != null);
        assertTrue(descriptor.getNodeDescriptor("title").isLookup());
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("title")).size() == 7);
    }
    
    /**
     * Test the lookup using a concept name using the country node defined in
     * the address.location archetype
     */
    public void testDatabaseLookupRetrievalFromNodeDescriptor()
    throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor("contact.location");
        assertTrue(descriptor.getNodeDescriptor("country") != null);
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("country")).size() > 0);
    }
    
    /**
     * Test the lookup using the same a differentr call
     */
    public void testDatabaseLookupRetrievalFromNodeDescriptor2()
    throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor("contact.location");
        assertTrue(descriptor.getNodeDescriptor("country") != null);
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("country"), null).size() > 0);
    }
    
    /**
     * Test the target lookup up or constrained lookups for the address.location
     * archetype for country Australia
     */
    public void testConstrainedLookupRetrievalFromNodeDescriptor()
    throws Exception {
        ArchetypeDescriptor descriptor = service.getArchetypeDescriptor("contact.location");
        Contact contact = (Contact)service.create(descriptor.getType());
        contact.getDetails().setAttribute("country", "Australia");
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("state"), contact).size() > 0);
        contact.getDetails().setAttribute("country", "Tasmania");
        assertTrue(LookupHelper.get(service, descriptor.getNodeDescriptor("state"), contact).size() == 0);
    }
    
    /**
     * Test for OVPMS-195
     */
    public void testOVPMS195()
    throws Exception {
        ArchetypeQuery query = new ArchetypeQuery(
                new String[]{"lookuprel.common"}, true, true)
                .setFirstRow(0)
                .setNumOfRows(ArchetypeQuery.ALL_ROWS);
        List<IMObject> objects = service.get(query).getRows();
        assertTrue(objects != null);
    }
    
    /**
     * Test retrieval by short nam using
     */
    
    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.service = (ArchetypeService)applicationContext.getBean(
            "archetypeService");
    }

    /**
     * This will create a country lookup
     * 
     * @param name
     *            the name of the country
     * @return Lookup
     */
    private Lookup createCountryLookup(String name) {
        Lookup country = (Lookup)service.create("lookup.country");
        country.setValue(name);
        
        return country;
    }
    
    /**
     * This will create a state lookup
     * 
     * @param name
     *            the name of the state
     * @return Lookup            
     */
    private Lookup createStateLookup(String name) {
        Lookup state = (Lookup)service.create("lookup.state");
        state.setValue(name);
        
        return state;
    }
    
    /**
     * Create an lookup relationship
     * 
     * @param source
     *            the source relationship
     * @param target
     *            the target relationship
     * @return LookupRelationship                        
     */
     private LookupRelationship createLookupRelationship(Lookup source, Lookup target) {
         LookupRelationship rel = (LookupRelationship)service.create("lookuprel.common");
         rel.setSource(source.getObjectReference());
         rel.setTarget(target.getObjectReference());
         
         return rel;
     }
}
