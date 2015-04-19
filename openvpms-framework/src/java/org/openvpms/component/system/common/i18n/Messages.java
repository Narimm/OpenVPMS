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
package org.openvpms.component.system.common.i18n;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Factory for {@link Message}s.
 * <p/>
 * Messages are obtained from a resource bundle, with each message having a numeric code.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Messages {

    /**
     * The resource bundle path.
     */
    private final String bundlePath;

    /**
     * The group that the messages belong to.
     */
    private final String groupId;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(Messages.class);


    /**
     * Constructs a <tt>Message</tt>.
     *
     * @param groupId    the group that the messages belong to
     * @param bundlePath the resource bundle path
     */
    public Messages(String groupId, String bundlePath) {
        this.groupId = groupId;
        this.bundlePath = bundlePath;
    }

    /**
     * Creates a new {@link Message} instance for the default locale, containing the formatted message with id
     * <tt>code</tt>, from the resource bundle.
     *
     * @param code the message code, corresponding to an entry in the resource bundle
     * @param args the format arguments
     * @return a new message
     */
    public Message getMessage(int code, Object... args) {
        return getMessage(code, Locale.getDefault(), args);
    }

    /**
     * Creates a new {@link Message} instance for the specified locale, containing the formatted message with id
     * <tt>code</tt>, from the resource bundle.
     *
     * @param code   the message code, corresponding to an entry in the resource bundle
     * @param locale the locale
     * @param args   the format arguments
     * @return a new message
     */
    public Message getMessage(int code, Locale locale, Object... args) {
        String message = getString(Integer.toString(code), locale, args);
        return new Message(groupId, code, message);
    }

    /**
     * Formats a string using the string with id <tt>key</tt>, for the specified locale, from the resource bundle.
     *
     * @param key    the message key, corresponding to an entry in the resource bundle
     * @param locale the locale
     * @param args   the format arguments
     * @return a new message
     */
    protected String getString(String key, Locale locale, Object... args) {
        String result = getValue(key, locale);
        if (result == null) {
            result = formatMissingKey(key, args);
        } else if (args.length != 0) {
            MessageFormat format = new MessageFormat(result, locale);
            try {
                result = format.format(args);
            } catch (Throwable exception) {
                result = formatFailed(key, result, args, exception);
            }
        }
        return result;
    }

    /**
     * Returns a value for the specified key and locale.
     *
     * @param key    the key for the desired string
     * @param locale the locale for which a string is desired
     * @return the corresponding value, or <tt>null</tt> if its not found
     */
    protected String getValue(String key, Locale locale) {
        ResourceBundle bundle = getResourceBundle(locale);
        String result = null;
        try {
            result = StringUtils.trimToNull(bundle.getString(key));
        } catch (MissingResourceException exception) {
            // ignore
        }
        return result;
    }

    /**
     * Returns the resource bundle for the specified locale.
     *
     * @param locale the locale
     * @return the corresponding resource bundle
     */
    protected ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(bundlePath, locale, getClassLoader());
    }

    /**
     * Retrns the class loader to load the resource bundle.
     *
     * @return the class loader
     */
    protected ClassLoader getClassLoader() {
        ClassLoader result = Thread.currentThread().getContextClassLoader();
        return (result != null) ? result : getClass().getClassLoader();
    }

    /**
     * Formats a message when the corresponding resource bundle key doesn't exist.
     * <p/>
     * This is to help return some message to the user, in the case where the resource bundle is not in sync with
     * the code that generates the message.
     *
     * @param key  the resource bundle key
     * @param args the arguments to format
     * @return a message
     */
    protected String formatMissingKey(String key, Object... args) {
        log.error("ResourceBundle=" + bundlePath + " missing key=" + key);
        StringBuilder result = new StringBuilder("?" + key + "?");
        if (args.length != 0) {
            result.append('[');
            result.append(StringUtils.join(args, ','));
            result.append(']');
        }
        return result.toString();
    }

    /**
     * Formats a message when it cannot be formattted by <tt>MessageFormat</tt> due to an exception.
     * <p/>
     * This is to help return some message to the user, in the case where the resource bundle is not in sync with
     * the code that generates the message.
     *
     * @param key       the resource bundle key
     * @param message   the message
     * @param args      the arguments to format
     * @param exception the cause of the failure
     * @return a message corresponding to the arguments
     */
    protected String formatFailed(String key, String message, Object[] args, Throwable exception) {
        String argText = StringUtils.join(args, ',');
        log.error("Failed to format message, bundle=" + bundlePath + ", key=" + key + ", text='" + message
                  + "', arguments=[" + argText + "]", exception);
        StringBuilder result = new StringBuilder("Message='");
        result.append(message);
        result.append("'");
        if (args.length != 0) {
            result.append(". Arguments=[");
            result.append(argText);
            result.append(']');
        }
        return result.toString();
    }
}
