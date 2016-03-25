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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.report;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link SQLReportException} class.
 *
 * @author Tim Anderson
 */
public class SQLReportExceptionTestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     2, SQLReportException.ErrorCode.values().length);
        checkException(SQLReportException.ErrorCode.NoQuery,
                       "Cannot run reports that don't have an embedded SQL query");
        checkException(SQLReportException.ErrorCode.ConnectionError,
                       "Failed to get a database connection to run the report");
    }

    /**
     * Creates an {@link SQLReportException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(SQLReportException.ErrorCode code, String expected, Object... args) {
        SQLReportException exception = new SQLReportException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }


}
