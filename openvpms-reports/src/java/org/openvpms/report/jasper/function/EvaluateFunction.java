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

package org.openvpms.report.jasper.function;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.functions.AbstractFunctionSupport;
import net.sf.jasperreports.functions.annotations.Function;
import net.sf.jasperreports.functions.annotations.FunctionCategories;
import net.sf.jasperreports.functions.annotations.FunctionParameter;
import net.sf.jasperreports.functions.annotations.FunctionParameters;
import org.openvpms.report.jasper.JDBCQueryExecuter;

/**
 * Provides a JasperReport function to evaluate an xpath expression.
 *
 * @author Tim Anderson
 */
@FunctionCategories({OpenVPMSCategory.class})
public class EvaluateFunction extends AbstractFunctionSupport {

    /**
     * Evaluates an xpath expression.
     *
     * @param expression the expression to evaluate
     * @return the result of the evaluation. May be {@code null}
     */
    @Function("EVALUATE")
    @FunctionParameters({@FunctionParameter("expression")})
    public Object EVALUATE(String expression) {
        JRDataSource dataSource = (JRDataSource) getContext().getParameterValue(JRParameter.REPORT_DATA_SOURCE, true);
        if (dataSource instanceof JDBCQueryExecuter.FieldDataSource) {
            return ((JDBCQueryExecuter.FieldDataSource) dataSource).getValue(expression);
        }
        return null;
    }

}
