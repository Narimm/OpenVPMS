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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springmodules.jsr94.core.Jsr94RuleSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The Drools rule engine is integrated through the JSR-94 runtime api and the
 * springframework interceptors. This provides a separation of concerns between
 * the service objects, which contains the static business logic and the rules
 * engine that provides dynamic business capabilities.
 * <p/>
 * The class extends {@link Jsr94RuleSupport}, which eases the integration with
 * other JSR-94 compliant rules engines and provides convenience methods for
 * creating session with the rule engine and exeucting rules.
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
public class DroolsRuleEngine extends Jsr94RuleSupport
        implements MethodInterceptor, IStatelessRuleEngineInvocation {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(DroolsRuleEngine.class);

    /**
     * Cache a copy of the rule source
     */
    private BaseRuleSource ruleSource;

    /**
     * The transaction manager.
     */
    private PlatformTransactionManager txnManager;

    /**
     * The default transaction definition.
     */
    private static final TransactionDefinition TXN_DEFINITION
            = new DefaultTransactionDefinition();


    /**
     * Creates a new <tt>DroolsRuleEngine</tt>.
     *
     * @param ruleSource the rule source
     */
    public DroolsRuleEngine(BaseRuleSource ruleSource) {
        this.ruleSource = ruleSource;
    }

    /**
     * Sets the transaction manager.
     *
     * @param manager the transaction manager
     */
    public void setTransactionManager(PlatformTransactionManager manager) {
        txnManager = manager;
    }

    /* (non-Javadoc)
    * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
    */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        TransactionStatus status = txnManager.getTransaction(TXN_DEFINITION);
        Object result;

        try {
            beforeMethodInvocation(invocation);
            result = invocation.proceed();
            afterMethodInvocation(invocation);
            txnManager.commit(status);
        } catch (Exception exception) {
            txnManager.rollback(status);
            throw exception;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        // no op
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.ruleengine.IStatelessRuleEngineInvocation#executeRule(java.lang.String, java.util.Map, java.util.List)
     */
    @SuppressWarnings("unchecked")
    public List<Object> executeRule(String ruleUri, Map<String, Object> props,
                                    List<Object> facts) {
        return (List<Object>) executeStateless(ruleUri, props, facts, null);
    }

    /**
     * This is executed before the business logic of the intercepted method.
     *
     * @param invocation the method invocation
     */
    private void beforeMethodInvocation(MethodInvocation invocation) {
        String uri = RuleSetUriHelper.getRuleSetURI(invocation, true);
        if (logger.isDebugEnabled()) {
            logger.debug("beforeMethodInvocation: Invoking rule set URI "
                    + uri + " for object " + invocation.getThis().toString());
        }
        executeRule(uri, invocation);
    }

    /**
     * This is executed after the business logic of the intercepted method.
     *
     * @param invocation the method invocation
     */
    private void afterMethodInvocation(MethodInvocation invocation) {
        String uri = RuleSetUriHelper.getRuleSetURI(invocation, false);
        if (logger.isDebugEnabled()) {
            logger.debug("afterMethodInvocation: Invoking rule set URI "
                    + uri + " for object " + invocation.getThis().toString());
        }
        executeRule(uri, invocation);
    }

    /**
     * Execute rules associated with the specified URI.
     *
     * @param uri the rule set URI
     * @param invocation the method invocation
     */
    private void executeRule(String uri, MethodInvocation invocation) {
        // only invoke the rule engine if there is an corresponding rule set
        // for the uri.
        if (ruleSource.hasRuleExecutionSet(uri)) {
            try {
                executeStateless(uri, getFacts(invocation));
            } catch (Exception exception) {
                throw new RuleEngineException(
                        RuleEngineException.ErrorCode.FailedToExecuteRule,
                        new Object[]{invocation.getMethod().getName()},
                        exception);
            }
        }
    }

    /**
     * Return the list of facts that should be injected into the working
     * memory
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