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

import java.math.BigDecimal;

import org.apache.commons.jxpath.ri.compiler.CoreOperationNotEqual;
import org.apache.commons.jxpath.ri.compiler.Expression;

/**
 * BigDecimal support
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class BigDecimalOperationNotEqual extends CoreOperationNotEqual {

    /** 
     * Constructs a <tt>BigDecimalOperationNotEqual</tt>.
     * 
     * @param arg1 the left hand side of the expression
     * @param arg2 the right hand side of the expression
     */
    public BigDecimalOperationNotEqual(Expression arg1, Expression arg2) {
        super(arg1, arg2);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.CoreOperationCompare#equal(java.lang.Object, java.lang.Object)
     */
    @Override
    protected boolean equal(Object l, Object r) {
        if ((l instanceof BigDecimal) && (r instanceof BigDecimal)) {
            return ((BigDecimal) l).compareTo((BigDecimal) r) == 0;
        } else {
            return super.equal(l, r);
        }
    }
}
