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

// java core
import java.math.BigDecimal;

// commons-jxpath
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.CoreFunction;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Exrends the CoreFunction class to support BigDecimal functions such as 
 * sum.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OpenVPMSCoreFunction extends CoreFunction {
    
    private static final BigDecimal ZERO = new BigDecimal(0);

    /**
     * Delegate to parent.
     * 
     * @param functionCode
     * @param args
     */
    public OpenVPMSCoreFunction(int functionCode, Expression[] args) {
        super(functionCode, args);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.CoreFunction#functionSum(org.apache.commons.jxpath.ri.EvalContext)
     */
    @Override
    protected Object functionSum(EvalContext context) {
        Object v = getArg1().compute(context);
        if (v == null) {
            return ZERO;
        }
        else if (v instanceof EvalContext) {
            BigDecimal sum = new BigDecimal(0.0);
            EvalContext ctx = (EvalContext) v;
            while (ctx.hasNext()) {
                NodePointer ptr = (NodePointer) ctx.next();
                sum = sum.add(TypeConversionUtil.bigDecimalValue(ptr));
            }
            return sum;
        }
        throw new JXPathException(
            "Invalid argument type for 'sum': " + v.getClass().getName());
    }

}
