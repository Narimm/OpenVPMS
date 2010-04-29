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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionContext;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.springframework.test.context.ContextConfiguration;


/**
 * Tests the {@link NumericAssertions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class NumericAssertionTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link NumericAssertions} methods.
     */
    @Test
    public void testNumericAssertions() {
        assertTrue(NumericAssertions.negative(create(-1)));
        assertFalse(NumericAssertions.negative(create(0)));
        assertFalse(NumericAssertions.negative(create(1)));

        assertFalse(NumericAssertions.positive(create(-1)));
        assertFalse(NumericAssertions.positive(create(0)));
        assertTrue(NumericAssertions.positive(create(1)));

        assertFalse(NumericAssertions.nonNegative(create(-1)));
        assertTrue(NumericAssertions.nonNegative(create(0)));
        assertTrue(NumericAssertions.nonNegative(create(1)));
    }

    /**
     * Tests the {@link NumericAssertions} methods when invoked via
     * archetype service validation.
     */
    @Test
    public void testNumericAssertionsValidation() {
        Act act = (Act) create("act.numericAssertions");
        validateObject(act);  // default values are all valid

        act.getDetails().put("positive", 0.0);
        act.getDetails().put("negative", 0.0);
        act.getDetails().put("nonNegative", -1.0);
        try {
            validateObject(act);
            fail("Expected validation to fail");
        } catch (ValidationException expected) {
            // one error per node
            assertEquals(3, expected.getErrors().size());
        }

    }

    /**
     * Helper to create a new context with a value.
     *
     * @param value the value
     * @return a new context
     */
    private ActionContext create(Object value) {
        return new ActionContext(null, null, null, value);
    }
}
