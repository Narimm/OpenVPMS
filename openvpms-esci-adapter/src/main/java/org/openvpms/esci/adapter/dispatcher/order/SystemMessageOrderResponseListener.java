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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.esci.adapter.dispatcher.order;

import static org.openvpms.archetype.rules.supplier.OrderStatus.ACCEPTED;
import static org.openvpms.archetype.rules.workflow.SystemMessageReason.ORDER_ACCEPTED;
import static org.openvpms.archetype.rules.workflow.SystemMessageReason.ORDER_REJECTED;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.esci.adapter.dispatcher.AbstractSystemMessageFactory;


/**
 * An {@link OrderResponseListener} that creates a new <em>act.systemMessage</em> with a link to the order,
 * and addressed to the author of the order.
 * <p/>
 * If the order has no author participation, then no message is created.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SystemMessageOrderResponseListener extends AbstractSystemMessageFactory
        implements OrderResponseListener {

    /**
     * Invoked after a response is received for an order.
     *
     * @param order the order
     */
    public void receivedResponse(FinancialAct order) {
        String reason = ACCEPTED.equals(order.getStatus()) ? ORDER_ACCEPTED : ORDER_REJECTED;
        createMessage(order, reason);
    }

}
