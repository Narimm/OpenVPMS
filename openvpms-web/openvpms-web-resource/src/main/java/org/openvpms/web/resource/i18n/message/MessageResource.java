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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.resource.i18n.message;

import java.util.Locale;

/**
 * Interface for resolving formatted, internationalised messages.
 *
 * @author Tim Anderson
 */
public interface MessageResource {

    /**
     * Returns localised text.
     *
     * @param key the key of the text to be returned
     * @return the appropriate localized text (if the key is not defined, the string "!key!" is returned)
     */
    String get(String key);

    /**
     * Returns localised text.
     *
     * @param key    the key of the text to be returned
     * @param locale the locale
     * @return the appropriate localized text (if the key is not defined, the string "!key!" is returned)
     */
    String get(String key, Locale locale);

    /**
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     * is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    String get(String key, boolean allowNull);

    /**
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @param locale    the locale
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     * is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    String get(String key, boolean allowNull, Locale locale);

    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text (if the key is not defined, the string "!key!" is returned)
     */
    String format(String key, Object... arguments);

    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param locale    the locale
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text (if the key is not defined, the string "!key!" is returned)
     */
    String format(String key, Locale locale, Object... arguments);

    /**
     * Returns a localised, formatted message, if the specified key exists.
     *
     * @param key       the key of the message to be returned
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text, or {@code null} if the key doesn't exist
     */
    String formatNull(String key, Object... arguments);

    /**
     * Returns a localised, formatted message, if the specified key exists.
     *
     * @param key       the key of the message to be returned
     * @param locale    the locale
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text, or {@code null} if the key doesn't exist
     */
    String formatNull(String key, Locale locale, Object... arguments);

    /**
     * Returns the locale to use, if no locale is specified.
     *
     * @return the locale
     */
    Locale getLocale();
}
