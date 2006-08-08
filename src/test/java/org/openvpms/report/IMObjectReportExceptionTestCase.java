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

package org.openvpms.report;

import junit.framework.TestCase;
import static org.openvpms.report.IMObjectReportException.ErrorCode.*;

/**
 * {@link IMObjectReportException} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportExceptionTestCase extends TestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     5, IMObjectReportException.ErrorCode.values().length);
        checkException(FailedToCreateReport, "Failed to create report: foo",
                       "foo");
        checkException(FailedToGenerateReport, "Failed to generate report: foo",
                       "foo");
        checkException(InvalidNode, "Invalid node name: foo", "foo");
        checkException(InvalidObject,
                       "Node does not refer to a valid object: foo", "foo");
        checkException(UnsupportedMimeTypes,
                       "Mime types not supported by report");
    }

    /**
     * Creates an {@link IMObjectReportException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(IMObjectReportException.ErrorCode code,
                                String expected, Object ... args) {
        IMObjectReportException exception = new IMObjectReportException(code,
                                                                        args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }
}
