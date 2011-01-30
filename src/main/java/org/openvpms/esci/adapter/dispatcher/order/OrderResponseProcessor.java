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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ubl.OrderResponseSimpleType;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.esci.adapter.dispatcher.Document;
import org.openvpms.esci.adapter.dispatcher.DocumentProcessor;
import org.openvpms.esci.adapter.i18n.ESCIAdapterMessages;
import org.openvpms.esci.adapter.i18n.Message;
import org.openvpms.esci.adapter.map.order.OrderResponseMapper;
import org.openvpms.esci.adapter.util.ESCIAdapterException;

import javax.annotation.Resource;


/**
 * Order response service implementation that adapts UBL OrderResponse documents to their corresponding OpenVPMS
 * <em>act.supplierOrder</em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OrderResponseProcessor implements DocumentProcessor {

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
    private static final Log log = LogFactory.getLog(OrderResponseProcessor.class);

    /**
     * Default constructor.
     */
    public OrderResponseProcessor() {
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
     * Determines if this processor can handle the supplied document.
     *
     * @param document the document
     * @return <tt>true</tt> if the processor can handle the document, otherwise <tt>false</tt>
     */
    public boolean canHandle(Document document) {
        return document.getContent() instanceof OrderResponseSimpleType;
    }

    /**
     * Process the supplied document.
     *
     * @param document      the document to process  @throws ESCIAdapterException for any error
     * @param supplier      the supplier submitting the document
     * @param stockLocation the stock location
     * @param accountId     the supplier account identifier  @throws ESCIAdapterException for any error
     */
    public void process(Document document, Party supplier, Party stockLocation, String accountId) {
        OrderResponseSimpleType response = (OrderResponseSimpleType) document.getContent();
        try {
            FinancialAct order = mapper.map(response, supplier, stockLocation, accountId);
            service.save(order);
            notifyListener(order);
        } catch (ESCIAdapterException exception) {
            throw exception;
        } catch (Throwable exception) {
            Message message = ESCIAdapterMessages.failedToProcessOrderResponse(exception.getMessage());
            throw new ESCIAdapterException(message, exception);
        }
    }

    /**
     * Notifies the registered listener that an order response has been received and processed.
     *
     * @param order the order that the response was received for
     */
    protected void notifyListener(FinancialAct order) {
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
