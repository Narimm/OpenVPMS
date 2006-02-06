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


package org.openvpms.component.system.service.ognl;

import org.apache.commons.jxpath.JXPathContext;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class JXPathEvaluator {
    /**
     * The root object for the JXPath contect
     */
    private Object root;
    
    /**
     * The jxpath expression
     */
    private String expression;
    
    /**
     * The value that is used when a call to set is made
     */
    private Object value;
    
    /**
     * Contstruct a JXPath evaluator using a root object and an expression.
     * 
     * @param root
     *            the root object
     * @param expr
     *            the xpath expression            
     */
    public JXPathEvaluator(Object root, String expr) {
        this.root = root;
        this.expression = expr;
    }

    /**
     * Contstruct a JXPath evaluator using a root object, an expression
     * and a value object. The value object is used during a set epression
     * on the root object
     * 
     * @param root
     *            the root object
     * @param expr
     *            the xpath expression
     * @param value
     *            the value to use during a set                        
     */
    public JXPathEvaluator(Object root, String expr, Object value) {
        this.root = root;
        this.expression = expr;
        this.value = value;
    }

    /**
     * @return Returns the expression.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression The expression to set.
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * @return Returns the root.
     */
    public Object getRoot() {
        return root;
    }

    /**
     * @param root The root to set.
     */
    public void setRoot(Object root) {
        this.root = root;
    }
    
    /**
     * @return Returns the value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Evaluate a getValue on the JXPathContect object
     * 
     * @return Object
     */
    public Object evaluateGetValue() {
        return JXPathContext.newContext(root).getValue(expression);
    }
    
    /**
     * Evaluate a setValue on the JXPathContect object
     */
    public void evaluateSetValue() {
        JXPathContext.newContext(root).setValue(expression, value);
    }
}
