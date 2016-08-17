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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesHolder;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRJdbcQueryExecuterFactory;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import org.apache.commons.jxpath.Functions;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@code JRQueryExecuterFactory} for {@link JDBCQueryExecuter}.
 *
 * @author Tim Anderson
 */
public class JDBCQueryExecuterFactory extends JRJdbcQueryExecuterFactory {


    /**
     * Fields to pass to the report. May be {@code null}
     */
    private ResolvingPropertySet fields;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The extension functions.
     */
    private final Functions functions;

    /**
     * Constructs a {@link JDBCQueryExecuterFactory}.
     *
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param functions the extension functions
     */
    public JDBCQueryExecuterFactory(IArchetypeService service, ILookupService lookups, Functions functions) {
        this.service = service;
        this.lookups = lookups;
        this.functions = functions;
    }

    /**
     * Sets fields to pass to the report.
     *
     * @param fields a map of additional field names and their values, to pass to the report. May be {@code null}
     */
    public void setFields(Map<String, Object> fields) {
        this.fields = (fields != null) ? new ResolvingPropertySet(fields, service, lookups) : null;
    }

    /**
     * Creates a query executer.
     *
     * @param context    the jasper reports context
     * @param report     the report
     * @param parameters the report parameters
     * @return a new query executer
     * @throws JRException for any error
     */
    public JRQueryExecuter createQueryExecuter(JasperReportsContext context, JasperReport report,
                                               Map<String, Object> parameters) throws JRException {
        JRDataset dataset = report.getMainDataset();
        return createQueryExecuter(context, dataset, convert(dataset, parameters));
    }

    /**
     * Creates a query executer.
     *
     * @param context    the jasper reports context
     * @param dataset    the report data set
     * @param parameters the report parameters
     * @return a new query executer
     * @throws JRException for any error
     */
    @Override
    public JRQueryExecuter createQueryExecuter(JasperReportsContext context, JRDataset dataset,
                                               Map<String, ? extends JRValueParameter> parameters) throws JRException {
        return new JDBCQueryExecuter(context, dataset, parameters, fields, service, lookups, functions);
    }

    /**
     * Helper to convert a map of parameters to those required by {@code JRJdbcQueryExecuter}.
     *
     * @param dataset    the report data set
     * @param parameters the report parameters
     * @return the converted parameters
     */
    public static Map<String, ? extends JRValueParameter> convert(JRDataset dataset, Map<String, Object> parameters) {
        JRParameter[] list = dataset.getParameters();
        Map<String, Parameter> result = new HashMap<>();
        if (list != null) {
            for (JRParameter parameter : list) {
                String name = parameter.getName();
                if (JRParameter.REPORT_PARAMETERS_MAP.equals(name)) {
                    result.put(name, new Parameter(parameter, parameters));
                } else {
                    result.put(name, new Parameter(parameter, parameters.get(name)));
                }
            }
        }
        return result;
    }

    /**
     * Helper to adapt JRParameter.
     */
    private static class Parameter implements JRValueParameter {

        /**
         * The underlying parameter.
         */
        private final JRParameter parameter;

        /**
         * The parameter value.
         */
        private Object value;

        /**
         * Constructs an {@link Parameter}.
         *
         * @param parameter the underlying parameter
         * @param value     the parameter value. May be {@code null}
         */
        public Parameter(JRParameter parameter, Object value) {
            this.parameter = parameter;
            this.value = value;
        }

        /**
         * Checks whether the object has any properties.
         *
         * @return whether the object has any properties
         */
        @Override
        public boolean hasProperties() {
            return parameter.hasProperties();
        }

        /**
         * Returns the parameter name.
         *
         * @return the parameter name
         */
        @Override
        public String getName() {
            return parameter.getName();
        }

        /**
         * Returns the parameter description.
         *
         * @return the parameter description
         */
        @Override
        public String getDescription() {
            return parameter.getDescription();
        }

        /**
         * Sets the parameter description.
         *
         * @param description the description
         */
        @Override
        public void setDescription(String description) {
            parameter.setDescription(description);
        }

        /**
         * Returns the parameter value class.
         *
         * @return the value class
         */
        @Override
        public Class getValueClass() {
            return parameter.getValueClass();
        }

        /**
         * Returns the parameter value class name.
         *
         * @return the value class name
         */
        @Override
        public String getValueClassName() {
            return parameter.getValueClassName();
        }

        /**
         * Determines if the parameter is system defined.
         *
         * @return {@code true} if the parameter is system defined
         */
        @Override
        public boolean isSystemDefined() {
            return parameter.isSystemDefined();
        }

        /**
         * Determines if the parameter is for prompting.
         *
         * @return {@code true} if the parameter is for prompting
         */
        @Override
        public boolean isForPrompting() {
            return parameter.isForPrompting();
        }

        /**
         * Returns the default value expression.
         *
         * @return the default value expression. May be {@code null}
         */
        @Override
        public JRExpression getDefaultValueExpression() {
            return parameter.getDefaultValueExpression();
        }

        /**
         * Returns the parameter nested value type.
         *
         * @return the nested value type for this parameter, or {@code null }if none set
         */
        @Override
        public Class getNestedType() {
            return parameter.getNestedType();
        }

        /**
         * Returns the name of the parameter nested value type.
         *
         * @return the name of the nested value type for this parameter, or {@code null} if none set
         */
        @Override
        public String getNestedTypeName() {
            return parameter.getNestedTypeName();
        }

        /**
         * Returns this object's properties map.
         *
         * @return this object's properties map
         */
        @Override
        public JRPropertiesMap getPropertiesMap() {
            return parameter.getPropertiesMap();
        }

        /**
         * Returns the parent properties holder, whose properties are used as defaults
         * for this object.
         *
         * @return the parent properties holder, or {@code null} if no parent
         */
        @Override
        public JRPropertiesHolder getParentProperties() {
            return parameter.getParentProperties();
        }

        /**
         * Returns the value assigned to the parameter.
         *
         * @return the value assigned to the parameter
         */
        @Override
        public Object getValue() {
            return value;
        }

        /**
         * Assigns the value to the parameter.
         *
         * @param value the value assigned to the parameter
         */
        @Override
        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public Object clone() {
            throw new JRRuntimeException("Clone not supported");
        }
    }

}
