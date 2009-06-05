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


package org.openvpms.component.business.service.archetype.assertion;

import org.openvpms.component.business.domain.im.archetype.descriptor.ActionContext;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;

/**
 * String assertion methods to use in conjunction with
 * {@link org.openvpms.component.business.domain.im.archetype.descriptor.ActionTypeDescriptor}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class StringAssertions {

    /**
     * Default constructor.
     */
    public StringAssertions() {
    }

    /**
     * Test that the action value matches the specified regular expression.
     *
     * @param context the action context
     * @return <tt>true</tt> if value  matches the assertion's regular expression; otherwise <tt>false</tt>
     */
    public static boolean regularExpressionMatch(ActionContext context) {
        String str = (String) context.getValue();
        NamedProperty expression = context.getProperty("expression");
        String regExpr = (expression != null) ? (String) expression.getValue() : null;
        return (str != null && regExpr != null) && str.matches(regExpr);
    }

    /**
     * Converts the action context value string to upper case.
     *
     * @param context the action context
     * @return the converted string. May be <tt>null</tt>
     */
    public static String uppercase(ActionContext context) {
        String str = (String) context.getValue();
        return (str != null) ? str.toUpperCase() : null;
    }

    /**
     * Converts the action context value string to lower case.
     *
     * @param context the action context
     * @return the converted string. May be <tt>null</tt>
     */
    public static String lowercase(ActionContext context) {
        String str = (String) context.getValue();
        return (str != null) ? str.toLowerCase() : null;
    }

    /**
     * Converts the action context value string to proper case.
     *
     * @param context the action context
     * @return the converted string. May be <tt>null</tt>
     */
    public static String propercase(ActionContext context) {
        String str = (String) context.getValue();
        if (str != null) {
            NodeDescriptor node = context.getNode();
            IMObject parent = context.getParent();
            String existing = (String) node.getValue(parent);
            if (existing == null || !str.equalsIgnoreCase(existing)) {
                ProperCaseConverter converter = ProperCaseConverterHelper.getConverter();
                if (converter != null) {
                    str = converter.convert(str);
                }
            }
        }

        return str;
    }

}
