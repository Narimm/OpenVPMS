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

package org.openvpms.report;

import org.apache.commons.jxpath.JXPathContext;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.system.common.jxpath.JXPathHelper;

import java.util.Date;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;


/**
 * Evaluates report expressions.
 * <p/>
 * Expressions may take one of two forms:
 * <ol>
 * <li>node1.node2.nodeN</li>
 * <li>${expr}</li>
 * </ol>
 * Expressions of the first type are evaluated using {@link NodeResolver};
 * the second by <code>JXPath</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ExpressionEvaluator {

    /**
     * The object.
     */
    private final IMObject object;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The JXPath context.
     */
    private JXPathContext context;

    /**
     * The node resolver.
     */
    private NodeResolver resolver;


    /**
     * Constructs a new <code>ExpressionEvaluator</code>.
     *
     * @param object  the object
     * @param service the archetype service
     */
    public ExpressionEvaluator(IMObject object, IArchetypeService service) {
        this.object = object;
        this.service = service;
    }

    /**
     * Returns the value of an expression.
     * If the expression is of the form ${expr} this will be evaluated
     * using <code>JXPath</code>, otherwise it will be evaluated using
     * {@link NodeResolver}.
     *
     * @param expression the expression
     * @return the result of the expression
     */
    public Object getValue(String expression) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String eval = expression.substring(2, expression.length() - 1);
            return evaluate(eval);
        } else {
            return getNodeValue(expression);
        }
    }

    /**
     * Returns the formatted value of an expression.
     *
     * @param expression the expression
     * @return the result of the expression
     */
    public String getFormattedValue(String expression) {
        Object value = getValue(expression);
        if (value instanceof Date) {
            Date date = (Date) value;
            return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        } else if (value instanceof Money) {
            return NumberFormat.getCurrencyInstance().format(value);
        } else if (value instanceof BigDecimal) {
            DecimalFormat format = new DecimalFormat("#,##0.00;-#,##0.00");
            return format.format(value);
        } else if (value != null) {
            return value.toString();
        }
        return null;
    }

    /**
     * Evaluates an expression.
     *
     * @param expression the expression to evaluate
     * @return the value of the expression
     */
    protected Object evaluate(String expression) {
        if (context == null) {
            context = JXPathHelper.newContext(object);
        }
        return context.getValue(expression);
    }

    /**
     * Returns a node value.
     *
     * @param name the node name
     * @return the node value
     */
    protected Object getNodeValue(String name) {
        if (resolver == null) {
            resolver = new NodeResolver(object, service);
        }
        return ReportHelper.getValue(name, resolver);
    }

}
