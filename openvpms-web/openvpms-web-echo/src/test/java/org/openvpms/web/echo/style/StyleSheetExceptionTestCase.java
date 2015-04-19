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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.style;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Tests {@link StyleSheetException}.
 *
 * @author Tim Anderson
 */
public class StyleSheetExceptionTestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     6, StyleSheetException.ErrorCode.values().length);
        checkException(StyleSheetException.ErrorCode.ResourceNotFound, "Resource not found: foo", "foo");
        checkException(StyleSheetException.ErrorCode.InvalidStyleSheet, "Style sheet is invalid");
        checkException(StyleSheetException.ErrorCode.UnterminatedProperty, "Unterminated property at line 1, column 2",
                       "1", "2");
        checkException(StyleSheetException.ErrorCode.UndefinedProperty, "Property is not defined: foo", "foo");
        checkException(StyleSheetException.ErrorCode.InvalidResolution, "Invalid resolution: foo", "foo");
        checkException(StyleSheetException.ErrorCode.InvalidExpression, "Failed to evaluate expression: foo", "foo");
    }

    /**
     * Creates a {@link StyleSheetException} with the supplied code and arguments and verifies that the generated
     * message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void checkException(StyleSheetException.ErrorCode code, String expected, Object... args) {
        StyleSheetException exception = new StyleSheetException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }

}
