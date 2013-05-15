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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.error;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.report.DocFormats;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * Sends an {@link ErrorReport} via email.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ErrorReporter {

    /**
     * The from address.
     */
    private String from = "bm9yZXBseUBvcGVudnBtcy5vcmc=";

    /**
     * The to address.
     */
    private String to = "ZXJyb3ItcmVwb3J0c0BsaXN0cy5vcGVudnBtcy5vcmc=";

    /**
     * Error reporter configuration.
     */
    public ErrorReporterConfig config;

    /**
     * The logger.
     */
    private Log log = LogFactory.getLog(ErrorReporter.class);

    /**
     * The reporter configuration resource name.
     */
    private static final String RESOURCE = "/ErrorReporter.xml";


    /**
     * Constructs an <tt>ErrorReporter</tt>.
     */
    public ErrorReporter() {
        InputStream stream = getClass().getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Failed to find error reporter configuration:" + RESOURCE);
        }
        config = ErrorReporterConfig.read(stream);
        from = new String(Base64.decodeBase64(from)); // poor persons anti spam....
        to = new String(Base64.decodeBase64(to));
    }

    /**
     * Sets the from address.
     *
     * @param from the from address
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Sets the to address.
     *
     * @param to the to address
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Reports an error.
     *
     * @param report the error report
     */
    public void report(final ErrorReport report) {
        try {
            JavaMailSender sender = ServiceHelper.getMailSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String subject = report.getVersion() + ": " + report.getMessage();
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setTo(to);
            String text = getText(report);
            if (text != null) {
                helper.setText(text);
            }
            InputStreamSource source = new InputStreamSource() {
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(report.toXML().getBytes());
                }
            };
            helper.addAttachment("error-report.xml", source, DocFormats.XML_TYPE);
            sender.send(message);
        } catch (Throwable exception) {
            log.error(exception, exception);
            ErrorDialog.show(exception);
        }
    }

    /**
     * Determines if an exception sbould be reported.
     *
     * @param exception the exception
     * @return <tt>true</tt> if the exception is reportable
     */
    public boolean isReportable(Throwable exception) {
        return !config.isExcluded(exception);
    }

    /**
     * Returns the message body from a report.
     *
     * @param report the report
     * @return the message body, or <tt>null</tt> if the report doesn't contain an exception
     */
    private String getText(ErrorReport report) {
        ThrowableAdapter exception = report.getException();
        if (exception != null) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer, true);
            exception.printStackTrace(printWriter);
            return writer.getBuffer().toString();
        }
        return null;
    }

}
