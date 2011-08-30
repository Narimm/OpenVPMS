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

package org.openvpms.web.resource.util;

import nextapp.echo2.app.ApplicationInstance;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * A utility class that provides resources for obtaining localized messages.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class Messages {

    /**
     * Resource bundle name.
     */
    private static final String BUNDLE_NAME
            = "org.openvpms.web.resource.localisation.messages";

    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param arguments an array of arguments to be inserted into the message
     * @return the appropriate formatted localized text
     *         (if the key is not defined, the string "!key!" is returned)
     */
    public static String get(String key, Object ... arguments) {
        return get(key, false, arguments);
    }

    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @param arguments an array of arguments to be inserted into the message
     * @return the appropriate formatted localized text
     *         (if the key is not defined, the string "!key!" is returned)
     */
    public static String get(String key, boolean allowNull,
                             Object ... arguments) {
        String result = null;
        Locale locale = getLocale();
        String pattern = get(key, allowNull);
        if (pattern != null) {
            MessageFormat format = new MessageFormat(pattern, locale);
            result = format.format(arguments);
        }
        return result;
    }

    /**
     * Returns localised text.
     *
     * @param key the key of the text to be returned
     * @return the appropriate localized text (if the key is not defined, the
     *         string "!key!" is returned)
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
        ApplicationInstance active = ApplicationInstance.getActive();
        return (active != null) ? active.getLocale()
                : Locale.getDefault();
    }

    /**
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @return the appropriate formatted localized text; or <code>null</code>
     *         if the key doesn't exist and <tt>allowNull</tt> is <tt>true</tt>;
     *         or the string "!key!" if the key doesn't exist and
     *         <tt>allowNull</tt> is <tt>false</tt>
     */
    public static String get(String key, boolean allowNull) {
        String result = null;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME,
                                                             getLocale());
            result = bundle.getString(key);
        } catch (MissingResourceException exception) {
            if (!allowNull) {
                result = '!' + key + '!';
            }
        }
        return result;
    }
}
