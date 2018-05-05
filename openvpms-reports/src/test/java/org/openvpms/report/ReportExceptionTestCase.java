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
 */

package org.openvpms.report;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.openvpms.report.ReportException.ErrorCode.FailedToCreateReport;
import static org.openvpms.report.ReportException.ErrorCode.FailedToFindSubReport;
import static org.openvpms.report.ReportException.ErrorCode.FailedToGenerateReport;
import static org.openvpms.report.ReportException.ErrorCode.FailedToPrintReport;
import static org.openvpms.report.ReportException.ErrorCode.NoExpressionEvaluatorForType;
import static org.openvpms.report.ReportException.ErrorCode.UnsupportedMimeType;


/**
 * {@link ReportException} test case.
 *
 * @author Tim Anderson
 */
public class ReportExceptionTestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     10, ReportException.ErrorCode.values().length);
        checkException(FailedToCreateReport, "Failed to create report foo: bar", "foo", "bar");
        checkException(FailedToFindSubReport, "There is no sub-report named: foo\nThis is needed by report: bar",
                       "foo", "bar");
        checkException(FailedToGenerateReport, "Failed to generate report foo: bar", "foo", "bar");
        checkException(FailedToPrintReport, "Failed to print report foo on printer bar: gum", "foo", "bar", "gum");
        checkException(UnsupportedMimeType,
                       "foo is not a supported mime type by bar", "foo", "bar");
        checkException(NoExpressionEvaluatorForType,
                       "No ExpressionEvalutor for type: foo", "foo");
        checkException(ReportException.ErrorCode.FailedToGetParameters, "Failed to get report parameters for foo",
                       "foo");
        checkException(ReportException.ErrorCode.NoTemplateForArchetype,
                       "No document template available for report type: foo", "foo");
        checkException(ReportException.ErrorCode.UnsupportedTemplate,
                       "Unsupported document template: foo", "foo");
        checkException(ReportException.ErrorCode.NoPagesToPrint, "Report has no pages to print: foo", "foo");
    }

    /**
     * Creates an {@link ReportException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    private void checkException(ReportException.ErrorCode code,
                                String expected, Object... args) {
        ReportException exception = new ReportException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }
}
