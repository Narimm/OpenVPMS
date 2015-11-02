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

import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * An {@link ResourceBundleMessageSource} that can expose all of the resource bundle keys.
 *
 * @author Tim Anderson
 */
public class KeyMessageSource extends ResourceBundleMessageSource {

    /**
     * The resource bundle base names
     */
    private String[] baseNames;

    /**
     * Set an array of basenames, each following {@link ResourceBundle}
     * conventions: essentially, a fully-qualified classpath location. If it
     * doesn't contain a package qualifier (such as {@code org.mypackage}),
     * it will be resolved from the classpath root.
     * <p>The associated resource bundles will be checked sequentially
     * when resolving a message code. Note that message definitions in a
     * <i>previous</i> resource bundle will override ones in a later bundle,
     * due to the sequential lookup.
     * <p>Note that ResourceBundle names are effectively classpath locations: As a
     * consequence, the JDK's standard ResourceBundle treats dots as package separators.
     * This means that "test.theme" is effectively equivalent to "test/theme",
     * just like it is for programmatic {@code java.util.ResourceBundle} usage.
     *
     * @param basenames the base names
     * @see ResourceBundle#getBundle(String)
     */
    @Override
    public void setBasenames(String... basenames) {
        super.setBasenames(basenames);
        this.baseNames = basenames;
    }

    /**
     * Returns all of the keys for the given locale.
     *
     * @param locale the locale
     * @return the keys for the locale
     */
    public Set<String> getKeys(Locale locale) {
        Set<String> result = new HashSet<>();
        if (baseNames != null) {
            for (String bundleName : baseNames) {
                ResourceBundle bundle = getResourceBundle(bundleName, locale);
                if (bundle != null) {
                    result.addAll(Collections.list(bundle.getKeys()));
                }
            }
        }
        return result;
    }
}
