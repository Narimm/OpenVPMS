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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import java.math.BigDecimal;


/**
 * Delivery statuses for supplier orders.
 * <p/>
 * An order is:
 * <table>
 * <tr><td>{@link #PENDING}</td><td>if nothing has been received/cancelled</td></tr>
 * <tr><td>{@link #PART}</td><td>if some of the order has been received/cancelled</td></tr>
 * <tr><td>{@link #FULL}</td><td>if all of the order has been received/cancelled</td></tr>
 * </table>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public enum DeliveryStatus {

    PENDING, PART, FULL;

    /**
     * Determines the delivery status of an order.
     *
     * @param quantity  the order quantity
     * @param received  the received quantity
     * @param cancelled the cancelled quantity
     * @return the delivery status
     */
    public static DeliveryStatus getStatus(BigDecimal quantity,
                                           BigDecimal received,
                                           BigDecimal cancelled) {
        DeliveryStatus result = DeliveryStatus.PENDING;
        if (quantity.compareTo(BigDecimal.ZERO) != 0) {
            // can only be PART or FULL delivery status if there is an expected
            // quantity
            BigDecimal sum = received.add(cancelled);
            if (sum.compareTo(BigDecimal.ZERO) != 0) {
                int status = sum.compareTo(quantity);
                if (status == -1) {
                    if (received.compareTo(BigDecimal.ZERO) != 0) {
                        result = DeliveryStatus.PART;
                    }
                } else if (status >= 0) {
                    result = DeliveryStatus.FULL;
                }
            }
        }
        return result;
    }

}
