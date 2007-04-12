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

package org.openvpms.etl.load;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses references of the form:
 * <pre>
 * reference = "&lt;" &lt;archetype&gt; "&gt;" &lt;expression&gt;
 * expression = &lt;query&gt; | &lt;rowId&gt;
 * query=&lt;name&gt;=&lt;value&gt;
 * </pre>
 * <p/>
 * E.g:
 * <pre>
 * &lt;lookup.contactPurpose&gt;code=MAILING
 * &lt;party.customerPerson&gt;12345
 * </pre>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReferenceParser {

    /**
     * The pattern.
     */
    private static final Pattern pattern
            = Pattern.compile("<([^<>]+)>([^<>\\s=]+)(=(\\w+))?"); // NON-NLS

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
                String rowId = matcher.group(2);
                result = new Reference(archetype, rowId);
            }
        }
        return result;
    }
}
