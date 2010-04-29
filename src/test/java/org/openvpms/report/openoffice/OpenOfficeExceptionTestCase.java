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

package org.openvpms.report.openoffice;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.openvpms.report.openoffice.OpenOfficeException.ErrorCode.*;


/**
 * {@link OpenOfficeException} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeExceptionTestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     12, OpenOfficeException.ErrorCode.values().length);
        checkException(InvalidURL, "Invalid OpenOffice URL: foo", "foo");
        checkException(FailedToConnect, "Failed to connect to OpenOffice");
        checkException(FailedToGetService,
                       "Failed to get OpenOffice service foo", "foo");
        checkException(FailedToStartService,
                       "Failed to start OpenOffice service: foo", "foo");
        checkException(ServiceNotInit,
                       "OpenOfficeServiceHelper has not been initialised");
        checkException(FailedToCreateDoc, "Failed to create document");
        checkException(FailedToPrint, "Printing failed: foo", "foo");
        checkException(FailedToGetField, "Failed to get user field: foo",
                       "foo");
        checkException(FailedToSetField, "Failed to set user field: foo",
                       "foo");
        checkException(FailedToExportDoc, "Failed to export document: foo",
                       "foo");
        checkException(FailedToGetInputFields, "Failed to get input fields");
        checkException(FailedToGetUserFields, "Failed to get user fields");
    }

    /**
     * Creates an {@link OpenOfficeException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(OpenOfficeException.ErrorCode code,
                                String expected, Object... args) {
        OpenOfficeException exception = new OpenOfficeException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }
}
