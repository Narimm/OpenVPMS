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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.math;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Math rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MathRules {

    /**
     * Rounds a value to the default no. of decimal places.
     * todo - could be configurable via a properies file
     *
     * @param value the value
     * @return the rounded value
     */
    public static BigDecimal round(BigDecimal value) {
        return round(value, 2);
    }

    /**
     * Rounds a value.
     *
     * @param value the value
     * @param scale the no. of decimal places
     */
    public static BigDecimal round(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
    }
}
