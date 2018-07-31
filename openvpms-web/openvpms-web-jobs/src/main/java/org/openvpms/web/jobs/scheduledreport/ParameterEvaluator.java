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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.jobs.scheduledreport;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.report.ParameterType;
import org.openvpms.web.workspace.admin.job.scheduledreport.ExpressionType;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;
import static org.openvpms.archetype.rules.util.DateRules.toDate;

/**
 * Scheduled report parameter evaluartor.
 *
 * @author Tim Anderson
 */
class ParameterEvaluator {

    /**
     * Evaluates each of the parameters.
     *
     * @param config         the scheduled report configuration
     * @param parameterTypes the available parameters
     * @return the evaluated parameters
     */
    public Map<String, Object> evaluate(IMObjectBean config, Set<ParameterType> parameterTypes) {
        Map<String, Object> result = new HashMap<>();
        int i = 0;
        while (true) {
            String node = "paramName" + i;
            if (config.hasNode(node)) {
                String name = config.getString(node);
                if (!StringUtils.isEmpty(name)) {
                    checkParameterType(parameterTypes, name);
                    Object value = evaluate(config, i);
                    result.put(name, value);
                }
            } else {
                break;
            }
            ++i;
        }
        return result;
    }

    /**
     * Returns the today's date.
     *
     * @return the today's date
     */
    protected LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Evaluates a parameter.
     *
     * @param config the scheduled report configuration
     * @param index  the parameter index
     * @return the parameter value
     */
    protected Object evaluate(IMObjectBean config, int index) {
        Class type = getClass(config.getString("paramType" + index));
        Object value;
        if (type != null && Date.class.isAssignableFrom(type)) {
            ExpressionType expressionType = getExpressionType(config, index);
            value = getDate(config, expressionType, index);
        } else {
            value = config.getValue("paramValue" + index);
        }
        return value;
    }

    /**
     * Evaluates a date parameter.
     *
     * @param config         the scheduled report configuration
     * @param expressionType the expression type
     * @param index          the parameter index
     * @return the parameter value
     */
    private Date getDate(IMObjectBean config, ExpressionType expressionType, int index) {
        Date result;
        if (expressionType == ExpressionType.NOW) {
            result = new Date();
        } else if (expressionType == ExpressionType.VALUE) {
            result = config.getDate("paramValue" + index);
        } else {
            LocalDate date = getDate(expressionType);
            result = toDate(date);
        }
        return result;
    }

    /**
     * Evaluates a date expression.
     *
     * @param expressionType the expression type
     * @return the date
     */
    private LocalDate getDate(ExpressionType expressionType) {
        LocalDate today = today();
        LocalDate result;
        switch (expressionType) {
            case TODAY:
                result = today;
                break;
            case YESTERDAY:
                result = today.minusDays(1);
                break;
            case TOMORROW:
                result = today.plusDays(1);
                break;
            case START_OF_MONTH:
                result = today.with(firstDayOfMonth());
                break;
            case END_OF_MONTH:
                result = today.with(lastDayOfMonth());
                break;
            case START_OF_LAST_MONTH:
                result = today.minusMonths(1).with(firstDayOfMonth());
                break;
            case END_OF_LAST_MONTH:
                result = today.minusMonths(1).with(lastDayOfMonth());
                break;
            case START_OF_NEXT_MONTH:
                result = today.plusMonths(1).with(firstDayOfMonth());
                break;
            case END_OF_NEXT_MONTH:
                result = today.plusMonths(1).with(lastDayOfMonth());
                break;
            case START_OF_YEAR:
                result = today.with(firstDayOfYear());
                break;
            case END_OF_YEAR:
                result = today.with(lastDayOfYear());
                break;
            case START_OF_LAST_YEAR:
                result = today.with(firstDayOfYear()).minusYears(1);
                break;
            case END_OF_LAST_YEAR:
                result = today.with(lastDayOfYear()).minusYears(1);
                break;
            case START_OF_NEXT_YEAR:
                result = today.with(firstDayOfYear()).plusYears(1);
                break;
            case END_OF_NEXT_YEAR:
                result = today.with(lastDayOfYear()).plusYears(1);
                break;
            case JUNE_30:
                result = today.withMonth(6).withDayOfMonth(30);
                break;
            case LAST_JUNE_30:
                result = today.withMonth(6).withDayOfMonth(30).minusYears(1);
                break;
            case NEXT_JUNE_30:
                result = today.withMonth(6).withDayOfMonth(30).plusYears(1);
                break;
            case JULY_1:
                result = today.withMonth(7).withDayOfMonth(1);
                break;
            case LAST_JULY_1:
                result = today.withMonth(7).withDayOfMonth(1).minusYears(1);
                break;
            case NEXT_JULY_1:
                result = today.withMonth(7).withDayOfMonth(1).plusYears(1);
                break;
            default:
                throw new IllegalArgumentException("Invalid argument value: " + expressionType);
        }
        return result;
    }

    /**
     * Returns the expression type of the specified parameter.
     *
     * @param config the configuration
     * @param index  the parameter index
     * @return the expression type
     */
    private ExpressionType getExpressionType(IMObjectBean config, int index) {
        return ExpressionType.valueOf(config.getString("paramExprType" + index, ExpressionType.VALUE.toString()));
    }

    /**
     * Returns a class given its name.
     *
     * @param className the class name. May be {@code null}
     * @return the class, or {@code null}
     */
    private Class getClass(String className) {
        Class type = null;
        if (!StringUtils.isEmpty(className)) {
            try {
                type = ClassUtils.getClass(className);
            } catch (ClassNotFoundException exception) {
                // no-op
            }
        }
        return type;
    }

    /**
     * Verifies that a parameter is supported by the report.
     *
     * @param parameterTypes the report parameter types
     * @param name           the parameter name
     * @throws IllegalStateException if the parameter is not supported
     */
    private void checkParameterType(Set<ParameterType> parameterTypes, String name) {
        for (ParameterType type : parameterTypes) {
            if (!type.isSystem() && StringUtils.equals(type.getName(), name)) {
                return;
            }
        }
        throw new IllegalStateException("Invalid parameter " + name);
    }


}
