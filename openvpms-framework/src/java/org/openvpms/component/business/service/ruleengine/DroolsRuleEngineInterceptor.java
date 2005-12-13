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
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// aop alliance
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

// log4j
import org.apache.log4j.Logger;

// commons-lang
import org.apache.commons.lang.StringUtils;

//commons-io
import org.apache.commons.io.FileUtils;

// spring modules
import org.springmodules.jsr94.core.Jsr94RuleSupport;

/**
 * The Drools rule engine is integrated through the JSR-94 runtime api and the
 * springframework interceptors. This provides a separation of concerns between
 * the service objects, which contains the static business logic and the rules
 * engine that provides dynamic business logic capabilities.
 * <p>
 * The class extends {@link Jsr94RuleSupport}, which eases the integration and
 * provides convenience methods for creating session with the rule engine and 
 * exeucting rules. 
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
     * The base directory where all rules are stored
     */
    private String ruleDirectory;
    
    /**
     * Caches the name of the various rule sets
     */
     private Map<String, String> ruleSetNames =
         new HashMap<String, String>();
    
    
    /**
     * Create an instance of the interceptor by specifying the base rules
     * directory. This directory should contain a rules file for each service
     * and operation that the requires rule engine invocation. 
     */
    public DroolsRuleEngineInterceptor(String ruleDirectroy) {
        this.ruleDirectory = ruleDirectroy;
        cacheRuleSets();
    }
    
    /* (non-Javadoc)
     * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object rval = null;
        
        beforeMethodInvocation(invocation);
        rval = invocation.proceed();
        afterMethodInvocation(invocation);

        return rval;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        // no op

    }

    /**
     * @return Returns the ruleDirectory.
     */
    public String getRuleDirectory() {
        return ruleDirectory;
    }

    /**
     * @param ruleDirectory The ruleDirectory to set.
     */
    public void setRuleDirectory(String ruleDirectory) {
        this.ruleDirectory = ruleDirectory; 
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
        // TODO Need a standard mechanism to do it.
        if (ruleSetNames.containsKey(uri)) {
            executeStateless(uri, Arrays.asList(invocation.getArguments()));
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
        // TODO Need a standard mechanism to do it.
        if (ruleSetNames.containsKey(uri)) {
            executeStateless(uri, Arrays.asList(invocation.getArguments()));
        }
    }

    /**
     * Iterate through the ruleDirectory and its sub-directories and cache
     * the names of every Drools rule file (i.e. with extension drl). The
     * file name determines when to apply the rule set.
     * 
     * @throws RuleEngineException
     *            thrown if a rule directory was not specified or an invalud
     *            rule directory was specified
     */
    private void cacheRuleSets() {
        if (StringUtils.isEmpty(ruleDirectory)) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.NoRuleDirSpecified);
        }
        
        // check that a valid directory was specified
        File dir = new File(ruleDirectory);
        if (!dir.isDirectory()) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidDir,
                    new Object[] { ruleDirectory });
        }
        // cache all file names encountered
        Collection collection = FileUtils.listFiles(dir, new String[] {"drl"}, true);
        Iterator files = collection.iterator();
        while (files.hasNext()) {
            File file = (File)files.next();
            ruleSetNames.put(file.getName(), file.getAbsolutePath());
        }
    }
}
