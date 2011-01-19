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

import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;

import javax.annotation.Resource;


/**
 * An {@link InvoiceListener}
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SystemMessageInvoiceListener implements InvoiceListener {

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
     * Invoked when an invoice has been received and mapped to a delivery.
     *
     * @param delivery the delivery
     */
    public void receivedInvoice(Act delivery) {
        ActBean bean = factory.createActBean(delivery);
        IMObjectReference author = bean.getNodeParticipantRef("author");
        if (author != null) {
            ActBean message = factory.createActBean("act.systemMessage");
            message.addNodeRelationship("item", delivery);
            message.addNodeParticipation("to", author);
            message.setValue("reason", SystemMessageReason.ORDER_INVOICED);
            message.save();
        }
    }
}
