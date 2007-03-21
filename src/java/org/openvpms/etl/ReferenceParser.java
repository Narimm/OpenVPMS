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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses references of the form:
 * <code>
 * reference = "&lt;" &lt;archetype&gt; "&gt;" &lt;expression&gt;
 * expression = &lt;query&gt;&lt;legacyid&gt;
 * query=&lt;name&gt;=&lt;value&gt;
 * </code>
 * <p/>
 * E.g:
 * <code>
 * &lt;lookup.contactPurpose&gt;code=MAILING
 * <party.customerPerson>12345
 * </code>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReferenceParser {

    /**
     * The pattern.
     */
    private static final Pattern pattern
            = Pattern.compile("<([^<>]+)>(\\w+)(=(\\w+))?");

    /**
     * Parses a reference.
     *
     * @param reference the reference to parse
     * @return the reference, or <tt>null</tt> if it can't be parsed
     */
    public static Reference parse(String reference) {
        Matcher matcher = pattern.matcher(reference);
        Reference result = null;
        if (matcher.find()) {
            if (matcher.start() != 0 || matcher.end() != reference.length()) {
                return null;
            }
            String archetype = matcher.group(1);
            if (matcher.group(3) != null) {
                String name = matcher.group(2);
                String value = matcher.group(4);
                result = new Reference(archetype, name, value);
            } else {
                String legacyId = matcher.group(2);
                result = new Reference(archetype, legacyId);
            }
        }
        return result;
    }
}
