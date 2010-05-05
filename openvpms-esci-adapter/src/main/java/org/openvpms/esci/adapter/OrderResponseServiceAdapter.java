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
package org.openvpms.esci.adapter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ubl.OrderResponseSimpleType;
import org.oasis.ubl.OrderResponseType;
import org.oasis.ubl.common.basic.IDType;
import org.oasis.ubl.common.basic.RejectionNoteType;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.exception.ESCIException;
import org.openvpms.esci.service.OrderResponseService;
import org.openvpms.archetype.rules.supplier.OrderStatus;

import javax.annotation.Resource;


/**
 * Order response service implementation that adapts UBL OrderResponse documents to their corresponding OpenVPMS
 * <em>act.supplierOrder</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderResponseServiceAdapter implements OrderResponseService {

    /**
     * The bean factory.
     */
    private IMObjectBeanFactory factory;

    /**
     * Order archetype identifier.
     */
    private ArchetypeId ORDER_ID = new ArchetypeId("act.supplierOrder");

    /**
     * Listener to notify when an order response is received.
     */
    private OrderResponseListener listener;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(OrderResponseServiceAdapter.class);

    /**
     * Default constructor.
     */
    public OrderResponseServiceAdapter() {
    }

    /**
     * Registers the bean factory.
     *
     * @param factory the factory
     */
    @Resource
    public void setBeanFactory(IMObjectBeanFactory factory) {
        this.factory = factory;
    }

    /**
     * Registers a listener to be notified when an order response is received.
     *
     * @param listener the listener to notify. May be <tt>null</tt>
     */
    public void setOrderResponseListener(OrderResponseListener listener) {
        this.listener = listener;
    }

    /**
     * Submits a simple response to an order.
     *
     * @param response the response
     * @throws ESCIException if the response is invalid or cannot be processed
     */
    public void submitSimpleResponse(OrderResponseSimpleType response) throws ESCIException {
        IMObjectReference reference = getOrderReference(response);
        ActBean bean = factory.createActBean(reference);
        if (bean == null) {
            throw new ESCIException("OrderReference with ID " + reference.getId()
                                    + " does not refer to a valid order");
        }
        if (response.getAcceptedIndicator().isValue()) {
            bean.setValue("status", OrderStatus.ACCEPTED);
            bean.setValue("supplierResponse", "Order Accepted");   // TODO localise
        } else {
            String message = null;
            RejectionNoteType note = response.getRejectionNote();
            if (note != null) {
                message = note.getValue();
            }
            if (StringUtils.isEmpty(message)) {
                message = "Order rejected without any message";   // TODO localise
            }
            bean.setValue("status", OrderStatus.REJECTED);
            bean.setValue("supplierResponse", message);
        }
        bean.save();
        notifyListener(bean.getAct());
    }

    /**
     * Submits a response to an order.
     *
     * @param response the response
     * @throws ESCIException if the response is invalid or cannot be processed
     */
    public void submitResponse(OrderResponseType response) throws ESCIException {
        throw new ESCIException("Unsupported operation");
    }

    private IMObjectReference getOrderReference(OrderResponseSimpleType response) {
        IDType type = response.getOrderReference().getID();
        long id;
        try {
            id = Long.valueOf(type.getValue());
        } catch (NumberFormatException exception) {
            throw new ESCIException(type.getValue() + " is not a valid order reference");
        }
        return new IMObjectReference(ORDER_ID, id);
    }

    /**
     * Notifies the registered listener that an order response has been received and processed.
     *
     * @param order the order that the response was received for
     */
    private void notifyListener(Act order) {
        OrderResponseListener l = listener;
        if (l != null) {
            try {
                l.receivedResponse(order);
            } catch (Throwable exception) {
                log.error("OrderResponseListener threw exception", exception);
            }
        }
    }

}
