/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.component.business.service.ruleengine;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import javax.rules.admin.RuleExecutionSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * This class is responsible for loading rules from a rule descriptor file. The
 * rule descriptor file contains an entry for each rule that is to be loaded.
 * Each rule contains a source and path attribute, which denotes how to load
 * the rule and the location of the rule respectively.
 *
 * @author Jim Alateras
 */
public class RuleDescriptorRuleSource extends BaseRuleSource {

    /**
     * The descriptor file name that contains the rule entries. It
     * defaults to an xml file in the root class path
     */
    private String descriptorFileName = "rules.xml";

    /**
     * Caches the name of the various rule sets
     */
    private Map<String, String> ruleSetNames =
            new HashMap<String, String>();

    /**
     * Local Rule execution set provider properties -- passed to the
     * getLocalRuleExecutionSetProvider method. This field can be null.
     *
     * @see javax.rules.admin.RuleAdministrator#getLocalRuleExecutionSetProvider(Map)
     */
    private Map providerProperties;

    /**
     * Local ruleset properties -- passed to the createRuleExecutionSet method
     * This field can be null.
     *
     * @see javax.rules.admin.LocalRuleExecutionSetProvider#createRuleExecutionSet(InputStream,
     *      Map)
     */
    private Map rulesetProperties;

    /**
     * Rule execution set registration properties -- passed to the
     * registerRuleExecutionSet method This field can be null.
     *
     * @see javax.rules.admin.RuleAdministrator#registerRuleExecutionSet(String,
     *      RuleExecutionSet, Map)
     */
    private Map registrationProperties;

    /**
     * The rule source mapping resource path.
     */
    private static final String RULE_SOURCE_MAPPING
            = "org/openvpms/component/business/service/ruleengine/rule-source-mapping-file.xml";


    /*
     * (non-Javadoc)
     * 
     * @see org.springmodules.jsr94.support.AbstractRuleSource#registerRuleExecutionSets()
     */
    protected void registerRuleExecutionSets() throws RuleEngineException {
        RuleDescriptors rdescs;
        try {
            Mapping mapping = new Mapping();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            mapping.loadMapping(new InputSource(new InputStreamReader(
                    loader.getResourceAsStream(RULE_SOURCE_MAPPING))));
            Unmarshaller unmarshaller = new Unmarshaller(mapping);

            Enumeration<URL> urls = loader.getResources(descriptorFileName);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                InputStreamReader is = new InputStreamReader(url.openStream());
                rdescs = (RuleDescriptors) unmarshaller.unmarshal(is);
                addRules(rdescs);
            }
        } catch (RuleEngineException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToRegisterRuleExecutionSets,
                    exception);

        }
    }

    /**
     * Sets new value for field providerProperties
     *
     * @param providerProperties The providerProperties to set.
     */
    public void setProviderProperties(Map providerProperties) {
        this.providerProperties = providerProperties;
    }

    /**
     * Sets new value for field registrationProperties
     *
     * @param registrationProperties The registrationProperties to set.
     */
    public void setRegistrationProperties(Map registrationProperties) {
        this.registrationProperties = registrationProperties;
    }

    /**
     * Sets new value for field rulesetProperties
     *
     * @param rulesetProperties The rulesetProperties to set.
     */
    public void setRulesetProperties(Map rulesetProperties) {
        this.rulesetProperties = rulesetProperties;
    }

    /**
     * @return Returns the descriptorFileName.
     */
    public String getDescriptorFileName() {
        return descriptorFileName;
    }

    /**
     * @param descriptorFileName The descriptorFileName to set.
     */
    public void setDescriptorFileName(String descriptorFileName) {
        this.descriptorFileName = descriptorFileName;
    }

    /**
     * Indicates whether there is a rule set defined for the specified uri.
     *
     * @param uri the rule set uri
     */
    public boolean hasRuleExecutionSet(String uri) {
        return ruleSetNames.containsKey(uri);
    }

    /**
     * Registers rules from their descriptors.
     *
     * @param descriptors the rule descriptors
     */
    private void addRules(RuleDescriptors descriptors) {
        // for each on process according to the source
        for (RuleDescriptor rdesc : descriptors.getRuleDescriptors()) {
            try {
                switch (rdesc.getSource()) {
                    case SYSTEM:
                        registerFromFile(rdesc.getPath());
                        break;

                    case CLASSPATH:
                        registerFromResource(rdesc.getPath());
                        break;
                }
            } catch (Exception exception) {
                throw new RuleEngineException(
                        RuleEngineException.ErrorCode.FailedToProcessRuleDescriptor,
                        exception, rdesc.getSource(), rdesc.getPath());
            }
        }
    }

    /**
     * Register rule execution sets from a resource path.
     *
     * @param path the resource path
     */
    private void registerFromResource(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream(path);
        if (is == null) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidFile, path);
        }
        registerRuleExecutionSet(is, path);
    }

    /**
     * Register rule execution sets from a file.
     *
     * @param path the file path
     * @throws IOException for any I/O error
     */
    private void registerFromFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidFile, path);
        }
        registerRuleExecutionSet(new FileInputStream(file), path);
    }

    /**
     * Create and register a rule execution set.
     *
     * @param is   the input stream to a rule set
     * @param path the file/resource path of the stream
     * @throws RuleEngineException if it cannot create or register the rule set
     */
    private void registerRuleExecutionSet(InputStream is, String path) {
        String uri = null;
        try {
            RuleExecutionSet ruleExecutionSet = ruleAdministrator
                    .getLocalRuleExecutionSetProvider(providerProperties)
                    .createRuleExecutionSet(is, rulesetProperties);
            uri = ruleExecutionSet.getName();
            ruleAdministrator.registerRuleExecutionSet(uri, ruleExecutionSet,
                                                       registrationProperties);
            ruleSetNames.put(uri, uri);
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToRegister,
                    exception, uri, path);
        }
    }
}
