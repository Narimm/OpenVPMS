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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.system.common.util;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.perl.Perl5Util;

import java.util.ArrayList;


/**
 * Holds a number of string utility methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class StringUtilities {

    /**
     * Uncamel cases the specified name.
     *
     * @param name the camel cased name. May be <tt>null</tt>
     * @return the uncamel cased name. May be <tt>null</tt>
     */
    public static String unCamelCase(String name) {
        ArrayList<String> words = new ArrayList<String>();
        Perl5Util perl = new Perl5Util();

        while (perl.match("/(\\w+?)([A-Z].*)/", name)) {
            String word = perl.group(1);
            name = perl.group(2);
            words.add(StringUtils.capitalize(word));
        }

        words.add(StringUtils.capitalize(name));

        return StringUtils.join(words.iterator(), " ");
    }

    /**
     * Convert the incoming string to a regular expression. This means
     * escaping the '.' and converting all the '*' to '.*'
     *
     * @param input the input string
     * @return the converted string
     */
    public static String toRegEx(String input) {
        return input.replace(".", "\\.").replace("*", ".*");

    }

    /**
     * The '*' character denotes a wildcard character. This method will do a
     * regular expression match against the input string. It first converts any
     * '*' characters to the equivalent '.*' regular expression before executing
     * a regex match
     *
     * @param str        the string that is matched
     * @param expression the expression to match, which can contain wild card
     *                   characters
     * @return boolean
     *         true if it matches
     */
    public static boolean matches(String str, String expression) {
        if (!expression.contains("*")) {
            // use faster equals() when no wildcard specified
            return str.equals(expression);
        }
        return str.matches(toRegEx(expression));
    }
}
