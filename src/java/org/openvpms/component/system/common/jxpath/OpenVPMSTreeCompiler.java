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


package org.openvpms.component.system.common.jxpath;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NameAttributeTest;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.apache.commons.jxpath.ri.compiler.TreeCompiler;
import org.apache.commons.jxpath.ri.compiler.Constant;

import java.math.BigDecimal;

/**
 * This extension to the JXPath TreeCompiler class provides support for
 * BigDecimal and BigInteger.
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OpenVPMSTreeCompiler extends TreeCompiler {
    /**
     * Required to copy it across so that it can support extension
     */
    private static final QName QNAME_NAME = new QName(null, "name");

    /**
     * Default constructor
     */
    public OpenVPMSTreeCompiler() {
        super();
    }

    /**
     * Produces an EXPRESSION object that represents a numeric constant.
     */
    @Override
    public Object number(String value) {
        return new Constant(new BigDecimal(value));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#divide(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object divide(Object left, Object right) {
        return new BigDecimalOperationDivide((Expression) left, (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#equal(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object equal(Object left, Object right) {
        if (isNameAttributeTest((Expression) left)) {
            return new NameAttributeTest((Expression) left, (Expression) right);
        }
        else {
            return new BigDecimalOperationEqual(
                (Expression) left,
                (Expression) right);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#greaterThan(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object greaterThan(Object left, Object right) {
        return new BigDecimalOperationGreaterThan(
                (Expression) left,
                (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#greaterThanOrEqual(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object greaterThanOrEqual(Object left, Object right) {
        return new BigDecimalOperationGreaterThanOrEqual(
                (Expression) left,
                (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#lessThan(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object lessThan(Object left, Object right) {
        return new BigDecimalOperationLessThan((Expression) left, (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#lessThanOrEqual(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object lessThanOrEqual(Object left, Object right) {
        return new BigDecimalOperationLessThanOrEqual(
                (Expression) left,
                (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#minus(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object minus(Object left, Object right) {
        return new BigDecimalOperationSubtract(
                (Expression) left,
                (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#minus(java.lang.Object)
     */
    @Override
    public Object minus(Object argument) {
        return new BigDecimalOperationNegate((Expression) argument);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#mod(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object mod(Object left, Object right) {
        return new BigDecimalOperationMod((Expression) left, (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#multiply(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object multiply(Object left, Object right) {
        return new BigDecimalOperationMultiply((Expression) left, (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#notEqual(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object notEqual(Object left, Object right) {
        return new BigDecimalOperationNotEqual(
                (Expression) left,
                (Expression) right);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#sum(java.lang.Object[])
     */
    @Override
    public Object sum(Object[] arguments) {
        return new BigDecimalOperationAdd(toExpressionArray(arguments));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.TreeCompiler#function(int, java.lang.Object[])
     */
    @Override
    public Object function(int code, Object[] args) {
        return new OpenVPMSCoreFunction(code, toExpressionArray(args));
    }

    /**
     * Copy from base class to support extension. The base class method
     * should've been marked as protected
     * 
     * @param arg
     * @return
     */
    private boolean isNameAttributeTest(Expression arg) {
        if (!(arg instanceof LocationPath)) {
            return false;
        }

        Step[] steps = ((LocationPath) arg).getSteps();
        if (steps.length != 1) {
            return false;
        }
        if (steps[0].getAxis() != Compiler.AXIS_ATTRIBUTE) {
            return false;
        }
        NodeTest test = steps[0].getNodeTest();
        if (!(test instanceof NodeNameTest)) {
            return false;
        }
        if (!((NodeNameTest) test).getNodeName().equals(QNAME_NAME)) {
            return false;
        }
        return true;
    }

    /*
     * Copy from base class to support extension. The base class method
     * should've been marked as protected
     */
    private Expression[] toExpressionArray(Object[] array) {
        Expression expArray[] = null;
        if (array != null) {
            expArray = new Expression[array.length];
            for (int i = 0; i < expArray.length; i++) {
                expArray[i] = (Expression) array[i];
            }
        }
        return expArray;
    }
}
