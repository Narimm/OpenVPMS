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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.test.TestHelper;


/**
 * Tests the {@link MacroEvaluator} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MacrosTestCase extends AbstractAppTest {

    /**
     * Tests {@link MacroEvaluator#evaluate(String, Object)}.
     */
    public void testMacros() {
        Party person = TestHelper.createCustomer();
        Object text1 = MacroEvaluator.evaluate("@macro1", person);
        assertEquals("macro 1 text", text1);

        Object text2 = MacroEvaluator.evaluate("@macro2", person);
        assertEquals("onetwothree", text2);

        Object text3 = MacroEvaluator.evaluate("test @macro1 @macro2 endtest",
                                               person);
        assertEquals("test macro 1 text onetwothree endtest", text3);

        Object text4 = MacroEvaluator.evaluate("@displayName", person);
        assertEquals("Customer(Person)", text4);

        // verifies that invalid macros don't expand
        Object text5 = MacroEvaluator.evaluate("@invalidNode", person);
        assertEquals("@invalidNode", text5);

        // verifies that non-existent macros don't expand
        Object text6 = MacroEvaluator.evaluate("@non existent", person);
        assertEquals("@non existent", text6);
    }

}
