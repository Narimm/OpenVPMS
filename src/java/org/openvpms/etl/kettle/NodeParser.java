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

package org.openvpms.etl.kettle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses nodes of the form:
 * <code>
 * node = "&lt;" &lt;archetype&gt; "&gt;" &lt;node&gt; "[" collection "]"
 * collection = "[" &lt;index&gt; "]"  node
 * </code>
 * <p/>
 * E.g:
 * <code>
 * &lt;party.customerPerson&gt;firstName
 * &lt;party.customerPerson&gt;contacts[0]address
 * </code>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeParser {

    /**
     * The pattern.
     */
    private static final Pattern pattern
            = Pattern.compile("<([^<>]+)>(\\w+)(\\[(\\d+)\\])?");

    /**
     * Parses a node.
     *
     * @param node the node to parse
     * @return the node, or <tt>null</tt> if it can't be parsed
     */
    public static Node parse(String node) {
        Matcher matcher = pattern.matcher(node);
        int start = 0;
        Node result = null;
        Node parent = null;
        while (start < node.length() && matcher.find(start)) {
            if (start != matcher.start()) {
                return null;
            }
            String archetype = matcher.group(1);
            String name = matcher.group(2);
            String indexStr = matcher.group(4);
            int index = (indexStr != null) ? Integer.parseInt(indexStr) : -1;
            Node current = new Node(archetype, name, index);
            if (parent != null) {
                parent.setChild(current);
            }
            parent = current;
            if (result == null) {
                result = parent;
            }
            start = matcher.end();
        }
        return (start == node.length()) ? result : null;
    }
}
