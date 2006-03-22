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
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThanOrEqual;
import org.apache.commons.jxpath.ri.compiler.Expression;

/**
 * BigDecimal support
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class BigDecimalOperationGreaterThanOrEqual extends
        CoreOperationGreaterThanOrEqual {

    /**
     * Base class constuctor support
     * 
     * @param arg1
     * @param arg2
     */
    public BigDecimalOperationGreaterThanOrEqual(Expression arg1,
            Expression arg2) {
        super(arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.CoreOperationGreaterThanOrEqual#computeValue(org.apache.commons.jxpath.ri.EvalContext)
     */
    @Override
    public Object computeValue(EvalContext context) {
        BigDecimal l = TypeConversionUtil.bigDecimalValue(args[0].computeValue(context));
        BigDecimal r = TypeConversionUtil.bigDecimalValue(args[1].computeValue(context));
        return l.compareTo(r) >= 0 ? Boolean.TRUE : Boolean.FALSE;
    }

}
