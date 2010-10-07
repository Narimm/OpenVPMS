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
package org.openvpms.esci.adapter.i18n;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Messages {

    private final String bundlePath;

    private final String project;

    private static final Log log = LogFactory.getLog(Messages.class);

    public Messages(String project, String bundlePath) {
        this.project = project;
        this.bundlePath = bundlePath;
    }

    public Message getMessage(int code, Object... args) {
        return getMessage(code, Locale.getDefault(), args);
    }

    public Message getMessage(int code, Locale locale, Object... args) {
        String message = getString(Integer.toString(code), locale, args);
        return new Message(project, code, message);
    }

    public String getString(int code, Locale locale, Object... args) {
        return getString(Integer.toString(code), locale, args);
    }

    public String getString(String key, Object... args) {
        return getString(key, Locale.getDefault(), args);
    }

    public String getString(String key, Locale locale, Object... args) {
        String result = getValue(key, locale);
        if (result == null) {
            result = formatMissingKey(key, args);
        } else if (args.length != 0) {
            MessageFormat format = new MessageFormat(result, locale);
            result = format.format(args);
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

    protected ResourceBundle getResourceBundle(Locale locale) {
        return ResourceBundle.getBundle(bundlePath, locale, getClassLoader());
    }

    protected ClassLoader getClassLoader() {
        ClassLoader result = Thread.currentThread().getContextClassLoader();
        return (result != null) ? result : getClass().getClassLoader();
    }

    protected String formatMissingKey(String key, Object... args) {
        log.error("ResourceBundle=" + bundlePath + " missing key=" + key);
        StringBuilder result = new StringBuilder("?" + key + "?");
        if (args.length != 0) {
            result.append('[');
            for (int i = 0; i < args.length; ++i) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(args[i]);
            }
        }
        return result.toString();
    }
}
