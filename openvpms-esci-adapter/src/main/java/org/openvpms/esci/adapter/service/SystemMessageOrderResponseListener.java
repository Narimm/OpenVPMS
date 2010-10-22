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
package org.openvpms.esci.adapter.service;

import static org.openvpms.archetype.rules.supplier.OrderStatus.ACCEPTED;
import static org.openvpms.archetype.rules.workflow.SystemMessageReason.ORDER_ACCEPTED;
import static org.openvpms.archetype.rules.workflow.SystemMessageReason.ORDER_REJECTED;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;

import javax.annotation.Resource;


/**
 * An {@link OrderResponseListener} that creates a new <em>act.systemMessage</em> with a link to the order,
 * and addressed to the author of the order.
 * <p/>
 * If the order has no author participation, then no message is created.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SystemMessageOrderResponseListener implements OrderResponseListener {

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;


    /**
     * Registers the bean factory.
     *
     * @param factory the bean factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Invoked after a response is received for an order.
     *
     * @param order the order
     */
    public void receivedResponse(Act order) {
        ActBean bean = factory.createActBean(order);
        IMObjectReference author = bean.getNodeParticipantRef("author");
        if (author != null) {
            ActBean message = factory.createActBean("act.systemMessage");
            message.addNodeRelationship("item", order);
            message.addNodeParticipation("to", author);
            String status = ACCEPTED.equals(order.getStatus()) ? ORDER_ACCEPTED : ORDER_REJECTED;
            message.setValue("reason", status);
            message.save();
        }
    }

}
