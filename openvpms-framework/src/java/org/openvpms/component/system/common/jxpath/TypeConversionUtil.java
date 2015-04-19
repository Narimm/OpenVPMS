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

// java maths
import java.math.BigDecimal;
import java.math.BigInteger;

// commons-jxpath
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * Type conversion borrowed from commons-jxpath. Original author is Dmitri
 * Plotnikov
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
class TypeConversionUtil {
    /**
     * Converts the supplied object to BigDecimal
     * 
     * @param object
     *            the incoming object
     * @return BigDecimal            
     */
    public static BigDecimal bigDecimalValue(Object object) {
        if (object instanceof BigDecimal) {
            return (BigDecimal) object;
        } else if (object instanceof BigInteger) {
            return new BigDecimal((BigInteger) object);
        } else if (object instanceof Number) {
            return new BigDecimal(((Number) object).doubleValue());
        } else if (object instanceof Boolean) {
            return new BigDecimal(((Boolean) object).booleanValue() ? 0.0 : 1.0);
        } else if (object instanceof String) {
            BigDecimal value = new BigDecimal(0.0);
            if (!object.equals("")) {
                try {
                    value = new BigDecimal((String) object);
                } catch (NumberFormatException ex) {
                    value = BigDecimal.ZERO;
                }
            }
            return value;
        } else if (object instanceof NodePointer) {
            return bigDecimalValue(((NodePointer) object).getValue());
        } else if (object instanceof EvalContext) {
            EvalContext ctx = (EvalContext) object;
            Pointer ptr = ctx.getSingleNodePointer();
            if (ptr != null) {
                return bigDecimalValue(ptr);
            }
            return BigDecimal.ZERO;
        }
        return bigDecimalValue(InfoSetUtil.stringValue(object));
    }
}