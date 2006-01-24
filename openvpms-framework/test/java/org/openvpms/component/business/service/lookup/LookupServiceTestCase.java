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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.service.archetype.ArchetypeService;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupServiceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    
    /**
     * Holds a reference to the lookup service
     */
    private LookupService lookupService;
    
    /**
     * Holds a reference to the archetype service
     */
    private ArchetypeService archetypeService;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LookupServiceTestCase.class);
    }

    /**
     * Default constructor
     */
    public LookupServiceTestCase() {
    }

    /**
     * @param partyService The lookupService to set.
     */
    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    /**
     * @param archetypeService The archetypeService to set.
     */
    public void setArchetypeService(ArchetypeService archetypeService) {
        this.archetypeService = archetypeService;
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
            Lookup lookup = lookupService.create("lookup.country");
            assertTrue(lookup != null);
            
            // set to meet the archetype requirements
            lookup.setValue("Autralia-" + index);
            lookup.setCode("AU-" + index);
            
            // insert the lookup object
            lookupService.insert(lookup);
        }
    }

    /**
     * Test the retrieval of lookups. This test assumes that the database is
     * loaded with stuff
     */
    public void testLookupRetrievalByString() {
        List<Lookup> results = lookupService.getTargetLookups("country.state", "Australia");
        assertTrue(results.size() > 0);
        results = lookupService.getSourceLookups("country.state", "Victoria");
        assertTrue(results.size() > 0);
    }
    
    /**
     * The the retrieval of look ups given a node descriptor 
     */
    public void testLookupRetrievalFromNodeDescriptor()
    throws Exception {
        ArchetypeDescriptor descriptor = archetypeService
            .getArchetypeDescriptor("person.person");
        assertTrue(descriptor.getNodeDescriptor("title") != null);
        assertTrue(descriptor.getNodeDescriptor("title").isLookup());
        assertTrue(lookupService.get(descriptor.getNodeDescriptor("title")).size() == 4);
    }
    
    /**
     * Test the lookup using a concept name using the country node defined in
     * the address.location archetype
     */
    public void testDatabaseLookupRetrievalFromNodeDescriptor()
    throws Exception {
        ArchetypeDescriptor descriptor = archetypeService
            .getArchetypeDescriptor("contact.location");
        assertTrue(descriptor.getNodeDescriptor("country") != null);
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(lookupService.get(descriptor.getNodeDescriptor("country")).size() > 0);
    }
    
    /**
     * Test the lookup using the same a differentr call
     */
    public void testDatabaseLookupRetrievalFromNodeDescriptor2()
    throws Exception {
        ArchetypeDescriptor descriptor = archetypeService
            .getArchetypeDescriptor("contact.location");
        assertTrue(descriptor.getNodeDescriptor("country") != null);
        assertTrue(descriptor.getNodeDescriptor("country").isLookup());
        assertTrue(lookupService.get(descriptor.getNodeDescriptor("country"), null).size() > 0);
    }
    
    /**
     * Test the target lookup up or constrained lookups for the address.location
     * archetype for country Australia
     */
    public void testConstrainedLookupRetrievalFromNodeDescriptor()
    throws Exception {
        ArchetypeDescriptor descriptor = archetypeService
            .getArchetypeDescriptor("contact.location");
        Contact contact = (Contact)archetypeService.create(
            descriptor.getType());
        contact.getDetails().setAttribute("country", "Australia");
        assertTrue(lookupService.get(descriptor.getNodeDescriptor("state"), contact).size() > 0);
        contact.getDetails().setAttribute("country", "Tasmania");
        assertTrue(lookupService.get(descriptor.getNodeDescriptor("state"), contact).size() == 0);
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
        
        this.lookupService = (LookupService)applicationContext.getBean(
            "lookupService");
        this.archetypeService = (ArchetypeService)applicationContext.getBean(
            "archetypeService");
    }

}
