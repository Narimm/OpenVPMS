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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.error;

import net.sf.jasperreports.engine.JRException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.statement.StatementProcessorException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.security.OpenVPMSAccessDeniedException;
import org.openvpms.report.ReportException;
import org.openvpms.web.component.im.query.QueryException;

import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Tests the {@link ErrorReporterConfig} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ErrorReporterConfigTestCase {

    /**
     * Tests the {@link ErrorReporterConfig#isExcluded(Throwable)} method for exceptions with particular codes.
     */
    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testIsExcludedByCode() {
        ErrorReporterConfig config = new ErrorReporterConfig();

        // create some exceptions
        Throwable a = new ArchetypeServiceException(ArchetypeServiceException.ErrorCode.FailedToCreateObject);
        Throwable b = new ArchetypeServiceException(ArchetypeServiceException.ErrorCode.FailedToExecuteQuery);
        Throwable c = new ArchetypeServiceException(ArchetypeServiceException.ErrorCode.FailedToSaveObject);
        Throwable d = new ValidationException(Collections.<ValidationError>emptyList(),
                                              ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype);
        Throwable e = new QueryException(QueryException.ErrorCode.InvalidType);

        // verify that no exception is excluded from reporting
        assertFalse(config.isExcluded(a));
        assertFalse(config.isExcluded(b));
        assertFalse(config.isExcluded(c));
        assertFalse(config.isExcluded(d));
        assertFalse(config.isExcluded(e));

        // now exclude:
        // . ArchetypeServiceException with errors FailedToSaveObject and FailedToExcecuteQuery
        // . all ValidationExceptions
        ExceptionConfig exception1Cfg = new ExceptionConfig(ArchetypeServiceException.class.getName());
        exception1Cfg.setCodes(Arrays.asList(ArchetypeServiceException.ErrorCode.FailedToSaveObject.toString(),
                                             ArchetypeServiceException.ErrorCode.FailedToExecuteQuery.toString()));

        ExceptionConfig exception3Cfg = new ExceptionConfig(ReportException.class.getName());
        ExceptionConfig cause = new ExceptionConfig(PrinterException.class.getName());
        exception3Cfg.setCauses(Arrays.asList(cause));

        ExceptionConfig exception2Cfg = new ExceptionConfig(ValidationException.class.getName());
        config.setExcludes(Arrays.asList(exception1Cfg, exception2Cfg));


        // verify the correct exceptions are excluded
        assertFalse(config.isExcluded(a));
        assertTrue(config.isExcluded(b));
        assertTrue(config.isExcluded(c));
        assertTrue(config.isExcluded(d));
        assertFalse(config.isExcluded(e));
    }

    /**
     * Tests the {@link ErrorReporterConfig#isExcluded(Throwable)} method for exceptions with particular messages.
     */
    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testIsExcludedByMessage() {
        ErrorReporterConfig config = new ErrorReporterConfig();

        // create some exceptions
        Throwable a = new PrinterException("Printer is not accepting job.");
        Throwable b = new PrinterException("Printer not found.");

        // verify that no exception is excluded from reporting
        assertFalse(config.isExcluded(a));
        assertFalse(config.isExcluded(b));

        // now exclude the "Printer is not accepting job." exception
        ExceptionConfig exception = new ExceptionConfig(PrinterException.class.getName());

        exception.setMessages(Arrays.asList("Printer is not accepting job."));

        config.setExcludes(Arrays.asList(exception));

        // verify the correct exceptions are excluded
        assertTrue(config.isExcluded(a));
        assertFalse(config.isExcluded(b));
    }

    /**
     * Tests the {@link ErrorReporterConfig#isExcluded(Throwable)} method for exceptions with particular root causes.
     */
    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testIsExcludedByCause() {
        ErrorReporterConfig config = new ErrorReporterConfig();

        // create some exceptions
        Throwable a = new ReportException(ReportException.ErrorCode.FailedToGenerateReport);
        Throwable b = new ReportException(new JRException(new PrinterException("Printer is not accepting job.")),
                                          ReportException.ErrorCode.FailedToGenerateReport);

        // verify that no exception is excluded from reporting
        assertFalse(config.isExcluded(a));
        assertFalse(config.isExcluded(b));

        // now exclude the "Printer is not accepting job." exception when it is a root cause for ReportException
        ExceptionConfig cause = new ExceptionConfig(PrinterException.class.getName());
        ExceptionConfig exception = new ExceptionConfig(ReportException.class.getName());
        exception.setCauses(Arrays.asList(cause));

        config.setExcludes(Arrays.asList(exception));

        // verify the correct exceptions are excluded
        assertFalse(config.isExcluded(a));
        assertTrue(config.isExcluded(b));
    }

    /**
     * Tests the {@link ErrorReporterConfig#isExcluded(Throwable)} method for exceptions with particular root cause's
     * message.
     */
    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testIsExcludedByCauseMessage() {
        ErrorReporterConfig config = new ErrorReporterConfig();

        // create some exceptions
        Throwable a = new ReportException(ReportException.ErrorCode.FailedToGenerateReport);
        Throwable b = new ReportException(new PrinterException("Printer is not accepting job."),
                                          ReportException.ErrorCode.FailedToGenerateReport);
        Throwable c = new ReportException(new PrinterException("Printer not found."),
                                          ReportException.ErrorCode.FailedToGenerateReport);

        // verify that no exception is excluded from reporting
        assertFalse(config.isExcluded(a));
        assertFalse(config.isExcluded(b));
        assertFalse(config.isExcluded(c));

        // now exclude the "Printer is not accepting job." exception when it is a root cause for ReportException
        ExceptionConfig cause = new ExceptionConfig(PrinterException.class.getName());
        cause.setMessages(Arrays.asList("Printer is not accepting job."));

        ExceptionConfig exception = new ExceptionConfig(ReportException.class.getName());
        exception.setCauses(Arrays.asList(cause));

        config.setExcludes(Arrays.asList(exception));

        // verify the correct exceptions are excluded
        assertFalse(config.isExcluded(a));
        assertTrue(config.isExcluded(b));
        assertFalse(config.isExcluded(c));
    }

    /**
     * Tests that an ErrorReporterConfig can be written and re-read.
     */
    @Test
    public void testWriteRead() {
        ErrorReporterConfig config = new ErrorReporterConfig();
        ExceptionConfig error1 = createConfig(ValidationException.class);
        ExceptionConfig error2 = createConfig(ArchetypeServiceException.class,
                                              ArchetypeServiceException.ErrorCode.FailedToSaveObject,
                                              ArchetypeServiceException.ErrorCode.FailedToExecuteQuery);
        config.setExcludes(Arrays.asList(error1, error2));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        config.write(stream);

        ErrorReporterConfig read = ErrorReporterConfig.read(new ByteArrayInputStream(stream.toByteArray()));
        List<ExceptionConfig> excludes = read.getExcludes();
        assertEquals(2, excludes.size());
        checkConfig(error1, excludes.get(0));
        checkConfig(error2, excludes.get(1));
    }

    /**
     * Tests exclusions from the default ErrorReporter.xml configuration.
     */
    @Test
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testDefaultConfig() {
        ErrorReporterConfig config = ErrorReporterConfig.read(getClass().getResourceAsStream("/ErrorReporter.xml"));
        Throwable ex1 = new ValidationException(Collections.<ValidationError>emptyList(),
                                                ValidationException.ErrorCode.FailedToValidObjectAgainstArchetype);
        Throwable ex2 = new OpenVPMSAccessDeniedException(OpenVPMSAccessDeniedException.ErrorCode.AccessDenied);
        Throwable ex3 = new ReportException(ReportException.ErrorCode.FailedToFindSubReport);
        Throwable ex4 = new StatementProcessorException(StatementProcessorException.ErrorCode.InvalidConfiguration);
        Throwable ex5 = new ReportException(new JRException(new PrinterException("Printer is not accepting job.")),
                                            ReportException.ErrorCode.FailedToGenerateReport);

        Throwable ex6 = new ReportException(new JRException(new PrinterException("No printer found.")),
                                            ReportException.ErrorCode.FailedToGenerateReport);
        assertTrue(config.isExcluded(ex1));
        assertTrue(config.isExcluded(ex2));
        assertTrue(config.isExcluded(ex3));
        assertTrue(config.isExcluded(ex4));
        assertTrue(config.isExcluded(ex5));
        assertTrue(config.isExcluded(ex6));

        Throwable inc1 = new NullPointerException();
        Throwable inc2 = new ReportException(ReportException.ErrorCode.FailedToCreateReport);
        Throwable inc3
                = new StatementProcessorException(StatementProcessorException.ErrorCode.FailedToProcessStatement);
        Throwable inc4 = new ReportException(new JRException(new PrinterException("Some error message.")),
                                             ReportException.ErrorCode.FailedToGenerateReport);
        assertFalse(config.isExcluded(inc1));
        assertFalse(config.isExcluded(inc2));
        assertFalse(config.isExcluded(inc3));
        assertFalse(config.isExcluded(inc4));
    }

    /**
     * Verifies that an ExceptionConfig matches the expected result.
     *
     * @param expected the expected result
     * @param actual   the actual result
     */
    private void checkConfig(ExceptionConfig expected, ExceptionConfig actual) {
        assertEquals(expected.getClassName(), actual.getClassName());
        assertEquals(expected.getCodes(), actual.getCodes());
    }

    /**
     * Helper to create an ExceptionConfig.
     *
     * @param exception the exception class name
     * @param excludes  the list of error codes to exclude
     * @return a new <tt>ExceptionConfig</tt>
     */
    private ExceptionConfig createConfig(Class exception, Enum... excludes) {
        ExceptionConfig config = new ExceptionConfig(exception.getName());
        List<String> list = new ArrayList<String>();
        for (Enum exclude : excludes) {
            list.add(exclude.toString());
        }
        config.setCodes(list);
        return config;
    }

}
