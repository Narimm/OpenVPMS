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


package org.openvpms.component.system.service.jxpath;

import java.math.BigDecimal;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class BigDecimalValues {
    private BigDecimal low;
    private BigDecimal high;
    
    /**
     * @param high
     * @param low
     */
    public BigDecimalValues(BigDecimal high, BigDecimal low) {
        // TODO Auto-generated constructor stub
        this.high = high;
        this.low = low;
    }

    /**
     * @return Returns the high.
     */
    public BigDecimal getHigh() {
        return high;
    }

    /**
     * @param high The high to set.
     */
    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    /**
     * @return Returns the low.
     */
    public BigDecimal getLow() {
        return low;
    }

    /**
     * @param low The low to set.
     */
    public void setLow(BigDecimal low) {
        this.low = low;
    }
}
