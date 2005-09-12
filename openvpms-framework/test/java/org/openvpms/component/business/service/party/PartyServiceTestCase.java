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

package org.openvpms.component.business.service.party;

// spring-context
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//openvpms-framework
import org.openvpms.component.business.domain.im.party.Party;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PartyServiceTestCase extends
        AbstractDependencyInjectionSpringContextTests {
    
    /**
     * Holds a reference to the party service
     */
    private PartyService partyService;
    

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PartyServiceTestCase.class);
    }

    /**
     * Default constructor
     */
    public PartyServiceTestCase() {
    }

    /**
     * @param partyService The partyService to set.
     */
    public void setPartyService(PartyService partyService) {
        this.partyService = partyService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    @Override
    protected String[] getConfigLocations() {
        return new String[] { 
                "org/openvpms/component/business/service/party/party-service-appcontext.xml" 
                };
    }

    /**
     * Test that we can create an object through this service
     */
    public void testPartyObjectCreation()
    throws Exception {
        for (int index = 0; index < 5; index++) {
            Party party = partyService.createParty("person.person");
            assertTrue(party != null);
            
            // insert the party object
            partyService.insertParty(party);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        this.partyService = (PartyService)applicationContext.getBean(
                "partyService");
    }

}
