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

package org.openvpms.archetype.rules.finance.deposit;

import junit.framework.TestCase;
import org.junit.Test;
import static org.openvpms.archetype.rules.finance.deposit.DepositRuleException.ErrorCode.*;


/**
 * {@link DepositRuleException}  test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DepositRuleExceptionTestCase extends TestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     4, DepositRuleException.ErrorCode.values().length);

        checkException(InvalidDepositArchetype,
                       "foo is not a valid Deposit act", "foo");
        checkException(MissingAccount,
                       "No Deposit Account specified in act foo", "foo");
        checkException(UndepositedDepositExists, "Cannot save Deposit balance. "
                + "An uncleared Deposit exists for Deposit Account foo", "foo");
        checkException(DepositAlreadyDeposited,
                       "Cannot save Deposit balance. Deposit has already been "
                               + "deposited");
    }

    /**
     * Creates an {@link DepositRuleException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(DepositRuleException.ErrorCode code,
                                String expected, Object ... args) {
        DepositRuleException exception = new DepositRuleException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }

}
