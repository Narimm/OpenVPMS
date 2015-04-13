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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.component.system.common.jxpath;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.SelfContext;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Base implementation of Expression for the operations "&gt;", "&gt;=", "&lt;", "&lt;=".
 * <p/>
 * NOTE: this largely duplicates the <tt>org.apache.commons.jxpath.ri.compiler.CoreOperationRelationalExpression</tt>
 * class but changes evaluation using Double to that using BigDecimal.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class BigDecimalOperationRelationalExpression extends CoreOperation {

    /**
     * Constructs a <tt>RelationalExpression</tt>.
     *
     * @param args the arguments
     */
    protected BigDecimalOperationRelationalExpression(Expression[] args) {
        super(args);
    }

    public Object computeValue(EvalContext context) {
        return compute(args[0].compute(context), args[1].compute(context))
               ? Boolean.TRUE : Boolean.FALSE;
    }

    protected final int getPrecedence() {
        return RELATIONAL_EXPR_PRECEDENCE;
    }

    protected final boolean isSymmetric() {
        return false;
    }

    /**
     * Template method for subclasses to evaluate the result of a comparison.
     *
     * @param compare result of comparison to evaluate
     * @return ultimate operation success/failure
     */
    protected abstract boolean evaluateCompare(int compare);

    /**
     * Compare left to right.
     *
     * @param left  left operand
     * @param right right operand
     * @return operation success/failure
     */
    private boolean compute(Object left, Object right) {
        left = reduce(left);
        right = reduce(right);

        if (left instanceof InitialContext) {
            ((InitialContext) left).reset();
        }
        if (right instanceof InitialContext) {
            ((InitialContext) right).reset();
        }
        if (left instanceof Iterator && right instanceof Iterator) {
            return findMatch((Iterator) left, (Iterator) right);
        }
        if (left instanceof Iterator) {
            return containsMatch((Iterator) left, right);
        }
        if (right instanceof Iterator) {
            return containsMatch((Iterator) right, left);
        }
        BigDecimal ld = TypeConversionUtil.bigDecimalValue(left);
        BigDecimal rd = TypeConversionUtil.bigDecimalValue(right);
        return evaluateCompare(ld.compareTo(rd));
    }

    /**
     * Reduce an operand for comparison.
     *
     * @param o Object to reduce
     * @return reduced operand
     */
    private Object reduce(Object o) {
        if (o instanceof SelfContext) {
            o = ((EvalContext) o).getSingleNodePointer();
        }
        if (o instanceof Collection) {
            o = ((Collection) o).iterator();
        }
        return o;
    }

    /**
     * Learn whether any element returned from an Iterator matches a given value.
     *
     * @param it    Iterator
     * @param value to look for
     * @return whether a match was found
     */
    private boolean containsMatch(Iterator it, Object value) {
        while (it.hasNext()) {
            Object element = it.next();
            if (compute(element, value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Learn whether there is an intersection between two Iterators.
     *
     * @param lit left Iterator
     * @param rit right Iterator
     * @return whether a match was found
     */
    private boolean findMatch(Iterator lit, Iterator rit) {
        HashSet<Object> left = new HashSet<Object>();
        while (lit.hasNext()) {
            left.add(lit.next());
        }
        while (rit.hasNext()) {
            if (containsMatch(left.iterator(), rit.next())) {
                return true;
            }
        }
        return false;
    }

}
