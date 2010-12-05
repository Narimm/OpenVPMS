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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ubl.OrderResponseSimpleType;
import org.oasis.ubl.OrderResponseType;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.order.OrderResponseMapper;
import org.openvpms.esci.exception.ESCIException;
import org.openvpms.esci.service.OrderResponseService;

import javax.annotation.Resource;


/**
 * Order response service implementation that adapts UBL OrderResponse documents to their corresponding OpenVPMS
 * <em>act.supplierOrder</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderResponseServiceAdapter extends AbstractUBLServiceAdapter implements OrderResponseService {

    /**
     * The order response mapper.
     */
    private OrderResponseMapper mapper;

    /**
     * Listener to notify when an order response is received.
     */
    private OrderResponseListener listener;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

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
     * Registers the order response mapper.
     *
     * @param mapper the mapper
     */
    @Resource
    public void setOrderResponseMapper(OrderResponseMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Registers a listener to be notified when an order response is received.
     *
     * @param listener the listener to notify. May be <tt>null</tt>
     */
    @Resource
    public void setOrderResponseListener(OrderResponseListener listener) {
        this.listener = listener;
    }

    /**
     * Registers the archetype service.
     *
     * @param service the archetype service
     */
    @Resource
    public void setArchetypeService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Submits a simple response to an order.
     *
     * @param response the response
     * @throws ESCIException if the response is invalid or cannot be processed
     */
    public void submitSimpleResponse(OrderResponseSimpleType response) throws ESCIException {
        try {
            User user = getUser();
            FinancialAct order = mapper.map(response, user);
            service.save(order);
            notifyListener(order);
        } catch (ESCIException exception) {
            throw exception;
        } catch (Throwable exception) {
            Message message = ESCIAdapterMessages.failedToSubmitOrderResponse(exception.getMessage());
            throw new ESCIException(message.toString(), exception);
        }
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

    /**
     * Notifies the registered listener that an order response has been received and processed.
     *
     * @param order the order that the response was received for
     */
    private void notifyListener(FinancialAct order) {
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