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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.ruleengine.RuleEngineException;
import org.openvpms.component.business.service.ruleengine.RuleSetUriHelper;
import org.springmodules.jsr94.core.Jsr94RuleSupport;

import java.util.ArrayList;
import java.util.List;


/**
 * The rule engine is integrated through the JSR-94 runtime api and the
 * springframework interceptors. This provides a separation of concerns between
 * the service objects, which contains the static business logic and the rules
 * engine that provides dynamic business capabilities.
 * <p/>
 * The class extends {@link Jsr94RuleSupport}, which eases the integration with
 * other JSR-94 compliant rules engines and provides convenience methods for
 * creating session with the rule engine and executing rules.
 * <p/>
 * This class also implements the AOPAlliance {@link MethodInterceptor} interface
 * so that it can execute business rules before and or after the method
 * invocation.
 * <p/>
 * It is important to understand that handing control over to the rule engine
 * is an expensive operation and must be used under careful consideration.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class RuleEngineInterceptor extends Jsr94RuleSupport implements
                                                            MethodInterceptor {
    /**
     * Define a logger for this class.
     */
    private static final Log _log
            = LogFactory.getLog(RuleEngineInterceptor.class);

    /**
     * The rule source.
     */
    private ResourceRuleSource _ruleSource;


    /**
     * Create an instance of the interceptor by specifying the rule source.
     *
     * @param ruleSource the rule source
     */
    public RuleEngineInterceptor(ResourceRuleSource ruleSource) {
        _ruleSource = ruleSource;
    }

    /* (non-Javadoc)
    * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
    */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result;

        beforeMethodInvocation(invocation);
        result = invocation.proceed();
        afterMethodInvocation(invocation);
        return result;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        // no op
    }

    /**
     * This is executed before the business logic of the intercepted method.
     *
     * @param invocation the method invocation
     */
    private void beforeMethodInvocation(MethodInvocation invocation) {
        String uri = RuleSetUriHelper.getRuleSetURI(invocation, true);
        if (_log.isDebugEnabled()) {
            _log.debug("beforeMethodInvocation: Invoking rule set URI "
                    + uri + " for object " + invocation.getThis().toString());
        }
        executeRules(uri, invocation);
    }

    /**
     * This is executed after the business logic of the intercepted method.
     *
     * @param invocation the methof invocation
     */
    private void afterMethodInvocation(MethodInvocation invocation) {
        String uri = RuleSetUriHelper.getRuleSetURI(invocation, false);
        if (_log.isDebugEnabled()) {
            _log.debug("afterMethodInvocation: Invoking rule set URI "
                    + uri + " for object " + invocation.getThis().toString());
        }
        executeRules(uri, invocation);
    }

    /**
     * Executes rules if there is a corresponding rule set for the uri.
     *
     * @param uri        the uri
     * @param invocation the method invocation
     * @throws RuleEngineException if the rule fails to execute
     */
    private void executeRules(String uri, MethodInvocation invocation) {
        if (_ruleSource.hasRuleExecutionSet(uri)) {
            try {
                executeStateless(uri, getFacts(invocation));
            } catch (Throwable exception) {
                throw new RuleEngineException(
                        RuleEngineException.ErrorCode.FailedToExecuteRule,
                        new Object[]{invocation.getMethod().getName()},
                        exception);
            }
        }
    }

    /**
     * Return the list of facts that should be injected into the working
     * memory.
     *
     * @param invocation meta data about the method that is being invoked
     * @return List<Object>
     */
    private List<Object> getFacts(MethodInvocation invocation) {
        List<Object> facts = new ArrayList<Object>();
        for (Object fact : invocation.getArguments()) {
            facts.add(fact);
        }

        // now add the service that was intercepted
        facts.add(invocation.getThis());

        return facts;
    }
}
