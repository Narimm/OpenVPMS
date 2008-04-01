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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.assertion;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Tests the {@link EntityRelationshipAssertions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipAssertionTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests the {@link EntityRelationshipAssertions} methods when invoked via
     * archetype service validation.
     */
    public void testValidation() {
        Party patient = (Party) service.create("party.patientpet");
        Party owner1 = (Party) service.create("party.customerperson");
        Party owner2 = (Party) service.create("party.customerperson");

        EntityBean bean = new EntityBean(patient);
        bean.setValue("name", "Foo");
        bean.setValue("species", "CANINE");

        service.validateObject(patient); // check validity

        EntityBean owner1Bean = new EntityBean(owner1);
        owner1Bean.addRelationship("entityRelationship.patientOwner", patient);

        service.validateObject(patient);  // should still be valid

        // add another active owner relationship. Should still be valid
        EntityBean owner2Bean = new EntityBean(owner2);
        owner2Bean.addRelationship("entityRelationship.patientOwner", patient);

        // now add another relationship to owner1. Validation should fail
        owner1Bean.addRelationship("entityRelationship.patientOwner", patient);

        try {
            service.validateObject(patient);
            fail("Expected validation to fail");
        } catch (ValidationException expected) {
            assertEquals(1, expected.getErrors().size());
        }
    }

    /**
     * Sets up the test.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Returns the spring application context paths.
     *
     * @return the spring application context paths
     */
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openvpms/component/business/service/archetype/archetype-service-appcontext.xml"
        };
    }
}
