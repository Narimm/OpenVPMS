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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.macro.IMObjectVariables;
import org.openvpms.web.system.ServiceHelper;

/**
 * Formats file names based on a document template's {@link DocumentTemplate#getFileNameExpression()}.
 *
 * @author Tim Anderson
 */
public class FileNameFormatter {

    /**
     * The archetype service.
     */
    private final IArchetypeService archetypeService;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Characters to exclude from file names.
     */
    private static final String ILLEGAL_CHARACTERS = "\\|/|:|\\*|\\?|<|>|\\|";

    /**
     * Constructs an {@link FileNameFormatter}.
     */
    public FileNameFormatter() {
        archetypeService = ServiceHelper.getArchetypeService();
        lookups = ServiceHelper.getLookupService();
    }

    /**
     * Formats a file name using the jxpath expression returned by {@link DocumentTemplate#getFileNameExpression()}.
     *
     * @param name     the original file name. The base name of this is passed to the expression in the {@code $file}
     *                 variable
     * @param object   the context object. If this an act, any related customer, patient, or supplier will be passed as
     *                 the variables $customer, $patient, and $supplier respectively.
     * @param template the document template
     * @return the formatted name, or {@code name} if the template doesn't specify a format, or generation fails
     */
    public String format(String name, IMObject object, DocumentTemplate template) {
        String result;
        String expression = template.getFileNameExpression();
        if (!StringUtils.isEmpty(expression)) {
            String file = FilenameUtils.getBaseName(name);
            JXPathContext context = JXPathHelper.newContext(object != null ? object : new Object());
            FileNameVariables variables = new FileNameVariables(archetypeService,
                                                                lookups);
            context.setVariables(variables);
            Party patient = null;
            Party customer = null;
            Party supplier = null;
            if (object instanceof Act) {
                Act act = (Act) object;
                ActBean bean = new ActBean(act);

                if (bean.hasNode("patient")) {
                    patient = (Party) bean.getNodeParticipant("patient");
                }
                if (bean.hasNode("customer")) {
                    customer = (Party) bean.getNodeParticipant("customer");
                } else if (patient != null) {
                    customer = ServiceHelper.getBean(PatientRules.class).getOwner(patient, act.getActivityStartTime(),
                                                                                  false);
                }
                if (bean.hasNode("supplier")) {
                    supplier = (Party) bean.getNodeParticipant("supplier");
                }
            }
            variables.declareVariable("customer", customer);
            variables.declareVariable("patient", patient);
            variables.declareVariable("supplier", supplier);
            variables.declareVariable("file", file);
            try {
                Object value = context.getValue(expression);
                result = (value != null) ? clean(value.toString()) : name;
            } catch (Throwable exception) {
                result = name;
            }
        } else {
            result = name;
        }
        return result;
    }

    private String clean(String name) {
        return name.replaceAll(ILLEGAL_CHARACTERS, "_");
    }

    private static class FileNameVariables implements Variables {

        private final IMObjectVariables variables;

        public FileNameVariables(IArchetypeService service, ILookupService lookups) {
            variables = new IMObjectVariables(service, lookups);
        }

        /**
         * Returns true if the specified variable is declared.
         *
         * @param varName variable name
         * @return boolean
         */
        @Override
        public boolean isDeclaredVariable(String varName) {
            return variables.exists(varName);
        }

        /**
         * Returns the value of the specified variable.
         *
         * @param varName variable name
         * @return Object value
         * @throws IllegalArgumentException if there is no such variable.
         */
        @Override
        public Object getVariable(String varName) {
            return variables.get(varName);
        }

        /**
         * Defines a new variable with the specified value or modifies
         * the value of an existing variable.
         * May throw UnsupportedOperationException.
         *
         * @param varName variable name
         * @param value   to declare
         */
        @Override
        public void declareVariable(String varName, Object value) {
            variables.add(varName, value);
        }

        /**
         * Removes an existing variable
         *
         * @param varName is a variable name without the "$" sign
         */
        @Override
        public void undeclareVariable(String varName) {
            // no-op
        }
    }
}
