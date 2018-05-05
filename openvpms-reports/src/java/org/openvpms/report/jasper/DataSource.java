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
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * JasperReports data source that operates on {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public interface DataSource extends JRDataSource {

    /**
     * Returns the field value.
     *
     * @param name the field name
     * @return the field value. May be {@code null}
     */
    Object getFieldValue(String name);

    /**
     * Evaluates an xpath expression.
     *
     * @param expression the expression
     * @return the result of the expression. May be {@code null}
     */
    Object evaluate(String expression);

    /**
     * Evaluates an xpath expression against an object.
     *
     * @param object     the object
     * @param expression the expression
     * @return the result of the expression. May be {@code null}
     */
    Object evaluate(Object object, String expression);

    /**
     * Returns a data source for a collection node.
     *
     * @param name the collection node name
     * @return the data source
     * @throws JRException for any error
     */
    JRRewindableDataSource getDataSource(String name) throws JRException;

    /**
     * Returns a data source for a collection node.
     *
     * @param name      the collection node name
     * @param sortNodes the list of nodes to sort on
     * @return the data source
     * @throws JRException for any error
     */
    JRRewindableDataSource getDataSource(String name, String[] sortNodes) throws JRException;

    /**
     * Returns a data source for the given jxpath expression.
     *
     * @param expression the expression. Must return an {@code Iterable} or {@code Iterator} returning
     *                   {@link IMObject}s
     * @return the data source
     * @throws JRException for any error
     */
    JRRewindableDataSource getExpressionDataSource(String expression) throws JRException;

}
