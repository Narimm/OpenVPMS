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
package org.openvpms.component.business.service.archetype.assertion;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocaleProperCaseRules implements ProperCaseRules {

    /**
     * The locale.
     */
    private final Locale locale;

    /**
     * Resource bundle name.
     */
    private static final String BUNDLE_NAME
            = "org.openvpms.component.business.service.archetype.assertion.propercase";

    public LocaleProperCaseRules() {
        this(Locale.getDefault());
    }

    public LocaleProperCaseRules(Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns a list of strings that force capitalisation of the next character when they are encountered at the start
     * of a word.
     *
     * @return a list of strings
     */
    public String[] getStartsWith() {
        return getStrings("startsWith");
    }

    /**
     * Returns a list of strings that force capitalisation of the next character when they are encountered within a
     * word.
     *
     * @return a list of strings
     */
    public String[] getContains() {
        return getStrings("contains");
    }

    /**
     * Returns a list of strings that must appear with the specified case at the end of a word.
     *
     * @return a list of strings
     */
    public String[] getEndsWith() {
        return getStrings("endsWith");
    }

    /**
     * Returns a list of strings that are exceptions to the above rules.
     *
     * @return a list of strings that should appear as is
     */
    public String[] getExceptions() {
        return getStrings("exceptions");
    }

    /**
     * Returns the version of the case rules.
     * <p/>
     * These can be used to detect when the rules change.
     *
     * @return the version
     */
    public int getVersion() {
        return 0;
    }

    /**
     * Returns a list of strings for the specified resource bundle key prefix.
     *
     * @param prefix the resource bundle key prefix
     * @return a list of strings
     */
    private String[] getStrings(String prefix) {
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
        ArrayList<String> matches = new ArrayList<String>();

        for (Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            if (key.startsWith(prefix)) {
                matches.add(key);
            }
        }
        String[] result = new String[matches.size()];

        for (int i = 0; i < matches.size(); i++) {
            result[i] = bundle.getString(matches.get(i));
        }

        return result;
    }
}
