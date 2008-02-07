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

import org.springmodules.jsr94.core.Jsr94RuleSupport;

import java.util.List;
import java.util.Map;


/**
 * Implementation of the {@link IRuleEngine} interface, using the JSR-94 API.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class RuleEngine extends Jsr94RuleSupport implements IRuleEngine {

    /**
     * The rule source.
     */
    private BaseRuleSource ruleSource;


    /**
     * Creates a new <tt>RuleEngine</tt>.
     *
     * @param ruleSource the rule source
     */
    public RuleEngine(BaseRuleSource ruleSource) {
        this.ruleSource = ruleSource;
        setRuleSource(ruleSource);
    }

    /**
     * Determines if there are any rules for the specified URI.
     *
     * @param uri the rule st URI
     * @return <tt>true</tt> if there are any rules for <tt>uri</tt>,
     *         otherwise <tt>false</tt>
     */
    public boolean hasRules(String uri) {
        return ruleSource.hasRuleExecutionSet(uri);
    }

    /**
     * Executes any rules associated with the specified URI.
     *
     * @param uri   the rule set URI
     * @param facts a list of facts that are asserted in the working memory
     * @return a list objects. May be empty
     */
    @SuppressWarnings("unchecked")
    public List<Object> executeRules(String uri, List<Object> facts) {
        try {
            return (List<Object>) executeStateless(uri, facts);
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToExecuteRule,
                    exception, uri);
        }
    }

    /**
     * Executes any rules associated with the specified rule URI.
     *
     * @param uri        the rule set URI
     * @param properties a set of properties that can be used by the rule engine
     * @param facts      a list of facts that are asserted in the working memory
     * @return a list objects. May be empty
     */
    @SuppressWarnings("unchecked")
    public List<Object> executeRules(String uri, Map<String, Object> properties,
                                     List<Object> facts) {
        try {
            return (List<Object>) executeStateless(uri, properties, facts,
                                                   null);
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToExecuteRule,
                    exception, uri);
        }
    }

}