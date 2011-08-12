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

package org.openvpms.archetype.function.math;

import org.openvpms.archetype.rules.math.MathRules;

import java.math.BigDecimal;


/**
 * JXPath math extension functions.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class MathFunctions {

    /**
     * Rounds an amount.
     * <p/>
     * This implementation rounds to 2 decimal places.
     * TODO - this should round according to practice rounding conventions.
     *
     * @param amount the amount to round.
     * @return the rounded amount
     */
    public BigDecimal roundAmount(BigDecimal amount) {
        return MathRules.round(amount, 2);
    }
}
