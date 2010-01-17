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
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;


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
     * Exception class names to exclude.
     */
    private Set<String> exclude = new HashSet<String>();

    /**
     * Error reporter resource bundle.
     */
    public ResourceBundle bundle;

    /**
     * The logger.
     */
    private Log log = LogFactory.getLog(ErrorReporter.class);

    /**
     * The reporter configuration properties resource name.
     */
    private static final String RESOURCE = "ErrorReporter";


    /**
     * Construct an <tt>ErrorReporter</tt>.
     */
    public ErrorReporter() {
        bundle = ResourceBundle.getBundle(RESOURCE);
        from = new String(Base64.decodeBase64(from)); // poor persons anti spam....
        to = new String(Base64.decodeBase64(to));
        for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            if (key.startsWith("exclude.")) {
                exclude.add(bundle.getString(key));
            }
        }
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
            String subject = getString("report.subject", report.getVersion(), report.getMessage());
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
        return !exclude.contains(exception.getClass().getName());
    }

    /**
     * Returns the message body from a report.
     *
     * @param report the report
     * @return the message body, or <tt>null</tt> if the report doesn't contain an exception
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    private String getText(ErrorReport report) {
        Throwable exception = report.getException();
        if (exception != null) {
            Throwable cause = ExceptionUtils.getRootCause(exception);
            if (cause == null) {
                cause = exception;
            }
            return ExceptionUtils.getStackTrace(cause);
        }
        return null;
    }

    /**
     * Returns a formatted message
     *
     * @param key  the resource bundle key
     * @param args arguments to format the message with
     * @return the formatted message
     */
    private String getString(String key, String... args) {
        String result;
        try {
            result = bundle.getString(key);
            if (args.length != 0) {
                MessageFormat format = new MessageFormat(result);
                result = format.format(args);
            }
        } catch (MissingResourceException exception) {
            result = '!' + key + '!';
        }
        return result;
    }
}
