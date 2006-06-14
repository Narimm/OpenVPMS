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

import java.util.List;
import java.util.Map;

/**
 * This interface supports the invocation of a rule in a stateless 
 * session
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public interface IStatelessRuleEngineInvocation {
    /**
     * Execute the specified rule in a stateless session. 
     * 
     * @param ruleUri
     *            the rule uri
     * @param properties
     *            a set of properties that can be used by the rule engine
     * @param facts
     *            a list of facts that are asserted in to the working memory
     * @return List<Object>
     *            a list objects, which maybe an empty list.
     */
    public List<Object> executeRule(String ruleUri, Map<String, Object> props,
            List<Object> facts);

}
