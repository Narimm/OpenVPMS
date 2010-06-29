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

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.CoreOperationDivide;
import org.apache.commons.jxpath.ri.compiler.Expression;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Supports big decimal divide operation.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BigDecimalOperationDivide extends CoreOperationDivide {

    /**
     * Constructs a <tt>BigDecimalOperationDivide</tt>.
     *
     * @param lhs the left hand expression
     * @param rhs the right hand expression
     */
    public BigDecimalOperationDivide(Expression lhs, Expression rhs) {
        super(lhs, rhs);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.CoreOperationDivide#computeValue(org.apache.commons.jxpath.ri.EvalContext)
     */
    @Override
    public Object computeValue(EvalContext context) {
        BigDecimal l = TypeConversionUtil.bigDecimalValue(args[0].computeValue(context));
        BigDecimal r = TypeConversionUtil.bigDecimalValue(args[1].computeValue(context));
        return l.divide(r, MathContext.DECIMAL128);
    }

}
