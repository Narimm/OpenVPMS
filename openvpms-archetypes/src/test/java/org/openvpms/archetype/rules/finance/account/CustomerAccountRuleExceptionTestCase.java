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

package org.openvpms.archetype.rules.finance.account;

import junit.framework.TestCase;

import java.math.BigDecimal;
import java.util.Locale;


/**
 * {@link CustomerAccountRuleException}  test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerAccountRuleExceptionTestCase extends TestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    public void testMessages() {
        // set the default locale to format currency amounts as expected.
        Locale.setDefault(Locale.US);

        assertEquals("Need to update tests to incorporate new messages",
                     2, CustomerAccountRuleException.ErrorCode.values().length);

        checkException(CustomerAccountRuleException.ErrorCode.MissingCustomer,
                       "No Customer specified in act foo", "foo");
        checkException(CustomerAccountRuleException.ErrorCode.InvalidBalance,
                       "Invalid foo0 total. Expected $0.00 but got $1.00",
                       "foo0", BigDecimal.ZERO, BigDecimal.ONE);
    }

    /**
     * Creates an {@link CustomerAccountRuleException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void checkException(CustomerAccountRuleException.ErrorCode code,
                                String expected, Object ... args) {
        CustomerAccountRuleException exception = new CustomerAccountRuleException(
                code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }

}
