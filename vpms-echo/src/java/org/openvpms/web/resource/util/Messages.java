package org.openvpms.web.resource.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import nextapp.echo2.app.ApplicationInstance;


/**
 * A utility class that provides resources for obtaining localized messages.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
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
     */
    public static String get(String key, Object ... arguments) {
        Locale locale = ApplicationInstance.getActive().getLocale();
        String pattern = get(key);
        MessageFormat format = new MessageFormat(pattern, locale);
        return format.format(arguments);
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
     * Returns localised text.
     *
     * @param key       the key of the text to be returned
     * @param allowNull determines behaviour if the key doesn't exist
     * @return the appropriate localized text; or <code>null</code> if the key
     *         doesn't exist and <code>allowNull</code> is <code>true</code>; or
     *         the string "!key!" if the key doesn't exist and
     *         <code>allowNull</code> is <code>false</code>
     */
    public static String get(String key, boolean allowNull) {
        String result = null;
        try {
            Locale locale = ApplicationInstance.getActive().getLocale();
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME,
                    locale);
            result = bundle.getString(key);
        } catch (MissingResourceException exception) {
            if (!allowNull) {
                result = '!' + key + '!';
            }
        }
        return result;
    }
}
