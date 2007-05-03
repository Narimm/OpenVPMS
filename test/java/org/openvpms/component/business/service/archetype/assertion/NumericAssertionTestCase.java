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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.assertion;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * Tests the {@link NumericAssertions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NumericAssertionTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests the {@link NumericAssertions} methods.
     */
    public void testNumericAssertions() {
        assertTrue(NumericAssertions.negative(-1, null, null));
        assertFalse(NumericAssertions.negative(0, null, null));
        assertFalse(NumericAssertions.negative(1, null, null));

        assertFalse(NumericAssertions.positive(-1, null, null));
        assertFalse(NumericAssertions.positive(0, null, null));
        assertTrue(NumericAssertions.positive(1, null, null));

        assertFalse(NumericAssertions.nonNegative(-1, null, null));
        assertTrue(NumericAssertions.nonNegative(0, null, null));
        assertTrue(NumericAssertions.nonNegative(1, null, null));
    }

    /**
     * Tests the {@link NumericAssertions} methods when invoked via
     * archetype service validation.
     */
    public void testNumericAssertionsValidation() {
        Act act = (Act) service.create("act.numericAssertions");
        assertNotNull(act);
        service.validateObject(act);  // default values are all valid

        act.getDetails().put("positive", 0.0);
        act.getDetails().put("negative", 0.0);
        act.getDetails().put("nonNegative", -1.0);
        try {
            service.validateObject(act);
            fail("Expected validation to fail");
        } catch (ValidationException expected) {
            // one error per node
            assertEquals(3, expected.getErrors().size());
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
