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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.resource.i18n;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * A utility class that provides resources for obtaining localized messages.
 *
 * @author Tim Anderson
 */
public final class Messages {

    /**
     * Messages resource bundle name.
     */
    public static final String MESSAGES = "localisation.messages";

    /**
     * The help resource bundle name.
     */
    public static final String HELP = "localisation.help";

    /**
     * Default resource bundle name.
     */
    private static final String DEFAULT_BUNDLE_NAME = "org.openvpms.web.resource.localisation.messages";


    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param arguments an array of arguments to be inserted into the message
     * @return the appropriate formatted localized text (if the key is not defined, the string "!key!" is returned)
     */
    public static String format(String key, Object... arguments) {
        String result = null;
        String pattern = get(key, false);
        if (pattern != null) {
            result = formatPattern(pattern, arguments);
        }
        return result;
    }

    /**
     * Returns a localised, formatted message, if the specified key exists.
     *
     * @param key       the key of the message to be returned
     * @param arguments an array of arguments to be inserted into the message
     * @return the appropriate formatted localized text, or {@code null} if the key doesn't exist
     */
    public static String formatNull(String key, Object... arguments) {
        String result = null;
        String pattern = get(key, true);
        if (pattern != null) {
            result = formatPattern(pattern, arguments);
        }
        return result;
    }

    /**
     * Returns localised text.
     *
     * @param key the key of the text to be returned
     * @return the appropriate localized text (if the key is not defined, the string "!key!" is returned)
     */
    public static String get(String key) {
        return get(key, false);
    }

    /**
     * Returns the current locale.
     *
     * @return the current locale
     */
    public static Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     *         is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    public static String get(String key, boolean allowNull) {
        String result = null;
        try {
            result = getString(key, getLocale(), MESSAGES, DEFAULT_BUNDLE_NAME);
        } catch (MissingResourceException exception) {
            if (!allowNull) {
                result = '!' + key + '!';
            }
        }
        return result;
    }

    /**
     * Returns localised text.
     *
     * @param key        the key of the text to be returned
     * @param bundleName the resource bundle name
     * @param allowNull  determines behaviour if the key doesn't exist
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     *         is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    public static String get(String key, String bundleName, boolean allowNull) {
        String result = null;
        try {
            result = getString(key, getLocale(), bundleName);
        } catch (MissingResourceException exception) {
            if (!allowNull) {
                result = '!' + key + '!';
            }
        }
        return result;
    }

    /**
     * Returns the keys for the specified bundle.
     *
     * @param bundleName the resource bundle name
     * @return the keys
     */
    public static Enumeration<String> getKeys(String bundleName) {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, getLocale());
        return bundle.getKeys();
    }

    /**
     * Returns localised text.
     *
     * @param key         the key of the text to be returned.
     * @param locale      the locale
     * @param bundleNames the resource bundles to look for the text
     * @return the text
     * @throws MissingResourceException if the resource cannot be found
     */
    private static String getString(String key, Locale locale, String... bundleNames) {
        String result = null;
        for (int i = 0; i < bundleNames.length; ++i) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(bundleNames[i], locale);
                result = bundle.getString(key);
                break;
            } catch (MissingResourceException exception) {
                if (i == bundleNames.length - 1) {
                    throw exception;
                }
            }
        }
        return result;
    }

    /**
     * Helper to format a string.
     *
     * @param pattern   the pattern
     * @param arguments the arguments
     * @return the formatted string
     */
    private static String formatPattern(String pattern, Object[] arguments) {
        String result;
        Locale locale = getLocale();
        MessageFormat format = new MessageFormat(pattern, locale);
        result = format.format(arguments);
        return result;
    }

}
