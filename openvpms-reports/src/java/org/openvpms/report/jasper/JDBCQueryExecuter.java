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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRJdbcQueryExecuter;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectVariables;
import org.openvpms.component.business.service.archetype.helper.ResolvingPropertySet;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.report.AbstractExpressionEvaluator;

import java.util.HashMap;
import java.util.Map;

/**
 * An extension to the {@code JRJdbcQueryExecuter} that supports fields passed to the report.
 *
 * @author Tim Anderson
 * @see AbstractJasperIMReport
 */
public class JDBCQueryExecuter extends JRJdbcQueryExecuter {

    /**
     * The fields.
     */
    private final PropertySet fields;

    /**
     * The report fields.
     */
    private Map<String, JRField> reportFields = new HashMap<>();

    /**
     * The report parameters.
     */
    private Map<String, String> reportParameters = new HashMap<>();

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
     * The logger.
     */
    private static final Log log = LogFactory.getLog(JDBCQueryExecuter.class);

    /**
     * Constructs an {@link JDBCQueryExecuter}.
     *
     * @param context    the jasper reports report context
     * @param dataset    the report data set
     * @param parameters the report parameters
     * @param fields     a map of additional field names and their values, to pass to the report. May be {@code null}
     * @param service    the archetype service
     * @param lookups    the lookup service
     * @param functions  the extension functions
     */
    public JDBCQueryExecuter(JasperReportsContext context, JRDataset dataset,
                             Map<String, ? extends JRValueParameter> parameters, ResolvingPropertySet fields,
                             IArchetypeService service, ILookupService lookups, Functions functions) {
        super(context, dataset, parameters);
        this.service = service;
        this.lookups = lookups;
        this.functions = functions;
        this.fields = fields;
        for (JRField field : dataset.getFields()) {
            if (!field.getName().startsWith("[")) {
                reportFields.put("F." + field.getName(), field);
            }
        }
        for (JRParameter parameter : dataset.getParameters()) {
            if (parameter.isForPrompting() && !parameter.isSystemDefined()) {
                reportParameters.put("P." + parameter.getName(), parameter.getName());
            }
        }
    }

    /**
     * Creates a data source.
     *
     * @throws JRException for any error
     */
    @Override
    public JRDataSource createDatasource() throws JRException {
        return new FieldDataSource(super.createDatasource());
    }

    /**
     * Wraps an {@code JRDataSource}, in order to support {@link #fields}.
     */
    public class FieldDataSource implements DataSource {

        /**
         * The data source to delegate to. May be {@code null} if the report has no SQL statement.
         */
        private JRDataSource dataSource;

        /**
         * The expression evaluator.
         */
        private JDBCExpressionEvaluator evaluator;

        /**
         * Constructs an {@link FieldDataSource}.
         *
         * @param dataSource the data source to wrap. May be {@code null}
         */
        public FieldDataSource(JRDataSource dataSource) {
            this.dataSource = dataSource;
            evaluator = new JDBCExpressionEvaluator(dataSource, fields, service, lookups, functions);
        }

        /**
         * Tries to position the cursor on the next element in the data source.
         *
         * @return true if there is a next record, false otherwise
         * @throws JRException if any error occurs while trying to move to the next element
         */
        @Override
        public boolean next() throws JRException {
            return dataSource != null && dataSource.next();
        }

        /**
         * Returns the field value.
         *
         * @param name the field name
         * @return the field value. May be {@code null}
         */
        @Override
        public Object getFieldValue(String name) {
            return evaluator.getValue(name);
        }

        /**
         * Gets the field value for the current position.
         *
         * @return an object containing the field value. The object type must be the field object type.
         */
        @Override
        public Object getFieldValue(JRField field) throws JRException {
            return evaluator.getValue(field);
        }

        /**
         * Evaluates an xpath expression.
         *
         * @param expression the expression
         * @return the result of the expression. May be {@code null}
         */
        @Override
        public Object evaluate(String expression) {
            return evaluator.evaluate(expression);
        }

        /**
         * Evaluates an xpath expression against an object.
         *
         * @param object     the object
         * @param expression the expression
         * @return the result of the expression. May be {@code null}
         */
        @Override
        public Object evaluate(Object object, String expression) {
            return evaluator.evaluate(object, expression);
        }

        /**
         * Returns a data source for a collection node.
         *
         * @param name the collection node name
         * @return the data source
         * @throws JRException for any error
         */
        @Override
        public JRRewindableDataSource getDataSource(String name) throws JRException {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a data source for a collection node.
         *
         * @param name      the collection node name
         * @param sortNodes the list of nodes to sort on
         * @return the data source
         * @throws JRException for any error
         */
        @Override
        public JRRewindableDataSource getDataSource(String name, String[] sortNodes) throws JRException {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns a data source for the given jxpath expression.
         *
         * @param expression the expression. Must return an {@code Iterable} or {@code Iterator} returning
         *                   {@link IMObject}s
         * @return the data source
         * @throws JRException for any error
         */
        @Override
        public JRRewindableDataSource getExpressionDataSource(String expression) throws JRException {
            throw new UnsupportedOperationException();
        }
    }

    private class JDBCExpressionEvaluator extends AbstractExpressionEvaluator<Object> {

        /**
         * The data source.
         */
        private final JRDataSource dataSource;

        /**
         * Constructs a {@link JDBCExpressionEvaluator}.
         *
         * @param fields    additional report fields. These override any in the report. May be {@code null}
         * @param service   the archetype service
         * @param lookups   the lookup service
         * @param functions the JXPath extension functions
         */
        public JDBCExpressionEvaluator(JRDataSource dataSource, PropertySet fields, IArchetypeService service,
                                       ILookupService lookups, Functions functions) {
            super(new Object(), null, fields, service, lookups, functions);
            this.dataSource = dataSource;
        }

        public Object getValue(JRField field) {
            String expression = field.getName();
            Object result;
            try {
                if (isJXPath(expression)) {
                    result = getJXPathValue(expression);
                } else if (isField(expression)) {
                    result = getFieldValue(expression);
                } else {
                    result = dataSource.getFieldValue(field);
                }
            } catch (Exception exception) {
                log.warn("Failed to evaluate: " + expression, exception);
                // TODO localise
                result = "Expression Error";
            }
            return result;
        }

        /**
         * Returns a node value.
         *
         * @param name the node name
         * @return {@code null}
         */
        @Override
        protected Object getNodeValue(String name) {
            return null;
        }

        @Override
        protected IMObjectVariables createVariables() {
            return new IMObjectVariables(service, lookups) {
                /**
                 * Determines if a variable exists.
                 *
                 * @param name the variable name
                 * @return {@code true} if the variable exists
                 */
                @Override
                public boolean exists(String name) {
                    return reportFields.containsKey(name) || reportParameters.containsKey(name) || super.exists(name);
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
                    JRField field = reportFields.get(varName);
                    if (field != null) {
                        try {
                            return dataSource.getFieldValue(field);
                        } catch (JRException e) {
                            throw new IllegalStateException("Failed to retrieve value for field " + varName, e);
                        }
                    } else {
                        String parameter = reportParameters.get(varName);
                        if (parameter != null) {
                            return getParameterValue(parameter);
                        }
                    }
                    return super.getVariable(varName);
                }
            };
        }
    }

}
