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

// java core
import java.util.ArrayList;
import java.util.List;

// aop alliance
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

// log4j
import org.apache.log4j.Logger;

// spring modules
import org.springmodules.jsr94.core.Jsr94RuleSupport;

/**
 * The Drools rule engine is integrated through the JSR-94 runtime api and the
 * springframework interceptors. This provides a separation of concerns between
 * the service objects, which contains the static business logic and the rules
 * engine that provides dynamic business capabilities.
 * <p>
 * The class extends {@link Jsr94RuleSupport}, which eases the integration with
 * other JSR-94 compliant rules engines and provides convenience methods for 
 * creating session with the rule engine and exeucting rules. 
 * <p>
 * This class also implements the AOPAlliance {@link MethodInterceptor} interface
 * so that it can execute business rules before and or after the method 
 * invocation.
 * <p>
 * It is important to understand that handing control over to the rule engine
 * is an expensive operation and must be used under careful consideration.  
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class DroolsRuleEngineInterceptor extends Jsr94RuleSupport implements
        MethodInterceptor {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(DroolsRuleEngineInterceptor.class);
    
    /**
     * Cache a copy of the directory rule source
     */
    private DirectoryRuleSource ruleSource;
    
    
    /**
     * Create an instance of the interceptor by specifying the directory
     * rule source
     * 
     * @param ruleSource
 *                the directory rule source
     */
    public DroolsRuleEngineInterceptor(DirectoryRuleSource ruleSource) {
        this.ruleSource = ruleSource;
    }
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object rval = null;
        
        // all exceptions and errors will be caught and wrapped in a 
        // {@link RuleEngineException| before rethrowing it to the client 
        try {
            beforeMethodInvocation(invocation);
            rval = invocation.proceed();
            afterMethodInvocation(invocation);
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToExecuteRule,
                    new Object[] {invocation.getMethod().getName()},
                    exception);
        }

        return rval;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        // no op
    }

    /**
     * This is executed before the business logic of the intercepted method
     * 
     * @param invocation
     *            the method invocation
     */
    private void beforeMethodInvocation(MethodInvocation invocation) {
        String uri = RuleSetUriHelper.getRuleSetURI(invocation, true);
        if (logger.isDebugEnabled()) {
            logger.debug("beforeMethodInvocation: Invoking rule set URI "
                    + uri + " for object " + invocation.getThis().toString());
        }
        
        // only invoke the rule engine if there is an corresponding rule set
        // for the uri.
        if (ruleSource.hasRuleExecutionSet(uri)) {
            executeStateless(uri, getFacts(invocation));
        }
    }

    /**
     * This is executed after the business logic of the intercepted method
     * 
     * @param invocation
     *            the methof invocation
     */
    private void afterMethodInvocation(MethodInvocation invocation) {
        String uri = RuleSetUriHelper.getRuleSetURI(invocation, false);
        if (logger.isDebugEnabled()) {
            logger.debug("afterMethodInvocation: Invoking rule set URI "
                    + uri + " for object " + invocation.getThis().toString());
        }

        // only invoke the rule engine if there is an corresponding rule set
        // for the uri.
        if (ruleSource.hasRuleExecutionSet(uri)) {
            executeStateless(uri, getFacts(invocation));
        }
    }
    
    /**
     * Return the list of facts that should be injected into the working
     * memory
     *  
     * @param invocation
     *            meta data about the method that is being invoked
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