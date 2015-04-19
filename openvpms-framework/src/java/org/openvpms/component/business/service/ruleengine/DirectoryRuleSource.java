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
 *  $Id: AuditServiceTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */
package org.openvpms.component.business.service.ruleengine;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.rules.admin.RuleExecutionSet;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This class will source rule-sets from one or more drl files stored in the
 * specified root directory. It will search all subdirectories and process and
 * rule files.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
public class DirectoryRuleSource extends BaseRuleSource {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DirectoryRuleSource.class);

    /**
     * The root rule directory.
     */
    private String directory;

    /**
     * Caches the name of the various rule sets.
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
     * @see javax.rules.admin.LocalRuleExecutionSetProvider#createRuleExecutionSet(java.io.InputStream,
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

    /*
     * (non-Javadoc)
     * 
     * @see org.springmodules.jsr94.support.AbstractRuleSource#registerRuleExecutionSets()
     */
    protected void registerRuleExecutionSets() throws RuleEngineException {
        // check that a non-null directory was specified
        if (StringUtils.isEmpty(directory)) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.NoDirSpecified);
        }

        // check that a valid directory was specified
        File dir = FileUtils.toFile(Thread.currentThread()
                .getContextClassLoader().getResource(directory));
        if (dir == null) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidDir, directory);
        }

        if (log.isDebugEnabled()) {
            log.debug("The base rules directory is " + dir.getAbsolutePath());
        }

        if (!dir.isDirectory()) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidDir, directory);
        }

        // process all the files in the directory, that match the filter
        Collection collection = FileUtils.listFiles(dir, new String[]{"drl"},
                                                    true);
        for (Object object : collection) {
            File file = (File) object;
            if (log.isDebugEnabled()) {
                log.debug("Registering the rule set in "
                        + file.getAbsolutePath());
            }
            registerRuleExecutionSet(file);
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
     * @return Returns the directory.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @param directory The directory to set.
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Indicates whether there is a rule set defined for the specified
     * uri
     *
     * @param uri the rule set uri
     */
    public boolean hasRuleExecutionSet(String uri) {
        return ruleSetNames.containsKey(uri);
    }

    /**
     * Create and register a rule execution set
     *
     * @param file the file containing the rule set
     * @throws RuleEngineException if it cannot create or register the rule set
     */
    private void registerRuleExecutionSet(File file) {
        // check that the file exists.
        if (!file.exists()) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidFile, file.getName());
        }

        // creater and register the rule execution set (i.e. rule set)
        try {
            RuleExecutionSet ruleExecutionSet = ruleAdministrator
                    .getLocalRuleExecutionSetProvider(providerProperties)
                    .createRuleExecutionSet(new FileInputStream(file),
                                            rulesetProperties);
            String uri = ruleExecutionSet.getName();
            ruleAdministrator.registerRuleExecutionSet(uri, ruleExecutionSet,
                                                       registrationProperties);
            ruleSetNames.put(uri, file.getName());
        } catch (Exception exception) {
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.FailedToRegister,
                    exception, file.getName());
        }
    }
}
