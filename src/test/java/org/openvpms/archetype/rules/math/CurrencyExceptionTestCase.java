/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.math;

import junit.framework.TestCase;
import org.junit.Test;


/**
 * {@link CurrencyException} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CurrencyExceptionTestCase extends TestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     3, CurrencyException.ErrorCode.values().length);

        checkException(CurrencyException.ErrorCode.InvalidCurrencyCode,
                       "'foo' is not a valid ISO currency code", "foo");
        checkException(CurrencyException.ErrorCode.InvalidRoundingMode,
                       "'foo' is not a valid rounding mode for currency code "
                       + "bar", "foo", "bar");
        checkException(CurrencyException.ErrorCode.NoLookupForCode,
                       "No lookup.currency has been defined for currency code "
                       + "foo", "foo");
    }

    /**
     * Creates an {@link CurrencyException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(CurrencyException.ErrorCode code,
                                String expected, Object... args) {
        CurrencyException exception = new CurrencyException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }

}
