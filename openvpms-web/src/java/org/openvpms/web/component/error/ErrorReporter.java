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

import com.thoughtworks.xstream.XStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
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
    private String from = "noreply" + "@" + "openvpms.org";

    /**
     * The to address.
     */
    private String to;

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
    public void report(ErrorReport report) {
        try {
            JavaMailSender sender = ServiceHelper.getMailSender();
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            String subject = getString("subject", report.getVersion(), report.getMessage());
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setText(new XStream().toXML(report));
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
