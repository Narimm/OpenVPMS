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

import org.openvpms.component.business.service.ruleengine.RuleEngineException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springmodules.jsr94.rulesource.AbstractRuleSource;

import javax.rules.admin.LocalRuleExecutionSetProvider;
import javax.rules.admin.RuleAdministrator;
import javax.rules.admin.RuleExecutionSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A <code>RuleSource</code> that loads rules using {@link Class#getResource}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ResourceRuleSource extends AbstractRuleSource {

    /**
     * The rule resources.
     */
    private List<Resource> _resources;

    /**
     * Caches the name of the various rule sets.
     */
    private Map<String, String> _ruleSetNames = new HashMap<String, String>();

    /**
     * Local Rule execution set provider properties - passed to the
     * getLocalRuleExecutionSetProvider method. May be <code>null</code>
     *
     * @see RuleAdministrator#getLocalRuleExecutionSetProvider(java.util.Map)
     */
    private Map _providerProperties;

    /**
     * Local ruleset properties - passed to the createRuleExecutionSet method.
     * May be <code>null</code>.
     *
     * @see LocalRuleExecutionSetProvider#createRuleExecutionSet
     */
    private Map _rulesetProperties;

    /**
     * Rule execution set registration properties - passed to the
     * registerRuleExecutionSet method. May be <code>null</code>.
     *
     * @see RuleAdministrator#registerRuleExecutionSet
     */
    private Map _registrationProperties;


    /**
     * Sets the rule resources to be found in the class path.
     *
     * @param rules the rule resources
     */
    public void setRuleResources(String[] rules) {
        _resources = new ArrayList<Resource>();
        for (String rule : rules) {
            _resources.add(new ClassPathResource(rule.trim()));
        }
    }

    /**
     * Sets the provider properties.
     *
     * @param properties the provider properties.
     */
    public void setProviderProperties(Map properties) {
        _providerProperties = properties;
    }

    /**
     * Sets the registration properties.
     *
     * @param properties the registration properties
     */
    public void setRegistrationProperties(Map properties) {
        _registrationProperties = properties;
    }

    /**
     * Sets the ruleset properties.
     *
     * @param properties the ruleset properties.
     */
    public void setRulesetProperties(Map properties) {
        _rulesetProperties = properties;
    }

    /**
     * Indicates whether there is a rule set defined for the specified uri.
     *
     * @param uri the rule set uri
     */
    public boolean hasRuleExecutionSet(String uri) {
        return _ruleSetNames.containsKey(uri);
    }

    /**
     * Registers the rule excecution sets.
     *
     * @throws RuleEngineException if a ruleset can't be registered
     */
    protected void registerRuleExecutionSets() throws RuleEngineException {
        if (_resources != null) {
            for (Resource resource : _resources) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Registering the rule set in  " + resource);
                }
                registerRuleExecutionSet(resource);
            }
        }
    }

    /**
     * Create and register a rule execution set.
     *
     * @param resource the reource containing the rule set
     * @throws RuleEngineException if it cannot create or register the rule set
     */
    private void registerRuleExecutionSet(Resource resource) {
        // creater and register the rule execution set (i.e. rule set)
        try {
            LocalRuleExecutionSetProvider provider
                    = ruleAdministrator.getLocalRuleExecutionSetProvider(
                    _providerProperties);
            RuleExecutionSet ruleExecutionSet
                    = provider.createRuleExecutionSet(resource.getInputStream(),
                                                      _rulesetProperties);
            String uri = ruleExecutionSet.getName();
            ruleAdministrator.registerRuleExecutionSet(uri, ruleExecutionSet,
                                                       _registrationProperties);
            _ruleSetNames.put(uri, resource.getFilename());
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToRegister,
                    new Object[]{resource.getFilename()}, exception);
        }
    }

}
