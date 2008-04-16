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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.assertion;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.ActionContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Assertions based on xpath expressions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ExpressionAssertions {

    /**
     * Pattern that matches names, qualified names and functions.
     * TODO - really want pattern that only matches names.
     */
    private static final Pattern PATTERN =
            Pattern.compile("\\p{Alpha}[\\w|/]*\\(?");


    /**
     * Determines if a node value is valid according to an associated
     * expression.
     *
     * @param context the assertion context
     * @return <tt>true</tt> if the expression is valid,
     *         otherwise <tt>false</tt>
     */
    public static boolean validate(ActionContext context) {
        NamedProperty property = context.getProperty("expression");
        String expression = (String) property.getValue();
        ArchetypeDescriptor archetype
                = getArchetypeDescriptor(context.getNode());
        if (archetype != null) {
            expression = getExpression(archetype, expression);
        }
        JXPathContext pathContext
                = JXPathHelper.newContext(context.getParent());
        Object result = pathContext.getValue(expression, Boolean.class);
        return (result != null) && (Boolean) result;
    }

    /**
     * Helper to replace nodes in an expression with their corresponding
     * path.
     *
     * @param descriptor the archetype descriptor
     * @param expression the expression
     * @return the expression with nodes replaced
     */
    private static String getExpression(ArchetypeDescriptor descriptor,
                                        String expression) {
        Matcher matcher = PATTERN.matcher(expression);
        int start = 0;
        StringBuffer result = new StringBuffer();
        while (matcher.find(start)) {
            String match = matcher.group();
            result.append(expression.substring(start, matcher.start()));
            if (!match.matches("(.*[/(].*)")) {
                // if its a name, try and find the corresponding node.
                // TODO - can qnames and functions be excluded by PATTERN?
                NodeDescriptor node = descriptor.getNodeDescriptor(match);
                if (node != null) {
                    result.append(node.getPath());
                } else {
                    result.append(match);
                }
            } else {
                result.append(match);
            }
            start = matcher.end();
        }
        result.append(expression.substring(start));
        return result.toString();
    }

    /**
     * Returns the archetype descriptor associated with a node.
     *
     * @param node the node. May be <tt>null</tt>
     * @return the archetype descriptor associated with the node, or
     *         <tt>null</tt>, if none is found
     */
    private static ArchetypeDescriptor getArchetypeDescriptor(
            NodeDescriptor node) {
        ArchetypeDescriptor result = null;
        while (result == null && node != null) {
            if (node.getArchetypeDescriptor() != null) {
                result = node.getArchetypeDescriptor();
            } else {
                result = getArchetypeDescriptor(node.getParent());
            }
        }
        return result;
    }
}
