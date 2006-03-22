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

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.CoreOperationAdd;
import org.apache.commons.jxpath.ri.compiler.Expression;

/**
 * Support big decimal add
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class BigDecimalOperationAdd extends CoreOperationAdd {

    /**
     * Support base class construction
     * 
     * @param args
     */
    public BigDecimalOperationAdd(Expression[] args) {
        super(args);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.ri.compiler.CoreOperationAdd#computeValue(org.apache.commons.jxpath.ri.EvalContext)
     */
    @Override
    public Object computeValue(EvalContext context) {
        BigDecimal s = new BigDecimal(0.0);
        for (int i = 0; i < args.length; i++) {
            BigDecimal temp = TypeConversionUtil.bigDecimalValue(args[i].computeValue(context));
            s = s.add(temp);
        }
        return s;
    }

}
