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


package org.openvpms.component.business.service.ruleengine;

// aop alliance
import org.aopalliance.intercept.MethodInvocation;

// commons-lang
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class holds utility methods for formulating rule set names. The rules 
 * are as follows.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class RuleSetUriHelper {
    /**
     * This is appended to the rule set URI to form the name of the rules that
     * are executed before the method invocation
     */
    private static final String BEFORE_URI_FRAGMENT = "before";
    
    /**
     * This is appended to the rule set URI to form the name of the rules that
     * are executed after the method invocation
     */
    private static final String AFTER_URI_FRAGMENT = "after";

    /**
     * Return the rule set URI for the specified method invocation. If the 
     * before flag is set then we need to return the rule set name that is 
     * used before the method invocation. 
     * <p>
     * This is how the URI is formulated.
     * 
     * 1. Get the short name of the service that is being invoked.
     * 2. Append the name of the method that is being invoked.
     * 3. Determine if this is a before or after method invocation
     * 
     * @param invocation
     *            the method invocation
     * @param before
     *            indicates whether it is before the method invocation
     * @return String
     *            the rule set URI                        
     */
    public static String getRuleSetURI(MethodInvocation invocation, boolean before) {
        StringBuffer buf = new StringBuffer(ClassUtils.getShortClassName(
                invocation.getThis(), ""));
        buf.append(".");
        buf.append(invocation.getMethod().getName());
        buf.append(".");
        if (before) {
            buf.append(BEFORE_URI_FRAGMENT);
        } else {
            buf.append(AFTER_URI_FRAGMENT);
        }
        
        return StringUtils.uncapitalize(buf.toString());
    }
}
