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

import static org.junit.Assert.*;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;


/**
 * Tests the {@link ExpressionAssertions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
@ContextConfiguration("../archetype-service-appcontext.xml")
public class ExpressionAssertionTestCase extends AbstractArchetypeServiceTest {

    /**
     * Tests the {@link ExpressionAssertions} methods when invoked via
     * archetype service validation.
     */
    @Test
    public void testAssertions() {
        Act act = (Act) create("act.expressionAssertions");
        assertNotNull(act);
        ActBean bean = new ActBean(act);
        bean.setValue("amount", BigDecimal.ZERO);
        try {
            validateObject(act);
            fail("Expected validation to fail");
        } catch (ValidationException expected) {
            assertEquals(3, expected.getErrors().size());
            checkError(expected, 0, "act.expressionAssertions", "amount",
                       "Amount must be > 0");
            checkError(expected, 1, "act.expressionAssertions", "value1",
                       "Value1 must be < amount");
            checkError(expected, 2, "act.expressionAssertions", "value2",
                       "Value2 must be < amount");
        }
        bean.setValue("amount", new BigDecimal("100.00"));
        validateObject(act);
    }

    private void checkError(ValidationException exception,
                            int index, String archetype, String node,
                            String message) {
        ValidationError error = exception.getErrors().get(index);
        assertEquals(archetype, error.getArchetype());
        assertEquals(node, error.getNode());
        assertEquals(message, error.getMessage());
    }
}
