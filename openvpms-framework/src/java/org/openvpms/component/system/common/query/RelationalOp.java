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
package org.openvpms.component.system.common.query;

/**
 * An enumeration of relational operators
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public enum RelationalOp {
    @Deprecated IsNULL(0),
    IS_NULL(0),
    NOT_NULL(0),
    EQ(1),
    NE(1),
    LT(1),
    LTE(1), 
    GT(1),
    GTE(1),
    BTW(2),
    IN(1);
    
    /**
     * The number of parameters required for each operator
     */
    int paramCount;
    
    /**
     * This is the constructor for the enumeration 
     * 
     * @param paramCount
     *            the number of parameters for this operator
     */
    RelationalOp(int paramCount) {
        this.paramCount = paramCount;
    }

    /**
     * @return Returns the paramCount.
     */
    public int getParamCount() {
        return paramCount;
    }
}