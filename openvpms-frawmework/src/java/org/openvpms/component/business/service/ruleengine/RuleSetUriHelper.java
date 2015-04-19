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


/**
 * This class holds utility methods for formulating rule set names URIs.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
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
     * Returns the rule set URI for an service operation on an archetype.
     * The URI is of the form:
     * <p/>
     * <em>service</em>.<em>operation</em>.<tt>&lt;before&gt;|&lt;after&gt;.</tt><em>shortName</em>
     * <p/>
     * E.g:
     * <ul>
     * <li>archetypeService.save.before.party.patientpet
     * <li>archetypeService.remove.after.act.customerAccountChargesInvoice
     * <ul>
     *
     * @param service   the service name
     * @param operation the service operation
     * @param before    if <tt>true</tt> create a <em>before</em> rule set URI,
     *                  otherwise create an <em>after</em> URI
     * @param shortName the archetype short name
     * @return the rult set URI
     */
    public static String getRuleSetURI(String service, String operation,
                                       boolean before, String shortName) {
        StringBuffer result = new StringBuffer(service).append('.').append(
                operation).append('.').append(shortName).append('.');
        if (before) {
            result.append(BEFORE_URI_FRAGMENT);
        } else {
            result.append(AFTER_URI_FRAGMENT);
        }
        return result.toString();
    }
}
