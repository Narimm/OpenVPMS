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

package org.openvpms.web.resource.i18n;

import org.openvpms.web.resource.i18n.message.DefaultMessageResource;
import org.openvpms.web.resource.i18n.message.MessageResource;
import org.springframework.context.support.ResourceBundleMessageSource;

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
     * Default resource bundle name.
     */
    private static final String FALLBACK_MESSAGES = "org.openvpms.web.resource.localisation.messages";

    /**
     * The message resource.
     */
    private static MessageResource resource;

    static {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        try {
            ResourceBundle.getBundle(MESSAGES);
            source.setBasenames(MESSAGES, FALLBACK_MESSAGES);
        } catch (MissingResourceException exception) {
            // if there is no localisation/messages.properties, just use the fallback
            source.setBasenames(FALLBACK_MESSAGES);
        }
        setMessageResource(new DefaultMessageResource(source));
    }

    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param arguments an array of arguments to be inserted into the message
     * @return the appropriate formatted localized text (if the key is not defined, the string "!key!" is returned)
     */
    public static String format(String key, Object... arguments) {
        return resource.format(key, arguments);
    }

    /**
     * Returns a localised, formatted message, if the specified key exists.
     *
     * @param key       the key of the message to be returned
     * @param arguments an array of arguments to be inserted into the message
     * @return the appropriate formatted localized text, or {@code null} if the key doesn't exist
     */
    public static String formatNull(String key, Object... arguments) {
        return resource.formatNull(key, arguments);
    }

    /**
     * Returns localised text.
     *
     * @param key the key of the text to be returned
     * @return the appropriate localized text (if the key is not defined, the string "!key!" is returned)
     */
    public static String get(String key) {
        return resource.get(key);
    }

    /**
     * Returns the current locale.
     *
     * @return the current locale
     */
    public static Locale getLocale() {
        return resource.getLocale();
    }

    /**
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     * is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    public static String get(String key, boolean allowNull) {
        return resource.get(key, allowNull);
    }

    /**
     * Registers the message resource.
     *
     * @param resource the message resource
     */
    public static void setMessageResource(MessageResource resource) {
        Messages.resource = resource;
    }

}
