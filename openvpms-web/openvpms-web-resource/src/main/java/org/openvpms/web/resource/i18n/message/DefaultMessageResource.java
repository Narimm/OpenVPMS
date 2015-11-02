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

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

/**
 * Default implementation of {@link MessageResource}.
 *
 * @author Tim Anderson
 */
public class DefaultMessageResource implements MessageResource {

    /**
     * The message source.
     */
    private final MessageSource source;

    /**
     * The default locale.
     */
    private final Locale locale;


    /**
     * Constructs a {@link DefaultMessageResource}.
     *
     * @param bundles the resource bundle base names
     */
    public DefaultMessageResource(String[] bundles) {
        this(createMessageSource(bundles));
    }

    /**
     * Constructs a {@link DefaultMessageResource}.
     *
     * @param source the message source
     */
    public DefaultMessageResource(MessageSource source) {
        this(source, Locale.getDefault());
    }

    /**
     * Constructs a {@link DefaultMessageResource}.
     *
     * @param source the message source
     * @param locale the locale to use, if none is specified
     */
    public DefaultMessageResource(MessageSource source, Locale locale) {
        this.source = source;
        this.locale = locale;
    }

    /**
     * Returns localised text.
     *
     * @param key the key of the text to be returned
     * @return the appropriate localized text (if the key is not defined, the string "!key!" is returned)
     */
    @Override
    public String get(String key) {
        return get(key, locale);
    }

    /**
     * Returns localised text.
     *
     * @param key    the key of the text to be returned
     * @param locale the locale
     * @return the appropriate localized text (if the key is not defined, the string "!key!" is returned)
     */
    @Override
    public String get(String key, Locale locale) {
        return get(key, false, locale);
    }

    /**
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     * is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    @Override
    public String get(String key, boolean allowNull) {
        return get(key, allowNull, locale);
    }

    /**
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @param locale    the locale
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     * is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    @Override
    public String get(String key, boolean allowNull, Locale locale) {
        return getMessage(key, new Object[0], allowNull, locale);
    }

    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text (if the key is not defined, the string "!key!" is returned)
     */
    @Override
    public String format(String key, Object... arguments) {
        return format(key, locale, arguments);
    }

    /**
     * Returns a localised, formatted message.
     *
     * @param key       the key of the message to be returned
     * @param locale    the locale
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text (if the key is not defined, the string "!key!" is returned)
     */
    @Override
    public String format(String key, Locale locale, Object... arguments) {
        return getMessage(key, arguments, false, locale);
    }

    /**
     * Returns a localised, formatted message, if the specified key exists.
     *
     * @param key       the key of the message to be returned
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text, or {@code null} if the key doesn't exist
     */
    @Override
    public String formatNull(String key, Object... arguments) {
        return formatNull(key, locale, arguments);
    }

    /**
     * Returns a localised, formatted message, if the specified key exists.
     *
     * @param key       the key of the message to be returned
     * @param locale    the locale
     * @param arguments arguments to be inserted into the message
     * @return the appropriate formatted localized text, or {@code null} if the key doesn't exist
     */
    @Override
    public String formatNull(String key, Locale locale, Object... arguments) {
        return getMessage(key, arguments, true, locale);
    }

    /**
     * Returns the locale to use, if no locale is specified.
     *
     * @return the locale
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the message source.
     *
     * @return the message source
     */
    protected MessageSource getSource() {
        return source;
    }

    /**
     * Returns a localised message.
     *
     * @param key       the key of the message to be returned
     * @param arguments arguments to be inserted into the message
     * @param allowNull determines behaviour if the key doesn't exist
     * @param locale    the locale
     * @return the appropriate formatted localized text; or {@code null} if the key doesn't exist and {@code allowNull}
     * is {@code true}; or the string "!key!" if the key doesn't exist and {@code allowNull} is {@code false}
     */
    protected String getMessage(String key, Object[] arguments, boolean allowNull, Locale locale) {
        String result;
        try {
            result = (allowNull) ? source.getMessage(key, arguments, null, locale)
                                 : source.getMessage(key, arguments, locale);
        } catch (NoSuchMessageException exception) {
            result = '!' + key + '!';
        }
        return result;
    }

    /**
     * Creates a new message source.
     *
     * @param bundles the resource bundle names
     * @return a new message source
     */
    protected static MessageSource createMessageSource(String[] bundles) {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames(bundles);
        return source;
    }

}
