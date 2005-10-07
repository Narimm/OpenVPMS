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

// spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

// openvpms-framework
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;

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
