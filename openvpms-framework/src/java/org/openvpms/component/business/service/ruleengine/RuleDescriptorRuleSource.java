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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// jsr94
import javax.rules.admin.RuleExecutionSet;

// spring-modules
import org.apache.log4j.Logger;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

/**
 * This class is responsible for loading rules from a rule descriptor file. The
 * rule descriptor file contains an entry for each rule that is to be loaded. 
 * Each rule contains a source and path attribute, which denotes how to load
 * the rule and the location of the rule respectively.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class RuleDescriptorRuleSource extends BaseRuleSource {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(RuleDescriptorRuleSource.class);

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
     * @see javax.rules.admin.RuleAdministrator#getLocalRuleExecutionSetProvider(java.util.Map)
     */
    private Map providerProperties;

    /**
     * Local ruleset properties -- passed to the createRuleExecutionSet method
     * This field can be null.
     * 
     * @see javax.rules.admin.LocalRuleExecutionSetProvider#createRuleExecutionSet(java.io.InputStream,
     *      java.util.Map)
     */
    private Map rulesetProperties;

    /**
     * Rule execution set registration properties -- passed to the
     * registerRuleExecutionSet method This field can be null.
     * 
     * @see javax.rules.admin.RuleAdministrator#registerRuleExecutionSet(java.lang.String,
     *      javax.rules.admin.RuleExecutionSet, java.util.Map)
     */
    private Map registrationProperties;

    /*
     * (non-Javadoc)
     * 
     * @see org.springmodules.jsr94.support.AbstractRuleSource#registerRuleExecutionSets()
     */
    protected void registerRuleExecutionSets() throws RuleEngineException {
        RuleDescriptors rdescs = null;
        try {
            Mapping mapping = new Mapping();
            mapping.loadMapping(new InputSource(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(
                            "org/openvpms/component/business/service/ruleengine/rule-source-mapping-file.xml"))));
            
            InputStreamReader is = new InputStreamReader(
                    Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(descriptorFileName));
            if (is == null) {
                throw new RuleEngineException(
                        RuleEngineException.ErrorCode.InvalidDescriptorFileName,
                        new Object[] {descriptorFileName});
                
            }
            rdescs = (RuleDescriptors)new Unmarshaller(mapping).unmarshal(is);
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToRegisterRuleExecutionSets,
                    null, exception);
                    
        }
            
        // for each on process according to the source
        for (RuleDescriptor rdesc : rdescs.getRuleDescriptors()) {
            try {
                switch (rdesc.getSource()) {
                case SYSTEM:
                    processFromSytemPath(rdesc.getPath());
                    break;
                    
                case CLASSPATH:
                    processFromClassPath(rdesc.getPath());
                    break;
                    
                }
            } catch (Exception exception) {
                throw new RuleEngineException(
                        RuleEngineException.ErrorCode.FailedToProcessRuleDescriptor,
                        new Object[] {rdesc.getSource(), rdesc.getPath()},
                        exception);
            }
        }
            
    }

    /**
     * The following path refers to a file that is stored in the class path
     * 
     * @param path
     *            a valid path in the class path
     * @throws Exception
     *            propagate exception to caller            
     */
    private void processFromClassPath(String path) 
    throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidFile,
                    new Object[] { path });
        }
        
        registerRuleExecutionSet(is);
        
    }

    /**
     * The following path refers to a file that is stored in the file system
     * 
     * @param path
     *            a valid path in the file system
     * @throws Exception
     *            propagate exception to caller            
     */
    private void processFromSytemPath(String path) 
    throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidFile,
                    new Object[] {path});
        }
        
        registerRuleExecutionSet(new FileInputStream(file));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springmodules.jsr94.support.AbstractRuleSource#initRuleSource()
     */
    protected void initRuleSource() throws Exception {
    }

    /**
     * Sets new value for field providerProperties
     * 
     * @param providerProperties
     *            The providerProperties to set.
     */
    public void setProviderProperties(Map providerProperties) {
        this.providerProperties = providerProperties;
    }

    /**
     * Sets new value for field registrationProperties
     * 
     * @param registrationProperties
     *            The registrationProperties to set.
     */
    public void setRegistrationProperties(Map registrationProperties) {
        this.registrationProperties = registrationProperties;
    }

    /**
     * Sets new value for field rulesetProperties
     * 
     * @param rulesetProperties
     *            The rulesetProperties to set.
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
     * Indicates whether there is a rule set defined for the specified 
     * uri
     * 
     * @param uri
     *            the rule set uri
     */
    public boolean hasRuleExecutionSet(String uri) {
        return ruleSetNames.containsKey(uri);
    }
    
    /**
     * Create and register a rule execution set
     * 
     * @param is
     *            the input stream to a rule set
     * @throws RuleEngineException 
     *            if it cannot create or register the rule set            
     */
    private void registerRuleExecutionSet(InputStream is) {
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
                    new Object[] {uri}, exception);
        }
    }
}
