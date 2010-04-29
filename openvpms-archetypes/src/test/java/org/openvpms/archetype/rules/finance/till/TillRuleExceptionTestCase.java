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

package org.openvpms.archetype.rules.finance.till;

import junit.framework.TestCase;
import org.junit.Test;
import static org.openvpms.archetype.rules.finance.till.TillRuleException.ErrorCode.*;


/**
 * {@link TillRuleException}  test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TillRuleExceptionTestCase extends TestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     9, TillRuleException.ErrorCode.values().length);

        checkException(InvalidTillArchetype,
                       "foo is not a valid till act", "foo");
        checkException(MissingTill, "No till specified in act foo", "foo");
        checkException(UnclearedTillExists,
                       "Cannot save till balance. An uncleared till exists "
                               + "for till foo", "foo");
        checkException(ClearedTill, "Cannot modify Cleared till");
        checkException(CantAddActToTill, "Cannot add acts of type foo to till",
                       "foo");
        checkException(TillNotFound, "Failed to retrieve till foo", "foo");
        checkException(BalanceNotFound, "Failed to retrieve till balance");
        checkException(MissingRelationship, "No act relationship found");
        checkException(InvalidTransferTill, "Cannot transfer to till foo",
                       "foo");
    }

    /**
     * Creates an {@link TillRuleException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(TillRuleException.ErrorCode code,
                                String expected, Object ... args) {
        TillRuleException exception = new TillRuleException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }

}
